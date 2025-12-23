package pro.maximon.lab4.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val skippedLessons: Int = 0,
    val completedWorks: Int = 0,
)
