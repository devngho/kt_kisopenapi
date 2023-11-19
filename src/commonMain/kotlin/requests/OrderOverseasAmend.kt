package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

class OrderOverseasAmend(override val client: KisOpenApi) :
    DataRequest<OrderOverseasAmend.OrderData, OrderOverseasAmend.OrderResponse> {
    private val url =
        if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/order-rvsecncl"
        else "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/trading/order-rvsecncl"

    @Serializable
    data class OrderResponse(
        @SerialName("tr_cont") override var tradeContinuous: String?,
        @SerialName("tr_id") override var tradeId: String?,
        @SerialName("gt_uid") override var globalTradeID: String?,
        @SerialName("msg_cd") override val code: String?,
        @SerialName("msg1") override val msg: String?,
        @SerialName("rt_cd") @Serializable(with = ResultCodeSerializer::class) override val isOk: Boolean?,

        var output: OrderResponseOutput?, override var next: (suspend () -> Response)?
    ) : Response, TradeContinuousResponse, TradeIdMsg {
        @SerialName("error_description")
        override val errorDescription: String? = null

        @SerialName("error_code")
        override val errorCode: String? = null
    }

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class OrderResponseOutput(
        @SerialName("KRX_FWDG_ORD_ORGNO") val orderOffice: String?,
        @SerialName("ODNO") val orderNumber: String?,
        @SerialName("ORD_TMD") @Serializable(with = HHMMSSSerializer::class) val orderTime: Time?,
    )

    @Suppress("SpellCheckingInspection")
    data class OrderData(
        @SerialName("PDNO") override val ticker: String,
        @Transient val market: OverseasMarket,
        @SerialName("ORD_QTY") val count: BigInteger,
        @SerialName("ORD_UNPR") val price: BigDecimal,
        @SerialName("ORGN_ODNO") val orderNumber: String,
        @SerialName("CANO") val accountNumber: String? = null,
        @SerialName("ACNT_PRDT_CD") val accountProductCode: String? = null,
        @SerialName("KRX_FWDG_ORD_ORGNO") val orderOffice: String? = null,
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker {
        @SerialName("RVSE_CNCL_DVSN_CD")
        val isAmendOrCancel = "01"
    }

    override suspend fun call(data: OrderData): OrderResponse = client.rateLimiter.rated {
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
                        OverseasMarket.BAA -> "T1004U" // USA
                        OverseasMarket.TOYKO,
                        OverseasMarket.TSE -> "S0309U"

                        OverseasMarket.SHANGHAI,
                        OverseasMarket.SHANGHAI_INDEX,
                        OverseasMarket.SHS,
                        OverseasMarket.SHI -> "S0302U"

                        OverseasMarket.HONGKONG,
                        OverseasMarket.HKS -> "S1003U"

                        OverseasMarket.SHENZHEN,
                        OverseasMarket.SHENZHEN_INDEX,
                        OverseasMarket.SZI,
                        OverseasMarket.SZS -> "S0306U"

                        OverseasMarket.HANOI,
                        OverseasMarket.HOCHIMINH,
                        OverseasMarket.HNX,
                        OverseasMarket.HSX -> "S0312U"
                    })

        val res = client.httpClient.post(url) {
            auth(client)
            tradeId(tradeId)
            data.corp?.let { corporation(it) }
            setBody(
                data
                    .let { if (it.accountNumber != null) it else it.copy(accountNumber = client.account!![0]) }
                    .let { if (it.accountProductCode != null) it else it.copy(accountProductCode = client.account!![1]) }
            )

            hashKey<OrderData>(client)
        }

        res.body<OrderResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
            setNext(data, this)
        }
    }
}