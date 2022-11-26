import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.requests.util.LockCode
import io.github.devngho.kisopenapi.requests.util.PeriodDivisionCode
import io.github.devngho.kisopenapi.requests.util.SignYesterday
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import com.ionspin.kotlin.bignum.serialization.kotlinx.humanReadableSerializerModule
import io.github.devngho.kisopenapi.requests.*
import io.github.devngho.kisopenapi.requests.util.InquireDivisionCode
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
                token[0], key[0], key[1], false
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

            println(InquirePrice(api).call(InquirePrice.InquirePriceData("012450")).toString().replace(", ", ", \n"))
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

            @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        serializersModule = humanReadableSerializerModule
        isLenient = true
        explicitNulls = false
    }

    @Test
    fun testStockDays(){
        runBlocking {
            println(json.encodeToString(
                    InquirePricePerDay.InquirePricePerDayResponse(
                        "a", "b", "c", "d", "e",
                        listOf(
                            InquirePricePerDay.InquirePricePerDayResponseOutput(
                                "a", BigInteger(2), BigInteger(3), BigInteger(4), BigInteger(5), BigInteger(6),
                                SignYesterday.Complement, BigDecimal.fromInt(1), LockCode.AllocationLock, BigDecimal.fromInt(2), BigInteger(2), BigDecimal.fromInt(3), BigInteger(4), BigDecimal.fromInt(5)
                            )), null)
                )
            )
        }
    }
}