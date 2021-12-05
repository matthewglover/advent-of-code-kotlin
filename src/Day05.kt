fun main() {
    fun part1(input: List<String>): Int {
        val lines = input.map(InputParser::toLineConfig)
            .filter { it.isVerticalLine() || it.isHorizontalLine() }
            .map { it.toLine() }

        val grid = Grid(lines)

        return grid.totalOverlappingPoints()
    }

    fun part2(input: List<String>): Int {
        val lines = input.map(InputParser::toLineConfig)
            .map { it.toLine() }

        val grid = Grid(lines)

        return grid.totalOverlappingPoints()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test")
    check(part1(testInput) == 5)
    check(part2(testInput) == 12)

    val input = readInput("Day05")
    println(part1(input))
    println(part2(input))
}


typealias Line = List<Point>
typealias LineConfig = Pair<Point, Point>

data class Point(val x: Int, val y: Int)

object InputParser {
    val lineRegEx = """^([0-9,]+)\s+->\s+([0-9,]+)\s*$""".toRegex()

    fun toLineConfig(rawLine: String): LineConfig {
        val result = lineRegEx.find(rawLine)!!
        val (rawStartPoint, rawEndPoint) = result.destructured

        val startPoint = toPoint(rawStartPoint)
        val endPoint = toPoint(rawEndPoint)

        return Pair(startPoint, endPoint)
    }

    private fun toPoint(rawPoint: String): Point {
        val points = rawPoint.split(",").map { it.toInt() }

        return Point(points.first(), points.last())
    }

}

fun LineConfig.toLine(): Line {
    val line = mutableListOf<Point>()
    val (start, end) = this

    if (isHorizontalLine()) {
        var x = start.x

        while (x != end.x) {
            line.add(Point(x, start.y))
            x += horizontalStep()
        }

        line.add(end)
    } else if (isVerticalLine()) {
        var y = start.y

        while (y != end.y) {
            line.add(Point(start.x, y))
            y += verticalStep()
        }

        line.add(end)
    } else {
        var x = start.x
        var y = start.y

        while (x != end.x && y != end.y) {
            line.add(Point(x, y))

            y += verticalStep()
            x += horizontalStep()
        }

        line.add(end)
    }

    return line
}

fun LineConfig.horizontalStep(): Int {
    val (start, end) = this

    return if (start.x < end.x) {
        1
    } else if (start.x > end.x) {
        -1
    } else {
        0
    }
}

fun LineConfig.verticalStep(): Int {
    val (start, end) = this

    return if (start.y < end.y) {
        1
    } else if (start.y > end.y) {
        -1
    } else {
        0
    }
}

fun LineConfig.isVerticalLine(): Boolean {
    return first.x == second.x
}

fun LineConfig.isHorizontalLine(): Boolean {
    return first.y == second.y
}

class Grid(lines: List<Line>, private val points: MutableMap<Point, Int> = mutableMapOf()) {

    init {
        lines.forEach { addLine(it) }
    }

    fun totalOverlappingPoints(): Int {
        return points.values.filter { it >= 2 }.size
    }

    private fun addLine(line: Line) {
        line.forEach { point ->
            points.compute(point) { _, currentValue -> currentValue?.let { it + 1 } ?: 1 }
        }
    }
}
