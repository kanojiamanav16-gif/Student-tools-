package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String, // Physics, Chemistry, Mathematics, Biology, general
    val category: String, // Lecture, Self-study, Practice, Revision
    val notes: String = "",
    val isCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class TestHomeworkReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String, // e.g., "Weekly Mock Test #4", "HC Verma Electrostatics HW"
    val type: String, // "Test" or "Homework"
    val subject: String, // Physics, Chemistry, Maths, Biology
    val dateTime: Long, // timestamp
    val prepStatus: String = "Not Started", // Not Started, Preparing, Ready
    val isCompleted: Boolean = false,
    val marksObtained: Int? = null, // Optional for tests
    val totalMarks: Int? = null,
    val frequency: String = "Once", // "Once", "Daily", "Weekly"
    val timing: String = "Morning (8 AM)" // "Morning (8 AM)", "Afternoon (2 PM)", "Night (8 PM)"
)

@Entity(tableName = "syllabus")
data class SyllabusTopic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String, // Physics, Chemistry, Mathematics, Biology
    val examType: String, // JEE or NEET
    val category: String, // e.g., "Mechanics", "Organic", "Algebra"
    val topicName: String, // e.g., "Rotational Dynamics"
    val completionPercent: Int = 0, // 0 to 100
    val confidenceLevel: String = "Low" // Low, Medium, High
)

@Entity(tableName = "doubt_history")
data class DoubtHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String,
    val subject: String, // Physics, Chemistry, Mathematics, Biology, General
    val timestamp: Long = System.currentTimeMillis()
)
