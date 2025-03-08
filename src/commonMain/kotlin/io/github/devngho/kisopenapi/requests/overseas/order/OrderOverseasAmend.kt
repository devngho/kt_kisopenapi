package io.github.devngho.kisopenapi.requests.overseas.order

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.auth.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.data.*
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 해외 주식 종목 주문을 정정하고, 주문 정보를 반환합니다.
 */
class OrderOverseasAmend(override val client: KISApiClient) :
    DataRequest<OrderOverseasAmend.OrderData, OrderOverseasAmend.OrderResponse> {
    private val url = "${client.options.baseUrl}/uapi/overseas-stock/v1/trading/order-rvsecncl"

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
        @SerialName("CANO") override val accountNumber: String? = null,
        @SerialName("ACNT_PRDT_CD") override val accountProductCode: String? = null,
        @SerialName("KRX_FWDG_ORD_ORGNO") val orderOffice: String? = null,
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker, AccountInfo {
        @SerialName("RVSE_CNCL_DVSN_CD")
        val isAmendOrCancel = "01"
    }

    override suspend fun call(data: OrderData) = request(data) {
        val tradeId = OrderOverseasCancel.getOverseasCancelAmendTrId(client.isDemo, it.market)

        client.httpClient.post(url) {
            setAuth(client)
            setTR(tradeId)
            setCorporation(it.corp)
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