fun main() {
    fun part1(input: List<String>): Int {
        val positions = input[0].split(",").map { it.toInt() }

        return minMax(positions).minOf { fuelUsage(positions, it, ::identity) }
    }

    fun part2(input: List<String>): Int {
        val positions = input[0].split(",").map { it.toInt() }

        return minMax(positions).minOf { fuelUsage(positions, it, ::calculateCrabFuelCost) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 37)
    check(part2(testInput) == 168)

    val input = readInput("Day07")
    println(part1(input))
    println(part2(input))
}

fun minMax(positions: List<Int>): IntRange = (positions.minOrNull()!!..positions.maxOrNull()!!)

fun fuelUsage(startPositions: List<Int>, endPosition: Int, calculateFuelCost: (Int) -> Int): Int {
    return startPositions.sumOf {
        calculateFuelCost(if (it > endPosition) it - endPosition else endPosition - it)
    }
}

fun <T> identity(a: T): T = a

fun calculateCrabFuelCost(fuelUsage: Int): Int = (0..fuelUsage).sum()

