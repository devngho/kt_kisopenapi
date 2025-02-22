package io.github.devngho.kisopenapi.requests.domestic.order

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.Data
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.auth.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.data.AccountInfo
import io.github.devngho.kisopenapi.requests.data.CorporationRequest
import io.github.devngho.kisopenapi.requests.data.TradeContinuousData
import io.github.devngho.kisopenapi.requests.response.stock.Ticker
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * 국내 주식 종목을 매수하고, 주문 정보를 반환합니다.
 */
class OrderBuyV2(override val client: KISApiClient) :
    DataRequest<OrderBuyV2.OrderDataV2, OrderBuy.OrderResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/trading/order-cash"

    @Serializable
    @Suppress("SpellCheckingInspection")
    data class OrderDataV2(
        @SerialName("PDNO") override val ticker: String,
        @SerialName("ORD_DVSN") val orderType: OrderTypeCode,
        @Contextual @SerialName("ORD_QTY") val count: BigInteger,
        @Contextual @SerialName("ORD_UNPR") val price: BigInteger = BigInteger(0),
        @SerialName("CANO") override val accountNumber: String? = null,
        @SerialName("ACNT_PRDT_CD") override val accountProductCode: String? = null,
        /** 스탑지정가호가 주문 시 사용되는 조건 가격 */
        @Contextual @SerialName("CNDT_PRIC") val conditionPrice: BigInteger = BigInteger(0),
        @SerialName("EXCG_ID_DVSN_CD") val market: Market,
        @Transient override var corp: CorporationRequest? = null,
        @Transient override var tradeContinuous: String? = ""
    ) : Data, TradeContinuousData, Ticker, AccountInfo

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: OrderDataV2): Result<OrderBuy.OrderResponse> {
        if (client.options.useV1PolyfillForV2 && !isATSAvailable()) {
            return OrderBuy(client).call(
                OrderBuy.OrderData(
                    ticker = data.ticker,
                    orderType = data.orderType,
                    count = data.count,
                    price = data.price,
                    accountNumber = data.accountNumber,
                    accountProductCode = data.accountProductCode,
                    corp = data.corp,
                    tradeContinuous = data.tradeContinuous
                )
            )
        }

        return request(data) {
            if (it.price.isZero() && it.orderType == OrderTypeCode.SelectPrice) throw RequestException(
                "지정가 주문에서 가격은 필수 값입니다.",
                RequestCode.InvalidOrder
            )

            client.httpClient.post(url) {
                setAuth(client)
                setTR(if (client.isDemo) "VTTC0802U" else "TTTC0012U")
                setStock(it.ticker)
                setCorporation(it.corp)

                setBody(
                    it.copy(
                        accountNumber = it.accountNumber ?: client.account!!.first,
                        accountProductCode = it.accountProductCode ?: client.account!!.second
                    )
                )

                hashKey<OrderDataV2>(client)
            }
        }
    }
}