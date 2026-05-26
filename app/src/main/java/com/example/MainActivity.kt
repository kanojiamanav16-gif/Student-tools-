package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.StudyRepository
import com.example.ui.screens.StudyAppMainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyViewModel
import com.example.ui.viewmodel.StudyViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 1. Initialize DB components
    val database = AppDatabase.getDatabase(this)
    val repository = StudyRepository(
        studyTaskDao = database.studyTaskDao(),
        reminderDao = database.reminderDao(),
        syllabusDao = database.syllabusDao(),
        doubtDao = database.doubtDao()
    )

    // 2. Initialize ViewModel with appropriate factory bounds
    val factory = StudyViewModelFactory(repository)
    val viewModel = ViewModelProvider(this, factory)[StudyViewModel::class.java]

    setContent {
      MyApplicationTheme {
        StudyAppMainScreen(viewModel = viewModel)
      }
    }
  }
}

