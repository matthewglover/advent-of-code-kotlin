fun main() {
    fun part1(input: List<String>): Int {
        val totalReadings = input.size

        var increasingDepthCount = 0

        for (index in 0 until totalReadings) {
            if (index == 0) {
                continue
            }

            val previousDepth = input[index - 1].toInt()
            val currentDepth = input[index].toInt()

            if (currentDepth > previousDepth) {
                increasingDepthCount += 1
            }
        }

        return increasingDepthCount
    }

    fun part2(input: List<String>): Int {
        val interval = 3
        val totalReadings = input.size

        var increasingDepthCount = 0

        for (index in 0..totalReadings) {
            if (index < interval || index >= totalReadings) {
                continue
            }

            val previousDepth = input.subList(index - interval, index).sumOf { it.toInt() }
            val currentDepth = input.subList(index - interval + 1, index + 1).sumOf { it.toInt() }

            if (currentDepth > previousDepth) {
                increasingDepthCount += 1
            }
        }

        return increasingDepthCount
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part2(testInput) == 5)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))
}
