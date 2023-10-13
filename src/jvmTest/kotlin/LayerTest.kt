import io.github.devngho.kisopenapi.layer.AccountDomestic
import io.github.devngho.kisopenapi.layer.AccountOverseas
import io.github.devngho.kisopenapi.layer.StockDomestic
import io.github.devngho.kisopenapi.layer.StockOverseas
import io.github.devngho.kisopenapi.requests.response.BalanceAccount
import io.github.devngho.kisopenapi.requests.response.BaseInfo
import io.github.devngho.kisopenapi.requests.response.StockOverseasPrice
import io.github.devngho.kisopenapi.requests.response.StockPrice
import io.github.devngho.kisopenapi.requests.util.Currency
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.withTimeout
import java.util.concurrent.CountDownLatch
import kotlin.reflect.full.declaredMembers

@OptIn(ExperimentalKotest::class)
class LayerTest : BehaviorSpec({
    given("API 토큰") {
        given("종목 코드") {
            `when`("StockDomestic 업데이트") {
                val stock = StockDomestic(api, testStock)

                stock.updateBy(StockPrice::class)
                stock.updateBy(BaseInfo::class)

                then("종목 이름을 가져올 수 있다") {
                    stock.name.nameShort shouldBe "삼성전자"
                }
                then("종목 가격을 가져올 수 있다") {
                    stock.price.price shouldNotBe null
                }
                xthen("실시간 가격을 가져올 수 있다") {
                    val latch = CountDownLatch(1)
                    stock.useLiveConfirmPrice {
                        latch.countDown()
                        it.price shouldNotBe null
                    }
                    latch.await()
                }
            }

            `when`("StockOverseas 업데이트") {
                val stock = StockOverseas(api, testOverseasStock, testOverseasMarket)

                stock.updateBy(StockOverseasPrice::class)
                stock.updateBy(BaseInfo::class)

                then("종목 이름을 가져올 수 있다") {
                    stock.name.name shouldBe "애플"
                }
                then("종목 가격을 가져올 수 있다") {
                    stock.price.price shouldNotBe null
                }
                xthen("실시간 가격을 가져올 수 있다") {
                    var isDone = false
                    withTimeout(1000) {
                        val latch = CountDownLatch(1)
                        stock.useLiveConfirmPrice {
                            latch.countDown()
                            it.price shouldNotBe null
                        }
                        latch.await()
                        isDone = true
                    }
                    isDone shouldBe true
            }
        }

        `when`("AccountDomestic 업데이트") {
            val accountLayer = AccountDomestic(api)

            accountLayer.updateBy(BalanceAccount::class)

            then("계좌 잔액을 가져올 수 있다") {
                accountLayer.assetAmount shouldNotBe null
            }
            then("계좌 주식을 가져올 수 있다") {
                accountLayer.accountStocks shouldNotBe null
                accountLayer.accountStocks.forEach {
                    it::class.declaredMembers.forEach { f ->
                        f.call() shouldNotBe null
                    }
                }
            }
        }

        `when`("AccountOverseas 업데이트") {
            val accountLayer = AccountOverseas(api, testOverseasMarket, Currency.USD)

            accountLayer.updateBy(BalanceAccount::class)

            then("보유한 주식을 가져올 수 있다") {
                accountLayer.accountStocks shouldNotBe null
            }
        }
        }
    }
})