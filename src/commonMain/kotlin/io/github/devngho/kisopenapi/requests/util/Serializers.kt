package io.github.devngho.kisopenapi.requests.util

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BigDecimalPreciseSerializer : KSerializer<BigDecimal?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal?", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigDecimal?) {
        if (value != null) {
            encoder.encodeString(value.toString())
        } else {
            encoder.encodeNull()
        }
    }

    override fun deserialize(decoder: Decoder): BigDecimal? = runCatching {
        BigDecimal.parseString(decoder.decodeString().trim())
    }.getOrNull()
}

object BigIntegerPreciseSerializer : KSerializer<BigInteger?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigInteger?", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigInteger?) {
        if (value != null) {
            encoder.encodeString(value.toString())
        } else {
            encoder.encodeNull()
        }
    }

    override fun deserialize(decoder: Decoder): BigInteger? {
        return BigDecimal.parseString(decoder.decodeString().trim()).toBigInteger()
    }
}

object BigIntegerFromDecimalSerializer : KSerializer<BigInteger?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigInteger?", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: BigInteger?) {
        if (value != null) {
            encoder.encodeString(value.toString())
        } else {
            encoder.encodeNull()
        }
    }

    override fun deserialize(decoder: Decoder): BigInteger? = runCatching {
        BigDecimal.parseString(decoder.decodeString().trim()).toBigInteger()
    }.getOrNull()
}

typealias Date = LocalDate
typealias Time = LocalTime

/**
 * Serialize date to YYYYMMDD format
 * like 20210101
 */
object YYYYMMDDSerializer : KSerializer<Date> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(serializeDate(value))
    }

    private fun serializeDate(value: Date): String =
        "${value.year.toString().padStart(4, '0')}${value.month.number.toString().padStart(2, '0')}${
            value.dayOfMonth.toString().padStart(2, '0')
        }"

    private fun deserializeDate(value: String): Date =
        value.trim()
            .takeIf { it.length == 8 }
            ?.let {
                Date(
                    it.substring(0, 4).toInt(),
                    it.substring(4, 6).toInt(),
                    it.substring(6, 8).toInt()
                )
            } ?: Date(0, 1, 1)


    override fun deserialize(decoder: Decoder): Date {
        return deserializeDate(decoder.decodeString())
    }

    val Date.YYYYMMDD
        get() = serializeDate(this)

    val String.YYYYMMDD
        get() = deserializeDate(this)
}

object HHMMSSSerializer : KSerializer<Time> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Time", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Time) {
        encoder.encodeString(serializeTime(value))
    }

    private fun serializeTime(value: Time): String =
        "${value.hour.toString().padStart(2, '0')}${value.minute.toString().padStart(2, '0')}${
            value.second.toString().padStart(2, '0')
        }"

    private fun deserializeTime(value: String): Time = Time(
        value.substring(0, 2).toInt(),
        value.substring(2, 4).toInt(),
        value.substring(4, 6).toInt()
    )


    override fun deserialize(decoder: Decoder): Time {
        return deserializeTime(decoder.decodeString())
    }

    @Suppress("unused")
    val Time.HHMMSS
        get() = serializeTime(this)

    val String.HHMMSS
        get() = deserializeTime(this)
}

object ResultCodeSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("rt_cd", PrimitiveKind.BOOLEAN)
    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeString(if (value) "0" else "1")
    }

    override fun deserialize(decoder: Decoder): Boolean {
        return decoder.decodeString() == "0"
    }
}

object YNSerializer : KSerializer<Boolean> {
    private fun serializeBoolean(value: Boolean): String = if (value) "y" else "n"
    private fun deserializeBoolean(value: String): Boolean = value.lowercase() == "y"


    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("YN", PrimitiveKind.BOOLEAN)
    override fun serialize(encoder: Encoder, value: Boolean) = encoder.encodeString(serializeBoolean(value))

    override fun deserialize(decoder: Decoder): Boolean = deserializeBoolean(decoder.decodeString())

    val Boolean.YN
        get() = serializeBoolean(this)

    val String.YN
        get() = deserializeBoolean(this)
}