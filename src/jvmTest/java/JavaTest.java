import io.github.devngho.kisopenapi.JavaUtil;
import io.github.devngho.kisopenapi.KisOpenApi;
import io.github.devngho.kisopenapi.layer.StockDomestic;
import io.github.devngho.kisopenapi.requests.InquirePrice;
import io.github.devngho.kisopenapi.requests.response.BaseInfo;
import io.github.devngho.kisopenapi.requests.response.CorporationRequest;
import io.github.devngho.kisopenapi.requests.response.StockPrice;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class JavaTest {
    private final String testStock = "005930";

    private KisOpenApi getApi() {
        try {
            String[] key = Files.readString(new File("key.txt").toPath()).split("\n");
            String token = Files.readString(new File("token.txt").toPath()).trim();
            String account = Files.readString(new File("account.txt").toPath()).trim();

            return KisOpenApi.withToken(
                    token, key[0].trim(), key[1].trim(), false, null, account, null, new CorporationRequest(), false);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Test
    public void loadStockLowJava() throws ExecutionException, InterruptedException {
        KisOpenApi api = getApi();
        assert api != null;

        InquirePrice.InquirePriceResponse result = JavaUtil.callWithData(new InquirePrice(api), new InquirePrice.InquirePriceData(testStock, null, "")).get();
        System.out.println(String.valueOf(Objects.requireNonNull(Objects.requireNonNull(result.getOutput()).getPrice())));
    }

    @Test
    public void loadStockJava() throws ExecutionException, InterruptedException {
        KisOpenApi api = getApi();
        assert api != null;


        StockDomestic stock = new StockDomestic(api, testStock);
        JavaUtil.updateByClass(stock, StockPrice.class).get();
        JavaUtil.updateByClass(stock, BaseInfo.class).get();
        System.out.println(Objects.requireNonNull(stock.getName().getName()));
        System.out.println(String.valueOf(Objects.requireNonNull(stock.price.getPrice())));
    }
}
