package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.response.CorporationRequest
import io.github.devngho.kisopenapi.requests.response.TradeContinuousData
import io.github.devngho.kisopenapi.requests.response.TradeContinuousResponse
import io.github.devngho.kisopenapi.requests.response.TradeIdMsg
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.OverseasMarket.Companion.fourChar
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class OrderOverseasBuy(override val client: KisOpenApi):
    DataRequest<OrderOverseasBuy.OrderData, OrderOverseasBuy.OrderResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/order"
    else               "https://openapi.koreainvestment.com:9443/uapi/overseas-stock/v1/trading/order"

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

    data class OrderData(val stockCode: String, val market: OverseasMarket, val orderType: OrderTypeCode, val count: BigInteger, val price: BigDecimal = BigDecimal.fromInt(0),
                         override var corp: CorporationRequest? = null, override val tradeContinuous: String? = ""): Data, TradeContinuousData
    @Serializable
    data class OrderDataJson(val CANO: String, val ACNT_PRDT_CD: String, val OVRS_EXCG_CD: String, val PDNO: String, val ORD_DVSN: String, @Contextual val ORD_QTY: BigInteger, @Contextual val OVRS_ORD_UNPR: BigDecimal, val ORD_SVR_DVSN_CD: String, val SLL_TYPE: String = "")

    override suspend fun call(data: OrderData): OrderResponse {
        if (data.corp == null) data.corp = client.corp

        val tradeId =
            (if (client.isDemo) "VTT" else "TTT") +
            (when (data.market) {
                OverseasMarket.NASDAQ,
                OverseasMarket.NAS,
                OverseasMarket.NASDAQ_DAY,
                OverseasMarket.BAQ,
                OverseasMarket.NEWYORK,
                OverseasMarket.NYS,
                OverseasMarket.NEWYORK_DAY,
                OverseasMarket.BAY,
                OverseasMarket.AMEX,
                OverseasMarket.AMS,
                OverseasMarket.AMEX_DAY,
                OverseasMarket.BAA -> "T1002U" // USA
                OverseasMarket.TOYKO,
                OverseasMarket.TSE -> "S0308U"
                OverseasMarket.SHANGHAI,
                OverseasMarket.SHANGHAI_INDEX,
                OverseasMarket.SHS,
                OverseasMarket.SHI -> "S0202U"
                OverseasMarket.HONGKONG,
                OverseasMarket.HKS -> "S1002U"
                OverseasMarket.SHENZHEN,
                OverseasMarket.SHENZHEN_INDEX,
                OverseasMarket.SZI,
                OverseasMarket.SZS -> "S0305U"
                OverseasMarket.HANOI,
                OverseasMarket.HOCHIMINH,
                OverseasMarket.HNX,
                OverseasMarket.HSX -> "S0311U"
            })

        val orderType =
            when(data.market) {
                OverseasMarket.NASDAQ,
                OverseasMarket.NAS,
                OverseasMarket.NASDAQ_DAY,
                OverseasMarket.BAQ,
                OverseasMarket.NEWYORK,
                OverseasMarket.NYS,
                OverseasMarket.NEWYORK_DAY,
                OverseasMarket.BAY,
                OverseasMarket.AMEX,
                OverseasMarket.AMS,
                OverseasMarket.AMEX_DAY,
                OverseasMarket.BAA -> {
                    when(data.orderType) {
                        OrderTypeCode.SelectPrice -> "00"
                        OrderTypeCode.USALimitOnClose -> "34"
                        OrderTypeCode.USALimitOnOpen -> "32"
                        else -> throw RequestError("Invalid order type. Only SelectPrice, USALimitOnClose, USALimitOnOpen are allowed in USA stock exchange.")
                    }
                }
                OverseasMarket.HONGKONG,
                OverseasMarket.HKS -> {
                    when(data.orderType) {
                        OrderTypeCode.SelectPrice -> "00"
                        else -> throw RequestError("Invalid order type. Only SelectPrice are allowed in HKEX.")
                    }
                }
                else -> ""
            }

        if (data.price.isZero() && data.orderType == OrderTypeCode.SelectPrice) throw RequestError("Price must be set when order type is SelectPrice.")

        val res = client.httpClient.post(url) {
            auth(client)
            tradeId(tradeId)
            stock(data.stockCode)
            data.corp?.let { corporation(it) }
            setBody(OrderDataJson(client.account!![0], client.account!![1], data.market.fourChar, data.stockCode, orderType, data.count, data.price, "0"))

            hashKey<OrderDataJson>(client)
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