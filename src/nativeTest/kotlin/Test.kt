import com.github.devngho.kisopenapi.KisOpenApi
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class Test {
    @Test
    fun grantToken(){
        runBlocking {
            println(
                KisOpenApi.with(
                    readln(), readln(),
                    false
                )
            )
        }
    }
}