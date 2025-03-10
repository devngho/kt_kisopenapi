package io.github.devngho.kisopenapi.requests.domestic.order

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.auth.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.domestic.order.OrderBuy.OrderData
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*

/**
 * 국내 주식 종목을 매도하고, 주문 정보를 반환합니다.
 */
class OrderSell(override val client: KISApiClient) :
    DataRequest<OrderData, OrderBuy.OrderResponse> {
    private val url = "${client.options.baseUrl}/uapi/domestic-stock/v1/trading/order-cash"

    @Suppress("SpellCheckingInspection")
    override suspend fun call(data: OrderData): Result<OrderBuy.OrderResponse> {
        return request(data) {
            if (it.price.isZero() && it.orderType.isPriceSelectable) throw RequestException(
                "주문 ${it.orderType}에서 가격은 필수 값입니다.",
                RequestCode.InvalidOrder
            )

            client.httpClient.post(url) {
                setAuth(client)
                setTR(if (client.isDemo) "VTTC0011U" else "TTTC0011U")
                setStock(it.ticker)
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
}