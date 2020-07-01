package minesweeper

import minesweeper.Minesweeper.Cmd.Companion.fromCode
import java.util.*
import kotlin.random.Random

fun main() {
    val minesweeper = Minesweeper()
    minesweeper.play()
}

class Minesweeper {
    data class Cell(val x: Int, val y: Int) {
        fun isValid() = x in 0 until LIMIT && y in 0 until LIMIT
    }

    enum class Cmd(val code: String) {
        FREE("free"), MINE("mine");

        companion object {
            fun fromCode(code: String): Cmd = values().first { it.code == code }
        }
    }

    data class Command(val x: Int, val y: Int, val cmd: Cmd)
    enum class CellValue(val display: Char, val hasMine: Boolean) {
        UNEXPLORED_MINE('.', true),
        FREE('/', false),
        N_1('1', false),
        N_2('2', false),
        N_3('3', false),
        N_4('4', false),
        N_5('5', false),
        N_6('6', false),
        N_7('7', false),
        N_8('8', false),
        FINAL_MINE('X', true),
    }

    private val board = mutableMapOf<Cell, CellValue>()

    private fun MutableMap<Cell, CellValue>.hasMine(cell: Cell): Boolean {
        return this[cell]?.hasMine ?: false
    }

    private var firstFree = true

    private val marked = mutableSetOf<Cell>()

    private var mines: Int = 0

    fun play() {
        mines = queryMines()
        var win = false
        var fail = false
        display(true)
        do {
            val ok = processCommand()
            if (!ok) {
                fail = true
                processFail()
            }
            win = checkWin()
            display(false)
        }while(!win && !fail)

        if (fail) {
            println("You stepped on a mine and failed!")
        }
        if (win) {
            println("Congratulations! You found all the mines!")
        }
    }

    private fun checkWin(): Boolean {
        if (markAllMines()) return true
        if (exploreAllExplored()) return true
        return false
    }

    private fun markAllMines(): Boolean {
        if (marked.size != mines) {
            return false
        }
        marked.forEach {
            if (!board.hasMine(it)) return false
        }
        return true
    }

    private fun exploreAllExplored(): Boolean {
        return board.size == LIMIT * LIMIT
    }

    private fun processFail() {
        board.asSequence().filter { it.value.hasMine }.forEach {
            board[it.key] = CellValue.FINAL_MINE
        }
    }

    private fun display(initial: Boolean) {
        println(" │123456789│")
        println("—│—————————│")

        for (i in 0 until LIMIT) {
            print("${i + 1}│")
            for (j in 0 until LIMIT) {
                if (initial) {
                    print(HIDED)
                } else {
                    print(displayCell(i, j))
                }
            }
            println("│")
        }
        println("—│—————————│")
    }

    private fun displayCell(x: Int, y: Int): Char {
        val cell = Cell(x, y)

        if (marked.contains(cell)) {
            return MARKED
        }

        val cellValue = board[cell] ?: return HIDED

        return cellValue.display
    }

    private fun processCommand(): Boolean {
        println("Set/unset mines marks or claim a cell as free: ")
        val command = readCommand()
        val cell = Cell(command.x, command.y)
        preprocessCommand(cell)
        return when (command.cmd) {
            Cmd.FREE -> processFreeCommand(cell)
            Cmd.MINE -> {
                processMarkCommand(cell)
                true
            }
        }
    }

    private fun processMarkCommand(cell: Cell) {
        if (marked.contains(cell)) {
            marked.remove(cell)
        } else {
            marked.add(cell)
        }
    }

    private fun processFreeCommand(cell: Cell): Boolean {
        if (board.containsKey(cell) && board[cell] == CellValue.UNEXPLORED_MINE) {
            return false
        }

        board[cell] = markByNear(cell)
        marked.remove(cell)

        if (board[cell] == CellValue.FREE) {
            exploreOthers(cell)
        }
        return true
    }

    private fun exploreOthers(cell: Cell) {
        for (i in -1..1) {
            for (j in -1..1) {
                if (i == 0 && j == 0) continue

                val probe = Cell(cell.x + i, cell.y + j)
                if (probe.isValid() && board[probe] == null) {
                    processFreeCommand(probe)
                }
            }
        }
    }

    private fun markByNear(cell:Cell) : CellValue {
        return when (minesNear(cell)) {
            0 -> CellValue.FREE
            1 -> CellValue.N_1
            2 -> CellValue.N_2
            3 -> CellValue.N_3
            4 -> CellValue.N_4
            5 -> CellValue.N_5
            6 -> CellValue.N_6
            7 -> CellValue.N_7
            8 -> CellValue.N_8
            else -> throw IllegalStateException()
        }
    }

    private fun minesNear(cell: Cell): Int {
        var result = 0
        for (i in -1..1) {
            for (j in -1..1) {
                if (i == 0 && j == 0) continue
                val probe = Cell(cell.x + i, cell.y + j)
                if (board.hasMine(probe)) ++result
            }
        }
        return result
    }

    private fun preprocessCommand(cell: Cell) {
        if (firstFree) {
            generate(cell)
            firstFree = false
        }
    }

    private fun generate(mustBeFree: Cell) {
        repeat(mines) {

            var passed = false
            do {
                val x = Random.nextInt(LIMIT)
                val y = Random.nextInt(LIMIT)
                val cell = Cell(x, y)
                if (cell != mustBeFree && !board.containsKey(cell)) {
                    passed = true
                    board[cell] = CellValue.UNEXPLORED_MINE
                }
            } while (!passed)
        }
    }

    private fun readCommand(): Command {
        val scanner = Scanner(System.`in`)
        val y = scanner.nextInt() - 1
        val x = scanner.nextInt() - 1
        val cmd = fromCode(scanner.next())
        return Command(x, y, cmd)
    }

    private fun queryMines(): Int {
        print("How many mines do you want on the field?")
        val scanner = Scanner(System.`in`)
        return scanner.nextInt()
    }

    companion object {
        private const val LIMIT = 9
        private const val HIDED = '.'
        private const val MARKED = '*'
    }
}