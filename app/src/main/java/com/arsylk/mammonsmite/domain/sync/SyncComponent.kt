package com.arsylk.mammonsmite.domain.sync

sealed interface SyncComponent {
    val tag: String
    val required: Boolean
    val weight: Float
    val order: Int
    val condition: suspend ISyncComponentContext.() -> Boolean get () = { true }
}

data class SyncComponentConditionException(
    val component: SyncComponent,
    override val cause: Throwable
) : Throwable(cause = cause)

@DslMarker
annotation class DSL