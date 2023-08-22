import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.*
import io.github.devngho.kisopenapi.requests.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.Test

class Tests {
    private val testStock = "005930"
    private val testOverseasStock = "KO"
    private val testOverseasMarket = OverseasMarket.NEWYORK

    private fun getApi(): KisOpenApi {
        val key = File("key.txt").readLines()
        val token = File("token.txt").readLines()
        val account = File("account.txt").readLines()

        return KisOpenApi.withToken(
            token[0], key[0], key[1], false, account = account[0]
        )
    }

    @Test
    fun grantToken(){
        runBlocking {
            val api = getApi()
            println(GrantToken(api).call().accessToken)
        }
    }

    @Test
    fun revokeToken(){
        runBlocking {
            val api = getApi()

            println(RevokeToken(api).call(RevokeToken.RevokeTokenData(api.oauthToken)))
        }
    }

    @Test
    fun loadStock(){
        runBlocking {
            val api = getApi()

            println(InquirePrice(api).call(InquirePrice.InquirePriceData(testStock)).toString().replace(", " , ", \n"))
        }
    }

    @Test
    fun loadOverseasStock(){
        runBlocking {
            val api = getApi()

            println(InquireOverseasPrice(api).call(InquireOverseasPrice.InquirePriceData(testOverseasStock, testOverseasMarket)).toString().replace(", " , ", \n"))
        }
    }

    @Test
    fun loadStockConfirm(){
        runBlocking {
            val api = getApi()

            val res = InquireConfirm(api).call(InquireConfirm.InquireConfirmData(testStock))

            println(res.toString().replace(", ", ", \n"))

            println(res.next)
        }
    }

    @Test
    fun loadStockDays(){
        runBlocking {
            val api = getApi()

            val res = InquirePricePerDay(api).call(InquirePricePerDay.InquirePricePerDayData(testStock, PeriodDivisionCode.Days30))

            println(res.toString().replace(", ", ", \n"))

            println(res.next)
        }
    }
    @Test
    fun loadStockMinutes(){
        runBlocking {
            val api = getApi()

            val res = InquirePriceTodayMinute(api).call(InquirePriceTodayMinute.InquirePriceTodayMinuteData(testStock, "083000", true)).output2!!.reversed()

            println(res.toString().replace(", ", ", \n"))
        }
    }


    @Test
    fun loadBalance() {
        runBlocking {
            val api = getApi()

            val res = InquireBalance(api).call(
                InquireBalance.InquireBalanceData(false, InquireDivisionCode.ByStock,
                    includeFund = false,
                    includeYesterdaySell = false
                )
            )

            println(res.toString().replace(", ", ", \n"))


            println(res.next)
        }
    }

    @Test
    fun loadOverseasBalance() {
        runBlocking {
            val api = getApi()

            val res = InquireOverseasBalance(api).call(
                InquireOverseasBalance.InquireBalanceData(OverseasMarket.NYS, Currency.USD)
            )

            println(res.toString().replace(", ", ", \n"))


            println(res.next)
        }
    }

    @Test
    fun loadHoliday() {
        runBlocking {
            val api = getApi()

            val res = InquireHoliday(api).call(InquireHoliday.InquireHolidayData("20230101"))

            println(res.toString().replace(", ", ", \n"))

            println(res.next!!())
        }
    }

    @Test
    fun loadLivePrice() {
        runBlocking {
            val key = File("key.txt").readLines()

            val api = KisOpenApi.with(
                key[0], key[1], false, grantWebsocket = true
            )

            InquireLivePrice(api).register(InquireLivePrice.InquireLivePriceData(testStock), {
                println(it.toString().replace(", ", ", \n"))
            }) {
                println(it.toString().replace(", ", ", \n"))
            }

            while (true) { delay(1000) }
        }
    }

    @Test
    fun loadOverseasLivePrice() {
        runBlocking {
            val key = File("key.txt").readLines()

            val api = KisOpenApi.with(
                key[0], key[1], false, grantWebsocket = true
            )

            InquireOverseasLivePrice(api).register(InquireOverseasLivePrice.InquireLivePriceData(testOverseasStock, testOverseasMarket), {
                println(it.toString().replace(", ", ", \n"))
            }) {
                println("${it.price?.doubleValue(false)} ${it.confirmVolume}")
            }

            while (true) { delay(1000) }
        }
    }

    @Test
    fun loadLiveConfirm() {
        runBlocking {
            val key = File("key.txt").readLines()
            val acc = File("account.txt").readLines()

            val api = KisOpenApi.with(
                key[0], key[1], false, grantWebsocket = true, id = acc[1]
            )

            InquireLiveConfirm(api).register(InquireLiveConfirm.InquireLiveConfirmData(), {
                println(it.toString().replace(", ", ", \n"))
            }) {
                println(it.toString().replace(", ", ", \n"))
            }

            while(true) { delay(1000) }
        }
    }

    @Test
    fun overseasCondition() {
        runBlocking {
            val api = getApi()

            InquireOverseasCondition(api).call(
                InquireOverseasCondition.ConditionData(
                    exchange = OverseasMarket.NEWYORK,
                    priceRange = BigDecimalRange(100.0, 101.0),
                )
            ).toString().replace(", ", ", \n").let { println(it) }
        }
    }
}