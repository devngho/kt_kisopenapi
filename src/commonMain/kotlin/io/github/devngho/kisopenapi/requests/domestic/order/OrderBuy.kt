package io.github.devngho.kisopenapi.requests.domestic.order

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.auth.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.data.*
import io.github.devngho.kisopenapi.requests.data.AccountInfo.Companion.fillFrom
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국내 주식 종목을 매수하고, 주문 정보를 반환합니다.
 */
class OrderBuy(override val client: KISApiClient) :
    DataRequest<OrderBuy.OrderData, OrderBuy.OrderResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/trading/order-cash"

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

    @Suppress("SpellCheckingInspection")
    data class OrderData(
        @SerialName("PDNO") override val ticker: String,
        @SerialName("ORD_DVSN") val orderType: OrderTypeCode,
        @SerialName("ORD_QTY") val count: BigInteger,
        @SerialName("ORD_UNPR") val price: BigInteger = BigInteger(0),
        @SerialName("CANO") override val accountNumber: String? = null,
        @SerialName("ACNT_PRDT_CD") override val accountProductCode: String? = null,
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker, AccountInfo

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: OrderData) = request(data) {
        if (it.price.isZero() && it.orderType == OrderTypeCode.SelectPrice) throw RequestException(
            "Price must be set when order type is SelectPrice.",
            RequestCode.InvalidOrder
        )

        client.httpClient.post(url) {
            setAuth(client)
            setTradeId(if (client.isDemo) "VTTC0802U" else "TTTC0802U")
            setStock(it.ticker)
            setCorporation(it.corp)

            setBody(it.fillFrom(client))

            hashKey<OrderData>(client)
        }
    }
}