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
    @Test
    fun loadStockPriceByLayer(){
        runBlocking {
            val key = File("key.txt").readLines()
            val token = File("token.txt").readLines()

            val api = KisOpenApi.withToken(
                token[0], key[0], key[1], false
            )

            val stock = Stock(api, "012450")

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
            val key = File("key.txt").readLines()
            val token = File("token.txt").readLines()
            val account = File("account.txt").readLines()

            val api = KisOpenApi.withToken(
                token[0], key[0], key[1], false, account[0]
            )

            val stock = Stock(api, "012450")

            stock.updateBy(StockPrice::class)
            stock.updateBy(BaseInfo::class)

            println(stock.buy(BigInteger(1), OrderTypeCode.MarketPrice))
        }
    }

    @Test
    fun sell() {
        runBlocking {
            val key = File("key.txt").readLines()
            val token = File("token.txt").readLines()
            val account = File("account.txt").readLines()

            val api = KisOpenApi.withToken(
                token[0], key[0], key[1], false, account[0]
            )

            val stock = Stock(api, "012450")
            println(stock.sell(BigInteger(1), OrderTypeCode.MarketPrice))
        }
    }

    @Test
    fun account() {
        runBlocking {
            val key = File("key.txt").readLines()
            val token = File("token.txt").readLines()
            val account = File("account.txt").readLines()

            val api = KisOpenApi.withToken(
                token[0], key[0], key[1], false, account[0]
            )

            val accountLayer = Account(api)

            accountLayer.updateBy(BalanceAccount::class)

            println(accountLayer.accountStocks.toString().replace(", ", ", \n"))
        }
    }
}