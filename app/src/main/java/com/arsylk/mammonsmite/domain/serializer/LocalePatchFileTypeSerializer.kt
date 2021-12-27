package com.arsylk.mammonsmite.domain.serializer

import com.arsylk.mammonsmite.model.destinychild.LocalePatchFile
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocalePatchFileTypeSerializer: KSerializer<LocalePatchFile.Type> {
    override val descriptor = PrimitiveSerialDescriptor("locale patch file type", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: LocalePatchFile.Type) {
        encoder.encodeInt(value.serialInt)
    }

    override fun deserialize(decoder: Decoder): LocalePatchFile.Type {
        val int = decoder.runCatching { decodeInt() }.getOrNull()
        return LocalePatchFile.Type.values()
            .firstOrNull { it.serialInt == int }
            ?: LocalePatchFile.Type.UNKNOWN
    }
}