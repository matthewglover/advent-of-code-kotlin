typealias HexString = String
typealias BinaryString = String
typealias Bit = Boolean
typealias Bits = BooleanArray


fun Bits.toInt(): Int =
    this.reversed().foldIndexed(0) { bitNumber, acc, currentBit ->
        if (currentBit) {
            acc or (1 shl bitNumber)
        } else {
            acc
        }
    }

fun Bits.toLong(): Long =
    this.reversed().foldIndexed(0) { bitNumber, acc, currentBit ->
        if (currentBit) {
            acc or (1L shl bitNumber)
        } else {
            acc
        }
    }

fun Bits.tail(): Bits = sliceArray(1 until size)

fun List<Bits>.concat(): Bits =
    map { bits -> bits.toList() }
        .flatten()
        .toBooleanArray()

class BitStream(private val hexString: HexString) {

    companion object {
        private const val ChunkSize = 4
    }

    private val totalBits: Int = hexString.length * ChunkSize

    private var currentBit: Int = 0

    private var currentBinaryString = binaryStringAt(Math.floorDiv(currentBit, ChunkSize))

    fun nextBits(totalBits: Int): Bits {
        val bits = BooleanArray(totalBits)

        for (pos in 0 until totalBits) {
            if (currentBit > 0 && currentBit % ChunkSize == 0) {
                currentBinaryString = binaryStringAt(Math.floorDiv(currentBit, ChunkSize))
            }

            val bit = currentBinaryString[offSet()].toBit()

            bits[pos] = bit

            currentBit++
        }

        return bits
    }

    fun currentBit(): Int {
        return currentBit
    }

    fun jumpToNext() {
        if (currentBit < totalBits) {
            nextBits(totalBits - currentBit)
        }
    }

    fun hasBits(): Boolean {
        return currentBit < totalBits
    }

    private fun offSet() = currentBit % ChunkSize

    private fun binaryStringAt(position: Int): BinaryString {
        val char = hexString[position]
        val decimalValue = char.toString().toInt(16)
        val simpleBinaryString = decimalValue.toString(2)

        return String.format("%${ChunkSize}s", simpleBinaryString).replace(' ', '0')
    }

    private fun Char.toBit(): Bit {
        return this == '1'
    }
}

sealed interface Packet {
    fun versionSum(): Int
    fun evaluate(): Long
}

sealed class OperatorPacket() : Packet {
    abstract val version: Int
    abstract val value: List<Packet>


    override fun versionSum(): Int {
        return version + value.sumOf { packet -> packet.versionSum() }
    }
}

data class Sum(override val version: Int, override val value: List<Packet>) : OperatorPacket() {

    override fun evaluate(): Long {
        return value.sumOf { packet -> packet.evaluate() }
    }
}

data class Product(override val version: Int, override val value: List<Packet>) : OperatorPacket() {

    override fun evaluate(): Long {
        return if (value.isEmpty()) {
            0
        } else {
            value.fold(1) { acc, b -> acc * b.evaluate() }
        }
    }
}

data class MinimumOf(override val version: Int, override val value: List<Packet>) : OperatorPacket() {

    override fun evaluate(): Long {
        return value.minOf { packet -> packet.evaluate() }
    }
}

data class MaximumOf(override val version: Int, override val value: List<Packet>) : OperatorPacket() {

    override fun evaluate(): Long {
        return value.maxOf { packet -> packet.evaluate() }
    }
}

data class LessThan(override val version: Int, override val value: List<Packet>) : OperatorPacket() {

    override fun evaluate(): Long {
        if (value.size < 2) throw RuntimeException("Less Than Error - insufficient packets")

        return if (value[0].evaluate() < value[1].evaluate()) {
            1
        } else {
            0
        }
    }
}

data class GreaterThan(override val version: Int, override val value: List<Packet>) : OperatorPacket() {

    override fun evaluate(): Long {
        if (value.size < 2) throw RuntimeException("Greater Than Error - insufficient packets")

        return if (value[0].evaluate() > value[1].evaluate()) {
            1
        } else {
            0
        }
    }
}

data class Equals(override val version: Int, override val value: List<Packet>) : OperatorPacket() {

