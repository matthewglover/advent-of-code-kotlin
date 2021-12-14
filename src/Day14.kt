import kotlin.math.ceil

typealias PolymerPair = Pair<Char, Char>
typealias PolymerMap = Map<PolymerPair, Long>
typealias PolymerExpansionMapping = Map<PolymerPair, Pair<PolymerPair, PolymerPair>>
typealias ElementMap = Map<Char, Long>

fun main() {

    fun List<Char>.toPair(): Pair<Char, Char> = Pair(first(), last())

    data class PolymerizationData(val polymerMap: PolymerMap, val polymerExpansionMapping: PolymerExpansionMapping)

    fun toPolymerExpansionMapping(input: List<String>): PolymerExpansionMapping {
        val insertionRules = input.associate {
            val parts = it.split(" -> ")
            Pair(Pair(parts[0].first(), parts[0].last()), parts[1].first())
        }

        return insertionRules
            .mapValues { (k, v) -> Pair(Pair(k.first, v), Pair(v, k.second)) }
    }

    fun toPolymerMap(polymerTemplate: String): PolymerMap {
        val windowed = polymerTemplate.toCharArray().toList()
            .windowed(size = 2, step = 1)
            .map { it.toPair() }

        return windowed
            .groupBy { it }
            .mapValues { (_, v) -> v.size.toLong() }
    }

    fun toPolymerizationData(input: List<String>): PolymerizationData {
        val polymerMap = toPolymerMap(input.first())

        val polymerExpansionMapping = toPolymerExpansionMapping(input.subList(2, input.size))

        return PolymerizationData(polymerMap, polymerExpansionMapping)
    }

    fun updatePolymer(polymerMap: PolymerMap, polymerExpansionMapping: PolymerExpansionMapping): PolymerMap {
        return polymerMap.entries
            .flatMap { (pair, count) ->
                val (first, second) = polymerExpansionMapping[pair]!!
                listOf(Pair(first, count), Pair(second, count))
            }
            .groupBy { it.first }
            .mapValues { (k, v) -> v.map { it.second }.sum() }
    }

    fun toElementMap(polymerMap: PolymerMap): ElementMap {
        val fromPairsToElements = polymerMap.entries
            .flatMap { (pair, count) ->
                val (first, second) = pair
                listOf(Pair(first, count), Pair(second, count))
            }

        return fromPairsToElements
            .groupBy { it.first }
            .mapValues { (_, v) -> ceil(v.map { it.second }.sum().toDouble() / 2).toLong() }
    }

    fun ElementMap.mostFrequent(): Long = maxByOrNull { it.value }?.value!!
    fun ElementMap.leastFrequent(): Long = minByOrNull { it.value }?.value!!

    fun part1(input: List<String>): Long {
        val (polymerMap, polymerExpansionMapping) = toPolymerizationData(input)

        val updatedPolymer = (1..10)
            .fold(polymerMap) { currentPolymer, _ ->
                updatePolymer(currentPolymer, polymerExpansionMapping)
            }

        val elementFrequencies = toElementMap(updatedPolymer)

        return elementFrequencies.mostFrequent() - elementFrequencies.leastFrequent()
    }

    fun part2(input: List<String>): Long {
        val (polymerMap, polymerExpansionMapping) = toPolymerizationData(input)

        val updatedPolymer = (1..40)
            .fold(polymerMap) { currentPolymer, _ ->
                updatePolymer(currentPolymer, polymerExpansionMapping)
            }

        val elementFrequencies = toElementMap(updatedPolymer)

        return elementFrequencies.mostFrequent() - elementFrequencies.leastFrequent()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
    check(part1(testInput) == 1588L)
    check(part2(testInput) == 2188189693529)

    val input = readInput("Day14")
    println(part1(input))
    println(part2(input))
}
