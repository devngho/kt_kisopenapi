package io.github.devngho.kisopenapi.requests.util

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.integer.BigInteger
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.number
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object BigDecimalPreciseSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: BigDecimal) {
        encoder.encodeString(value.toStringExpanded())
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        return BigDecimal.parseString(decoder.decodeString().trim())
    }
}

object BigIntegerPreciseSerializer : KSerializer<BigInteger> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BigInteger", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: BigInteger) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): BigInteger {
        return BigInteger.parseString(decoder.decodeString().trim())
    }
}

typealias Date = LocalDate
typealias Time = LocalTime

/**
 * Serializa date to YYYYMMDD format
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

    private fun deserializeDate(value: String): Date = Date(
        value.substring(0, 4).toInt(),
        value.substring(4, 6).toInt(),
        value.substring(6, 8).toInt()
    )


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