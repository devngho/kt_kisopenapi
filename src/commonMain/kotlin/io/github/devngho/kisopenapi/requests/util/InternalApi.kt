package io.github.devngho.kisopenapi.requests.util

@RequiresOptIn(
    message = "이 API는 내부적으로 사용할 목적의 API입니다. 직접 사용하지 않는 것을 권장합니다."
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
annotation class InternalApi
