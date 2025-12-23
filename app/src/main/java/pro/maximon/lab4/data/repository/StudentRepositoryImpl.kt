package pro.maximon.lab4.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pro.maximon.lab4.data.local.StudentDao
import pro.maximon.lab4.data.local.StudentEntity
import pro.maximon.lab4.models.StudentItem
import kotlin.uuid.Uuid

class StudentRepositoryImpl(
    private val studentDao: StudentDao,
): StudentRepository {

    override fun getStudents(): Flow<List<StudentItem>> {
        return studentDao.getAll().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addStudent(item: StudentItem) {
        withContext(Dispatchers.IO) {
            studentDao.insert(item.toEntity())
        }
    }

    override suspend fun updateStudent(item: StudentItem) {
        withContext(Dispatchers.IO) {
            studentDao.update(item.toEntity())
        }
    }

    override suspend fun removeStudent(id: Uuid) {
        withContext(Dispatchers.IO) {
            studentDao.deleteById(id.toString())
        }
    }

    override suspend fun getStudentsOnce(): List<StudentItem> {
        return withContext(Dispatchers.IO) {
            studentDao.getAllOnce().map { it.toDomain() }
        }
    }

    private fun StudentEntity.toDomain(): StudentItem {
        return StudentItem(
            id = Uuid.parse(id),
            name = name,
            skippedLessons = skippedLessons,
            completedWorks = completedWorks,
        )
    }

    private fun StudentItem.toEntity(): StudentEntity {
        return StudentEntity(
            id = id.toString(),
            name = name,
            skippedLessons = skippedLessons,
            completedWorks = completedWorks,
        )
    }
}