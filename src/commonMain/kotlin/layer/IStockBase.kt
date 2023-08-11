package io.github.devngho.kisopenapi.layer

import io.github.devngho.kisopenapi.KisOpenApi


interface IStockBase : Updatable{
    val client: KisOpenApi
    val code: String

    data class Name(
        var name: String? = null,
        var name120: String? = null,
        var nameEng: String? = null,
        var nameEng120: String? = null,
        var nameShort: String? = null,
        var nameEngShort: String? = null
    )

    var name: Name
}