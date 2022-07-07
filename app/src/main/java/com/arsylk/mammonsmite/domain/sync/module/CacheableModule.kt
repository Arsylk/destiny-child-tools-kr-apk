package com.arsylk.mammonsmite.domain.sync.module


import com.arsylk.mammonsmite.domain.sync.*
import com.arsylk.mammonsmite.model.common.HashUtils
import kotlinx.coroutines.flow.*
import java.io.File
import kotlin.experimental.ExperimentalTypeInference

class CacheableModule<T: Any>(
    override val tag: String,
    override val required: Boolean,
    override val weight: Float,
    override val order: Int,
    override val condition: suspend ISyncComponentContext.() -> Boolean,
    override val timeout: Long,
    override val modifiers: List<FlowModifier>,
    private val file: File,
    private val serialize: suspend SyncModuleContext.(value: T) -> String,
    private val deserialize: suspend SyncModuleContext.(string: String) -> T,
    private val fetch: suspend SyncModuleContext.(md5: String) -> T,
) : ISyncModule() {

    override fun flow(context: SyncModuleContext) = flow { emit(HashUtils.md5(file)) }
        .map { md5 -> fetch.invoke(context, md5 ?: "") }
        .onEach { value -> file.writeText(serialize.invoke(context, value)) }
        .catch { t ->
            t.printStackTrace()
            emit(deserialize.invoke(context, file.readText()) )
        }

    @DSL
    class Builder<T: Any> internal constructor() : SyncModuleBuilder() {
        lateinit var file: File
        private lateinit var serialize: suspend SyncModuleContext.(value: T) -> String
        private lateinit var deserialize: suspend SyncModuleContext.(string: String) -> T
        private lateinit var fetch: suspend SyncModuleContext.(md5: String) -> T

        fun fetch(block: suspend SyncModuleContext.(md5: String) -> T) = apply { fetch = block }

        fun serialize(block: suspend SyncModuleContext.(T) -> String) = apply { serialize = block }

        fun deserialize(block: suspend SyncModuleContext.(string: String) -> T) = apply { deserialize = block }

        override fun build(): CacheableModule<T> {
            return CacheableModule(tag, required, weight, order, condition, timeout, modifiers, file, serialize, deserialize, fetch)
        }
    }
}

@OptIn(ExperimentalTypeInference::class)
@BuilderInference
fun <T: Any> SyncGroupBuilder.cacheableModule(block: CacheableModule.Builder<T>.() -> Unit) {
    val module = CacheableModule.Builder<T>().apply(block).build()
    addComponent(module)
}
