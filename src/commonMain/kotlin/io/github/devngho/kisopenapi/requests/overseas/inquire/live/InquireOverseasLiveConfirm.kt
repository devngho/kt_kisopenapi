package io.github.devngho.kisopenapi.requests.overseas.inquire.live

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.integer.toBigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.LiveData
import io.github.devngho.kisopenapi.requests.LiveRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.LiveResponse
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.response.stock.price.overseas.StockOverseasPriceBase
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HHMMSS
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer.YYYYMMDD
import kotlinx.coroutines.Job
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class InquireOverseasLiveConfirm(override val client: KISApiClient) :
    LiveRequest<InquireOverseasLiveConfirm.InquireOverseasLiveConfirmData, InquireOverseasLiveConfirm.InquireOverseasLiveConfirmResponse> {
    private val tradeId = "H0GSCNI0"

    @Serializable
    data class InquireOverseasLiveConfirmResponse(
        @SerialName("cust_id") val customerId: String? = null,
        @SerialName("acnt_no") val accountNumber: String? = null,
        @SerialName("oder_no") @Contextual val orderNumber: BigInteger? = null,
        @SerialName("ooder_no") @Contextual val originalOrderNumber: BigInteger? = null,
        val isBuy: Boolean? = null,
        val isSell: Boolean? = null,
        @SerialName("rctf_cls") val receiptAmendClassification: String? = null,
        @SerialName("oder_kind2") val orderKind2: String? = null,
        @SerialName("mksc_shrn_iscd") override val ticker: String? = null,
        @SerialName("oder_qty") @Contextual val orderCount: BigInteger? = null,
        /**
         * 현재 체결 가격입니다. 서버에서 소숫점이 생략되어 전달됩니다. 미국은 4번째, 일본 1번째, 중국/홍콩은 3번째, 베트남 0번째에 소숫점이 있다고 가정하고 해석해야 합니다.
         */
        @SerialName("cntg_unpr") @Contextual override val price: BigDecimal? = null,
        @SerialName("stck_cntg_hour") val stockConfirmTime: Time? = null,
        @SerialName("rfus_yn") val isRefused: Boolean? = null,
        @SerialName("cntg_yn") val isConfirmed: Boolean? = null,
        @SerialName("acpt_yn") val isAccepted: Boolean? = null,
        @SerialName("brnc_no") val branchNumber: String? = null,
        @SerialName("cntg_qty") @Contextual val confirmCount: BigInteger? = null,
        @SerialName("acnt_name") @Contextual val accountName: String? = null,
        @SerialName("cntg_isnm") @Contextual val stockName: String? = null,
        @SerialName("oder_cond") val orderCondition: String? = null,
        @SerialName("debt_gb") val debtType: String? = null,
        @SerialName("debt_date") @Serializable(with = YYYYMMDDSerializer::class) val debtDate: Date? = null,

        ) : Response, Ticker, StockOverseasPriceBase {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    data class InquireOverseasLiveConfirmData(override var corp: CorporationRequest? = null) : LiveData {
        override fun tradeKey(client: KISApiClient): String {
            return client.htsId!!
        }
    }

    private var job: Job? = null
    private var subscribed: KISApiClient.WebSocketSubscribed? = null

    @Suppress("UNCHECKED_CAST")
    override suspend fun register(
        data: InquireOverseasLiveConfirmData,
        wait: Boolean,
        force: Boolean,
        init: (suspend (Result<LiveResponse>) -> Unit)?,
        block: suspend (InquireOverseasLiveConfirmResponse) -> Unit
    ) {
        subscribed = KISApiClient.WebSocketSubscribed(
            this@InquireOverseasLiveConfirm, data, init,
            block as suspend (Response) -> Unit
        )


        requestStart(
            data, subscribed!!, tradeId, data.tradeKey(client), wait,
            updateJob = { job = it },
            init = init ?: {},
            block = block,
        ) {
            InquireOverseasLiveConfirmResponse(
                it[0],
                it[1],
                it[2].toBigInteger(),
                if (it[3].isNotEmpty() && it[3][0].isDigit()) it[3].toBigInteger() else null,
                it[4] == "02",
                it[4] == "01",
                it[5],
                it[6],
                it[7],
                it[8].toBigInteger(),
                it[9].toBigDecimal(),
                if (it[10].count() == 6) it[10].HHMMSS else null,
                it[11] != "0",
                it[12] == "2",
                it[13] == "2",
                it[14],
                if (it[15].isNotEmpty() && it[15][0].isDigit()) it[15].toBigInteger() else null,
                it[16],
                it[17],
                it[18],
                it[19],
                if (it[20][0].isDigit()) it[20].YYYYMMDD else null,
            )
        }
    }

    override suspend fun unregister(data: InquireOverseasLiveConfirmData, wait: Boolean) =
        requestEnd(data, subscribed, tradeId, data.tradeKey(client), wait, job)
}