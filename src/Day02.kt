
sealed interface Instruction
data class Forward(val value: Int): Instruction
data class Up(val value: Int): Instruction
data class Down(val value: Int): Instruction


fun toInstruction(line: String): Instruction {
    val instructionParts = line.split(" ")

    val instructionType = instructionParts[0].trim()
    val value = instructionParts[1].toInt()

    return when (instructionType) {
        "forward" -> Forward(value)
        "up" -> Up(value)
        "down" -> Down(value)
        else -> {
            throw RuntimeException("Parsing exception - could not parse line: `$line`")
        }
    }
}
fun main() {
    fun part1(input: List<String>): Int {
        var horizontal = 0
        var depth = 0

        for (line in input) {
            when (val instruction = toInstruction(line)) {
                is Forward -> horizontal += instruction.value
                is Up -> depth -= instruction.value
                is Down -> depth += instruction.value
            }
        }

        return horizontal * depth
    }

    fun part2(input: List<String>): Int {
        var horizontal = 0
        var depth = 0
        var aim = 0

        for (line in input) {
            when (val instruction = toInstruction(line)) {
                is Forward -> {
                    horizontal += instruction.value
                    depth += aim * instruction.value
                }
                is Up -> aim -= instruction.value
                is Down -> aim += instruction.value
            }
        }

        return horizontal * depth
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part2(testInput) == 900)

    val input = readInput("Day02")
    println(part1(input))
    println(part2(input))
}
