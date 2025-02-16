package io.github.devngho.kisopenapi.requests.overseas.order

import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.DataRequest
import io.github.devngho.kisopenapi.requests.auth.HashKey.Companion.hashKey
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
                            OverseasMarket.TOKYO,
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
                            "모의 투자에서는 지정가 주문만 가능합니다.",
                            RequestCode.DemoUnavailable
                        )
                        when(data.orderType) {
                            OrderTypeCode.SelectPrice -> "00"
                            OrderTypeCode.USALimitOnClose -> "34"
                            OrderTypeCode.USALimitOnOpen -> "32"
                            OrderTypeCode.USAMarketOnClose -> "33"
                            OrderTypeCode.USAMarketOnOpen -> "31"
                            else -> throw RequestException(
                                "미국 거래소에서는 지정가, 장마감지정가(USALimitOnClose), 장개시지정가(USALimitOnOpen), 장마감시장가(USAMarketOnClose), 장개시시장가(USAMarketOnOpen) 주문만 가능합니다.",
                                RequestCode.InvalidOrder
                            )
                        }
                    }
                    OverseasMarket.HONGKONG,
                    OverseasMarket.HKS -> {
                        if (client.isDemo && data.orderType != OrderTypeCode.SelectPrice) throw RequestException(
                            "모의 투자에서는 지정가 주문만 가능합니다.",
                            RequestCode.DemoUnavailable
                        )
                        when(data.orderType) {
                            OrderTypeCode.SelectPrice -> "00"
                            OrderTypeCode.HONGKONGSingleSelectPrice -> "50"
                            else -> throw RequestException(
                                "홍콩 거래소에서는 지정가, 단주지정가(HONGKONGSingleSelectPrice) 주문만 가능합니다.",
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

            setTR(tradeId)
            setStock(data.ticker)
            data.corp?.let { setCorporation(it) }
            setBody(
                data
                    .copy(
                        sellType = "00",
                        marketId = tradeId,
                        orderTypeId = orderType,
                        accountNumber = it.accountNumber ?: client.account!!.first,
                        accountProductCode = it.accountProductCode ?: client.account!!.second
                    )
            )

            hashKey<OrderOverseasBuy.OrderData>(client)
        }
    }
}