package io.github.devngho.kisopenapi.requests

import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.response.setNext
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.OverseasMarket.Companion.fourChar
import io.ktor.client.call.*
import io.ktor.client.request.*

class OrderOverseasSell(override val client: KisOpenApi):
    DataRequest<OrderOverseasBuy.OrderData, OrderOverseasBuy.OrderResponse> {
    private val url = if (client.isDemo) "https://openapivts.koreainvestment.com:29443/uapi/overseas-stock/v1/trading/order"
    else               "https://openapi.koreainvestment.com:9443/uapi/overseas-stock/v1/trading/order"

    override suspend fun call(data: OrderOverseasBuy.OrderData): OrderOverseasBuy.OrderResponse {
        if (data.corp == null) data.corp = client.corp

        val res = client.httpClient.post(url) {
            auth(client)
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
                            OverseasMarket.BAA -> "T1006U" // USA
                            OverseasMarket.TOYKO,
                            OverseasMarket.TSE -> "S0307U"
                            OverseasMarket.SHANGHAI,
                            OverseasMarket.SHANGHAI_INDEX,
                            OverseasMarket.SHS,
                            OverseasMarket.SHI -> "S1005U"
                            OverseasMarket.HONGKONG,
                            OverseasMarket.HKS -> "S1001U"
                            OverseasMarket.SHENZHEN,
                            OverseasMarket.SHENZHEN_INDEX,
                            OverseasMarket.SZI,
                            OverseasMarket.SZS -> "S0304U"
                            OverseasMarket.HANOI,
                            OverseasMarket.HOCHIMINH,
                            OverseasMarket.HNX,
                            OverseasMarket.HSX -> "S0310U"
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
                        if (client.isDemo && data.orderType != OrderTypeCode.SelectPrice) throw RequestError("Invalid order type. Only SelectPrice is allowed in demo.")
                        when(data.orderType) {
                            OrderTypeCode.SelectPrice -> "00"
                            OrderTypeCode.USALimitOnClose -> "34"
                            OrderTypeCode.USALimitOnOpen -> "32"
                            OrderTypeCode.USAMarketOnClose -> "33"
                            OrderTypeCode.USAMarketOnOpen -> "31"
                            else -> throw RequestError("Invalid order type. Only SelectPrice, USALimitOnClose, USALimitOnOpen, USAMarketOnClose, USAMarketOnOpen are allowed in USA.")
                        }
                    }
                    OverseasMarket.HONGKONG,
                    OverseasMarket.HKS -> {
                        if (client.isDemo && data.orderType != OrderTypeCode.SelectPrice) throw RequestError("Invalid order type. Only SelectPrice is allowed in demo.")
                        when(data.orderType) {
                            OrderTypeCode.SelectPrice -> "00"
                            OrderTypeCode.HONGKONGSingleSelectPrice -> "50"
                            else -> throw RequestError("Invalid order type. Only SelectPrice, HONGKONGSingleSelectPrice are allowed in HKEX.")
                        }
                    }
                    else -> ""
                }

            if (data.price.isZero() && data.orderType == OrderTypeCode.SelectPrice) throw RequestError("Price must be set when order type is SelectPrice.")

            tradeId(tradeId)
            stock(data.ticker)
            data.corp?.let { corporation(it) }
            setBody(
                (OrderOverseasBuy.OrderDataJson(
                    client.account!![0],
                    client.account!![1],
                    data.market.fourChar,
                    data.ticker,
                    orderType,
                    data.count,
                    data.price,
                    "0",
                    "00"
                ))
            )

            hashKey<OrderOverseasBuy.OrderDataJson>(client)
        }
        return res.body<OrderOverseasBuy.OrderResponse>().apply {
            if (this.errorCode != null) throw RequestError(this.errorDescription)

            processHeader(res)
            setNext(data, this)
        }
    }
}