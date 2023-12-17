package io.github.devngho.kisopenapi.layer

sealed interface Account : Updatable {
    val accountStocks: MutableList<AccountStock>
}
