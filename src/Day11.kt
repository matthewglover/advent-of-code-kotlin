fun main() {

    class Octopus(var energy: Int) {

        private val flashingThreshold: Int = 10

        private var startedFlashingInSubStep: Boolean = false

        fun bumpEnergy() {
            energy += 1

            if (energy == flashingThreshold) {
                startedFlashingInSubStep = true
            }
        }

        fun updateNeighbours(neighbours: List<Octopus>) {
            if (!startedFlashingInSubStep) return

            neighbours.filter { !it.hasFlashedInCurrentStep() }.forEach { it.bumpEnergy() }

            startedFlashingInSubStep = false
        }

        fun startedFlashingInSubStep(): Boolean {
            return startedFlashingInSubStep
        }

        fun hasFlashedInCurrentStep(): Boolean {
            return energy > 9
        }

        fun isFlashingAfterLastStep(): Boolean {
            return energy == 0
        }

        fun resetAfterFlash() {
            if (energy <= 9) return

            energy = 0
        }

        override fun toString(): String {
            return energy.toString()
        }
    }

    class OctopusGrid(private val locations: List<List<Octopus>>) {

        private var totalFlashes: Int = 0

        private var totalSteps: Int = 0

        fun step() {
            forEach { it.bumpEnergy() }

            do {
                forEachWithNeighbours { octopus, neighbours -> octopus.updateNeighbours(neighbours) }
            } while (anyFlashing())

            updateFlashed()

            totalSteps += 1
        }

        fun totalFlashes(): Int {
            return totalFlashes
        }

        fun areAllFlashing(): Boolean {
            return locations.all { row -> row.all { octopus -> octopus.isFlashingAfterLastStep() } }
        }

        fun stepCount(): Int {
            return totalSteps
        }

        private fun updateFlashed() {
            val flashedOctopuses = locations.flatMap { row -> row.filter { octopus -> octopus.hasFlashedInCurrentStep() } }

            totalFlashes += flashedOctopuses.size

            flashedOctopuses.forEach { it.resetAfterFlash() }
        }

        private fun anyFlashing(): Boolean {
            return locations.any { row -> row.any { octopus -> octopus.startedFlashingInSubStep() } }
        }

        private fun forEach(f: (Octopus) -> Unit) {
            locations.forEach { row -> row.forEach { octopus -> f(octopus) } }
        }

        private fun forEachWithNeighbours(f: (Octopus, List<Octopus>) -> Unit) {
            locations.forEachIndexed { y, rows ->
                rows.forEachIndexed { x, octopus ->
                    f(octopus, neighboursOf(Pair(x, y)))
                }
            }
        }

        private fun neighboursOf(coordinates: Pair<Int, Int>): List<Octopus> {
            val (baseX, baseY) = coordinates

            return (baseX - 1..baseX + 1).flatMap { x ->
                (baseY - 1..baseY + 1).mapNotNull { y -> octopusAt(Pair(x, y)) }
            }
        }

        private fun octopusAt(coordinates: Pair<Int, Int>): Octopus? {
            val (x, y) = coordinates

            if (x < 0 || x >= locations[0].size || y < 0 || y >= locations.size) return null

            return locations[y][x]
        }

        override fun toString(): String {
            return locations.joinToString("\n") { it.joinToString("") }
        }
    }


    fun parseOctopusGrid(input: List<String>): OctopusGrid {
        val locations = input.map { row -> row.trim().toList().map { Octopus(it.toString().toInt()) } }
        return OctopusGrid(locations)
    }

    fun part1(input: List<String>): Int {
        val grid = parseOctopusGrid(input)

        repeat(100) { grid.step() }

        return grid.totalFlashes()
    }

    fun part2(input: List<String>): Int {
        val grid = parseOctopusGrid(input)

        do { grid.step() } while (!grid.areAllFlashing())

        return grid.stepCount()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day11_test")
    check(part1(testInput) == 1656)
    check(part2(testInput) == 195)

    val input = readInput("Day11")
    println(part1(input))
    println(part2(input))
}
