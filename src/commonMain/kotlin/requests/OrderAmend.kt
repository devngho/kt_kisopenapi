package io.github.devngho.kisopenapi.requests

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class OrderAmend(override val client: KisOpenApi) :
    DataRequest<OrderAmend.OrderData, OrderAmend.OrderResponse> {
    private val url =
        if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/trading/order-rvsecncl"
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
        override val errorDescription: String? = null
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
        @SerialName("ORD_DVSN") val orderType: OrderTypeCode,
        @SerialName("ORD_QTY") val count: BigInteger,
        @SerialName("ORD_UNPR") val price: BigInteger = BigInteger(0),
        @SerialName("ORGN_ODNO") val orderNumber: String,
        @SerialName("QTY_ALL_ORD_YN") @Serializable(with = YNSerializer::class) val orderAll: Boolean,
        @SerialName("CANO") val accountNumber: String? = null,
        @SerialName("ACNT_PRDT_CD") val accountProductCode: String? = null,
        @SerialName("KRX_FWDG_ORD_ORGNO") val orderOffice: String? = null,
        override var corp: CorporationRequest? = null,
        override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData {
        @SerialName("RVSE_CNCL_DVSN_CD")
        val isAmendOrCancel = "01"
    }

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: OrderData): OrderResponse = client.rateLimiter.rated {
        if (data.corp == null) data.corp = client.corp

        if (data.price.isZero() && data.orderType == OrderTypeCode.SelectPrice) throw RequestError("Price must be set when order type is SelectPrice.")

        val res = client.httpClient.post(url) {
            auth(client)
            tradeId(if (client.isDemo) "VTTC0803U" else "TTTC0803U")
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