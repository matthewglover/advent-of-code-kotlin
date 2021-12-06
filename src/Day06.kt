fun main() {
    fun part1(input: List<String>): Long {
        val initialFish = input[0].split(",").map { it.toInt() }

        return initialFish.sumOf { AngelFishModel.countFish(Pair(it, 80))}
    }

    fun part2(input: List<String>): Long {
        val initialFish = input[0].split(",").map { it.toInt() }

        return initialFish.sumOf { AngelFishModel.countFish(Pair(it, 256))}
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput) == 5934L)
    check(part2(testInput) == 26984457539)

    val input = readInput("Day06")
    println(part1(input))
    println(part2(input))
}

object AngelFishModel {
    private val ageOnFirstSpawn = 8
    private val ageAfterRespawn = 6
    private val intervalBetweenRespawns = ageAfterRespawn + 1

    private val memo = mutableMapOf<Pair<Int, Int>, Long>()

    fun countFish(fishState: Pair<Int, Int>): Long {
        memo[fishState]?.let { return it }

        val (age, daysRemaining) = fishState
        val dayRemainingAfterFirstSpawn = daysRemaining - (age + 1)

        if (dayRemainingAfterFirstSpawn < 0) {
            return 1L
        }

        val totalAdditionalSpawns = Math.floorDiv(dayRemainingAfterFirstSpawn, intervalBetweenRespawns)

        var totalFish = 1L

        for (spawnCount in 0..totalAdditionalSpawns) {
            val daysRemaining = dayRemainingAfterFirstSpawn - (spawnCount * intervalBetweenRespawns)

            totalFish += countFish(Pair(ageOnFirstSpawn, daysRemaining))
        }

        memo.putIfAbsent(fishState, totalFish)

        return totalFish
    }
}

