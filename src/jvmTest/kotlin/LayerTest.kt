import com.ionspin.kotlin.bignum.integer.BigInteger
import io.github.devngho.kisopenapi.KisOpenApi
import io.github.devngho.kisopenapi.layer.AccountDomestic
import io.github.devngho.kisopenapi.layer.AccountOverseas
import io.github.devngho.kisopenapi.layer.StockDomestic
import io.github.devngho.kisopenapi.layer.StockOverseas
import io.github.devngho.kisopenapi.requests.response.*
import io.github.devngho.kisopenapi.requests.util.Currency
import io.github.devngho.kisopenapi.requests.util.OrderTypeCode
import io.github.devngho.kisopenapi.requests.util.OverseasMarket
import kotlinx.coroutines.runBlocking
import org.testng.annotations.Test
import java.io.File

class LayerTest {
    val testStock = "005930"
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
    fun loadStockPriceByLayer(){
        runBlocking {
            val api = getApi()

            val stock = StockDomestic(api, testStock)

            stock.updateBy(StockPrice::class)
            stock.updateBy(BaseInfo::class)

            println(stock.price)
            println(stock.name)
            println(stock.tradeVolume)
        }
    }

    @Test
    fun loadStockOverseasPriceByLayer() {
        runBlocking {
            val api = getApi()

            val stock = StockOverseas(api, testOverseasStock, testOverseasMarket)

            stock.updateBy(StockOverseasPrice::class)
            stock.updateBy(BaseInfo::class)

            println(stock.price)
            println(stock.name)
        }
    }

    @Test
    fun buy() {
        runBlocking {
            val api = getApi()

            val stock = StockDomestic(api, testStock)

            stock.updateBy(StockPrice::class)
            stock.updateBy(BaseInfo::class)

            println(stock.buy(BigInteger(1), OrderTypeCode.MarketPrice))
        }
    }

    @Test
    fun buyOverseas() {
        runBlocking {
            val api = getApi()

            val stock = StockOverseas(api, testOverseasStock, testOverseasMarket)

            stock.updateBy(StockOverseasPrice::class)
            stock.updateBy(BaseInfo::class)

            println(stock.buy(BigInteger(1), OrderTypeCode.SelectPrice, stock.price.price!!))
        }
    }

    @Test
    fun sell() {
        runBlocking {
            val api = getApi()

            val stock = StockDomestic(api, testStock)
            println(stock.sell(BigInteger(1), OrderTypeCode.MarketPrice))
        }
    }

    @Test
    fun sellOverseas() {
        runBlocking {
            val api = getApi()

            val stock = StockOverseas(api, testOverseasStock, testOverseasMarket)

            stock.updateBy(StockOverseasPrice::class)
            stock.updateBy(BaseInfo::class)

            println(stock.sell(BigInteger(1), OrderTypeCode.SelectPrice, stock.price.price!!))
        }
    }

    @Test
    fun livePrice() {
        runBlocking {
            val api = getApi()

            val stock = StockDomestic(api, testStock)
            stock.useLiveConfirmPrice {
                println(it.price)
            }
        }
    }

    @Test
    fun livePriceOverseas() {
        runBlocking {
            val api = getApi()

            val stock = StockOverseas(api, testOverseasStock, testOverseasMarket)
            stock.useLiveConfirmPrice {
                println(it.price)
            }
        }
    }

    @Test
    fun account() {
        runBlocking {
            val api = getApi()

            val accountLayer = AccountDomestic(api)

            accountLayer.updateBy(BalanceAccount::class)

            println(accountLayer.assetAmount)
        }
    }

    @Test
    fun accountOverseas() {
        runBlocking {
            val api = getApi()

            val accountLayer = AccountOverseas(api, OverseasMarket.NYS, Currency.USD)

            accountLayer.updateBy(BalanceAccountOverseas::class)

            println(accountLayer.accountStocks.map { "${it.name.nameShort}: ${it.count}주(평단 $${it.buyPriceAverage?.toStringExpanded()})" }.joinToString("\n"))
        }
    }
}