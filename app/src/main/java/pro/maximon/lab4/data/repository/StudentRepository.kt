package pro.maximon.lab4.data.repository

import kotlinx.coroutines.flow.Flow
import pro.maximon.lab4.models.StudentItem
import kotlin.uuid.Uuid

interface StudentRepository {
    fun getStudents(): Flow<List<StudentItem>>
    suspend fun addStudent(item: StudentItem)
    suspend fun updateStudent(item: StudentItem)
    suspend fun removeStudent(id: Uuid)
    suspend fun getStudentsOnce(): List<StudentItem>
}