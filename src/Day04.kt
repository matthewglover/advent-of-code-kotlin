
fun main() {
    fun part1(input: List<String>): Int {
        val game = GameFactory.toGame(input)

        while (game.winningBoard() == null && !game.isOver()) {
            game.play()
        }

        return game.lastWinningBoardScore() ?: 0
    }

    fun part2(input: List<String>): Int {
        val game = GameFactory.toGame(input)

        while (!game.isOver()) {
            game.play()
        }

        return game.lastWinningBoardScore() ?: 0
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 4512)
    check(part2(testInput) == 1924)

    val input = readInput("Day04")
    println(part1(input))
    println(part2(input))
}

object GameFactory {
    fun toGame(input: List<String>): Game {
        val numbers = toNumbers(input[0])

        val boards = input.subList(2, input.size).chunked(6).map(::toBoard)

        return Game(numbers, boards)
    }

    private fun toNumbers(raw: String): List<Int> {
        return raw.split(",").map { it.toInt() }
    }

    private fun toBoard(input: List<String>): Board {
        val rows = input.take(5).map(::toRow)

        return Board(rows)
    }

    private fun toRow(raw: String): Row {
        val data = raw.trim()
            .split("\\s+".toRegex())
            .mapTo(mutableListOf()) { Pair(it.toInt(), false) }

        return Row(data)
    }
}

data class Game(val numbers: List<Int>, val boards: List<Board>, var currentRound: Int = 0) {

    private val completedBoards: MutableList<Board> = mutableListOf()

    fun play() {
        if (isOver()) {
            return
        }

        val currentNumber = numbers[currentRound++]
        boards.forEach { it.mark(currentNumber) }

        val newlyCompletedBoards = boards.filter { it.isComplete() && !completedBoards.contains(it) }
        completedBoards.addAll(newlyCompletedBoards)
    }

    fun isOver(): Boolean {
        return completedBoards.size == boards.size || currentRound >= numbers.size
    }

    fun lastWinningBoardScore(): Int? {
        val lastDrawnNumber = numbers[currentRound - 1]

        return winningBoard()?.let { it.sumOfUnmarkedNumbers() * lastDrawnNumber }
    }

    fun winningBoard(): Board? {
        return if (completedBoards.isNotEmpty()) {
            completedBoards.last()
        } else {
            null
        }
    }
}

data class Board(val rows: List<Row>) {
    fun mark(valueToMark: Int) {
        if (!isComplete()) {
            rows.forEach { it.mark(valueToMark) }
        }
    }

    fun isComplete(): Boolean {
        return isAnyRowComplete() || isAnyCompleteColumn()
    }

    fun sumOfUnmarkedNumbers(): Int {
        return rows.sumOf { it.sumOfUnmarkedNumbers() }
    }

    private fun isAnyRowComplete(): Boolean {
        return rows.any { it.isComplete() }
    }

    private fun isAnyCompleteColumn(): Boolean {
        for (columnIndex in rows[0].data.indices) {
            if (isCompleteColumn(columnIndex)) {
                return true
            }
        }

        return false
    }

    private fun isCompleteColumn(columnIndex: Int): Boolean {
        for (columnItemIndex in rows.indices) {
            if (!rows[columnItemIndex].data[columnIndex].second) {
                return false
            }
        }

        return true
    }
}

data class Row(val data: MutableList<Pair<Int, Boolean>>) {
    fun isComplete(): Boolean {
        return data.map { it.second }.all { it }
    }

    fun mark(valueToMark: Int) {
        for (index in 0 until data.size) {
            if (data[index].first == valueToMark) {
                data[index] = Pair(valueToMark, true)
            }
        }
    }

    fun sumOfUnmarkedNumbers(): Int {
        return data.filter { !it.second }.sumOf { it.first }
    }
}
