package io.github.devngho.kisopenapi.requests.domestic.order

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.auth.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.domestic.order.OrderBuyV2.OrderDataV2
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*

/**
 * 국내 주식 종목을 매도하고, 주문 정보를 반환합니다.
 */
class OrderSellV2(override val client: KISApiClient) :
    DataRequest<OrderDataV2, OrderBuy.OrderResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/trading/order-cash"

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: OrderDataV2): Result<OrderBuy.OrderResponse> {
        if (client.options.useV1PolyfillForV2 && !isATSAvailable()) {
            return OrderSell(client).call(
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
                setTR(if (client.isDemo) "VTTC0801U" else "TTTC0011U")
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