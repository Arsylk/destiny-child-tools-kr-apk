package com.manimani.app.domain.sync.module

import com.manimani.app.domain.sync.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat

data class PaginatedSyncModule(
    override val tag: String,
    override val required: Boolean,
    override val weight: Float,
    override val order: Int,
    override val condition: suspend ISyncComponentContext.() -> Boolean,
    override val modifiers: List<FlowModifier>,
    val action: suspend SyncModuleContext.(page: Int) -> Boolean,
    val pageModifiers: List<FlowModifier>,
) : ISyncModule() {

    override fun flow(context: SyncModuleContext): Flow<Any> {
        fun buildPageFlow(page: Int): Flow<Any> {
            var builtFlow = kotlinx.coroutines.flow.flow { emit(page) }
                .flatMapConcat {
                    if (action.invoke(context, page)) buildPageFlow(page + 1)
                    else emptyFlow()
                }
            modifiers.forEach {
                builtFlow = builtFlow.let(it)
            }
            return builtFlow
        }
        return buildPageFlow(1)
    }

    @DSL
    class Builder internal constructor() : SyncModuleBuilder() {
        private var action: suspend SyncModuleContext.(page: Int) -> Boolean = { false }
        private val pageModifiers = mutableListOf<FlowModifier>()

        fun pageAction(block: suspend SyncModuleContext.(page: Int) -> Boolean) =
            apply { action = block }

        internal fun pageModify(block: FlowModifier) = apply { pageModifiers += block }

        override fun build(): PaginatedSyncModule {
            return PaginatedSyncModule(tag, required, weight, order, condition, modifiers, action, pageModifiers)
        }
    }
}

fun SyncGroupBuilder.paginatedModule(block: PaginatedSyncModule.Builder.() -> Unit) {
    val module = PaginatedSyncModule.Builder().apply(block).build()
    addComponent(module)
}
