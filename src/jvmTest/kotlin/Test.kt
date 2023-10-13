import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.*
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HH_MM_SS
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer.YYYY_MM_DD
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.time.measureTime

const val testStock = "005930"
const val testOverseasStock = "AAPL"
val testOverseasMarket = OverseasMarket.NASDAQ

val api: KisOpenApi by lazy {
    val key = File("key.txt").readLines()
    val account = File("account.txt").readLines()

    runBlocking {
        KisOpenApi.with(
            key[0], key[1], false, account = account[0], grantWebsocket = true, hashKey = true
        )
    }
}

@OptIn(ExperimentalKotest::class)
class InquireTest : BehaviorSpec({
    given("API 토큰") {
        given("종목 코드") {
            `when`("InquirePrice 호출") {
                val res = InquirePrice(api).call(InquirePrice.InquirePriceData(testStock))

                then("성공한다") {
                    res.isOk shouldBe true
                    res.msg shouldNotBe null
                    res.code shouldNotBe null
                }
                then("정보를 반환한다") {
                    res.output!!.price shouldNotBe null
                    res.output!!.accumulateTradeVolume shouldNotBe null
                }
            }
            `when`("InquireOverseasPrice 호출") {
                val res = InquireOverseasPrice(api).call(
                    InquireOverseasPrice.InquirePriceData(
                        testOverseasStock,
                        testOverseasMarket
                    )
                )

                then("성공한다") {
                    res.isOk shouldBe true
                    res.msg shouldNotBe null
                    res.code shouldNotBe null
                }
                then("정보를 반환한다") {
                    res.output!!.price shouldNotBe null
                    res.output!!.tradeVolume shouldNotBe null
                }
            }
            `when`("InquireConfirm 호출") {
                val res = InquireConfirm(api).call(InquireConfirm.InquireConfirmData(testStock))

                then("성공한다") {
                    res.isOk shouldBe true
                    res.msg shouldNotBe null
                    res.code shouldNotBe null
                }
                then("빈 리스트를 반환하지 않는다") {
                    res.output shouldNotBe null
                }
                then("가격과 거래량을 반환한다") {
                    res.output!!.all { it.price != null } shouldBe true
                    res.output!!.all { it.confirmVolume != null } shouldBe true
                }
            }
            `when`("InquirePricePerDay 호출 (Days30)") {
                val res = InquirePricePerDay(api).call(
                    InquirePricePerDay.InquirePricePerDayData(
                        testStock,
                        InquirePricePerDay.PeriodDivisionCode.Days30
                    )
                )

                then("성공한다") {
                    res.isOk shouldBe true
                }
                then("빈 리스트를 반환하지 않는다") {
                    res.output shouldNotBe null
                }
                then("가격을 반환한다") {
                    res.output!!.all { it.price != null } shouldBe true
                }
                then("30일의 가격을 반환한다") {
                    res.output!!.count() shouldBe 30
                }
            }
            `when`("InquirePricePerDay 호출 (Weeks30)") {
                val res = InquirePricePerDay(api).call(
                    InquirePricePerDay.InquirePricePerDayData(
                        testStock,
                        InquirePricePerDay.PeriodDivisionCode.Weeks30
                    )
                )

                then("성공한다") {
                    res.isOk shouldBe true
                }
                then("빈 리스트를 반환하지 않는다") {
                    res.output shouldNotBe null
                }
                then("가격을 반환한다") {
                    res.output!!.all { it.price != null } shouldBe true
                }
                then("30주의 가격을 반환한다") {
                    res.output!!.count() shouldBeInRange 29..30
                }
            }
            `when`("InquirePricePerDay 호출 (Months30)") {
                val res = InquirePricePerDay(api).call(
                    InquirePricePerDay.InquirePricePerDayData(
                        testStock,
                        InquirePricePerDay.PeriodDivisionCode.Months30
                    )
                )

                then("성공한다") {
                    res.isOk shouldBe true
                }
                then("빈 리스트를 반환하지 않는다") {
                    res.output shouldNotBe null
                }
                then("가격을 반환한다") {
                    res.output!!.all { it.price != null } shouldBe true
                }
                then("30개월의 가격을 반환한다") {
                    res.output!!.count() shouldBeInRange 29..30
                }
            }
            `when`("InquirePriceSeries 호출") {
                val res = InquirePriceSeries(api)
                    .call(
                        InquirePriceSeries.InquirePriceSeriesData(
                            testStock,
                            InquirePriceSeries.PeriodDivisionCode.Days,
                            startDate = Date(2023, 1, 1),
                            endDate = Date(2023, 3, 31)
                        )
                    )

                then("성공한다") {
                    res.isOk shouldBe true
                }
                then("빈 리스트를 반환하지 않는다") {
                    res.output2 shouldNotBe null
                }
                then("가격을 반환한다") {
                    res.output2!!.all { it.price != null } shouldBe true
                }
                then("2023년 1월 1일부터 3월 31일까지의 가격을 반환한다") {
                    res.output2!!.count() shouldBe 62

                    res.output2!!.all { it.bizDate!!.YYYY_MM_DD.let { it.year == 2023 && (1..3).contains(it.month) } } shouldBe true
                }
                then("역순으로 정렬되어 있다") {
                    res.output2!!.sortedBy { it.bizDate!!.YYYY_MM_DD } shouldBe res.output2!!.reversed()
                }
            }
            `when`("InquireTodayMinute 호출") {
                val res = InquirePriceTodayMinute(api).call(
                    InquirePriceTodayMinute.InquirePriceTodayMinuteData(
                        testStock,
                        "083000",
                        true
                    )
                )

                then("성공한다") {
                    res.isOk shouldBe true
                }
                then("빈 리스트를 반환하지 않는다") {
                    res.output2 shouldNotBe null
                }
                then("가격을 반환한다") {
                    res.output2!!.all { it.price != null } shouldBe true
                }
                then("30분 동안의 가격을 반환한다") {
                    val first = res.output2!!.minBy { it.stockConfirmTime!!.HH_MM_SS }.stockConfirmTime!!.HH_MM_SS
                    val last = res.output2!!.maxBy { it.stockConfirmTime!!.HH_MM_SS }.stockConfirmTime!!.HH_MM_SS

                    val minuteDifference = (last.hour - first.hour) * 60 + (last.minute - first.minute)
                    minuteDifference shouldBe 29

                    res.output2!!.count() shouldBe minuteDifference + 1
                }
            }
        }
        `when`("InquireBalance 호출") {
            val res = InquireBalance(api).call(
                InquireBalance.InquireBalanceData(
                    false, InquireDivisionCode.ByStock,
                    includeFund = false,
                    includeYesterdaySell = false
                )
            )
            val output2 = res.output2!!.first()

            then("성공한다") {
                res.isOk shouldBe true
            }
            then("계좌 잔고를 반환한다") {
                output2.evalProfitLossTotalAmount shouldNotBe null
                output2.evalTotalAmount shouldNotBe null
                output2.assetChangeAmount shouldNotBe null
                output2.assetChangeRate shouldNotBe null
            }
            then("주식 잔고를 반환한다") {
                res.output1!!.all { it.ticker != null } shouldBe true
                res.output1!!.all { it.price != null } shouldBe true
                res.output1!!.all { it.productName != null } shouldBe true
            }
        }
        `when`("InquireOverseasBalance 호출") {
            val res = InquireOverseasBalance(api).call(
                InquireOverseasBalance.InquireBalanceData(
                    testOverseasMarket,
                    Currency.USD
                )
            )
            val output2 = res.output1!!.first()

            then("성공한다") {
                res.isOk shouldBe true
            }
            then("계좌 잔고를 반환한다") {
                output2.evalAmount shouldNotBe null
            }
            then("주식 잔고를 반환한다") {
                res.output1!!.all { it.ticker != null } shouldBe true
                res.output1!!.all { it.price != null } shouldBe true
                res.output1!!.all { it.productName != null } shouldBe true
            }
        }
        `when`("InquireHoliday 호출") {
            val res = InquireHoliday(api).call(InquireHoliday.InquireHolidayData("20230101"))

            then("성공한다") {
                res.isOk shouldBe true
            }
            then("빈 리스트를 반환하지 않는다") {
                res.output shouldNotBe null
            }
            then("주말은 휴장일이다") {
                res.output!!
                    .filter { it.weekdayCode == WeekdayCode.Sat || it.weekdayCode == WeekdayCode.Sun }
                    .none { it.isBizDay } shouldBe true
            }
            then("휴장일과 비휴장일이 모두 포함되어 있다") {
                res.output!!.any { it.isBizDay } shouldBe true
                res.output!!.any { !it.isBizDay } shouldBe true
            }
            then("다음 목록을 가져올 수 있다") {
                (res.next!!() as? InquireHoliday.InquireHolidayResponse)?.isOk shouldBe true
            }
        }
        `when`("InquireTradeVolumeRank 호출") {
            val result = InquireTradeVolumeRank(api).call(
                InquireTradeVolumeRank.InquireTradeVolumeRankData(
                    InquireTradeVolumeRank.BelongClassifier.AverageTradeVolume
                )
            )

            then("성공한다") {
                result.isOk shouldBe true
            }
            then("빈 리스트를 반환하지 않는다") {
                result.output shouldNotBe null
            }
            then("거래량을 반환한다") {
                result.output!!.all { it.averageTradeVolume != null } shouldBe true
            }
            then("거래량 순으로 정렬되어 있다") {
                result.output!!.sortedByDescending { it.averageTradeVolume!! } shouldBe result.output
            }
            then("중복되는 순위가 없다") {
                result.output!!.map { it.rank }.distinct().size shouldBe result.output!!.size
            }
            then("중복되는 종목이 없다") {
                result.output!!.map { it.ticker }.distinct().size shouldBe result.output!!.size
            }
        }
        `when`("InquireTradeVolumeRank 호출 (제외 조건 적용)") {
            val result = InquireTradeVolumeRank(api).call(
                InquireTradeVolumeRank.InquireTradeVolumeRankData(
                    InquireTradeVolumeRank.BelongClassifier.AverageTradeVolume,
                    excludeForAdministration = true,
                    excludeInvestmentAlert = true,
                    excludeInvestmentDanger = true,
                    excludeInvestmentWarning = true,
                    excludeUnreliableDisclosure = true,
                    excludePreferredShare = true,
                    excludeTradeSuspended = true,
                )
            )

            then("성공한다") {
                result.isOk shouldBe true
            }
            then("빈 리스트를 반환하지 않는다") {
                result.output shouldNotBe null
            }
            then("제외 조건을 만족한다") {
                result.output!!.map {
                    it to ProductBaseInfo(api).call(
                        ProductBaseInfo.ProductBaseInfoData(
                            it.ticker!!,
                            ProductTypeCode.Stock
                        )
                    ).output!!
                }.forEach { (rank, info) ->
                    rank.ticker shouldBe info.codeShort
                    rank.name shouldBe info.nameShort
                    info.productRiskGrade shouldBe ""
                }
            }
        }
        `when`("InquireOverseasCondition 호출") {
            val result = InquireOverseasCondition(api).call(
                InquireOverseasCondition.ConditionData(
                    OverseasMarket.AMEX,
                    priceRange = BigDecimalRange(BigDecimal.fromInt(100), BigDecimal.fromInt(200)),
                )
            )

            then("성공한다") {
                result.isOk shouldBe true
            }
            then("빈 리스트를 반환하지 않는다") {
                result.output shouldNotBe null
            }
            then("가격을 반환한다") {
                result.output!!.all { it.price != null } shouldBe true
            }
            then("가격 범위를 만족한다") {
                result.output!!.forEach {
                    it.price!! shouldBeGreaterThanOrEqualTo BigDecimal.fromInt(100)
                    it.price!! shouldBeLessThanOrEqualTo BigDecimal.fromInt(200)
                }
            }
        }
        `when`("유량을 넘는 호출") {
            var res: Result<Unit>? = null

            val t = measureTime {
                res = runCatching {
                    repeat(100) {
                        InquirePrice(api).call(InquirePrice.InquirePriceData(testStock))
                    }
                }
            }

            val minTime = api.rateLimiter.minDelay * 100

            then("성공한다") {
                res?.isSuccess shouldBe true
            }

            then("${minTime}ms 이상 걸린다") {
                t.inWholeMicroseconds shouldBeGreaterThanOrEqualTo minTime.toLong()
            }
        }
        given("웹소켓") {
            `when`("연결") {
                api.buildWebsocket()
                then("성공한다") {
                    api.websocket shouldNotBe null
                    api.websocketIncoming shouldNotBe null
                }
            }
        }
    }
})