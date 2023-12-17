package io.github.devngho.kisopenapi.layer

import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KISApiClient
import io.github.devngho.kisopenapi.requests.domestic.inquire.live.InquireLivePrice
import io.github.devngho.kisopenapi.requests.domestic.order.OrderAmend
import io.github.devngho.kisopenapi.requests.domestic.order.OrderBuy
import io.github.devngho.kisopenapi.requests.domestic.order.OrderCancel
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPriceBase
import io.github.devngho.kisopenapi.requests.response.stock.trade.StockTrade
import io.github.devngho.kisopenapi.requests.util.Closeable
import io.github.devngho.kisopenapi.requests.util.OrderTypeCode
import io.github.devngho.kisopenapi.requests.util.Result
import kotlin.jvm.JvmStatic


interface StockDomestic : StockBase {
    override val client: KISApiClient
    override val ticker: String

    var price: StockPriceBase
    override var name: StockBase.Name
    var tradeVolume: StockTrade

    /**
     * 주식을 매수합니다.
     *
     * @see [OrderBuy]
     * @param count 매수할 주식 수량
     * @param type 주문 구분
     * @param price 매수할 주식 가격
     * @return 주문 결과
     */
    suspend fun buy(
        count: BigInteger,
        type: OrderTypeCode,
        price: BigInteger = BigInteger(0)
    ): Result<OrderBuy.OrderResponse>

    /**
     * 주식을 매도합니다.
     *
     * @see [io.github.devngho.kisopenapi.requests.domestic.order.OrderSell]
     * @param count 매수할 주식 수량
     * @param type 주문 구분
     * @param price 매수할 주식 가격
     * @return 주문 결과
     */
    suspend fun sell(
        count: BigInteger,
        type: OrderTypeCode,
        price: BigInteger = BigInteger(0)
    ): Result<OrderBuy.OrderResponse>

    /**
     * 주식 주문을 정정합니다. 기존 주문 결과를 받아서 정정합니다.
     *
     * @see [OrderAmend]
     * @param order 정정할 주문 결과
     * @param count 정정할 주식 수량
     * @param type 주문 구분
     * @param price 정정할 주식 가격
     * @param orderAll 주문 수량의 전부를 정정할지 여부
     */
    suspend fun amend(
        order: OrderBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
        price: BigInteger = BigInteger(0),
        orderAll: Boolean
    ): Result<OrderAmend.OrderResponse>

    /**
     * 주식 주문을 취소합니다. 기존 주문 결과를 받아서 취소합니다.
     *
     * @see [OrderCancel]
     * @param order 취소할 주문 결과
     * @param count 취소할 주식 수량
     * @param type 주문 구분
     * @param orderAll 주문 수량의 전부를 취소할지 여부
     */
    suspend fun cancel(
        order: OrderBuy.OrderResponse,
        count: BigInteger,
        type: OrderTypeCode,
        orderAll: Boolean
    ): Result<OrderCancel.OrderResponse>

    /**
     * 실시간 가격을 사용합니다. 사용이 끝나면 [Closeable.close]를 사용해 닫을 수 있습니다.
     *
     * @see [InquireLivePrice]
     * @param block 가격이 업데이트 될 때마다 호출될 블록
     */
    suspend fun useLiveConfirmPrice(block: Closeable.(InquireLivePrice.InquireLivePriceResponse) -> Unit)

    companion object {
        @JvmStatic
        fun create(
            client: KISApiClient,
            ticker: String
        ): StockDomestic = StockDomesticImpl(client, ticker)
    }
}