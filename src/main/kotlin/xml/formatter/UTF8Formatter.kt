package xml.formatter

import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

class UTF8Formatter: XmlFileFormatter{

    override fun format(file: File): File {
        val newLines = file.readLines().map { line ->
            line.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&apos;", "'")
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&#13;","")
        }
        Files.write(file.toPath(), newLines, StandardCharsets.UTF_8)
        return file
    }
}