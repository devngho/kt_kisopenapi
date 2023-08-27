import io.github.devngho.kisopenapi.JavaUtil;
import io.github.devngho.kisopenapi.KisOpenApi;
import io.github.devngho.kisopenapi.layer.StockDomestic;
import io.github.devngho.kisopenapi.requests.InquirePrice;
import io.github.devngho.kisopenapi.requests.response.BaseInfo;
import io.github.devngho.kisopenapi.requests.response.CorporationRequest;
import io.github.devngho.kisopenapi.requests.response.StockPrice;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

import java.io.File;
import java.util.Objects;

public class JavaTest {
    private final String testStock = "005930";

    private KisOpenApi getApi() {
        try {
            String[] key = Files.readFile(new File("key.txt")).split("\n");
            String token = Files.readFile(new File("token.txt")).trim();
            String account = Files.readFile(new File("account.txt")).trim();

            return KisOpenApi.withToken(
                    token, key[0], key[1], false, null, account, null, new CorporationRequest(), false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void loadStockLowJava() {
        KisOpenApi api = getApi();
        assert api != null;

        try {
            InquirePrice.InquirePriceResponse result = JavaUtil.callWithData(new InquirePrice(api), new InquirePrice.InquirePriceData(testStock, null, "")).get();
            System.out.println(Objects.requireNonNull(Objects.requireNonNull(result.getOutput()).getPrice()).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void loadStockJava() {
        KisOpenApi api = getApi();
        assert api != null;

        try {
            StockDomestic stock = new StockDomestic(api, testStock);
            JavaUtil.updateByClass(stock, StockPrice.class).get();
            JavaUtil.updateByClass(stock, BaseInfo.class).get();
            System.out.println(Objects.requireNonNull(stock.getName().getName()));
            System.out.println(Objects.requireNonNull(stock.price.getPrice()).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
