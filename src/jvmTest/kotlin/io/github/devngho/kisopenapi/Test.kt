package io.github.devngho.kisopenapi

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.github.devngho.kisopenapi.KISApiClient.Companion.options
import io.github.devngho.kisopenapi.requests.common.InquireProductBaseInfo
import io.github.devngho.kisopenapi.requests.domestic.inquire.*
import io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasBalance
import io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasCondition
import io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasDetailedPrice
import io.github.devngho.kisopenapi.requests.overseas.inquire.InquireOverseasPrice
import io.github.devngho.kisopenapi.requests.util.*
import io.github.devngho.kisopenapi.requests.util.HHMMSSSerializer.HHMMSS
import io.github.devngho.kisopenapi.requests.util.YYYYMMDDSerializer.YYYYMMDD
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.collections.shouldBeSortedBy
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.number
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.io.FileNotFoundException
import kotlin.Result
import kotlin.time.measureTime

const val testStock = "005930"
val testStocks = listOf("005930", "373220", "000660", "207940", "005380")

@Suppress("SpellCheckingInspection")
const val testOverseasStock = "AAPL"
val testOverseasMarket = OverseasMarket.NASDAQ

@Suppress("SpellCheckingInspection")
val testOverseasStocks = listOf("AAPL", "MSFT", "AMZN", "GOOGL")

const val testSector = "0001" // KOSPI

fun readLines(path: String): List<String> = File(path).readLines()
fun writeText(path: String, text: String) = File(path).writeText(text)

/**
 * 테스트 시에 사용할 API 클라이언트입니다. 자동으로 토큰을 발급받습니다.
 * 테스트를 위해 key.txt, account.txt, id.txt 파일을 작성해주세요.
 *
 * 웹소켓 클라이언트가 [WebSocketTest.WebSocketMockClient]로 대체되어 있습니다.
 */
val api: KISApiClient by lazy {
    runBlocking {
        val key: List<String>
        val account: List<String>
        val token: KISApiClient.KISApiTokens
        val htsId: List<String>

        try {
            key = readLines("key.txt")
            account = readLines("account.txt")
            token = kotlin.runCatching {
                json.decodeFromString<KISApiClient.KISApiTokens>(
                    readLines("token.txt").joinToString("")
                )
            }.getOrDefault(KISApiClient.KISApiTokens("", ""))
            token.issueIfExpired()
            htsId = readLines("id.txt")
        } catch (e: FileNotFoundException) {
            throw FileNotFoundException("테스트를 위해 key.txt, account.txt, id.txt와 빈 token.txt 파일을 작성해주세요.")

            /*
            key.txt: 첫 줄에 appKey, 둘째 줄에 appSecret을 작성해주세요.
            account.txt: 첫 줄에 계좌번호를 작성해주세요.
            id: 첫 줄에 HTS ID를 작성해주세요.
             */
        }

        if (!token.isExpired) {
            KISApiClient.withToken(
                token,
                key[0],
                key[1],
                false,
                account = account[0],
                id = htsId[0]
            ).options {
//                useHashKey = true
                autoReconnect = true
                webSocketClient = WebSocketTest.WebSocketMockClient(webSocketClient)
            }
        } else {
            KISApiClient.with(
                key[0], key[1], false, account = account[0], id = htsId[0]
            ).options {
//                useHashKey = true
                autoReconnect = true
                webSocketClient = WebSocketTest.WebSocketMockClient(webSocketClient)
            }.apply {
                writeText(
                    "token.txt",
                    json.encodeToString(this.tokens)
                )
            }
        }
    }
}

