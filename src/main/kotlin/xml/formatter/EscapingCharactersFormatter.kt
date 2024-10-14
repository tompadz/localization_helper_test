package xml.formatter

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class EscapingCharactersFormatter: XmlFileFormatter {

    data class Rule(
        val regex: Regex,
        val action: (Regex, String) -> String
    )

    private val rules = arrayOf(
        Rule(
            regex = "(?<!\\\\)'".toRegex(),
            action = { regex, string   ->
                string.replace(regex, "\\\\'")
            }
        )
    )

    override fun format(file: File): File {
        val newLines = file.readLines().map { line ->
            var newLine = line
            rules.forEach { rule ->
                if (rule.regex.containsMatchIn(line)) {
                    newLine = rule.action.invoke(rule.regex, line)
                }
            }
            newLine
        }
        Files.write(file.toPath(), newLines, StandardCharsets.UTF_8)
        return file
    }
}