package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.TradeContinuousData
import io.github.devngho.kisopenapi.requests.response.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.response.TradeIdMsg
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class OrderBuy(override val client: KisOpenApi):
    DataRequest<OrderBuy.OrderData, OrderBuy.OrderResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/trading/order-cash"
    else               "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/trading/order-cash"

    @Serializable
    data class OrderResponse(
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: OrderResponseOutput?, override var next: (suspend () -> Response)?
    ): Response, TradeContinuousResponse, TradeIdMsg {
        override val errorDescription: String? = null
        override val errorCode: String? = null
    }

    @Serializable
    data class OrderResponseOutput(
        @SerialName("KRX_FWDG_ORD_ORGNO") val orderOffice: String?,
        @SerialName("ODNO") @Contextual val orderNumber: String?,
        @SerialName("ORD_TMD") @Serializable(with = HHMMSSSerializer::class) val orderTime: Time?,
    )

    data class OrderData(val stockCode: String, val orderType: OrderTypeCode, val count: BigInteger, val price: BigInteger = BigInteger(0),
                         override var corp: CorporationRequest? = null, override val tradeContinuous: String? = ""): Data, TradeContinuousData
    @Serializable
    data class OrderDataJson(val CANO: String, val ACNT_PRDT_CD: String, val PDNO: String, val ORD_DVSN: OrderTypeCode, @Contextual val ORD_QTY: BigInteger, @Contextual val ORD_UNPR: BigInteger)

    override suspend fun call(data: OrderData): OrderResponse {
        if (data.corp == null) data.corp = client.corp

        if (data.price.isZero() && data.orderType == OrderTypeCode.SelectPrice) throw RequestError("Price must be set when order type is SelectPrice.")

        val res = client.httpClient.post(url) {
            auth(client)
            tradeId(if(client.isDemo) "VTTC0802U" else "TTTC0802U")
            stock(data.stockCode)
            data.corp?.let { corporation(it) }
            setBody(OrderDataJson(client.account!![0], client.account!![1], data.stockCode, data.orderType, data.count, data.price))
        }
        return res.body<OrderResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            res.headers.forEach { s, strings ->
                when(s) {
                    "tr_id" -> this.tradeId = strings[0]
                    "tr_cont" -> this.tradeContinuous = strings[0]
                    "gt_uid" -> this.globalTradeID = strings[0]
                }
            }

            if (this.tradeContinuous == "F" || this.tradeContinuous == "M") {
                this.next = {
                    call(data.copy(tradeContinuous = "N"))
                }
            }
        }
    }
}