@OptIn(DemoNotSupported::class, ExperimentalSerializationApi::class)
@Suppress("SpellCheckingInspection")
class InquireTest : BehaviorSpec({
    given("API 토큰") {
        and("종목 코드") {
            `when`("InquirePrice 호출") {
                val res = InquirePrice(api).call(InquirePrice.InquirePriceData(testStock)).getOrThrow()

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
                ).getOrThrow()

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
            `when`("InquireOverseasDetailedPrice 호출") {
                val res = InquireOverseasDetailedPrice(api).call(
                    InquireOverseasDetailedPrice.InquirePriceData(
                        testOverseasStock,
                        testOverseasMarket
                    )
                ).getOrThrow()

                then("성공한다") {
                    res.isOk shouldBe true
                    res.msg shouldNotBe null
                    res.code shouldNotBe null
                }
                then("정보를 반환한다") {
                    res.output!!.price shouldNotBe null
                    res.output!!.tradeVolume shouldNotBe null
                    res.output!!.priceKRW shouldNotBe null
                }
            }
            `when`("InquireConfirm 호출") {
                val res = InquireConfirm(api).call(InquireConfirm.InquireConfirmData(testStock)).getOrThrow()

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
                ).getOrThrow()

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
                ).getOrThrow()

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
                ).getOrThrow()

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
                            PeriodDivisionCode.Days,
                            startDate = Date(2023, 1, 1),
                            endDate = Date(2023, 3, 31)
                        )
                    ).getOrThrow()

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

                    res.output2!!.shouldForAll { it.bizDate!!.dayOfMonth shouldBeInRange 1..31 }
                    res.output2!!.shouldForAll { it.bizDate!!.month.number shouldBeInRange 1..3 }
                }
                then("역순으로 정렬되어 있다") {
                    res.output2!!.sortedBy { it.bizDate!!.YYYYMMDD } shouldBe res.output2!!.reversed()
                }
            }
            `when`("InquireTodayMinute 호출") {
                val res = InquirePriceTodayMinute(api).call(
                    InquirePriceTodayMinute.InquirePriceTodayMinuteData(
                        testStock,
                        "083000".HHMMSS,
                        true
                    )
                ).getOrThrow()

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
                    val first = res.output2!!.minBy { it.stockConfirmTime!!.HHMMSS }.stockConfirmTime!!.HHMMSS
                    val last = res.output2!!.maxBy { it.stockConfirmTime!!.HHMMSS }.stockConfirmTime!!.HHMMSS

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
                    includeYesterdayTrade = false
                )
            ).getOrThrow()
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
            ).getOrThrow()

            val output2 = res.output2!!

            then("성공한다") {
                res.isOk shouldBe true
            }
            then("계좌 정보를 반환한다") {
                output2.totalProfitLoss shouldNotBe null
            }
            then("주식 잔고를 반환한다") {
                res.output1!!.all { it.ticker != null } shouldBe true
                res.output1!!.all { it.price != null } shouldBe true
                res.output1!!.all { it.productName != null } shouldBe true
            }
        }
        `when`("InquireHoliday 호출") {
            val res = InquireHoliday(api).call(InquireHoliday.InquireHolidayData("20230101".YYYYMMDD)).getOrThrow()

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
            then("정렬되어 있다") {
                res.output!! shouldBeSortedBy { it.baseDate }
            }

            and("다음 목록 조회") {
                val next = res.next!!().getOrThrow()

                then("다음 목록을 가져올 수 있다") {
                    next.isOk shouldBe true
                    next.output shouldNotBe null
                }

                then("이전 목록보다 이후의 날짜를 반환한다") {
                    next.output!!.shouldForAll { it.baseDate shouldBeGreaterThan res.output!!.last().baseDate }
                }
            }
        }
        `when`("InquireTradeVolumeRank 호출") {
            val result = InquireTradeVolumeRank(api).call(
                InquireTradeVolumeRank.InquireTradeVolumeRankData(
                    InquireTradeVolumeRank.BelongClassifier.AverageTradeVolume
                )
            ).getOrThrow()

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
            ).getOrThrow()

            then("성공한다") {
                result.isOk shouldBe true
            }
            then("빈 리스트를 반환하지 않는다") {
                result.output shouldNotBe null
            }
            then("제외 조건을 만족한다") {
                result.output!!.map {
                    it to InquireProductBaseInfo(api).call(
                        InquireProductBaseInfo.InquireProductBaseInfoData(
                            it.ticker!!,
                            ProductTypeCode.Stock
                        )
                    ).getOrThrow().output!!
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
                    priceRange = BigDecimal.fromInt(100)..BigDecimal.fromInt(200),
                )
            ).getOrThrow()

            then("성공한다") {
                result.isOk shouldBe true
            }
            then("빈 리스트를 반환하지 않는다") {
                result.output shouldNotBe null
            }
            then("정보를 반환한다") {
                result.output!!.all { it.price != null } shouldBe true
                result.output!!.all { it.rank != null } shouldBe true
            }
            then("가격 범위를 만족한다") {
                result.output!!.forEach {
                    it.price!! shouldBeGreaterThanOrEqualTo BigDecimal.fromInt(100)
                    it.price shouldBeLessThanOrEqualTo BigDecimal.fromInt(200)
                }
            }
        }
        `when`("InquireConditionList 호출") {
            val result = InquireConditionList(api).call(InquireConditionList.ConditionData()).getOrThrow()

            then("성공한다") {
                result.isOk shouldBe true
            }
            then("리스트를 반환한다") {
                result.output shouldNotBe null
            }
        }
        xwhen("InquireCondition 호출") {
            val results = InquireConditionList(api).call(InquireConditionList.ConditionData()).getOrThrow()
            val result = InquireCondition(api).call(
                InquireCondition.ConditionData(results.output?.first()?.conditionKey!!)
            ).getOrThrow()

            then("성공한다") {
                result.isOk shouldBe true
            }
            then("리스트를 반환한다") {
                result.output shouldNotBe null
            }
        }
        `when`("유량을 넘는 호출") {
            var res: Result<Unit>? = null

            measureTime {
                res = runCatching {
                    repeat(100) {
                        InquirePrice(api).call(InquirePrice.InquirePriceData(testStock))
                    }
                }
            }

            then("성공한다") {
                res?.isSuccess shouldBe true
            }
        }
        `when`("InquireSectorIndex 호출") {
            val result =
                InquireSectorIndex(api).call(InquireSectorIndex.InquireSectorIndexData(testSector)).getOrThrow()

            then("성공한다") {
                result.isOk shouldBe true
            }
            then("빈 리스트를 반환하지 않는다") {
                result.output shouldNotBe null
            }
            then("정보를 반환한다") {
                result.output!!.price shouldNotBe null
                result.output!!.change shouldNotBe null
                result.output!!.rate shouldNotBe null
            }
        }
        `when`("InquireSectorIndexSeries 호출") {
            val result = InquireSectorIndexSeries(api).call(
                InquireSectorIndexSeries.InquireSectorIndexSeriesData(
                    testSector,
                    PeriodDivisionCode.Days,
                    endDate = Date(2023, 3, 31)
                )
            ).getOrThrow()
            val resultNext = result.next!!.invoke().getOrThrow()

            then("성공한다") {
                result.isOk shouldBe true
            }
            then("빈 리스트를 반환하지 않는다") {
                result.output2 shouldNotBe null
            }
            then("정보를 반환한다") {
                result.output2!!.all { it.price != null } shouldBe true
                result.output2!!.all { it.change != null } shouldBe true
                result.output2!!.all { it.rate != null } shouldBe true
            }
            then("2023년 3월 31일 이전의 100개 데이터를 반환한다") {
                result.output2!!.count() shouldBe 100

                result.output2!!.shouldForAll { it.bizDate!! shouldBeLessThanOrEqualTo Date(2023, 3, 31) }
            }
            then("역순으로 정렬되어 있다") {
                result.output2!!.sortedBy { it.bizDate!!.YYYYMMDD } shouldBe result.output2!!.reversed()
            }
            then("다음 목록을 가져올 수 있다") {
                resultNext.isOk shouldBe true
                resultNext.output2 shouldNotBe null
            }
            then("이전 목록보다 이전의 100개 데이터를 반환한다") {
                resultNext.output2!!.count() shouldBe 100
                resultNext.output2!!.shouldForAll { it.bizDate!! shouldBeLessThan result.output2!!.last().bizDate!! }
            }
        }
        `when`("InquireStockBaseInfo 호출") {
            val result = InquireStockBaseInfo(api).call(
                InquireStockBaseInfo.InquireStockBaseInfoData(testStock, ProductTypeCode.Stock)
            ).getOrThrow()

            then("성공한다") {
                result.isOk shouldBe true
            }
            then("정보를 반환한다") {
                result.output shouldNotBe null
                result.output!!.name shouldNotBe null
                result.output!!.nameShort shouldNotBe null
                result.output!!.nameEng shouldNotBe null
                result.output!!.nameEngShort shouldNotBe null
                result.output!!.codeStandard shouldNotBe null
                result.output!!.isInKospi200 shouldBe true
            }
        }
    }
})