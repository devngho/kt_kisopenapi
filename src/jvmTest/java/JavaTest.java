import io.github.devngho.kisopenapi.JavaUtil;
import io.github.devngho.kisopenapi.KisOpenApi;
import io.github.devngho.kisopenapi.layer.StockDomestic;
import io.github.devngho.kisopenapi.requests.InquirePrice;
import io.github.devngho.kisopenapi.requests.response.BaseInfo;
import io.github.devngho.kisopenapi.requests.response.StockPrice;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class JavaTest {
    private final String testStock = "005930";

    @Test
    public void loadStockLowJava() throws InterruptedException, ExecutionException {
        KisOpenApi api = TestKt.getApi();

        var call = JavaUtil.callWithData(new InquirePrice(api), new InquirePrice.InquirePriceData(testStock, null, ""));
        var result = call.get();

        var output = result.getOutput();

        assert output != null;
        assert output.getPrice() != null;
    }

    @Test
    public void loadStockJava() throws InterruptedException, ExecutionException {
        KisOpenApi api = TestKt.getApi();

        StockDomestic stock = new StockDomestic(api, testStock);
        JavaUtil.updateBy(stock, StockPrice.class).get();
        JavaUtil.updateBy(stock, BaseInfo.class).get();

        assert stock.getName().getName() != null;
        assert stock.price.getPrice() != null;
    }
}
