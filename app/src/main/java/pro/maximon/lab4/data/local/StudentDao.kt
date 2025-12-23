package pro.maximon.lab4.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface StudentDao {
    @Query("SELECT * FROM students")
    fun getAll(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students")
    suspend fun getAllOnce(): List<StudentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity)

    @Update
    suspend fun update(student: StudentEntity)

    @Delete
    suspend fun delete(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteById(id: String)
}