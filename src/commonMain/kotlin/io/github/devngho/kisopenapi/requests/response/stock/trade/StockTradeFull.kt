package io.github.devngho.kisopenapi.requests.response.stock.trade

import io.github.devngho.kisopenapi.Updatable

/**
 * [KIS Developers 문서](https://apiportal.koreainvestment.com/apiservice/apiservice-domestic-stock-quotations)를 참조하세요.
 * @see io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice
 */
@Updatable
interface StockTradeFull : StockTradeAccumulate, StockTradeRate
