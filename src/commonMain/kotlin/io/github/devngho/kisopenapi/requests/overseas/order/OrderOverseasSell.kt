package io.github.devngho.kisopenapi.requests.overseas.order

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.auth.HashKey.Companion.hashKey
import io.github.devngho.kisopenapi.requests.data.AccountInfo.Companion.fillFrom
import io.github.devngho.kisopenapi.requests.util.*
import io.ktor.client.request.*

/**
 * 해외 주식 종목을 매도하고, 주문 정보를 반환합니다.
 */
class OrderOverseasSell(override val client: KISApiClient) :
    DataRequest<OrderOverseasBuy.OrderData, OrderOverseasBuy.OrderResponse> {
    private val url = "${client.options.baseUrl}/uapi/overseas-stock/v1/trading/order"

    override suspend fun call(data: OrderOverseasBuy.OrderData) = request(data) {
        client.httpClient.post(url) {
            setAuth(client)
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
                        if (client.isDemo && data.orderType != OrderTypeCode.SelectPrice) throw RequestException(
                            "Invalid order type. Only SelectPrice is allowed in demo.",
                            RequestCode.DemoUnavailable
                        )
                        when(data.orderType) {
                            OrderTypeCode.SelectPrice -> "00"
                            OrderTypeCode.USALimitOnClose -> "34"
                            OrderTypeCode.USALimitOnOpen -> "32"
                            OrderTypeCode.USAMarketOnClose -> "33"
                            OrderTypeCode.USAMarketOnOpen -> "31"
                            else -> throw RequestException(
                                "Invalid order type. Only SelectPrice, USALimitOnClose, USALimitOnOpen, USAMarketOnClose, USAMarketOnOpen are allowed in USA.",
                                RequestCode.InvalidOrder
                            )
                        }
                    }
                    OverseasMarket.HONGKONG,
                    OverseasMarket.HKS -> {
                        if (client.isDemo && data.orderType != OrderTypeCode.SelectPrice) throw RequestException(
                            "Invalid order type. Only SelectPrice is allowed in demo.",
                            RequestCode.DemoUnavailable
                        )
                        when(data.orderType) {
                            OrderTypeCode.SelectPrice -> "00"
                            OrderTypeCode.HONGKONGSingleSelectPrice -> "50"
                            else -> throw RequestException(
                                "Invalid order type. Only SelectPrice, HONGKONGSingleSelectPrice are allowed in Hong Kong Stock Exchange.",
                                RequestCode.InvalidOrder
                            )
                        }
                    }
                    else -> ""
                }

            if (data.price.isZero() && data.orderType == OrderTypeCode.SelectPrice) throw RequestException(
                "Price must be set when order type is SelectPrice.",
                RequestCode.InvalidOrder
            )

            setTradeId(tradeId)
            setStock(data.ticker)
            data.corp?.let { setCorporation(it) }
            setBody(
                data
                    .copy(sellType = "00", marketId = tradeId, orderTypeId = orderType)
                    .fillFrom(client)
            )

            hashKey<OrderOverseasBuy.OrderData>(client)
        }
    }
}