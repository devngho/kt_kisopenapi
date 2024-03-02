package io.github.devngho.kisopenapi.requests.util

@RequiresOptIn(
    message = "이 API는 모의 투자 환경에서 지원되지 않습니다.",
    level = RequiresOptIn.Level.WARNING
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION
)
annotation class DemoNotSupported
