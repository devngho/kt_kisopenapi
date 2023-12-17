package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KISApiClient


sealed interface StockBase : Updatable {
    val client: KISApiClient
    val ticker: String

    data class Name(
        var name: String? = null,
        var name120: String? = null,
        var nameEnglish: String? = null,
        var nameEng120: String? = null,
        var nameShort: String? = null,
        var nameEnglishShort: String? = null
    )

    var name: Name
}