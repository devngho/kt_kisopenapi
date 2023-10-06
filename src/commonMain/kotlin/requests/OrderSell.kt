package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.response.setNext
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class OrderSell(override val client: KisOpenApi):
    DataRequest<OrderBuy.OrderData, OrderBuy.OrderResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/domestic-stock/v1/trading/order-cash"
    else               "https://openapi.koreainvestment.com:9443/uapi/domestic-stock/v1/trading/order-cash"

    override suspend fun call(data: OrderBuy.OrderData): OrderBuy.OrderResponse {
        if (data.corp == null) data.corp = client.corp

        if (data.price.isZero() && data.orderType == OrderTypeCode.SelectPrice) throw RequestError("Price must be set when order type is SelectPrice.")

        val res = client.httpClient.post(url) {
            auth(client)
            tradeId(if(client.isDemo) "VTTC0801U" else "TTTC0801U")
            stock(data.ticker)
            data.corp?.let { corporation(it) }
            setBody(
                OrderBuy.OrderDataJson(
                    client.account!![0],
                    client.account!![1],
                    data.ticker,
                    data.orderType,
                    data.count,
                    data.price
                )
            )

            hashKey<OrderBuy.OrderDataJson>(client)
        }
        return res.body<OrderBuy.OrderResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
            setNext(data, this)
        }
    }
}