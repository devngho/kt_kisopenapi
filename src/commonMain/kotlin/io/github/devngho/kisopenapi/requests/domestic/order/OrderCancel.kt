package io.github.devngho.kisopenapi.requests.domestic.order

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.auth.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.data.*
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 국내 주식 종목의 주문을 취소하고 주문 정보를 반환합니다.
 */
class OrderCancel(override val client: KISApiClient) :
    DataRequest<OrderCancel.OrderData, OrderCancel.OrderResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/trading/order-rvsecncl"

    @Serializable
    data class OrderResponse(
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: OrderResponseOutput?, override var next: (suspend () -> Result<OrderResponse>)?
    ) : Response, TradeContinuousResponse<OrderResponse>, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class OrderResponseOutput(
        @SerialName("KRX_FWDG_ORD_ORGNO") val orderOffice: String?,
        @SerialName("ODNO") @Contextual val orderNumber: String?,
        @SerialName("ORD_TMD") @Serializable(with = HHMMSSSerializer::class) val orderTime: Time?,
    )

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class OrderData(
        @SerialName("ORD_DVSN") val orderType: OrderTypeCode,
        /** 전부 취소하려면 0으로 입력하세요.
         * 일부를 취소하려면 취소하려는 수량을 입력하세요.
         */
        @Contextual @SerialName("ORD_QTY") val count: BigInteger,
        @SerialName("ORGN_ODNO") val orderNumber: String,
        @SerialName("QTY_ALL_ORD_YN") @Serializable(with = YNSerializer::class) val orderAll: Boolean,
        @SerialName("CANO") override val accountNumber: String? = null,
        @SerialName("ACNT_PRDT_CD") override val accountProductCode: String? = null,
        @Transient override var corp: CorporationRequest? = null,
        @Transient override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, AccountInfo {
        @SerialName("RVSE_CNCL_DVSN_CD")
        val isAmendOrCancel = "02"

        @Contextual
        @SerialName("ORD_UNPR")
        val price: BigInteger = BigInteger(0)
        @SerialName("KRX_FWDG_ORD_ORGNO")
        val orderOffice: String = ""
    }

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: OrderData) = request(data) {
        if (it.price.isZero() && it.orderType == OrderTypeCode.SelectPrice) throw RequestException(
            "지정가 주문에서 가격은 필수 값입니다.",
            RequestCode.InvalidOrder
        )

        client.httpClient.post(url) {
            setAuth(client)
            setTR(if (client.isDemo) "VTTC0803U" else "TTTC0803U")
            it.corp?.let { setCorporation(it) }
            setBody(
                it.copy(
                    accountNumber = it.accountNumber ?: client.account!!.first,
                    accountProductCode = it.accountProductCode ?: client.account!!.second
                )
            )

            hashKey<OrderData>(client)
        }
    }
}