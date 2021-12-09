fun main() {

    fun part1(input: List<String>): Int {
        val heightMap = parseHeightMap(input)

        return heightMap.lowPoints()
            .sumOf { heightMap.heightAt(it) + 1 }
    }

    fun part2(input: List<String>): Int {
        val heightMap = parseHeightMap(input)

        return heightMap.basins()
            .map { it.size }
            .sortedDescending()
            .subList(0, 3)
            .reduce { a, b -> a * b }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day09_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 1134)

    val input = readInput("Day09")
    println(part1(input))
    println(part2(input))
}

typealias Matrix = List<List<Int>>
typealias Location = Pair<Int, Int>
typealias Basin = Set<Location>

class HeightMap(private val matrix: Matrix) : Matrix by matrix {

    companion object {
        private const val MaxHeight: Int = 9
    }

    private val horizontalSize: Int = matrix.first().size
    private val verticalSize: Int = matrix.size

    fun lowPoints(): List<Location> {
        return allLocations()
            .filter { isLowPoint(it) }
    }

    fun heightAt(location: Location): Int {
        val (x, y) = location
        return matrix[y][x]
    }

    fun basins(): List<Basin> {
        return lowPoints().map { basinFor(it) }
    }

    private fun allLocations(): List<Location> {
        return (0 until horizontalSize)
            .flatMap { x -> (0 until verticalSize).map { y -> Pair(x, y) } }
    }

    private fun basinFor(lowPoint: Location): Basin {
        val surroundingBasin = neighboursOf(lowPoint)
            .filter { !isMaxHeight(it) && isHigherThan(it, lowPoint) }
            .flatMap { basinFor(it) }
            .toSet()

        return setOf(lowPoint) + surroundingBasin
    }

    private fun isMaxHeight(location: Location): Boolean {
        return heightAt(location) == MaxHeight
    }

    private fun isHigherThan(location: Location, other: Location): Boolean {
        return heightAt(location) > heightAt(other)
    }

    private fun isLowPoint(location: Location): Boolean {
        val height = heightAt(location)
        val neighbourHeights = neighboursOf(location).map { heightAt(it) }

        return neighbourHeights.all { it > height }
    }

    private fun neighboursOf(location: Location): List<Location> {
        val (x, y) = location
        val prevX = x - 1
        val nextX = x + 1
        val prevY = y - 1
        val nextY = y + 1

        return listOf(Pair(prevX, y), Pair(x, prevY), Pair(nextX, y), Pair(x, nextY))
            .filter { inBoundsX(it.first) && inBoundsY(it.second) }
    }

    private fun inBoundsX(x: Int): Boolean {
        return x in 0 until horizontalSize
    }

    private fun inBoundsY(y: Int): Boolean {
        return y in 0 until verticalSize
    }
}

private val digitRE = "\\d".toRegex()

fun parseHeightMap(input: List<String>): HeightMap {
    val matrix = input
        .map { row ->
            row.split("")
                .filter { digitRE.matches(it) }
                .map { it.toInt() }
        }

    return HeightMap(matrix)
}
