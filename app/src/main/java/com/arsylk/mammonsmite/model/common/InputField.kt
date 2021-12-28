package com.arsylk.mammonsmite.model.common

import kotlin.reflect.KProperty

data class InputField<T>(
    val value: T,
    val validator: (value: T) -> Boolean = { true },
    val provideErrorText: (value: T) -> String? = { null },
) {
    val isError: Boolean = !validator.invoke(value)
    val errorText: String? = provideErrorText.invoke(value)

    operator fun getValue(thisRef: Any?, k: KProperty<*>) = value

    operator fun invoke(newValue: T) = copy(value = newValue)
}