import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.layer.Account
import io.github.devngho.kisopenapi.layer.Stock
import io.github.devngho.kisopenapi.requests.response.BalanceAccount
import io.github.devngho.kisopenapi.requests.response.BaseInfo
import io.github.devngho.kisopenapi.requests.response.StockPrice
import io.github.devngho.kisopenapi.requests.util.OrderTypeCode
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test
import java.io.File

class LayerTest {
    val testStock = "005930"

    private fun getApi(): KisOpenApi {
        val key = File("key.txt").readLines()
        val token = File("token.txt").readLines()
        val account = File("account.txt").readLines()

        return KisOpenApi.withToken(
            token[0], key[0], key[1], false, account[0]
        )
    }

    @Test
    fun loadStockPriceByLayer(){
        runBlocking {
            val api = getApi()

            val stock = Stock(api, testStock)

            stock.updateBy(StockPrice::class)
            stock.updateBy(BaseInfo::class)

            println(stock.price)
            println(stock.name)
            println(stock.tradeVolume)
        }
    }

    @Test
    fun buy() {
        runBlocking {
            val api = getApi()

            val stock = Stock(api, testStock)

            stock.updateBy(StockPrice::class)
            stock.updateBy(BaseInfo::class)

            println(stock.buy(BigInteger(1), OrderTypeCode.MarketPrice))
        }
    }

    @Test
    fun sell() {
        runBlocking {
            val api = getApi()

            val stock = Stock(api, testStock)
            println(stock.sell(BigInteger(1), OrderTypeCode.MarketPrice))
        }
    }

    @Test
    fun livePrice() {
        runBlocking {
            val api = getApi()

            val stock = Stock(api, testStock)
            stock.useLiveConfirmPrice {
                println(it.price)
            }
        }
    }

    @Test
    fun account() {
        runBlocking {
            val api = getApi()

            val accountLayer = Account(api)

            accountLayer.updateBy(BalanceAccount::class)

            println(accountLayer.accountStocks.toString().replace(", ", ", \n"))
        }
    }
}