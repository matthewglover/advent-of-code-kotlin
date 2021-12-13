typealias Cave = String
typealias PathValidator = (List<Cave>, Cave) -> Boolean

fun main() {

    fun Cave.isBigCave(): Boolean {
        return uppercase() == this
    }

    fun Cave.isStart(): Boolean {
        return "start" == this
    }

    fun Cave.isEnd(): Boolean {
        return "end" == this
    }

    fun Cave.isSmallCave(): Boolean {
        return !isBigCave() && !isStart() && !isEnd()
    }

    fun List<Cave>.isCompletePathFromStartToEnd(): Boolean {
        return "start" == first() && "end" == last()
    }

    fun List<Cave>.hasVisitedSmallCaveMoreThanOnce(): Boolean {
        val smallCavesVisited = filter { it.isSmallCave() }

        return smallCavesVisited.size > smallCavesVisited.toSet().size
    }

    class CaveMap {
        val caveConnections: MutableMap<Cave, MutableSet<Cave>> = mutableMapOf()

        fun addCaves(caveA: Cave, caveB: Cave) {
            connectCaves(caveA, caveB)
            connectCaves(caveB, caveA)
        }

        private fun connectCaves(fromCave: Cave, toCave: Cave) {
            val connectedCaves = caveConnections.getOrPut(fromCave) { mutableSetOf() }

            connectedCaves.add(toCave)
        }

        fun findAllPathsFromStartToEnd(isValidPath: PathValidator): List<List<Cave>> {
            return findAllPaths(listOf("start"), isValidPath)
                .filter { it.isCompletePathFromStartToEnd() }
        }

        private fun findAllPaths(visitedCaves: List<Cave>, isValidPath: PathValidator): List<List<Cave>> {
            val lastCaveVisited = visitedCaves.last()

            if (lastCaveVisited.isEnd()) {
                return listOf(visitedCaves)
            }

            val toVisit = caveConnections[lastCaveVisited]!!

            return toVisit
                .filter { nextCave -> !nextCave.isStart() && isValidPath(visitedCaves, nextCave) }
                .map { nextCave -> visitedCaves + listOf(nextCave) }
                .flatMap { findAllPaths(it, isValidPath) }
        }
    }

    fun parseCaveMap(input: List<String>): CaveMap {
        val caveMap = CaveMap()

        input.forEach { caveConnection ->
            val (caveA, caveB) = caveConnection.split("-").map { it.trim() }
            caveMap.addCaves(caveA, caveB)
        }

        return caveMap
    }

    fun part1(input: List<String>): Int {
        val caveMap = parseCaveMap(input)

        fun isValidPath(visitedCaves: List<Cave>, nextCave: Cave) =
            nextCave.isBigCave() || !visitedCaves.contains(nextCave)

        val pathsFromStartToEnd = caveMap.findAllPathsFromStartToEnd(::isValidPath)

        return pathsFromStartToEnd.size
    }

    fun part2(input: List<String>): Int {
        val caveMap = parseCaveMap(input)

        fun isValidPath(visitedCaves: List<Cave>, nextCave: Cave): Boolean {
            if (nextCave.isSmallCave() &&
                visitedCaves.hasVisitedSmallCaveMoreThanOnce() &&
                visitedCaves.contains(nextCave)) return false

            return true
        }

        val pathsFromStartToEnd = caveMap.findAllPathsFromStartToEnd(::isValidPath)

        return pathsFromStartToEnd.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day12_test")
    check(part1(testInput) == 19)
    check(part2(testInput) == 103)

    val input = readInput("Day12")
    println(part1(input))
    println(part2(input))
}
