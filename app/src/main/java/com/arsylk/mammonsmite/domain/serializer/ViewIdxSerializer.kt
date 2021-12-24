package com.arsylk.mammonsmite.domain.serializer

import com.arsylk.mammonsmite.model.destinychild.ViewIdx
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


object ViewIdxSerializer : KSerializer<ViewIdx> {
    override val descriptor = PrimitiveSerialDescriptor("view_idx", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ViewIdx) {
        encoder.encodeString(value.string)
    }

    override fun deserialize(decoder: Decoder): ViewIdx {
        val string = decoder.decodeString()
        return ViewIdx.parse(string)
            ?: throw SerializationException("Could not deserialize '$string' to ${ViewIdx::class.simpleName}")
    }
}