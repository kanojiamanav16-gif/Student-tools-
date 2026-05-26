package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.StudyTask
import com.example.data.model.TestHomeworkReminder
import com.example.data.model.SyllabusTopic
import com.example.data.model.DoubtHistory

@Database(
    entities = [
        StudyTask::class,
        TestHomeworkReminder::class,
        SyllabusTopic::class,
        DoubtHistory::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun syllabusDao(): SyllabusDao
    abstract fun doubtDao(): DoubtDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jee_neet_companion_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
