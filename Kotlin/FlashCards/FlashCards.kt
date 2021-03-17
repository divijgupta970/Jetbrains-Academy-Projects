package flashcards

import java.io.File
import java.io.FileNotFoundException
import java.lang.StringBuilder
import kotlin.math.max

fun main(args: Array<String>) {
    val cards = mutableMapOf<String, String>()
    val mistakes = mutableMapOf<String, Int>()
    val logs = StringBuilder()
    var toExport = false
    var exportFileName = ""
    if (args.size == 4) {
        exportFileName = if (args[0] == "-import") {
            import(cards, mistakes, logs, args[1])
            args[3]
        } else {
            import(cards, mistakes, logs, args[3])
            args[1]
        }
        toExport = true
    } else if (args.size == 2) {
        if (args[0] == "-import") {
            import(cards, mistakes, logs, args[1])
        } else {
            toExport = true
            exportFileName = args[1]
        }
    }
    while (true) {
        println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):", logs)
        when (readLine(logs)) {
            "add" -> add(cards, mistakes, logs)
            "remove" -> remove(cards, mistakes, logs)
            "import" -> {
                println("File name:", logs)
                val fileName = readLine(logs)
                import(cards, mistakes, logs, fileName)
            }
            "export" -> {
                println("File name:", logs)
                val fileName = readLine(logs)
                export(cards, mistakes, logs, fileName)
            }
            "ask" -> ask(cards, mistakes, logs)
            "exit" -> {
                if (toExport)
                    export(cards, mistakes, logs, exportFileName)
                println("Bye bye!", logs)
                break
            }
            "log" -> saveLog(logs)
            "hardest card" -> getHardestCard(mistakes, logs)
            "reset stats" -> resetStats(mistakes, logs)
        }
    }
}

fun resetStats(mistakes: MutableMap<String, Int>, logs: StringBuilder) {
    mistakes.forEach {
        mistakes[it.key] = 0
    }
    println("Card statistics have been reset.", logs)
}

fun getHardestCard(mistakes: MutableMap<String, Int>, logs: StringBuilder) {
    var max = 0
    mistakes.forEach {
        max = max(it.value, max)
    }
    if (max == 0) {
        println("There are no cards with errors.", logs)
        return
    }
    val maxs = mutableListOf<String>()
    mistakes.forEach {
        if (it.value == max)
            maxs.add(it.key)
    }
    if(maxs.size == 1) {
        println("The hardest card is \"${maxs[0]}\". You have $max errors answering it.")
    }
    val toPrint = StringBuilder("The hardest cards are ")
    maxs.forEachIndexed { index, s ->
        if (index != maxs.lastIndex) {
            toPrint.append(""""$s", """)
        } else {
            toPrint.append(""""$s". """)
        }
    }
    toPrint.append("You have $max errors answering them.")
    println(toPrint.toString(), logs)
}

fun saveLog(logs: StringBuilder) {
    println("File name:", logs)
    val fileName = readLine(logs)
    val file = File(fileName)
    file.appendText(logs.toString())
    println("The log has been saved.", logs)
}


fun println(toPrint: String, logs: StringBuilder) {
    println(toPrint)
    logs.append(toPrint).append("\n")
}

fun readLine(logs: StringBuilder): String {
    val line = readLine()!!
    logs.append(line).append("\n")
    return line
}

fun ask(cards: MutableMap<String, String>, mistakes: MutableMap<String, Int>, logs: StringBuilder) {
    val keyList = ArrayList(cards.keys)
    val valueList = ArrayList(cards.values)
    println("How many times to ask?", logs)
    repeat(readLine(logs).toInt()) {
        val random = (0 until keyList.size).random()
        val key = keyList[random]
        val value = valueList[random]
        println("Print the definition of \"${key}\":", logs)
        val ans = readLine(logs)
        when {
            ans == value -> {
                println("Correct!", logs)
            }
            cards.containsValue(ans) -> {
                mistakes[key] = mistakes[key]!! + 1
                println("Wrong. The right answer is \"${value}\", but your definition is correct for \"${getKey(cards, ans)}\".", logs)
            }
            else -> {
                mistakes[key] = mistakes[key]!! + 1
                println("Wrong. The right answer is \"${value}\".", logs)
            }
        }
    }
}

fun getKey(map: MutableMap<String, String>, target: String): String {
    for (entry in map) {
        if (entry.value == target)
            return entry.key
    }
    return ""
}

fun export(cards: MutableMap<String, String>, mistakes: MutableMap<String, Int>, logs: StringBuilder, fileName: String) {
    val file = File(fileName)
    val text = StringBuilder()
    cards.forEach {
        text.append("${it.key}#${it.value}#${mistakes[it.key]}\n")
    }
    file.writeText(text.toString())
    println("${cards.size} cards have been saved.", logs)
}

fun import(cards: MutableMap<String, String>, mistakes: MutableMap<String, Int>, logs: StringBuilder, fileName: String) {
    try {
        val file = File(fileName)
        val lines = file.readLines()
        for (line in lines) {
            val strs = line.split("#")
            cards[strs[0]] = strs[1]
            mistakes[strs[0]] = strs[2].toInt()
        }
        println("${lines.size} cards have been loaded.", logs)
    } catch (e: FileNotFoundException) {
        println("File not found.", logs)
    }
}

fun remove(cards: MutableMap<String, String>, mistakes: MutableMap<String, Int>, logs: StringBuilder) {
    println("Which card?", logs)
    val card = readLine(logs)
    if (!cards.contains(card)) {
        println("Can't remove \"$card\": there is no such card.", logs)
        return
    }
    cards.remove(card)
    mistakes.remove(card)
    println("The card has been removed.", logs)
}

fun add(cards: MutableMap<String, String>, mistakes: MutableMap<String, Int>, logs: StringBuilder) {
    println("The card:", logs)
    val card = readLine(logs)
    if (cards.containsKey(card)) {
        println("The card \"$card\" already exists.", logs)
        return
    }
    println("The definition of the card:", logs)
    val definition = readLine(logs)
    if (cards.containsValue(definition)) {
        println("The definition \"$definition\" already exists.", logs)
        return
    }
    cards[card] = definition
    mistakes[card] = 0
    println("The pair (\"$card\":\"$definition\") has been added.", logs)
}