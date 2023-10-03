package com.example.xmatenotes.logic.model.Page

private val subject = mapOf(
    "数学" to 0x7F82BB,
    "语文" to 0xB5E61D,
    "英语" to 0x9FFCFD,
    "物理" to 0xEF88BE,
    "化学" to 0xFFFD55,
    "生物" to 0x58135E,
    "政治" to 0x16417C
)

fun getSubjectColor(subjectName: String): Int {
    return subject[subjectName] ?: subject["数学"]!!
}