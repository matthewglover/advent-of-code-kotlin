enum class Axis {

    X, Y;

    companion object {
        fun from(s: String): Axis {
            return when (s) {
                "x" -> X
                "y" -> Y
                else -> throw RuntimeException("Could not convert '$s' to Axis")
            }
        }
    }
}

fun main() {
    data class FoldInstruction(val axis: Axis, val line: Int)

    data class DotLocation(val x: Int, val y: Int) {
        constructor(points: List<Int>) : this(points[0], points[1])

        fun update(axis: Axis, newPosition: Int): DotLocation {
            return when (axis) {
                Axis.X -> DotLocation(newPosition, y)
                Axis.Y -> DotLocation(x, newPosition)
            }
        }
    }

    class PaperGrid(var dots: Set<DotLocation>) {
        fun totalDots(): Int = dots.size

        fun fold(foldInstruction: FoldInstruction) {
            val dimensionSelector = when (foldInstruction.axis) {
                Axis.X -> { dot: DotLocation -> dot.x }
                Axis.Y -> { dot: DotLocation -> dot.y }
            }

            val groupedDots = dots.groupBy { dot -> dimensionSelector(dot) > foldInstruction.line }
            val unchangedDots = groupedDots[false] ?: listOf()
            val dotsToMove = groupedDots[true] ?: listOf()

            val movedDots = dotsToMove.map { dot ->
                val newPosition = foldInstruction.line - (dimensionSelector(dot) - foldInstruction.line)
                dot.update(foldInstruction.axis, newPosition)
            }

            dots = (unchangedDots + movedDots).toSet()
        }

        private fun grid(): List<List<String>> {
            return (0..height()).map { y ->
                (0..width()).map { x ->
                    if (dots.contains(DotLocation(x, y))) "#" else "."
                }
            }
        }

        private fun width(): Int = dots.maxOf { dot -> dot.x }

        private fun height(): Int = dots.maxOf { dot -> dot.y }


        override fun toString(): String {
            return grid().joinToString("\n") { line -> line.joinToString("") }
        }
    }

    data class CodeConfiguration(val paperGrid: PaperGrid, val instructions: List<FoldInstruction>)


    fun parseToCodeConfiguration(input: List<String>): CodeConfiguration {
        val dots = input.takeWhile { line -> line.isNotBlank() }
            .map { line -> DotLocation(line.split(",").map { it.toInt() }) }
            .toSet()

        val paperGrid = PaperGrid(dots)


        val foldInstructions = input.filter { line -> line.startsWith("fold along") }
            .mapNotNull { line -> """^.+([xy])=(\d+)$""".toRegex().find(line.trim()) }
            .map { match ->
                val (axisStr, line) = match.destructured
                FoldInstruction(Axis.from(axisStr), line.toInt())
            }

        return CodeConfiguration(paperGrid, foldInstructions)
    }

    fun part1(input: List<String>): Int {
        val (paperGrid, instructions) = parseToCodeConfiguration(input)

        val firstFold = instructions.first()

        paperGrid.fold(firstFold)

        return paperGrid.totalDots()
    }

    fun part2(input: List<String>): String {
        val (paperGrid, instructions) = parseToCodeConfiguration(input)

        instructions.forEach { instruction ->
            paperGrid.fold(instruction)
        }

        return paperGrid.toString()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day13_test")
    check(part1(testInput) == 17)
    check(
        part2(testInput) == """#####
#...#
#...#
#...#
#####""")

    val input = readInput("Day13")
    println(part1(input))
    println(part2(input))
}
