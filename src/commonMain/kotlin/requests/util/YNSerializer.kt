package io.github.devngho.kisopenapi.requests.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object YNSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("YN", PrimitiveKind.BOOLEAN)
    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeString(serializeBoolean(value))
    }

    private fun serializeBoolean(value: Boolean): String = if (value) "y" else "n"
    private fun deserializeBoolean(value: String): Boolean = value.lowercase() == "y"


    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeString().lowercase() == "y"
    }

    val Boolean.YN
        get() = serializeBoolean(this)

    val String.YN
        get() = deserializeBoolean(this)
}