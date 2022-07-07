package com.arsylk.mammonsmite.domain.sync

abstract class ISyncGroup : SyncComponent {
    open val concurrency: Int = 1
    open val components: List<SyncComponent> = emptyList()

    override fun toString() = "ISyncGroup($tag)"
}

data class SyncGroupException(
    val group: ISyncGroup,
    override val cause: Throwable,
) : Throwable(cause = cause)

class SyncGroup(
    override val tag: String,
    override val concurrency: Int,
    override val required: Boolean,
    override val weight: Float,
    override val order: Int,
    override val condition: suspend ISyncComponentContext.() -> Boolean,
    override val components: List<SyncComponent>,
) : ISyncGroup() {

    @DSL
    class Builder internal constructor() : SyncGroupBuilder() {
        var tag: String = "untagged"
        var concurrency: Int = 1
        var required: Boolean = false
        var weight: Float = 1.0f
        var order: Int = 1
        private var condition: suspend ISyncComponentContext.() -> Boolean = { true }

        fun condition(block: suspend ISyncComponentContext.() -> Boolean) = apply { condition = block }

        override fun build() = SyncGroup(tag, concurrency, required, weight, order, condition, components)
    }
}

class RootSyncGroup(
    override val components: List<SyncComponent>,
) : ISyncGroup() {
    override val tag = "root"
    override val required = true
    override val weight = 1.0f
    override val order = 1
    override val concurrency = 1
    val store: SyncGroupStore = SyncGroupStore()

    @DSL
    class Builder internal constructor() : SyncGroupBuilder() {

        override fun build() = RootSyncGroup(components)
    }
}

abstract class SyncGroupBuilder {
    protected val components = mutableListOf<SyncComponent>()

    fun group(block: SyncGroup.Builder.() -> Unit) {
        val group = SyncGroup.Builder().apply(block).build()
        addComponent(group)
    }

    fun module(block: SyncModule.Builder.() -> Unit) {
        val module = SyncModule.Builder().apply(block).build()
        addComponent(module)
    }

    fun addComponent(component: SyncComponent) { components += component }

    abstract fun build(): ISyncGroup
}