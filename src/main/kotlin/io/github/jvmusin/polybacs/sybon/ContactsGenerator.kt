package io.github.jvmusin.polybacs.sybon

import io.github.jvmusin.polybacs.ir.IRProblem
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object ContactsGenerator {
    private val ownerContacts = mapOf(
        "ATSTNG" to arrayOf(
            "https://vk.com/atstng",
            "https://t.me/ATSTNG",
            "atstng@gmail.com",
            "https://codeforces.com/profile/ATSTNG"
        ),
        "Musin" to arrayOf(
            "https://t.me/jvmusin",
            "jvmusin@gmail.com",
            "https://codeforces.com/profile/jvmusin"
        )
    )

    fun generate(problem: IRProblem) = buildString {
        val owner = problem.owner
        val timeNow = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME)

        appendLine("This problem was converted from polygon.codeforces.com package")
        appendLine("You can contact $owner and request access for original polygon problem or additional info")
        appendLine()
        ownerContacts[owner]?.let { contacts ->
            contacts.forEach(::appendLine)
            appendLine()
        }
        appendLine("Polygon name: ${problem.name}")
//        appendLine("Polygon revision: ${problem.revision}")
//        appendLine("Polygon URL: ${problem.polygonUrl}")
        appendLine("Conversion timestamp: $timeNow")
//        appendLine("BACS converter revision: 2023-11-11T00:00:00")
    }
}
