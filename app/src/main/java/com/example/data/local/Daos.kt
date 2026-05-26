package com.example.data.local

import androidx.room.*
import com.example.data.model.StudyTask
import com.example.data.model.TestHomeworkReminder
import com.example.data.model.SyllabusTopic
import com.example.data.model.DoubtHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, timestamp DESC")
    fun getAllTasks(): Flow<List<StudyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTask)

    @Update
    suspend fun updateTask(task: StudyTask)

    @Delete
    suspend fun deleteTask(task: StudyTask)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dateTime ASC")
    fun getAllReminders(): Flow<List<TestHomeworkReminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 ORDER BY dateTime ASC")
    fun getActiveReminders(): Flow<List<TestHomeworkReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: TestHomeworkReminder)

    @Update
    suspend fun updateReminder(reminder: TestHomeworkReminder)

    @Delete
    suspend fun deleteReminder(reminder: TestHomeworkReminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)
}

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM syllabus ORDER BY subject ASC, id ASC")
    fun getAllSyllabus(): Flow<List<SyllabusTopic>>

    @Query("SELECT * FROM syllabus WHERE examType = :examType ORDER BY subject ASC, id ASC")
    fun getSyllabusByExam(examType: String): Flow<List<SyllabusTopic>>

    @Query("SELECT COUNT(*) FROM syllabus")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabusList(topics: List<SyllabusTopic>)

    @Update
    suspend fun updateSyllabusTopic(topic: SyllabusTopic)
}

@Dao
interface DoubtDao {
    @Query("SELECT * FROM doubt_history ORDER BY timestamp DESC")
    fun getDoubtHistory(): Flow<List<DoubtHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoubt(doubt: DoubtHistory)

    @Query("DELETE FROM doubt_history")
    suspend fun clearHistory()
}
