fun main() {

    fun part1(input: List<String>): Int {
        val allOutputValues = input.map { parseSignalData(it) }.map { it.second }

        return allOutputValues
            .sumOf { signalPatterns ->
                signalPatterns.filter { it.isUniqueBySignalCount() }
                    .groupingBy { it }.eachCount()
                    .values.sum()
            }
    }

    fun part2(input: List<String>): Int {
        val allSignalData = input.map { parseSignalData(it) }

        return allSignalData.sumOf { parseOutput(it) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 26)
    check(part2(testInput) == 61229)

    val input = readInput("Day08")
    println(part1(input))
    println(part2(input))
}

fun parseOutput(data: Pair<List<Set<Char>>, List<Set<Char>>>): Int {
    val (signalPatterns, outputSignalPatterns) = data

    val parser = SevenSegmentParserFactory.createParser(signalPatterns)

    return outputSignalPatterns.map { parser.parse(it) }
        .joinToString("")
        .toInt()
}

object SevenSegmentParserFactory {

    fun createParser(signalPatterns: List<Set<Char>>): SevenSegmentParser {

        val uniquePatternBySignalCount = signalPatterns.filter { it.isUniqueBySignalCount() }
        val one = uniquePatternBySignalCount.first { it.isOne() }
        val four = uniquePatternBySignalCount.first { it.isFour() }
        val seven = uniquePatternBySignalCount.first { it.isSeven() }
        val eight = uniquePatternBySignalCount.first { it.isEight() }

        val sixSignalPatterns = signalPatterns.filter { it.size == 6 }
        val nine = sixSignalPatterns.first { it.containsAll(seven.plus(four)) }
        val six = sixSignalPatterns.first { !it.containsAll(seven) }
        val zero = sixSignalPatterns.minus(setOf(nine, six)).first()

        val fiveSignalPatterns = signalPatterns.filter { it.size == 5 }
        val five = fiveSignalPatterns.first { !it.containsAll(one) && six.containsAll(it) }
        val two = fiveSignalPatterns.first { !it.containsAll(one) && it != five }
        val three = fiveSignalPatterns.first { !listOf(two, five).contains(it) }

        return SevenSegmentParser(
            setOf(
                0 to zero,
                1 to one,
                2 to two,
                3 to three,
                4 to four,
                5 to five,
                6 to six,
                7 to seven,
                8 to eight,
                9 to nine
            )
        )
    }
}

class SevenSegmentParser(private val cipher: Set<Pair<Int, Set<Char>>>) {
    fun parse(input: Set<Char>): Int {
        return cipher.first { input == it.second }.first
    }
}

fun parseSignalData(input: String): Pair<List<Set<Char>>, List<Set<Char>>> {
    val parts = input.split("|")

    val signalPatterns = parts[0].trim().split(" ").map { it.trim().toSet() }
    val outputValues = parts[1].trim().split(" ").map { it.trim().toSet() }

    return Pair(signalPatterns, outputValues)
}

private val uniqueNumberMappings = mapOf(
    1 to 2,
    4 to 4,
    7 to 3,
    8 to 7
)

fun Set<Char>.isUniqueBySignalCount(): Boolean {
    return uniqueNumberMappings.values.contains(size)
}

fun Set<Char>.isOne(): Boolean = uniqueNumberMappings[1] == this.size
fun Set<Char>.isFour(): Boolean = uniqueNumberMappings[4] == this.size
fun Set<Char>.isSeven(): Boolean = uniqueNumberMappings[7] == this.size
fun Set<Char>.isEight(): Boolean = uniqueNumberMappings[8] == this.size
