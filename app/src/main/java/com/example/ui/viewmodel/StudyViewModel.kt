package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.StudyTask
import com.example.data.model.TestHomeworkReminder
import com.example.data.model.SyllabusTopic
import com.example.data.model.DoubtHistory
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudyViewModel(private val repository: StudyRepository) : ViewModel() {

    // Exam target filter: "JEE" or "NEET"
    private val _examFilter = MutableStateFlow("JEE")
    val examFilter: StateFlow<String> = _examFilter.asStateFlow()

    // Database Flows
    val tasks: StateFlow<List<StudyTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<TestHomeworkReminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeReminders: StateFlow<List<TestHomeworkReminder>> = repository.activeReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val syllabus: StateFlow<List<SyllabusTopic>> = repository.allSyllabus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val doubtHistory: StateFlow<List<DoubtHistory>> = repository.doubtHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered syllabus flow based on modern exam type selection
    val filteredSyllabus: StateFlow<List<SyllabusTopic>> = combine(syllabus, examFilter) { syllabusList, filter ->
        syllabusList.filter { it.examType.equals(filter, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time calculated Preparation Meter progress
    val preparationProgress: StateFlow<Int> = filteredSyllabus.map { list ->
        if (list.isEmpty()) 0
        else {
            val total = list.sumOf { it.completionPercent }
            total / list.size
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val physicsProgress: StateFlow<Int> = filteredSyllabus.map { list ->
        val subList = list.filter { it.subject.equals("Physics", ignoreCase = true) }
        if (subList.isEmpty()) 0 else subList.sumOf { it.completionPercent } / subList.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val chemistryProgress: StateFlow<Int> = filteredSyllabus.map { list ->
        val subList = list.filter { it.subject.equals("Chemistry", ignoreCase = true) }
        if (subList.isEmpty()) 0 else subList.sumOf { it.completionPercent } / subList.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val mathProgress: StateFlow<Int> = filteredSyllabus.map { list ->
        val subList = list.filter { it.subject.equals("Mathematics", ignoreCase = true) }
        if (subList.isEmpty()) 0 else subList.sumOf { it.completionPercent } / subList.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val biologyProgress: StateFlow<Int> = filteredSyllabus.map { list ->
        val subList = list.filter { it.subject.equals("Biology", ignoreCase = true) }
        if (subList.isEmpty()) 0 else subList.sumOf { it.completionPercent } / subList.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _linkCompletedTasksToProgress = MutableStateFlow(true)
    val linkCompletedTasksToProgress: StateFlow<Boolean> = _linkCompletedTasksToProgress.asStateFlow()

    fun setLinkCompletedTasksToProgress(link: Boolean) {
        _linkCompletedTasksToProgress.value = link
    }

    // Timer States
    private val _timerSeconds = MutableStateFlow(1500) // Default 25 minutes Pomodoro
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _timerTotalDuration = MutableStateFlow(1500)
    val timerTotalDuration: StateFlow<Int> = _timerTotalDuration.asStateFlow()

    private val _timerSubject = MutableStateFlow("Physics")
    val timerSubject: StateFlow<String> = _timerSubject.asStateFlow()

    private var timerJob: Job? = null

    // AI Doubt support States
    private val _doubtLoading = MutableStateFlow(false)
    val doubtLoading: StateFlow<Boolean> = _doubtLoading.asStateFlow()

    private val _currentDoubtAnswer = MutableStateFlow<String?>(null)
    val currentDoubtAnswer: StateFlow<String?> = _currentDoubtAnswer.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndPrepopulateSyllabus()
        }
    }

    fun setExamFilter(exam: String) {
        _examFilter.value = exam
    }

    // --- Task operations ---
    fun addTask(title: String, subject: String, category: String, notes: String = "") {
        viewModelScope.launch {
            repository.insertTask(
                StudyTask(
                    title = title,
                    subject = subject,
                    category = category,
                    notes = notes
                )
            )
        }
    }

    fun toggleTaskCompletion(task: StudyTask) {
        viewModelScope.launch {
            val nextState = !task.isCompleted
            repository.updateTask(task.copy(isCompleted = nextState))
            
            if (nextState && _linkCompletedTasksToProgress.value) {
                // Find least completed topic for this subject and bump progress by 10%
                val currentSyllabus = filteredSyllabus.value
                val subjectTopics = currentSyllabus.filter { it.subject.equals(task.subject, ignoreCase = true) || (task.subject.equals("Mathematics", ignoreCase = true) && it.subject.equals("Mathematics", ignoreCase = true)) }
                if (subjectTopics.isNotEmpty()) {
                    val targetTopic = subjectTopics.minByOrNull { it.completionPercent } ?: subjectTopics.first()
                    val newPercent = (targetTopic.completionPercent + 10).coerceAtMost(100)
                    updateSyllabusProgress(targetTopic, newPercent, targetTopic.confidenceLevel)
                }
            }
        }
    }

    fun deleteTask(task: StudyTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // --- Reminder operations ---
    fun addReminder(title: String, type: String, subject: String, dateTime: Long, prepStatus: String = "Not Started", frequency: String = "Once", timing: String = "Morning (8 AM)") {
        viewModelScope.launch {
            repository.insertReminder(
                TestHomeworkReminder(
                    title = title,
                    type = type,
                    subject = subject,
                    dateTime = dateTime,
                    prepStatus = prepStatus,
                    frequency = frequency,
                    timing = timing
                )
            )
        }
    }

    fun toggleReminderCompletion(reminder: TestHomeworkReminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun updateReminderPrepStatus(reminder: TestHomeworkReminder, newStatus: String) {
        viewModelScope.launch {
            repository.updateReminder(reminder.copy(prepStatus = newStatus))
        }
    }

    fun deleteReminder(reminder: TestHomeworkReminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
        }
    }

    // --- Syllabus operations (Preparation Meter) ---
    fun updateSyllabusProgress(topic: SyllabusTopic, percent: Int, confidence: String) {
        viewModelScope.launch {
            repository.updateSyllabusTopic(
                topic.copy(
                    completionPercent = percent.coerceIn(0, 100),
                    confidenceLevel = confidence
                )
            )
        }
    }

    // --- Study Timer Actions ---
    fun setTimerPreset(seconds: Int, presetName: String) {
        pauseTimer()
        _timerTotalDuration.value = seconds
        _timerSeconds.value = seconds
    }

    fun setTimerSubject(subject: String) {
        _timerSubject.value = subject
    }

    fun toggleTimer() {
        if (_timerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        _timerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_timerSeconds.value > 0 && _timerRunning.value) {
                delay(1000)
                _timerSeconds.value -= 1
            }
            if (_timerSeconds.value == 0) {
                _timerRunning.value = false
                // Auto-create a practice task when timer finishes to reward student!
                addTask(
                    title = "Completed study sprint in ${timerSubject.value}",
                    subject = timerSubject.value,
                    category = "Practice",
                    notes = "Timer completed for focus period."
                )
            }
        }
    }

    fun pauseTimer() {
        _timerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _timerSeconds.value = _timerTotalDuration.value
    }

    // --- AI Doubt Support ---
    fun askDoubt(question: String, subject: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _doubtLoading.value = true
            _currentDoubtAnswer.value = "Consulting AcademiCoach Kota Faculty..."
            
            val solution = repository.solveDoubt(question)
            
            _currentDoubtAnswer.value = solution
            _doubtLoading.value = false
            
            // Save to historical logs
            repository.insertDoubt(
                DoubtHistory(
                    question = question,
                    answer = solution,
                    subject = subject
                )
            )
        }
    }

    fun clearDoubtAnswer() {
        _currentDoubtAnswer.value = null
    }

    fun clearDoubtHistory() {
        viewModelScope.launch {
            repository.clearDoubtHistory()
        }
    }
}

// Simple Factory for ViewModel instantiation
class StudyViewModelFactory(private val repository: StudyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
