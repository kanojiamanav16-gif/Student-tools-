package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.local.StudyTaskDao
import com.example.data.local.ReminderDao
import com.example.data.local.SyllabusDao
import com.example.data.local.DoubtDao
import com.example.data.model.StudyTask
import com.example.data.model.TestHomeworkReminder
import com.example.data.model.SyllabusTopic
import com.example.data.model.DoubtHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StudyRepository(
    private val studyTaskDao: StudyTaskDao,
    private val reminderDao: ReminderDao,
    private val syllabusDao: SyllabusDao,
    private val doubtDao: DoubtDao
) {
    val allTasks: Flow<List<StudyTask>> = studyTaskDao.getAllTasks()
    val allReminders: Flow<List<TestHomeworkReminder>> = reminderDao.getAllReminders()
    val activeReminders: Flow<List<TestHomeworkReminder>> = reminderDao.getActiveReminders()
    val allSyllabus: Flow<List<SyllabusTopic>> = syllabusDao.getAllSyllabus()
    val doubtHistory: Flow<List<DoubtHistory>> = doubtDao.getDoubtHistory()

    fun getSyllabusByExam(examType: String): Flow<List<SyllabusTopic>> =
        syllabusDao.getSyllabusByExam(examType)

    // Task operations
    suspend fun insertTask(task: StudyTask) = studyTaskDao.insertTask(task)
    suspend fun updateTask(task: StudyTask) = studyTaskDao.updateTask(task)
    suspend fun deleteTask(task: StudyTask) = studyTaskDao.deleteTask(task)
    suspend fun deleteTaskById(id: Int) = studyTaskDao.deleteTaskById(id)

    // Reminder operations
    suspend fun insertReminder(reminder: TestHomeworkReminder) = reminderDao.insertReminder(reminder)
    suspend fun updateReminder(reminder: TestHomeworkReminder) = reminderDao.updateReminder(reminder)
    suspend fun deleteReminder(reminder: TestHomeworkReminder) = reminderDao.deleteReminder(reminder)
    suspend fun deleteReminderById(id: Int) = reminderDao.deleteReminderById(id)

    // Syllabus operations
    suspend fun updateSyllabusTopic(topic: SyllabusTopic) = syllabusDao.updateSyllabusTopic(topic)

    // Doubt History operations
    suspend fun insertDoubt(doubt: DoubtHistory) = doubtDao.insertDoubt(doubt)
    suspend fun clearDoubtHistory() = doubtDao.clearHistory()

    // Database pre-population check
    suspend fun checkAndPrepopulateSyllabus() {
        val count = syllabusDao.getCount()
        if (count == 0) {
            val list = mutableListOf<SyllabusTopic>()

            // --- JEE Syllabus Pre-population ---
            // Physics
            list.add(SyllabusTopic(subject = "Physics", examType = "JEE", category = "Mechanics", topicName = "Kinematics & Laws of Motion"))
            list.add(SyllabusTopic(subject = "Physics", examType = "JEE", category = "Mechanics", topicName = "Work, Energy & Power"))
            list.add(SyllabusTopic(subject = "Physics", examType = "JEE", category = "Mechanics", topicName = "Rotational Dynamics"))
            list.add(SyllabusTopic(subject = "Physics", examType = "JEE", category = "Thermodynamics", topicName = "Kinetic Theory & Thermodynamics"))
            list.add(SyllabusTopic(subject = "Physics", examType = "JEE", category = "Electrodynamics", topicName = "Electrostatics & Capacitance"))
            list.add(SyllabusTopic(subject = "Physics", examType = "JEE", category = "Electrodynamics", topicName = "Current Electricity & Magnetism"))
            list.add(SyllabusTopic(subject = "Physics", examType = "JEE", category = "Optics & Modern", topicName = "Semiconductor Electronics & Modern Physics"))

            // Chemistry
            list.add(SyllabusTopic(subject = "Chemistry", examType = "JEE", category = "Physical", topicName = "Some Basic Concepts & Chemical Bonding"))
            list.add(SyllabusTopic(subject = "Chemistry", examType = "JEE", category = "Physical", topicName = "Chemical Kinetics & Thermodynamics"))
            list.add(SyllabusTopic(subject = "Chemistry", examType = "JEE", category = "Inorganic", topicName = "p-Block Elements & Coordination Compounds"))
            list.add(SyllabusTopic(subject = "Chemistry", examType = "JEE", category = "Organic", topicName = "General Organic Chemistry (GOC)"))
            list.add(SyllabusTopic(subject = "Chemistry", examType = "JEE", category = "Organic", topicName = "Hydrocarbons & Carbonyls"))

            // Mathematics
            list.add(SyllabusTopic(subject = "Mathematics", examType = "JEE", category = "Algebra", topicName = "Matrices, Determinants & Theory of Equations"))
            list.add(SyllabusTopic(subject = "Mathematics", examType = "JEE", category = "Algebra", topicName = "Probability & Sequences and Series"))
            list.add(SyllabusTopic(subject = "Mathematics", examType = "JEE", category = "Calculus", topicName = "Limits, Continuity & Derivative Applications"))
            list.add(SyllabusTopic(subject = "Mathematics", examType = "JEE", category = "Calculus", topicName = "Definite & Indefinite Integration"))
            list.add(SyllabusTopic(subject = "Mathematics", examType = "JEE", category = "Coordinate Geometry", topicName = "Straight Lines, Circles & Conic Sections"))
            list.add(SyllabusTopic(subject = "Mathematics", examType = "JEE", category = "Vectors & 3D", topicName = "Vector Algebra & 3D Coordinate Geometry"))

            // --- NEET Syllabus Pre-population ---
            // Physics is similar for medical entrance
            list.add(SyllabusTopic(subject = "Physics", examType = "NEET", category = "Mechanics", topicName = "Laws of Motion & Gravitation"))
            list.add(SyllabusTopic(subject = "Physics", examType = "NEET", category = "Thermodynamics", topicName = "Properties of Matter & Thermodynamics"))
            list.add(SyllabusTopic(subject = "Physics", examType = "NEET", category = "Electrodynamics", topicName = "Electrostatics, Current & Magnetic Effects"))
            list.add(SyllabusTopic(subject = "Physics", examType = "NEET", category = "Optics & Modern", topicName = "Ray/Wave Optics, Dual Nature of Matter"))

            // Chemistry
            list.add(SyllabusTopic(subject = "Chemistry", examType = "NEET", category = "Physical", topicName = "Mole Concept, Equilibrium & Electrochemistry"))
            list.add(SyllabusTopic(subject = "Chemistry", examType = "NEET", category = "Inorganic", topicName = "Periodic Trends, Bonding & d-f Block"))
            list.add(SyllabusTopic(subject = "Chemistry", examType = "NEET", category = "Organic", topicName = "GOC, Hydrocarbons & Bio-Molecules"))

            // Biology
            list.add(SyllabusTopic(subject = "Biology", examType = "NEET", category = "Diversity & Organisation", topicName = "Plant/Animal Kingdom & Structure"))
            list.add(SyllabusTopic(subject = "Biology", examType = "NEET", category = "Cell Biology", topicName = "Cell: Unit of Life, Division & Biomolecules"))
            list.add(SyllabusTopic(subject = "Biology", examType = "NEET", category = "Plant Physiology", topicName = "Photosynthesis, Respiration & Growth"))
            list.add(SyllabusTopic(subject = "Biology", examType = "NEET", category = "Human Physiology", topicName = "Neural, Endocrine, Circulation & Digestion System"))
            list.add(SyllabusTopic(subject = "Biology", examType = "NEET", category = "Genetics & Evolution", topicName = "Principles of Inheritance & Molecular Basis"))
            list.add(SyllabusTopic(subject = "Biology", examType = "NEET", category = "Ecology & Biotech", topicName = "Biotechnology Principles & Ecosystem Dynamics"))

            syllabusDao.insertSyllabusList(list)
            Log.d("StudyRepository", "Successfully pre-populated JEE and NEET categories in syllabus")
        }
    }

    // Call Gemini API to solve student doubts
    suspend fun solveDoubt(prompt: String): String {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Unable to answer: Your API key is unconfigured. " +
                    "Please configure your real GEMINI_API_KEY in the Secrets panel in AI Studio UI to unlock expert AI Doubt Support!"
        }

        val systemPrompt = "You are 'AcademiCoach', a brilliant, encouraging, and supportive expert faculty for IIT-JEE and NEET-UG preparation, based out of Kota, India. " +
                "Your students are aspiring to crack extremely difficult, top-tier engineering and medical entries in India. " +
                "Solve the provided Physics, Chemistry, Mathematics, or Biology problem step-by-step. " +
                "Always explain the underlying concepts clearly with explanations and derivations if necessary. " +
                "MOST IMPORTANTLY, you MUST suggest specific reference books or materials standard for JEE/NEET prep (e.g., NCERT, HC Verma Concept of Physics, OP Tandon, Irodov, Morrison Boyd, Cengage, DC Pandey) and highlight the exact core syllabus topics to focus on. " +
                "Conclude with a very motivating, friendly Kota faculty coaching encouragement! Keep formatting neat and clear."

        val requestBody = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, requestBody)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (replyText.isNullOrBlank()) {
                "AcademiCoach: I couldn't generate a clear solution. Please rephrase your doubt or try again shortly!"
            } else {
                replyText
            }
        } catch (e: Exception) {
            "API Connection Error: ${e.localizedMessage}. Please double-check your connection and ensure your API key in the Secrets panel is valid."
        }
    }
}
