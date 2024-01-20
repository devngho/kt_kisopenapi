package io.github.devngho.kisopenapi.requests.domestic.order

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.auth.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.data.AccountInfo.Companion.fillFrom
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*

/**
 * 국내 주식 종목을 매도하고, 주문 정보를 반환합니다.
 */
class OrderSell(override val client: KISApiClient) :
    DataRequest<OrderBuy.OrderData, OrderBuy.OrderResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/trading/order-cash"

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: OrderBuy.OrderData) = request(data) {
        if (it.price.isZero() && it.orderType == OrderTypeCode.SelectPrice)
            throw RequestException("Price must be set when order type is SelectPrice.", RequestCode.Unknown)

        client.httpClient.post(url) {
            setAuth(client)
            setTradeId(if (client.isDemo) "VTTC0801U" else "TTTC0801U")
            setStock(it.ticker)
            setCorporation(it.corp)

            setBody(it.fillFrom(client))

            hashKey<OrderBuy.OrderData>(client)
        }
    }
}