fun main() {
    fun part1(input: List<String>): Int {
        val bitCounts = bitCountsFor(input)

        val gamma = bitCounts.map(::selectMostSignificantBit).toInt()
        val epsilon = bitCounts.map(::selectLeastSignificantBit).toInt()

        return gamma * epsilon
    }

    fun part2(input: List<String>): Int {
        val oxygenGeneratorRating = findMatchingInput(input, ::selectMostSignificantBit)
        val co2ScrubberRating = findMatchingInput(input, ::selectLeastSignificantBit)

        return oxygenGeneratorRating * co2ScrubberRating
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 198)
    check(part2(testInput) == 230)

    val input = readInput("Day03")
    println(part1(input))
    println(part2(input))
}

fun Pair<Int, Int>.incrementLeft(): Pair<Int, Int> {
    val (left, right) = this

    return Pair(left + 1, right)
}

fun Pair<Int, Int>.incrementRight(): Pair<Int, Int> {
    val (left, right) = this

    return Pair(left, right + 1)
}

fun List<Char>.toInt(): Int = joinToString("").toInt(2)

fun bitCountsFor(diagnosticValues: List<String>): List<Pair<Int, Int>> {
    val bitCounts = (0 until diagnosticValues[0].length).mapTo(mutableListOf()) { Pair(0, 0) }

    diagnosticValues.forEach {
        it.toCharArray().forEachIndexed { index, char ->
            when (char) {
                '0' -> {
                    bitCounts[index] = bitCounts[index].incrementLeft()
                }
                '1' -> {
                    bitCounts[index] = bitCounts[index].incrementRight()
                }
                else -> {
                    throw RuntimeException("Char identification error, expecting '0' or '1' but received $char")
                }
            }
        }
    }

    return bitCounts
}

fun bitCountForBit(input: List<String>, bit: Int): Pair<Int, Int> {
    return input.fold(Pair(0, 0)) { (zeroCount, oneCount), diagnosticValue ->
        if (diagnosticValue[bit] == '0') {
            Pair(zeroCount + 1, oneCount)
        } else {
            Pair(zeroCount, oneCount + 1)
        }
    }
}

fun findMatchingInput(input: List<String>, bitSelector: (bitCount: Pair<Int, Int>) -> Char): Int {
    val max = input[0].length
    var currentBit = 0
    var currentList = input

    while (currentList.size > 1 && currentBit < max) {
        val bitCount = bitCountForBit(currentList, currentBit)
        val bitToMatch = bitSelector(bitCount)

        currentList = currentList.filter { it[currentBit] == bitToMatch }

        currentBit += 1
    }

    return currentList[0].toCharArray().toList().toInt()
}

fun selectMostSignificantBit(bitCount: Pair<Int, Int>): Char {
    val (zeroCount, oneCount) = bitCount

    return if (zeroCount > oneCount) { '0' } else { '1' }
}

fun selectLeastSignificantBit(bitCount: Pair<Int, Int>): Char {
    val (zeroCount, oneCount) = bitCount

    return if (zeroCount > oneCount) { '1' } else { '0' }
}