    override fun evaluate(): Long {
        if (value.size < 2) throw RuntimeException("Equals Error - insufficient packets")

        return if (value[0].evaluate() == value[1].evaluate()) {
            1
        } else {
            0
        }
    }
}

data class Literal(val version: Int, val value: Long) : Packet {

    override fun versionSum(): Int {
        return version
    }

    override fun evaluate(): Long {
        return value
    }
}

enum class OperatorType {
    VALUE, SUM, PRODUCT, MINIMUM_OF, MAXIMUM_OF, LESS_THAN, GREATER_THAN, EQUALS;

    companion object {
        fun from(typeId: Int): OperatorType {
            return when (typeId) {
                0 -> SUM
                1 -> PRODUCT
                2 -> MINIMUM_OF
                3 -> MAXIMUM_OF
                4 -> VALUE
                5 -> GREATER_THAN
                6 -> LESS_THAN
                7 -> EQUALS
                else -> throw RuntimeException("Unhandled operator type id: $typeId")
            }
        }
    }
}

object PacketParser {

    fun parse(bitStream: BitStream): Packet {
        val version = bitStream.nextBits(3).toInt()
        val type = OperatorType.from(bitStream.nextBits(3).toInt())

        return when (type) {
            OperatorType.SUM -> Sum(version, toOperatorValue(bitStream))
            OperatorType.PRODUCT -> Product(version, toOperatorValue(bitStream))
            OperatorType.MINIMUM_OF -> MinimumOf(version, toOperatorValue(bitStream))
            OperatorType.MAXIMUM_OF -> MaximumOf(version, toOperatorValue(bitStream))
            OperatorType.LESS_THAN -> LessThan(version, toOperatorValue(bitStream))
            OperatorType.GREATER_THAN -> GreaterThan(version, toOperatorValue(bitStream))
            OperatorType.EQUALS -> Equals(version, toOperatorValue(bitStream))
            OperatorType.VALUE -> Literal(version, toLiteralValue(bitStream))
        }
    }

    private fun toLiteralValue(bitStream: BitStream): Long {
        return bitsList(bitStream).concat().toLong()
    }

    private fun bitsList(bitStream: BitStream): List<Bits> {
        val currentBits = bitStream.nextBits(5)

        return if (currentBits[0]) {
            listOf(currentBits.tail()) + bitsList(bitStream)
        } else {
            listOf(currentBits.tail())
        }
    }

    private fun toOperatorValue(bitStream: BitStream): List<Packet> {
        val lengthType = bitStream.nextBits(1).toInt()

        return when (lengthType) {
            0 -> typeZero(bitStream)
            1 -> typeOne(bitStream)
            else -> throw RuntimeException("Unexpected length type of: $lengthType")
        }
    }

    private fun typeZero(bitStream: BitStream): List<Packet> {
        val subPacketBits = bitStream.nextBits(15).toInt()
        val endBit = bitStream.currentBit() + subPacketBits

        val subPackets = mutableListOf<Packet>()

        while (bitStream.currentBit() < endBit) {
            subPackets.add(parse(bitStream))
        }

        return subPackets
    }

    private fun typeOne(bitStream: BitStream): List<Packet> {
        val totalPackets = bitStream.nextBits(11).toInt()

        val subPackets = mutableListOf<Packet>()

        while (subPackets.size < totalPackets) {
            subPackets.add(parse(bitStream))
        }

        return subPackets
    }
}

fun main() {

    fun part1(hexString: String): Int {
        val bitStream = BitStream(hexString)

        val packet = PacketParser.parse(bitStream)

        return packet.versionSum()
    }

    fun part2(hexString: String): Long {
        val bitStream = BitStream(hexString)

        val packet = PacketParser.parse(bitStream)

        return packet.evaluate()
    }

    // test if implementation meets criteria from the description, like:
    val tests = mapOf(
        "C200B40A82" to 3L,
        "04005AC33890" to 54L,
        "880086C3E88112" to 7L,
        "CE00C43D881120" to 9L,
        "D8005AC2A8F0" to 1L,
        "F600BC2D8F" to  0L,
        "9C005AC2F8F0" to 0L,
        "9C0141080250320F1802104A08" to 1L,
    )
    tests.entries.forEach { (input, result) ->
        check(part2(input) == result)
    }

    val input = readInput("Day16")
    println(part1(input.first()))
    println(part2(input.first()))
}
