package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.requests.Response
import kotlin.reflect.KClass

/**
 * 업데이트 가능한 객체입니다.
 * @see StockDomesticImpl
 */
interface Updatable {
    /**
     * 주어진 [res]에 따라 적절한 요청을 호출해 값을 업데이트합니다.
     */
    suspend fun update(res: KClass<out Response>)

    /**
     * 주어진 [res]로 값을 업데이트합니다.
     */
    fun updateBy(res: Response)

    companion object {
        /**
         * 주어진 [T] 타입에 따라 적절한 요청을 호출해 값을 업데이트합니다.
         */
        suspend inline fun <reified T : Response> Updatable.update() = update(T::class)
    }
}