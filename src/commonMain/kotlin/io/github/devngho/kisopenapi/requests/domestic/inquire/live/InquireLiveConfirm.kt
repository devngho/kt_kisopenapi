package io.github.devngho.kisopenapi.requests.domestic.inquire.live

import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HHMMSS
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer.YYYYMMDD
import kotlinx.coroutines.Job
import kotlinx.serialization.SerialName

/**
 * 자신의 주문 중 국내 주식의 실시간 체결을 가져옵니다.
 */
@Suppress("SpellCheckingInspection")
@OptIn(InternalApi::class)
class InquireLiveConfirm(override val client: KISApiClient) :
    LiveRequest<InquireLiveConfirm.InquireLiveConfirmData, InquireLiveConfirm.InquireLiveConfirmResponse> {
    private val tradeId = if (client.isDemo) "H0STCNI9" else "H0STCNI0"

    data class InquireLiveConfirmResponse(
        val customerId: String?,
        val accountNumber: String?,
        val orderNumber: String?,
        val originalOrderNumber: String?,
        /**
         * 주문구분
         * 01: 매도
         * 02: 매수
         */
        val sellBuyDivision: String?,
        /**
         * 정정구분
         * 0: 정상
         * 1: 정정
         * 2: 취소
         */
        val amendDivision: String?,
        val orderType: OrderTypeCode?,
        /**
         * 주문조건
         * 0: 없음
         * 1: IOC
         * 2: FOK
         */
        val orderCondition: String?,
        override val ticker: String?,
        val confirmedQuantity: BigInteger?,
        val confirmedPrice: BigInteger?,
        val confirmedTime: Time?,
        val isRefused: Boolean?,
        val isConfirmed: Boolean?,
        /**
         * 접수 여부
         * 1: 주문접수
         * 2: 확인
         * 3: 취소
         */
        val isAccepted: String?,
        val branchNumber: String?,
        val orderQuantity: BigInteger?,
        val accountName: String?,
        val confirmedStockName: String?,
        val creditDivision: String?,
        val loanDate: Date?,
        val confirmedStockName40: String?,
        val orderedPrice: BigInteger?,
    ) : Ticker, Response {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquireLiveConfirmData(override var corp: CorporationRequest? = null) : LiveData {
        override fun tradeKey(client: KISApiClient): String {
            return client.htsId!!
        }
    }

    private var job: Job? = null
    private var subscribed: WebSocketSubscribed? = null

    @Suppress("UNCHECKED_CAST")
    override suspend fun register(
        data: InquireLiveConfirmData,
        wait: Boolean,
        force: Boolean,
        init: (suspend (Result<LiveResponse>) -> Unit)?,
        block: suspend (InquireLiveConfirmResponse) -> Unit
    ) {
        subscribed = WebSocketSubscribed(
            this@InquireLiveConfirm, data, init,
            block as suspend (Response) -> Unit
        )

        requestStart(
            data, subscribed!!, tradeId, data.tradeKey(client),
            wait = wait,
            updateJob = { job = it },
            init = init ?: {},
            block = block,
            force = force,
            bodySize = 23
        ) {
            InquireLiveConfirmResponse(
                it[0],
                it[1],
                it[2],
                it[3],
                it[4],
                it[5],
                OrderTypeCode.fromCode(it[6]),
                it[7],
                it[8],
                it[9].toBigInteger(),
                it[10].toBigInteger(),
                it[11].HHMMSS,
                it[12] == "1", // 1: 거부
                it[13] == "2", // 2: 체결
                it[14],
                it[15],
                it[16].toBigInteger(),
                it[17],
                it[18],
                it[19],
                it[20].YYYYMMDD,
                it[21],
                if (it[22].contains("-")) it[22].toBigInteger() else null
            )
        }
    }

    override suspend fun unregister(data: InquireLiveConfirmData, wait: Boolean) =
        requestEnd(data, subscribed!!, tradeId, data.tradeKey(client), wait, job)
}