package pro.maximon.lab4.models

import kotlin.uuid.Uuid

data class StudentItem(
    val name: String,
    val skippedLessons: Int = 0,
    val completedWorks: Int = 0,
    val id: Uuid = Uuid.random(),
)
