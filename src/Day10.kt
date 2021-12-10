fun main() {

    val chunkMappings = mapOf(
        '(' to ')',
        '[' to ']',
        '{' to '}',
        '<' to '>'
    )

    val syntaxErrorScores = mapOf(
        ')' to 3,
        ']' to 57,
        '}' to 1197,
        '>' to 25137
    )

    val incompleteScores = mapOf(
        ')' to 1,
        ']' to 2,
        '}' to 3,
        '>' to 4
    )

    val startingChunkChars = chunkMappings.keys

    fun Char.isStartOfChunk(): Boolean = startingChunkChars.contains(this)
    fun Char.isClosingChunkFor(startingChunkChar: Char): Boolean = chunkMappings[startingChunkChar] == this

    data class SyntaxResult(val illegalChar: Char?, val incomplete: ArrayDeque<Char>?)

    fun String.checkSyntax(): SyntaxResult {
        val startedChunks = ArrayDeque<Char>()

        forEach {
            if (it.isStartOfChunk()) {
                startedChunks.addFirst(it)
            } else if (!it.isClosingChunkFor(startedChunks.removeFirst())) {
                return SyntaxResult(illegalChar = it, incomplete = null)
            }
        }

        return SyntaxResult(illegalChar = null, incomplete = startedChunks)
    }

    fun ArrayDeque<Char>.missingClosingChars(): List<Char> = map { chunkMappings[it]!! }

    fun List<Char>.incompleteScore(): Long {
        return fold(0L) { total, currentChar ->
            val incompleteScore = incompleteScores[currentChar]!!
            (total * 5) + incompleteScore
        }
    }

    fun part1(input: List<String>): Int {
        return input.mapNotNull { it.checkSyntax().illegalChar }.sumOf { syntaxErrorScores[it]!! }
    }

    fun part2(input: List<String>): Long {
        val incompleteLines = input.mapNotNull { it.checkSyntax().incomplete?.missingClosingChars() }
        val incompleteLineScores = incompleteLines.map { it.incompleteScore() }.sorted()

        return incompleteLineScores[incompleteLineScores.size / 2]
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 26397)
    check(part2(testInput) == 288957L)

    val input = readInput("Day10")
    println(part1(input))
    println(part2(input))
}
