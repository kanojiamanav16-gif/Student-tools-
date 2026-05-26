package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DoubtHistory
import com.example.data.model.StudyTask
import com.example.data.model.SyllabusTopic
import com.example.data.model.TestHomeworkReminder
import com.example.ui.viewmodel.StudyViewModel
import java.text.SimpleDateFormat
import java.util.*

// Sophisticated Dark Style Palette
object StudyPalette {
    val MidnightBg = Color(0xFF1C1B1F)  // Sophisticated Dark background
    val CardBg = Color(0xFF2B2930)      // Sleek deep gray cards
    val AccentTeal = Color(0xFFD0BCFF)  // Main elegant purple accent
    val AccentCoral = Color(0xFFFFB4AB) // Warning coral for tests & HW alerts
    val ColorSuccess = Color(0xFF10B981)// Emerald green positive feedback
    val ColorAmber = Color(0xFFFFCC00)  // Warm accent alerts
    val MutedText = Color(0xFFCAC4D0)   // High scannability text
    val CardBorder = Color(0xFF49454F)  // Precise subtle borders
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyAppMainScreen(viewModel: StudyViewModel) {
    var activeTab by remember { mutableStateOf(0) } // 0: Home/Meter, 1: Tasks, 2: Reminders, 3: Prep (Syllabus), 4: AI doubts
    val activeExam by viewModel.examFilter.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize().background(StudyPalette.MidnightBg),
        bottomBar = {
            NavigationBar(
                containerColor = StudyPalette.CardBg,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("app_bottom_bar")
            ) {
                val items = listOf(
                    Triple(0, Icons.Default.Dashboard, "Dash"),
                    Triple(1, Icons.Default.CheckCircle, "Tasks"),
                    Triple(2, Icons.Default.Notifications, "Alerts"),
                    Triple(3, Icons.Default.Timeline, "Prep"),
                    Triple(4, Icons.Default.AutoAwesome, "AI Doubt")
                )
                items.forEach { (index, icon, label) ->
                    NavigationBarItem(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(24.dp)) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = StudyPalette.AccentTeal,
                            selectedTextColor = StudyPalette.AccentTeal,
                            indicatorColor = StudyPalette.MidnightBg.copy(alpha = 0.5f),
                            unselectedIconColor = StudyPalette.MutedText,
                            unselectedTextColor = StudyPalette.MutedText
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(StudyPalette.MidnightBg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Main Modern Header
                HeaderWing(
                    activeExam = activeExam,
                    onExamChanged = { viewModel.setExamFilter(it) }
                )

                Divider(color = StudyPalette.CardBorder, thickness = 1.dp)

                // Screens Router
                Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "tab_transition"
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> DashboardScreen(viewModel)
                            1 -> TasksScreen(viewModel)
                            2 -> RemindersScreen(viewModel)
                            3 -> SyllabusScreen(viewModel)
                            4 -> AIDoubtsScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderWing(
    activeExam: String,
    onExamChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudyPalette.CardBg)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "ASPIRANT MODE",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                    color = StudyPalette.AccentTeal,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "JEE / NEET Aspirant Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            // High Contrast Target Toggle Switches
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, StudyPalette.CardBorder, RoundedCornerShape(24.dp))
                    .background(StudyPalette.MidnightBg)
                    .padding(2.dp)
            ) {
                listOf("JEE", "NEET").forEach { exam ->
                    val isSelected = activeExam == exam
                    val btnBg = if (isSelected) {
                        if (exam == "JEE") StudyPalette.AccentTeal.copy(alpha = 0.2f)
                        else StudyPalette.AccentCoral.copy(alpha = 0.2f)
                    } else Color.Transparent

                    val btnBorderColor = if (isSelected) {
                        if (exam == "JEE") StudyPalette.AccentTeal else StudyPalette.AccentCoral
                    } else Color.Transparent

                    val textColor = if (isSelected) Color.White else StudyPalette.MutedText

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(22.dp))
                            .background(btnBg)
                            .border(1.dp, btnBorderColor, RoundedCornerShape(22.dp))
                            .clickable { onExamChanged(exam) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                            .testTag("toggle_${exam.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = exam,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD SCREEN
// ==========================================
@Composable
fun DashboardScreen(viewModel: StudyViewModel) {
    val activeExam by viewModel.examFilter.collectAsStateWithLifecycle()
    val prepProgress by viewModel.preparationProgress.collectAsStateWithLifecycle()
    val remainingTasks by viewModel.tasks.collectAsStateWithLifecycle()
    val activeRemindersList by viewModel.activeReminders.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val timerRunning by viewModel.timerRunning.collectAsStateWithLifecycle()
    val timerSubject by viewModel.timerSubject.collectAsStateWithLifecycle()

    val pendingRemCount = activeRemindersList.size
    val incompleteTaskCount = remainingTasks.filter { !it.isCompleted }.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Motivation Greeting Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
                border = BorderStroke(1.dp, StudyPalette.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "TODAY'S MISSION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudyPalette.MutedText,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (activeExam == "JEE") "Aim high for IIT seats!" else "Secure that elite Medical AIIMS seat!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Unlock progress by editing your Master Syllabus check in the Prep tab.",
                            fontSize = 12.sp,
                            color = StudyPalette.MutedText,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (activeExam == "JEE") StudyPalette.AccentTeal.copy(alpha = 0.15f)
                                else StudyPalette.AccentCoral.copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activeExam == "JEE") Icons.Default.School else Icons.Default.MedicalServices,
                            contentDescription = "Target icon",
                            tint = if (activeExam == "JEE") StudyPalette.AccentTeal else StudyPalette.AccentCoral,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        // Preparation Meter Widget (Custom Visual Circular Gauge)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
                border = BorderStroke(1.dp, StudyPalette.CardBorder),
                modifier = Modifier.fillMaxWidth().testTag("prep_meter_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$activeExam PREPARATION METER",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(170.dp)
                    ) {
                        // Drawing custom modern gauge with Canvas behind
                        val primaryColor = if (activeExam == "JEE") StudyPalette.AccentTeal else StudyPalette.AccentCoral
                        val progressMultiplier = prepProgress / 100f

                        Canvas(modifier = Modifier.size(150.dp)) {
                            // Track circle
                            drawArc(
                                color = StudyPalette.CardBorder,
                                startAngle = -220f,
                                sweepAngle = 260f,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Content value arc
                            drawArc(
                                color = primaryColor,
                                startAngle = -220f,
                                sweepAngle = 260f * progressMultiplier,
                                useCenter = false,
                                style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$prepProgress%",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "Syllabus Complete",
                                fontSize = 11.sp,
                                color = StudyPalette.MutedText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Status Indicator
                    val prepLabel = when {
                        prepProgress < 20 -> "Beginner - Start studying"
                        prepProgress < 50 -> "Intermediate Focus - Build consistency"
                        prepProgress < 85 -> "Advanced level - Solving Kota sheets!"
                        else -> "Exam Ready! Bring on the grand tests"
                    }
                    Text(
                        text = prepLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeExam == "JEE") StudyPalette.AccentTeal else StudyPalette.AccentCoral,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Timer Panel Overview
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
                border = BorderStroke(1.dp, StudyPalette.CardBorder),
                modifier = Modifier.fillMaxWidth().testTag("timer_dashboard_panel")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = "Timer",
                                tint = StudyPalette.AccentTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "FOCUS study timer",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Subject pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(StudyPalette.MidnightBg)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = timerSubject.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StudyPalette.AccentTeal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val mins = timerSeconds / 60
                            val secs = timerSeconds % 60
                            val timeStr = String.format("%02d:%02d", mins, secs)

                            Text(
                                text = timeStr,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                color = if (timerRunning) StudyPalette.ColorSuccess else Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (timerRunning) "Session in momentum" else "Timer paused",
                                fontSize = 12.sp,
                                color = StudyPalette.MutedText
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Subject Select Action Button
                            var showSubjectMenu by remember { mutableStateOf(false) }
                            Box {
                                FilledIconButton(
                                    onClick = { showSubjectMenu = true },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = StudyPalette.MidnightBg
                                    )
                                ) {
                                    Icon(Icons.Default.Book, contentDescription = "Select subject", tint = Color.White)
                                }

                                DropdownMenu(
                                    expanded = showSubjectMenu,
                                    onDismissRequest = { showSubjectMenu = false },
                                    modifier = Modifier.background(StudyPalette.CardBg)
                                ) {
                                    val subjects = if (activeExam == "JEE") listOf("Physics", "Chemistry", "Mathematics")
                                    else listOf("Physics", "Chemistry", "Biology")

                                    subjects.forEach { sub ->
                                        DropdownMenuItem(
                                            text = { Text(sub, color = Color.White) },
                                            onClick = {
                                                viewModel.setTimerSubject(sub)
                                                showSubjectMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Play/Pause Action Button
                            FilledIconButton(
                                onClick = { viewModel.toggleTimer() },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = if (timerRunning) StudyPalette.AccentCoral else StudyPalette.AccentTeal
                                ),
                                modifier = Modifier.testTag("timer_dashboard_toggle")
                            ) {
                                Icon(
                                    imageVector = if (timerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Toggle",
                                    tint = Color.White
                                )
                            }

                            // Reset Action Button
                            FilledIconButton(
                                onClick = { viewModel.resetTimer() },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = StudyPalette.MidnightBg
                                )
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Study Presets Controls
                    Text(
                        text = "PRESETS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudyPalette.MutedText,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val presets = listOf(
                            Pair("25m Pomodoro", 1500),
                            Pair("45m Sprint", 2700),
                            Pair("3h Exam Sim", 10800)
                        )
                        presets.forEach { (name, sec) ->
                            OutlinedButton(
                                onClick = { viewModel.setTimerPreset(sec, name) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, StudyPalette.CardBorder),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Overview Task/Reminder Stats Summary Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
                    border = BorderStroke(1.dp, StudyPalette.CardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("ACTIVE TASKS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudyPalette.MutedText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$incompleteTaskCount pending", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
                    border = BorderStroke(1.dp, StudyPalette.CardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("MOCK TESTS & HW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudyPalette.MutedText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$pendingRemCount alert", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StudyPalette.AccentCoral)
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. DAILY TO-DO TASKS SCREEN
// ==========================================
@Composable
fun TasksScreen(viewModel: StudyViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val activeExam by viewModel.examFilter.collectAsStateWithLifecycle()

    var showAddTaskDialog by remember { mutableStateOf(false) }

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskSubject by remember { mutableStateOf("Physics") }
    var newTaskCategory by remember { mutableStateOf("Practice") }
    var newTaskNotes by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Focus Tasks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = {
                        newTaskSubject = if (activeExam == "JEE") "Physics" else "Physics"
                        showAddTaskDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPalette.AccentTeal),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_task_launcher")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = "Empty",
                            tint = StudyPalette.MutedText,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No tasks added yet!", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Add tasks for HCV practicing, lectures revision, or Allen sheets study.",
                            color = StudyPalette.MutedText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks) { task ->
                        TaskItemRow(
                            task = task,
                            onToggle = { viewModel.toggleTaskCompletion(task) },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }

        // Add Task Dialog Form
        if (showAddTaskDialog) {
            AlertDialog(
                onDismissRequest = { showAddTaskDialog = false },
                title = { Text("Log JEE/NEET Task", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text("Task description") },
                            placeholder = { Text("e.g., Complete Rotational Mechanics Sheet 1") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth().testTag("task_title_input"),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = StudyPalette.MidnightBg,
                                unfocusedContainerColor = StudyPalette.MidnightBg
                            )
                        )

                        // Subject Selection Row
                        Text("Subject", color = StudyPalette.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        val activeSubjects = if (activeExam == "JEE") listOf("Physics", "Chemistry", "Mathematics", "General")
                        else listOf("Physics", "Chemistry", "Biology", "General")

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeSubjects.forEach { sub ->
                                val selected = newTaskSubject == sub
                                FilterChip(
                                    selected = selected,
                                    onClick = { newTaskSubject = sub },
                                    label = { Text(sub) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudyPalette.AccentTeal.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = StudyPalette.MutedText
                                    )
                                )
                            }
                        }

                        // Category Chips List
                        Text("Task Category", color = StudyPalette.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        val categories = listOf("Lecture", "Self-study", "Practice", "Revision")
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                val selected = newTaskCategory == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { newTaskCategory = cat },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudyPalette.AccentTeal.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = StudyPalette.MutedText
                                    )
                                )
                            }
                        }

                        // Short Notes Text Field
                        OutlinedTextField(
                            value = newTaskNotes,
                            onValueChange = { newTaskNotes = it },
                            label = { Text("Supporting study notes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = StudyPalette.MidnightBg,
                                unfocusedContainerColor = StudyPalette.MidnightBg
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTaskTitle.isNotBlank()) {
                                viewModel.addTask(
                                    title = newTaskTitle,
                                    subject = newTaskSubject,
                                    category = newTaskCategory,
                                    notes = newTaskNotes
                                )
                                // Clear inputs
                                newTaskTitle = ""
                                newTaskNotes = ""
                                showAddTaskDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPalette.AccentTeal),
                        modifier = Modifier.testTag("task_submit_button")
                    ) {
                        Text("Save Task")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddTaskDialog = false }) {
                        Text("Cancel", color = StudyPalette.MutedText)
                    }
                },
                containerColor = StudyPalette.CardBg
            )
        }
    }
}

@Composable
fun TaskItemRow(
    task: StudyTask,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
        border = BorderStroke(1.dp, StudyPalette.CardBorder),
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox completion marker
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = StudyPalette.ColorSuccess,
                    uncheckedColor = StudyPalette.MutedText
                ),
                modifier = Modifier.testTag("checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) StudyPalette.MutedText else Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val subColor = when (task.subject.lowercase()) {
                        "physics" -> Color(0xFF3B82F6)
                        "chemistry" -> Color(0xFFA855F7)
                        "mathematics" -> Color(0xFFF59E0B)
                        "biology" -> Color(0xFF10B981)
                        else -> StudyPalette.MutedText
                    }

                    // Subject Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(subColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(task.subject, fontSize = 10.sp, color = subColor, fontWeight = FontWeight.Bold)
                    }

                    // Category Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudyPalette.MidnightBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(task.category, fontSize = 10.sp, color = StudyPalette.MutedText, fontWeight = FontWeight.Medium)
                    }
                }

                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.notes,
                        fontSize = 12.sp,
                        color = StudyPalette.MutedText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete task", tint = StudyPalette.AccentCoral)
            }
        }
    }
}


// ==========================================
// 3. TESTS AND HOMEWORK REMINDERS SCREEN
// ==========================================
@Composable
fun RemindersScreen(viewModel: StudyViewModel) {
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val activeExam by viewModel.examFilter.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddReminderDialog by remember { mutableStateOf(false) }

    var newRemTitle by remember { mutableStateOf("") }
    var newRemType by remember { mutableStateOf("Test") } // "Test" or "Homework"
    var newRemSubject by remember { mutableStateOf("Physics") }
    var newRemDaysAhead by remember { mutableStateOf(1) } // study deadlines options
    var newRemFrequency by remember { mutableStateOf("Once") } // "Once", "Daily", "Weekly"
    var newRemTiming by remember { mutableStateOf("Morning (8 AM)") } // "Morning (8 AM)", "Afternoon (2 PM)", "Night (8 PM)"

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HW & Test Alerts",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = {
                        newRemSubject = "Physics"
                        newRemFrequency = "Once"
                        newRemTiming = "Morning (8 AM)"
                        showAddReminderDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StudyPalette.AccentCoral),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_reminder_launcher")
                ) {
                    Icon(Icons.Default.AddAlert, contentDescription = "Add alert")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Alert", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // AI SUGGESTED REVIEW PLANNER PANEL
            val incompleteReminders = reminders.filter { !it.isCompleted }
            if (incompleteReminders.isNotEmpty()) {
                val nextUrgent = incompleteReminders.minByOrNull { it.dateTime }
                nextUrgent?.let { rem ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
                        border = BorderStroke(1.dp, StudyPalette.AccentTeal.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .testTag("ai_review_planner_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = StudyPalette.AccentTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI OPTIMAL REVIEW SUGGESTOR",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StudyPalette.AccentTeal,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Based on upcoming ${rem.type} '${rem.title}' due in " +
                                        "${((rem.dateTime - System.currentTimeMillis()) / (24L * 60L * 60L * 1000L)).coerceAtLeast(0)} days:",
                                fontSize = 12.sp,
                                color = StudyPalette.MutedText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Optimal Study Revision: Revise ${rem.subject} topic concepts during the ${rem.timing} slot (${rem.frequency}). This fills your identified scheduling gaps best.",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.addTask(
                                        title = "Revise ${rem.subject} for upcoming ${rem.type} (${rem.title})",
                                        subject = rem.subject,
                                        category = "Revision",
                                        notes = "AI optimization suggestion based on upcoming deadlines."
                                    )
                                    Toast.makeText(context, "Added optimal revision task to Checklist!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = StudyPalette.AccentTeal),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.AddTask, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Book Revision Slot", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF311060))
                            }
                        }
                    }
                }
            }

            if (reminders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Empty",
                            tint = StudyPalette.MutedText,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No test or homework reminders", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Log upcoming Part Syllabus Tests, Mock AIIMS/IIT papers, or Coach worksheets due.",
                            color = StudyPalette.MutedText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(reminders) { reminder ->
                        ReminderItemRow(
                            reminder = reminder,
                            onToggle = { viewModel.toggleReminderCompletion(reminder) },
                            onDelete = { viewModel.deleteReminder(reminder) },
                            onPrepStatusChanged = { newStatus -> viewModel.updateReminderPrepStatus(reminder, newStatus) }
                        )
                    }
                }
            }
        }

        // Add Reminder Dialog Screen
        if (showAddReminderDialog) {
            AlertDialog(
                onDismissRequest = { showAddReminderDialog = false },
                title = { Text("Log HW or Exam Alert", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newRemTitle,
                            onValueChange = { newRemTitle = it },
                            label = { Text("Title") },
                            placeholder = { Text("e.g., Allen All India Mock Test #2") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth().testTag("reminder_title_input"),
                            colors = TextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = StudyPalette.MidnightBg,
                                unfocusedContainerColor = StudyPalette.MidnightBg
                            )
                        )

                        // Type Choice row (Test vs Homework)
                        Text("Alert Type", color = StudyPalette.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("Test", "Homework").forEach { type ->
                                val selected = newRemType == type
                                FilterChip(
                                    selected = selected,
                                    onClick = { newRemType = type },
                                    label = { Text(type) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (type == "Test") StudyPalette.AccentCoral.copy(alpha = 0.2f) else StudyPalette.ColorSuccess.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = StudyPalette.MutedText
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Subject Checklist Selection
                        Text("Subject", color = StudyPalette.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        val activeSubjects = if (activeExam == "JEE") listOf("Physics", "Chemistry", "Mathematics")
                        else listOf("Physics", "Chemistry", "Biology")

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeSubjects.forEach { sub ->
                                val selected = newRemSubject == sub
                                FilterChip(
                                    selected = selected,
                                    onClick = { newRemSubject = sub },
                                    label = { Text(sub) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudyPalette.AccentCoral.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = StudyPalette.MutedText
                                    )
                                )
                            }
                        }

                        // Target Time/Duration Select Quick Slider
                        Text("Target due date / countdown", color = StudyPalette.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Pair("Tomorrow", 1),
                                Pair("In 3 Days", 3),
                                Pair("In 1 Week", 7),
                                Pair("In 2 Weeks", 14)
                            ).forEach { (display, days) ->
                                val selected = newRemDaysAhead == days
                                FilterChip(
                                    selected = selected,
                                    onClick = { newRemDaysAhead = days },
                                    label = { Text(display) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudyPalette.MidnightBg,
                                        selectedLabelColor = Color.White,
                                        labelColor = StudyPalette.MutedText
                                    )
                                )
                            }
                        }

                        // CUSTOMIZABLE FREQUENCY
                        Text("Reminder Frequency", color = StudyPalette.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("Once", "Daily", "Weekly").forEach { freq ->
                                val selected = newRemFrequency == freq
                                FilterChip(
                                    selected = selected,
                                    onClick = { newRemFrequency = freq },
                                    label = { Text(freq) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudyPalette.AccentTeal.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = StudyPalette.MutedText
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // CUSTOMIZABLE TIMING
                        Text("Preferred Slot Timing", color = StudyPalette.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Morning (8 AM)", "Afternoon (2 PM)", "Night (8 PM)").forEach { slot ->
                                val selected = newRemTiming == slot
                                FilterChip(
                                    selected = selected,
                                    onClick = { newRemTiming = slot },
                                    label = { Text(slot) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = StudyPalette.AccentTeal.copy(alpha = 0.2f),
                                        selectedLabelColor = Color.White,
                                        labelColor = StudyPalette.MutedText
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newRemTitle.isNotBlank()) {
                                val simulatedTime = System.currentTimeMillis() + (newRemDaysAhead * 24L * 60L * 60L * 1000L)
                                viewModel.addReminder(
                                    title = newRemTitle,
                                    type = newRemType,
                                    subject = newRemSubject,
                                    dateTime = simulatedTime,
                                    frequency = newRemFrequency,
                                    timing = newRemTiming
                                )
                                // Clear inputs
                                newRemTitle = ""
                                showAddReminderDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPalette.AccentCoral),
                        modifier = Modifier.testTag("reminder_submit_button")
                    ) {
                        Text("Add Alert")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddReminderDialog = false }) {
                        Text("Cancel", color = StudyPalette.MutedText)
                    }
                },
                containerColor = StudyPalette.CardBg
            )
        }
    }
}

@Composable
fun ReminderItemRow(
    reminder: TestHomeworkReminder,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onPrepStatusChanged: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
        border = BorderStroke(1.dp, StudyPalette.CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = reminder.isCompleted,
                        onCheckedChange = { onToggle() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = StudyPalette.ColorSuccess,
                            uncheckedColor = StudyPalette.MutedText
                        ),
                        modifier = Modifier.testTag("reminder_checkbox_${reminder.id}")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = reminder.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reminder.isCompleted) StudyPalette.MutedText else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val dateFormat = SimpleDateFormat("EE, dd MMM", Locale.getDefault())
                        val displayDate = dateFormat.format(Date(reminder.dateTime))
                        Text(
                            text = "Due: $displayDate at ${reminder.timing} (${reminder.frequency})",
                            fontSize = 12.sp,
                            color = StudyPalette.MutedText
                        )
                    }
                }

                // Delete Action
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StudyPalette.AccentCoral)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Secondary tags Info
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val typeBadgeBg = if (reminder.type == "Test") StudyPalette.AccentCoral.copy(alpha = 0.15f)
                    else StudyPalette.ColorAmber.copy(alpha = 0.15f)

                    val typeBadgeColor = if (reminder.type == "Test") StudyPalette.AccentCoral else StudyPalette.ColorAmber

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(typeBadgeBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(reminder.type.uppercase(), fontSize = 9.sp, color = typeBadgeColor, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudyPalette.MidnightBg)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(reminder.subject, fontSize = 9.sp, color = StudyPalette.MutedText, fontWeight = FontWeight.Bold)
                    }
                }

                // Preparation status indicator dropdown / toggle button
                var showStatusMenu by remember { mutableStateOf(false) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PREP STATE: ",
                        fontSize = 11.sp,
                        color = StudyPalette.MutedText,
                        fontWeight = FontWeight.Bold
                    )
                    Box {
                        val stateColor = when (reminder.prepStatus) {
                            "Ready" -> StudyPalette.ColorSuccess
                            "Preparing" -> StudyPalette.ColorAmber
                            else -> StudyPalette.AccentCoral
                        }
                        Text(
                            text = reminder.prepStatus.uppercase(),
                            fontSize = 11.sp,
                            color = stateColor,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .border(1.dp, stateColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .clickable { showStatusMenu = true }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )

                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false },
                            modifier = Modifier.background(StudyPalette.CardBg)
                        ) {
                            listOf("Not Started", "Preparing", "Ready").forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status, color = Color.White) },
                                    onClick = {
                                        onPrepStatusChanged(status)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. PREPARATION METER & SYLLABUS TRACKER SCREEN
// ==========================================
@Composable
fun SyllabusScreen(viewModel: StudyViewModel) {
    val examFilter by viewModel.examFilter.collectAsStateWithLifecycle()
    val syllabusList by viewModel.filteredSyllabus.collectAsStateWithLifecycle()
    val globalProgress by viewModel.preparationProgress.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Preparation Tracker Top Hub
        Card(
            colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
            border = BorderStroke(1.dp, StudyPalette.CardBorder),
            modifier = Modifier.fillMaxWidth().testTag("syllabus_progress_hub_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SUBJECT PREPARATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudyPalette.AccentTeal)
                        Text("$examFilter Syllabus Index", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(StudyPalette.MidnightBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$globalProgress%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = StudyPalette.ColorSuccess
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = globalProgress / 100f,
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (examFilter == "JEE") StudyPalette.AccentTeal else StudyPalette.AccentCoral,
                    trackColor = StudyPalette.MidnightBg
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Checking off subtopics recalculates your global Preparation Meter globally.",
                    color = StudyPalette.MutedText,
                    fontSize = 11.sp
                )

                HorizontalDivider(color = StudyPalette.CardBorder, modifier = Modifier.padding(vertical = 12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Link Task Completion to Progress",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Checking off a to-do task advances topic preparation by +10%",
                            fontSize = 11.sp,
                            color = StudyPalette.MutedText
                        )
                    }
                    val linkTasks by viewModel.linkCompletedTasksToProgress.collectAsStateWithLifecycle()
                    Switch(
                        checked = linkTasks,
                        onCheckedChange = { viewModel.setLinkCompletedTasksToProgress(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = StudyPalette.ColorSuccess,
                            checkedTrackColor = StudyPalette.ColorSuccess.copy(alpha = 0.4f),
                            uncheckedThumbColor = StudyPalette.MutedText,
                            uncheckedTrackColor = StudyPalette.MidnightBg
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Syllabus Scrollable Checklist
        Text(
            text = "CHAPTER INDEX",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = StudyPalette.MutedText,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (syllabusList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = StudyPalette.AccentTeal)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(syllabusList) { topic ->
                    SyllabusTopicRow(
                        topic = topic,
                        onUpdate = { percent, confidence ->
                            viewModel.updateSyllabusProgress(topic, percent, confidence)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SyllabusTopicRow(
    topic: SyllabusTopic,
    onUpdate: (Int, String) -> Unit
) {
    var sliderValue by remember { mutableStateOf(topic.completionPercent.toFloat()) }
    LaunchedEffect(topic.completionPercent) {
        sliderValue = topic.completionPercent.toFloat()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
        border = BorderStroke(1.dp, StudyPalette.CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.topicName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${topic.subject} • ${topic.category}",
                        fontSize = 11.sp,
                        color = StudyPalette.MutedText,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Confidence Chip Selector dropdown
                var showConfidenceMenu by remember { mutableStateOf(false) }
                val confColor = when (topic.confidenceLevel.lowercase()) {
                    "high" -> StudyPalette.ColorSuccess
                    "medium" -> StudyPalette.ColorAmber
                    else -> StudyPalette.AccentCoral
                }

                Box {
                    Text(
                        text = "CONFIDENCE: ${topic.confidenceLevel.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = confColor,
                        modifier = Modifier
                            .background(confColor.copy(alpha = 0.12f))
                            .border(1.dp, confColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .clickable { showConfidenceMenu = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    DropdownMenu(
                        expanded = showConfidenceMenu,
                        onDismissRequest = { showConfidenceMenu = false },
                        modifier = Modifier.background(StudyPalette.CardBg)
                    ) {
                        listOf("Low", "Medium", "High").forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level, color = Color.White) },
                                onClick = {
                                    onUpdate(topic.completionPercent, level)
                                    showConfidenceMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Percentage Slider interaction row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "COMPLETION:",
                    fontSize = 11.sp,
                    color = StudyPalette.MutedText,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Custom Slider Bar mapping
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    onValueChangeFinished = {
                        onUpdate(sliderValue.toInt(), topic.confidenceLevel)
                    },
                    valueRange = 0f..100f,
                    steps = 9,
                    modifier = Modifier.weight(1f).testTag("slider_${topic.id}"),
                    colors = SliderDefaults.colors(
                        thumbColor = StudyPalette.AccentTeal,
                        activeTrackColor = StudyPalette.AccentTeal,
                        inactiveTrackColor = StudyPalette.MidnightBg
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${sliderValue.toInt()}%",
                    fontSize = 13.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}


// ==========================================
// 5. AI DOUBT SUPPORT SCREEN
// ==========================================
@Composable
fun AIDoubtsScreen(viewModel: StudyViewModel) {
    val doubtAnswer by viewModel.currentDoubtAnswer.collectAsStateWithLifecycle()
    val isLoading by viewModel.doubtLoading.collectAsStateWithLifecycle()
    val doubtHistory by viewModel.doubtHistory.collectAsStateWithLifecycle()
    val activeExam by viewModel.examFilter.collectAsStateWithLifecycle()

    var userCustomQuestion by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Physics") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val context = LocalContext.current

    // Micro prompt suggestions for cracking competitive exams
    val prompts = if (activeExam == "JEE") listOf(
        Pair("Physics", "Differentiate Angular momentum conservation & Torque with an equation."),
        Pair("Chemistry", "Demonstrate Carbocation intermediate stability in electrophilic addition."),
        Pair("Maths", "Give shortcut tricks to solve Area under Curves integrations in IIT exams.")
    ) else listOf(
        Pair("Physics", "Explain Kirchhoff's laws applied to complex Wheatstone bridge loops simply."),
        Pair("Chemistry", "Explain Markovnikov vs Anti-Markovnikov rules with hydroboration-oxidation examples."),
        Pair("Biology", "Explain the transport of oxygen and carbon dioxide in human respiration cycle step-by-step.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Instructor Header info
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
                border = BorderStroke(1.dp, StudyPalette.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(StudyPalette.AccentTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = StudyPalette.AccentTeal)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("KOTA COACHING CHAT AI", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StudyPalette.AccentTeal, letterSpacing = 1.sp)
                        Text("Doubt Solver & Support", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Custom Question Input Area
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
                border = BorderStroke(1.dp, StudyPalette.CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "SOLVE AN ACADEMIC DOUBT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudyPalette.MutedText,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Subject Select Tags
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val subjects = if (activeExam == "JEE") listOf("Physics", "Chemistry", "Mathematics", "General")
                        else listOf("Physics", "Chemistry", "Biology", "General")

                        subjects.forEach { sub ->
                            val selected = selectedSubject == sub
                            FilterChip(
                                selected = selected,
                                onClick = { selectedSubject = sub },
                                label = { Text(sub) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudyPalette.AccentTeal.copy(alpha = 0.2f),
                                    selectedLabelColor = Color.White,
                                    labelColor = StudyPalette.MutedText
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = userCustomQuestion,
                        onValueChange = { userCustomQuestion = it },
                        placeholder = { Text("Ask AcademiCoach... (e.g., Calculate acceleration when cylinder rolls down an incline plane...)") },
                        modifier = Modifier.fillMaxWidth().height(110.dp).testTag("doubt_input_field"),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (userCustomQuestion.isNotBlank()) {
                                keyboardController?.hide()
                                viewModel.askDoubt(userCustomQuestion, selectedSubject)
                            }
                        }),
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = StudyPalette.MidnightBg,
                            unfocusedContainerColor = StudyPalette.MidnightBg
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (userCustomQuestion.isNotBlank()) {
                                keyboardController?.hide()
                                viewModel.askDoubt(userCustomQuestion, selectedSubject)
                            } else {
                                Toast.makeText(context, "Doubt textbox empty!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudyPalette.AccentTeal),
                        modifier = Modifier.fillMaxWidth().testTag("doubt_submit_button"),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask Coach Solution", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Suggestions Quick Prompts
        item {
            Column {
                Text(
                    text = "QUICK STUDY PROMPTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudyPalette.MutedText,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    prompts.forEach { (sub, text) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg.copy(alpha = 0.6f)),
                            border = BorderStroke(1.dp, StudyPalette.CardBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSubject = sub
                                    userCustomQuestion = text
                                    viewModel.askDoubt(text, sub)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Lightbulb, contentDescription = "Idea", tint = StudyPalette.ColorAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "[$sub] $text",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Active Solution Output panel
        doubtAnswer?.let { answer ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg),
                    border = BorderStroke(1.dp, StudyPalette.CardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("ai_solution_panel")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = StudyPalette.AccentTeal)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Académie Solution:", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Row {
                                // Copy answer button
                                IconButton(onClick = {
                                    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clipData = ClipData.newPlainText("AcademiCoach Response", answer)
                                    clipboardManager.setPrimaryClip(clipData)
                                    Toast.makeText(context, "Solution copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy solution", tint = Color.White, modifier = Modifier.size(20.dp))
                                }

                                IconButton(onClick = { viewModel.clearDoubtAnswer() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = StudyPalette.AccentCoral, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = answer,
                            fontSize = 14.sp,
                            color = Color.White,
                            lineHeight = 20.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Doubt History Logs list
        if (doubtHistory.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STUDIED RECORDED DOUBTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudyPalette.MutedText,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "CLEAR ALL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudyPalette.AccentCoral,
                        modifier = Modifier.clickable { viewModel.clearDoubtHistory() }
                    )
                }
            }

            items(doubtHistory) { item ->
                HistoryDoubtItem(item = item, onSelect = {
                    selectedSubject = item.subject
                    userCustomQuestion = item.question
                    viewModel.askDoubt(item.question, item.subject)
                })
            }
        }
    }
}

@Composable
fun HistoryDoubtItem(
    item: DoubtHistory,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StudyPalette.CardBg.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, StudyPalette.CardBorder),
        modifier = Modifier.fillMaxWidth().clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StudyPalette.MidnightBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(item.subject.uppercase(), fontSize = 9.sp, color = StudyPalette.AccentTeal, fontWeight = FontWeight.Bold)
                }

                val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                Text(df.format(Date(item.timestamp)), fontSize = 10.sp, color = StudyPalette.MutedText)
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = item.question, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = item.answer, fontSize = 12.sp, color = StudyPalette.MutedText, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
