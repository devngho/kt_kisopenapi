package io.github.devngho.kisopenapi.requests.auth

class TokenRefreshRequiredException(
    message: String = "Authentication token needs to be refreshed",
    cause: Throwable? = null
) : RuntimeException(message, cause)
