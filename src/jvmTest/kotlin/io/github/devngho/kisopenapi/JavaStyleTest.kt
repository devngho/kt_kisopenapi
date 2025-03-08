package io.github.devngho.kisopenapi

import io.github.devngho.kisopenapi.layer.StockDomestic
import io.github.devngho.kisopenapi.requests.NoDataRequest
import io.github.devngho.kisopenapi.requests.Response
import io.github.devngho.kisopenapi.requests.data.Msg
import io.github.devngho.kisopenapi.requests.domestic.inquire.InquirePrice
import io.github.devngho.kisopenapi.requests.response.stock.ProductInfo
import io.github.devngho.kisopenapi.requests.response.stock.price.domestic.StockPrice
import io.github.devngho.kisopenapi.requests.util.Result
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.mockk

@Suppress("SpellCheckingInspection")
class JavaStyleTest : BehaviorSpec({
    given("API 토큰, 종목 코드") {
        `when`("InquirePrice 호출(자바 형식)") {
            val instance = InquirePrice(api)
            val result = JavaUtil.callWithData(instance, InquirePrice.InquirePriceData(testStock)).join().getOrThrow()

            then("성공한다") {
                result.isOk shouldBe true
                result.msg shouldNotBe null
                result.code shouldNotBe null
            }
            then("정보를 반환한다") {
                result.output!!.price shouldNotBe null
                result.output!!.accumulateTradeVolume shouldNotBe null
            }
        }

        `when`("StockDomestic 업데이트(자바 형식)") {
            val stock = StockDomestic.create(api, testStock)
            JavaUtil.updateBy(stock, StockPrice::class.java)
            JavaUtil.updateBy(stock, ProductInfo::class.java)

            then("종목 이름을 가져올 수 있다") {
                stock.info.nameShort shouldBe "삼성전자"
            }
            then("종목 가격을 가져올 수 있다") {
                stock.price.price shouldNotBe null
            }
        }

        `when`("NoDataRequest 호출(자바 형식)") {
            data class FakeResponse(
                override val errorDescription: String?,
                override val errorCode: String?,
                override val code: String?,
                override val msg: String?,
                override val isOk: Boolean?
            ) : Response, Msg

            val instance = mockk<NoDataRequest<FakeResponse>>()
            coEvery { instance.call() } returns Result(
                FakeResponse(
                    errorDescription = null,
                    errorCode = null,
                    code = "0000",
                    msg = "정상처리되었습니다.",
                    isOk = true
                )
            )

            val result = JavaUtil.callWithoutData(instance).join().getOrThrow()

            then("성공한다") {
                result.isOk shouldBe true
                result.msg shouldNotBe null
                result.code shouldNotBe null
            }
        }
    }
})