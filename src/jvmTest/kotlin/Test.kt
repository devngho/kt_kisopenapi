import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.*
import io.github.devngho.kisopenapi.requests.util.InquireDivisionCode
import io.github.devngho.kisopenapi.requests.util.PeriodDivisionCode
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test
import java.io.File

class Tests {
    @Test
    fun grantToken(){
        runBlocking {
            val l = File("key.txt").readLines()
            val api = KisOpenApi.with(
                l[0], l[1], false
            )
            println(api.oauthToken)
        }
    }

    @Test
    fun revokeToken(){
        runBlocking {
            val key = File("key.txt").readLines()
            val token = File("token.txt").readLines()

            val api = KisOpenApi.withToken(
                token[0], key[0], key[1]
            )

            println(RevokeToken(api).call(RevokeToken.RevokeTokenData(token[0])))
        }
    }

    @Test
    fun loadStock(){
        runBlocking {
            val key = File("key.txt").readLines()
            val token = File("token.txt").readLines()

            val api = KisOpenApi.withToken(
                token[0], key[0], key[1], false
            )

            println(InquirePrice(api).call(InquirePrice.InquirePriceData("419530")).toString().replace(", ", ", \n"))
        }
    }

    @Test
    fun loadStockConfirm(){
        runBlocking {
            val key = File("key.txt").readLines()
            val token = File("token.txt").readLines()

            val api = KisOpenApi.withToken(
                token[0], key[0], key[1], false
            )

            val res = InquireConfirm(api).call(InquireConfirm.InquireConfirmData("012450"))

            println(res.toString().replace(", ", ", \n"))

            println(res.next)
        }
    }

    @Test
    fun loadStockDays(){
        runBlocking {
            val key = File("key.txt").readLines()
            val token = File("token.txt").readLines()

            val api = KisOpenApi.withToken(
                token[0], key[0], key[1], false
            )

            val res = InquirePricePerDay(api).call(InquirePricePerDay.InquirePricePerDayData("012450", PeriodDivisionCode.Days30))

            println(res.toString().replace(", ", ", \n"))

            println(res.next)
        }
    }
    @Test
    fun loadStockMinutes(){
        runBlocking {
            val key = File("key.txt").readLines()
            val token = File("token.txt").readLines()

            val api = KisOpenApi.withToken(
                token[0], key[0], key[1], false
            )

            val res = InquirePriceTodayMinute(api).call(InquirePriceTodayMinute.InquirePriceTodayMinuteData("012450", "150000", true)).output2!!.reversed()

            println(res.toString().replace(", ", ", \n"))
        }
    }


    @Test
    fun loadBalance() {
        runBlocking {
            val key = File("key.txt").readLines()
            val token = File("token.txt").readLines()
            val account = File("account.txt").readLines()

            val api = KisOpenApi.withToken(
                token[0], key[0], key[1], false, account[0]
            )

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
}