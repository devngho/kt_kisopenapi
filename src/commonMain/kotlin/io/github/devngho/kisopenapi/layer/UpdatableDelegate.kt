package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.requests.util.InternalApi
import kotlin.reflect.KClass

@InternalApi
class UpdatableDelegation<T : Any>(
    val clazz: KClass<T>,
    /**
     * @return Pair<newValue or null, shouldUpdate(true to update, false to skip)>
     */
    val maybeUpdate: (Any?) -> Pair<T?, Boolean> = { null to false }
) {
    private var value: T? = null

    @Suppress("unused")
    operator fun getValue(thisRef: Any?, property: Any?): T? = value

    @Suppress("unused")
    operator fun setValue(thisRef: Any?, property: Any?, value: T?) {
        this.value = value
    }

    internal fun safeUpdate(value: Any?) {
        if (clazz.isInstance(value)) @Suppress("UNCHECKED_CAST") {
            this.setValue(null, null, value as T?)
        }
    }
}

@InternalApi
class UpdatableManager() {
    private val delegates = mutableListOf<UpdatableDelegation<*>>()

    fun register(delegate: UpdatableDelegation<*>) {
        delegates.add(delegate)
    }

    fun broadcastUpdate(value: Any?) {
        delegates.forEach {
            it.maybeUpdate(value).takeIf { it.second == true }?.let { (newValue, _) ->
                it.safeUpdate(newValue)
            }
        }
    }

    inline fun <reified T : Any> tracked(noinline maybeUpdate: (Any?) -> Pair<T?, Boolean> = { null to false }) =
        UpdatableDelegation<T>(T::class, maybeUpdate).also { register(it) }
}