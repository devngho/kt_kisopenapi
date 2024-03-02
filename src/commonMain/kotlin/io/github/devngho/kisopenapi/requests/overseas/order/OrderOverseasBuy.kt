package io.github.devngho.kisopenapi.requests.overseas.order

import com.ionspin.kotlin.bignum.decimal.BigDecimal
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
import kotlinx.serialization.Transient

/**
 * 해외 주식 종목을 매수하고, 주문 정보를 반환합니다.
 */
class OrderOverseasBuy(override val client: KISApiClient) :
    DataRequest<OrderOverseasBuy.OrderData, OrderOverseasBuy.OrderResponse> {
    private val url = "${client.options.baseUrl}/uapi/overseas-stock/v1/trading/order"

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
        @Transient val market: OverseasMarket,
        @Transient val orderType: OrderTypeCode,
        @SerialName("ORD_QTY") val count: BigInteger,
        @SerialName("ORD_UNPR") val price: BigDecimal = BigDecimal.fromInt(0),
        @SerialName("CANO") override val accountNumber: String? = null,
        @SerialName("ACNT_PRDT_CD") override val accountProductCode: String? = null,
        @SerialName("ORD_SVR_DVSN_CD") val orderServerDivisionCode: String = "0",
        @SerialName("SLL_TYPE") val sellType: String = "",
        @SerialName("OVRS_EXCG_CD") val marketId: String = "",
        @SerialName("ORD_DVSN") val orderTypeId: String = "",
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker, AccountInfo

    override suspend fun call(data: OrderData) = request(data) {
        val tradeId =
            (if (client.isDemo) "VTT" else "TTT") +
                    (when (it.market) {
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
                        OverseasMarket.TOKYO,
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
            when (it.market) {
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
                    if (client.isDemo && data.orderType != OrderTypeCode.SelectPrice) throw RequestException(
                        "모의 투자에서는 지정가 주문만 가능합니다.",
                        RequestCode.DemoUnavailable
                    )

                    when (it.orderType) {
                        OrderTypeCode.SelectPrice -> "00"
                        OrderTypeCode.USALimitOnClose -> "34"
                        OrderTypeCode.USALimitOnOpen -> "32"
                        else -> throw RequestException(
                            "미국 거래소에서는 지정가, 장마감지정가(USALimitOnClose), 장개시지정가(USALimitOnOpen) 주문만 가능합니다.",
                            RequestCode.InvalidOrder
                        )
                    }
                }
                else -> ""
            }

        if (it.price.isZero() && it.orderType == OrderTypeCode.SelectPrice) throw RequestException(
            "지정가 주문에서 가격은 필수 값입니다.",
            RequestCode.InvalidOrder
        )

        client.httpClient.post(url) {
            setAuth(client)
            setTradeId(tradeId)
            setStock(it.ticker)
            setCorporation(it.corp)

            setBody(
                it
                    .copy(marketId = tradeId, orderTypeId = orderType)
                    .fillFrom(client)
            )

            hashKey<OrderData>(client)
        }
    }
}