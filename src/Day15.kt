typealias CavernGrid = List<List<Int>>

data class CavernCoordinate(val x: Int, val y: Int)

class Cavern(val grid: CavernGrid) {

    val width = grid.first().size
    val height = grid.size

    fun damageAt(coordinate: CavernCoordinate): Int {
        val (x, y) = coordinate

        return grid[y][x]
    }

    fun connectedTo(coordinate: CavernCoordinate): List<CavernCoordinate> {
        val up = moveY(coordinate, -1)
        val down = moveY(coordinate, 1)
        val left = moveX(coordinate, -1)
        val right = moveX(coordinate, 1)

        return listOfNotNull(up, down, left, right)
    }

    private fun moveX(coordinate: CavernCoordinate, step: Int): CavernCoordinate? {
        val next = CavernCoordinate(coordinate.x + step, coordinate.y)

        return if (isInBounds(next)) next else null
    }

    private fun moveY(coordinate: CavernCoordinate, step: Int): CavernCoordinate? {
        val next = CavernCoordinate(coordinate.x, coordinate.y + step)

        return if (isInBounds(next)) next else null
    }

    private fun isInBounds(coordinate: CavernCoordinate): Boolean {
        val (x, y) = coordinate

        return (0 until width).contains(x) && (0 until height).contains(y)
    }
}

class CavernCoordinatesQueue {

    private val coordinates = mutableMapOf<CavernCoordinate, Int>()

    fun put(coordinate: CavernCoordinate, score: Int) {
        coordinates.merge(coordinate, score) { a, b ->
            if (a < b) a else b
        }
    }

    fun takeMin(): Pair<CavernCoordinate, Int> {
        val entry = coordinates.entries
            .maxWithOrNull { a, b -> b.value - a.value } ?: throw RuntimeException("Queue is empty")

        coordinates.remove(entry.key)

        return entry.toPair()
    }
}

class Dijkstra(private val cavern: Cavern, private val start: CavernCoordinate, private val end: CavernCoordinate) {

    private val queue = CavernCoordinatesQueue()
    private val found = mutableMapOf<CavernCoordinate, Int>()

    fun run(): Int {
        queue.put(start, 0)

        while (!found.containsKey(end)) {
            val (currentCoordinate, currentScore) = queue.takeMin()

            found[currentCoordinate] = currentScore

            cavern.connectedTo(currentCoordinate).notAlreadyFound().forEach { coordinate ->
                queue.put(coordinate, currentScore + cavern.damageAt(coordinate))
            }
        }

        return found[end]!!
    }

    private fun List<CavernCoordinate>.notAlreadyFound(): List<CavernCoordinate> = filter { found[it] == null }
}

fun toCavern(input: List<String>): Cavern {
    val cavernGrid = input.map { row ->
        row.map { square -> square.toString().toInt() }
    }

    return Cavern(cavernGrid)
}

fun Int.increaseDamageBy(damageIncrease: Int): Int {
    val newDamage = this + damageIncrease

    return if (newDamage > 9) newDamage % 9 else newDamage
}

fun toExpandedCavern(baseCavern: Cavern): Cavern {
    val dim = 5
    val newWidth = baseCavern.width * dim
    val newHeight = baseCavern.height * dim

    val newGrid = (0 until newHeight).map { y ->
        val yStep = y / baseCavern.height
        val baseY = y % baseCavern.height

        (0 until newWidth).map { x ->
            val xStep = x / baseCavern.width
            val baseX = x % baseCavern.width

            baseCavern.damageAt(CavernCoordinate(baseX, baseY)).increaseDamageBy(xStep + yStep)
        }
    }

    return Cavern(newGrid)
}


fun main() {

    fun part1(input: List<String>): Int {
        val cavern = toCavern(input)

        val start = CavernCoordinate(0, 0)
        val end = CavernCoordinate(cavern.width - 1, cavern.height - 1)

        val dijkstra = Dijkstra(cavern, start, end)

        return dijkstra.run()
    }

    fun part2(input: List<String>): Int {
        val cavern = toCavern(input)

        val expandedCavern = toExpandedCavern(cavern)

        val start = CavernCoordinate(0, 0)
        val end = CavernCoordinate(expandedCavern.width - 1, expandedCavern.height - 1)

        val dijkstra = Dijkstra(expandedCavern, start, end)

        return dijkstra.run()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    check(part1(testInput) == 40)
    check(part2(testInput) == 315)

    val input = readInput("Day15")
    println(part1(input))
    println(part2(input))
}
