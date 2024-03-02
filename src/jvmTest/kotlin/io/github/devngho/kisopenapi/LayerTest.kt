package io.github.devngho.kisopenapi

import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import io.github.devngho.kisopenapi.layer.*
import io.github.devngho.kisopenapi.layer.Updatable.Companion.update
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquireTradeVolumeRank
import io.github.devngho.kisopenapi.requests.response.balance.domestic.BalanceAccount
import io.github.devngho.kisopenapi.requests.response.balance.overseas.BalanceAccountOverseas
import io.github.devngho.kisopenapi.requests.response.stock.BaseInfo
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPrice
import io.github.devngho.kisopenapi.requests.response.stock.price.overseas.StockOverseasPriceFull
import io.github.devngho.kisopenapi.requests.util.Currency
import io.github.devngho.kisopenapi.requests.util.Date
import io.github.devngho.kisopenapi.requests.util.DemoNotSupported
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

@OptIn(DemoNotSupported::class)
@Suppress("SpellCheckingInspection")
class LayerTest : BehaviorSpec({
    xgiven("앱 키, 시크릿") {
        `when`("API 토큰을 발급받는다") {
            api.tokens.issue()

            then("토큰을 발급받을 수 있다") {
                api.tokens.oauthToken shouldNotBe null
            }
        }
        `when`("API 토큰을 폐기한다") {
            api.tokens.revoke()

            then("토큰을 폐기할 수 있다") {
                api.tokens.oauthToken shouldBe null
            }
        }
        `when`("API 토큰이 만료되었을 때") {
            api.tokens.webSocketTokenExpire = 0

            then("만료 여부를 확인할 수 있다") {
                api.tokens.isExpired shouldBe true
            }
            then("재발급받을 수 있다") {
                api.tokens.issueIfExpired()

                api.tokens.isExpired shouldBe false
                api.tokens.oauthToken shouldNotBe null
            }
        }
    }
    given("API 토큰, 종목 코드") {
        `when`("StockDomestic 업데이트") {
            val stock = api.stockDomestic(testStock)

            stock.update<StockPrice>()
            stock.update<BaseInfo>()

            then("종목 이름을 가져올 수 있다") {
                stock.name.nameShort shouldBe "삼성전자"
            }
            then("종목 가격을 가져올 수 있다") {
                stock.price.price shouldNotBe null
            }
            xthen("실시간 가격을 가져올 수 있다") {
                val latch = Channel<Unit>()
                stock.useLiveConfirmPrice {
                    runBlocking {
                        latch.send(Unit)
                        it.price shouldNotBe null
                    }
                }
                latch.receive()
            }
        }
        `when`("StockOverseas 업데이트") {
            val stock = api.stockOverseas(testOverseasStock, testOverseasMarket)

            stock.update<StockOverseasPriceFull>()
            stock.update<BaseInfo>()

            then("종목 이름을 가져올 수 있다") {
                stock.name.name shouldBe "애플"
            }
            then("종목 가격을 가져올 수 있다") {
                stock.price.price shouldNotBe null
            }
            then("종목 상세 정보를 가져올 수 있다") {
                (stock.price as StockOverseasPriceFull).pbr shouldNotBe null
            }
            xthen("실시간 가격을 가져올 수 있다") {
                var isDone = false
                withTimeout(1000) {
                    val latch = Channel<Unit>()
                    stock.useLiveConfirmPrice {
                        runBlocking {
                            latch.send(Unit)
                            it.price shouldNotBe null
                        }
                    }
                    latch.receive()
                    isDone = true
                }
                isDone shouldBe true
            }
        }

        `when`("AccountDomestic 업데이트") {
            val account = api.accountDomestic()

            account.update<BalanceAccount>()

            then("계좌 잔액을 가져올 수 있다") {
                account.assetAmount shouldNotBe null
            }
            then("계좌 주식을 가져올 수 있다") {
                account.accountStocks shouldNotBe null
                account.accountStocks.forEach {
                    it.name shouldNotBe null
                    it.count shouldNotBe null
                }
            }
        }

        `when`("AccountOverseas 업데이트") {
            val account = api.accountOverseas(testOverseasMarket, Currency.USD)

            account.update<BalanceAccountOverseas>()

            then("보유한 주식을 가져올 수 있다") {
                account.accountStocks shouldNotBe null
            }
        }

        `when`("MarkerDomestic 종목 거래량 순위") {
            val market = api.krx()
            val res = market.getRank(InquireTradeVolumeRank.BelongClassifier.TradeVolume)

            then("가져올 수 있다") {
                res.isOk shouldBe true
                res.getOrThrow() shouldNotBe null
            }
        }

        `when`("MarkerDomestic 휴일 정보") {
            val market = api.krx()
            val resSingle = market.isHoliday(Date(2023, 12, 18))
            val resRange = market.getHolidays(Date(2023, 1, 1), Date(2023, 12, 31))
            val resRange2 = market.getHolidays(Date(2023, 1, 1)..Date(2023, 12, 31))

            then("단일 정보를 가져올 수 있다") {
                resSingle.isOk shouldBe true
                resSingle.getOrThrow() shouldNotBe null
            }
            then("2023년 12월 18일은 개장일이다") {
                resSingle.getOrThrow() shouldBe true
            }

            then("범위 정보를 가져올 수 있다") {
                resRange.isOk shouldBe true
                resRange.getOrThrow() shouldNotBe null
            }

            then("범위의 모든 날짜가 비어있지 않다") {
                resRange.getOrThrow().keys.distinct().size shouldBe resRange.getOrThrow().size
            }

            then("모든 날짜가 고유하다") {
                resRange.getOrThrow().keys.distinct().size shouldBe resRange.getOrThrow().size
            }

            then("2023년 1월 1일은 휴장일이다") {
                resRange.getOrThrow().entries.first { it.key == Date(2023, 1, 1) }.value shouldBe false
            }

            then("2023년 12월 31일은 휴장일이다") {
                resRange.getOrThrow().entries.first { it.key == Date(2023, 12, 31) }.value shouldBe false
            }

            then("두 범위 조회 결과가 같다") {
                resRange.getOrThrow() shouldBe resRange2.getOrThrow()
            }
        }

        `when`("MarketDomestic 조건 검색") {
            val market = api.krx()
            val conditions = market.getSearchConditions()
            val result = if (conditions.isOk) market.search(conditions.getOrThrow().first().conditionKey) else null

            then("조건 목록을 가져올 수 있다") {
                conditions.isOk shouldBe true
                conditions.getOrThrow() shouldNotBe null
            }

            then("가져온 조건을 검색할 수 있다") {
                result shouldNotBe null
                result?.isOk shouldBe true
                result?.getOrThrow() shouldNotBe null
            }
        }

        `when`("MarkerOverseas") {
            val market = api.marketOverseas(testOverseasMarket)

            then("종목을 검색할 수 있다 - 1") {
                val res = market.search {
                    priceRange = 100.toBigDecimal()..200.toBigDecimal()
                }

                res.isOk shouldBe true
                res.getOrThrow() shouldNotBe null
            }

            then("종목을 검색할 수 있다 - 2") {
                val res = market.search(MarketOverseas.StockSearchQuery.stockSearchQuery {
                    priceRange = 100.toBigDecimal()..200.toBigDecimal()
                })

                res.isOk shouldBe true
                res.getOrThrow() shouldNotBe null
            }
        }
    }
})