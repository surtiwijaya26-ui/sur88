package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import android.graphics.Bitmap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.UiState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    // Collect Flow state from ViewModel reactively
    val students by viewModel.students.collectAsStateWithLifecycle()
    val companies by viewModel.companies.collectAsStateWithLifecycle()
    val logbooks by viewModel.logbooks.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val targetRequiredHours by viewModel.targetRequiredHours.collectAsStateWithLifecycle()
    val milestoneNotifications by viewModel.milestoneNotifications.collectAsStateWithLifecycle()
    val activeMilestoneAlert by viewModel.activeMilestoneAlert.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf("dashboard") }

    // Dialog state controllers
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var showAddCompanyDialog by remember { mutableStateOf(false) }
    var showEditCompanyDialog by remember { mutableStateOf(false) }
    var showAddLogbookDialog by remember { mutableStateOf(false) }
    var showAutocratGuideDialog by remember { mutableStateOf(false) }
    var showEmailGeneratorDialog by remember { mutableStateOf(false) }
    var showPlacementEmailDialog by remember { mutableStateOf(false) }
    var showLogbookAnalysisDialog by remember { mutableStateOf(false) }

    // Selected items for direct actions
    var selectedStudentForEmail by remember { mutableStateOf<Student?>(null) }
    var selectedStudentForAnalysis by remember { mutableStateOf<Student?>(null) }
    var selectedCompanyForEdit by remember { mutableStateOf<Company?>(null) }
    var selectedCompanyForPlacementEmail by remember { mutableStateOf<Company?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "14",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "SimPKL DKV",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "SMKN 14 Kabupaten Tangerang",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAutocratGuideDialog = true },
                        modifier = Modifier.testTag("autocrat_guide_btn")
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Panduan Sheet & Apps Script Autocrat"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            ) {
                NavigationBarItem(
                    selected = currentTab == "dashboard",
                    onClick = { currentTab = "dashboard" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                    label = { Text("Mulai", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == "students",
                    onClick = { currentTab = "students" },
                    icon = { Icon(Icons.Default.Face, contentDescription = "Siswa") },
                    label = { Text("Siswa", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_students")
                )
                NavigationBarItem(
                    selected = currentTab == "logbooks",
                    onClick = { currentTab = "logbooks" },
                    icon = { Icon(Icons.Default.List, contentDescription = "Logbook") },
                    label = { Text("Logbook", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_logbooks")
                )
                NavigationBarItem(
                    selected = currentTab == "matchmaking",
                    onClick = { currentTab = "matchmaking" },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Matchmaker") },
                    label = { Text("Matchmaker", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_matchmaking")
                )
                NavigationBarItem(
                    selected = currentTab == "chatbot",
                    onClick = { currentTab = "chatbot" },
                    icon = { Icon(Icons.Default.Send, contentDescription = "Chatbot") },
                    label = { Text("Asisten AI", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    modifier = Modifier.testTag("nav_chatbot")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                "dashboard" -> DashboardTab(
                    viewModel = viewModel,
                    students = students,
                    logbooks = logbooks,
                    companies = companies,
                    onOpenAddCompany = { showAddCompanyDialog = true }
                )
                "students" -> StudentsTab(
                    viewModel = viewModel,
                    students = students,
                    companies = companies,
                    onOpenAddStudent = { showAddStudentDialog = true },
                    onOpenEmail = { student ->
                        selectedStudentForEmail = student
                        showEmailGeneratorDialog = true
                    },
                    onOpenAnalysis = { student ->
                        selectedStudentForAnalysis = student
                        showLogbookAnalysisDialog = true
                    },
                    onOpenAddCompany = { showAddCompanyDialog = true },
                    onEditCompany = { company ->
                        selectedCompanyForEdit = company
                        showEditCompanyDialog = true
                    },
                    onOpenPlacementEmail = { company ->
                        selectedCompanyForPlacementEmail = company
                        showPlacementEmailDialog = true
                    }
                )
                "logbooks" -> LogbooksTab(
                    viewModel = viewModel,
                    logbooks = logbooks,
                    students = students,
                    onOpenAddLogbook = { showAddLogbookDialog = true }
                )
                "matchmaking" -> MatchmakingTab(
                    viewModel = viewModel,
                    students = students,
                    companies = companies
                )
                "chatbot" -> ChatbotTab(
                    viewModel = viewModel,
                    chatMessages = chatMessages
                )
            }
        }
    }

    // --- Dialog Component Triggers ---

    if (showAddStudentDialog) {
        AddStudentDialog(
            companies = companies,
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { name, cls, status, skills, highlight, coId, coName ->
                viewModel.insertStudent(
                    Student(
                        name = name,
                        className = cls,
                        status = status,
                        skills = skills,
                        portfolioHighlight = highlight,
                        companyId = coId,
                        companyNameAssigned = coName
                    )
                )
                showAddStudentDialog = false
            }
        )
    }

    if (showAddCompanyDialog) {
        AddCompanyDialog(
            onDismiss = { showAddCompanyDialog = false },
            onConfirm = { name, industry, slots, email ->
                viewModel.insertCompany(
                    Company(name = name, industry = industry, slots = slots, emailHRD = email)
                )
                showAddCompanyDialog = false
            }
        )
    }

    if (showEditCompanyDialog && selectedCompanyForEdit != null) {
        EditCompanyDialog(
            company = selectedCompanyForEdit!!,
            onDismiss = {
                showEditCompanyDialog = false
                selectedCompanyForEdit = null
            },
            onConfirm = { updatedCompany ->
                viewModel.updateCompany(updatedCompany)
                showEditCompanyDialog = false
                selectedCompanyForEdit = null
            }
        )
    }

    if (showAddLogbookDialog) {
        AddLogbookDialog(
            students = students,
            onDismiss = { showAddLogbookDialog = false },
            onConfirm = { studentId, studentName, date, activity, tools, obstacle, solution, link, hours, photo ->
                viewModel.insertLogbook(
                    Logbook(
                        studentId = studentId,
                        studentName = studentName,
                        date = date,
                        activity = activity,
                        toolsUsed = tools,
                        obstacle = obstacle,
                        solution = solution,
                        projectLink = link,
                        hoursPerformed = hours,
                        photoUri = photo
                    )
                )
                showAddLogbookDialog = false
            }
        )
    }

    if (showAutocratGuideDialog) {
        AutocratGuideDialog(onDismiss = { showAutocratGuideDialog = false })
    }

    if (showEmailGeneratorDialog && selectedStudentForEmail != null) {
        EmailGeneratorDialog(
            student = selectedStudentForEmail!!,
            companies = companies,
            emailState = viewModel.emailState.collectAsStateWithLifecycle().value,
            onTrigger = { company, msg ->
                viewModel.triggerEmailGeneration(
                    student = selectedStudentForEmail!!,
                    companyName = company.name,
                    companyIndustry = company.industry,
                    customMessage = msg
                )
            },
            onDismiss = {
                viewModel.clearEmailState()
                showEmailGeneratorDialog = false
                selectedStudentForEmail = null
            }
        )
    }

    if (showPlacementEmailDialog && selectedCompanyForPlacementEmail != null) {
        val assignedStudents = students.filter { it.companyId == selectedCompanyForPlacementEmail!!.id }
        PlacementConfirmationEmailDialog(
            company = selectedCompanyForPlacementEmail!!,
            students = assignedStudents,
            emailState = viewModel.placementEmailState.collectAsStateWithLifecycle().value,
            onTrigger = { msg ->
                viewModel.triggerPlacementEmailGeneration(
                    students = assignedStudents,
                    company = selectedCompanyForPlacementEmail!!,
                    customMessage = msg
                )
            },
            onDismiss = {
                viewModel.clearPlacementEmailState()
                showPlacementEmailDialog = false
                selectedCompanyForPlacementEmail = null
            },
            onConfirmSent = {
                viewModel.markCompanyAsNotified(selectedCompanyForPlacementEmail!!)
            }
        )
    }

    if (showLogbookAnalysisDialog && selectedStudentForAnalysis != null) {
        LogbookAnalysisDialog(
            student = selectedStudentForAnalysis!!,
            analysisState = viewModel.logbookAnalysisState.collectAsStateWithLifecycle().value,
            onTrigger = { viewModel.triggerLogbookAnalysis(selectedStudentForAnalysis!!) },
            onDismiss = {
                viewModel.clearLogbookAnalysisState()
                showLogbookAnalysisDialog = false
                selectedStudentForAnalysis = null
            }
        )
    }
}

// ==========================================
// 1. DASHBOARD TAB SCREEN
// ==========================================
@Composable
fun DashboardTab(
    viewModel: MainViewModel,
    students: List<Student>,
    logbooks: List<Logbook>,
    companies: List<Company>,
    onOpenAddCompany: () -> Unit
) {
    val productivityState by viewModel.productivityState.collectAsStateWithLifecycle()

    // Trigger analysis initially if idle
    LaunchedEffect(students, logbooks) {
        if (viewModel.productivityState.value is UiState.Idle && students.isNotEmpty()) {
            viewModel.triggerProductivityAnalysis()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        // Mrs. Surti Wijaya greeting panel
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Selamat Datang, Ibu Surti Wijaya!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            Text(
                                "Kaprog Studi Desain Komunikasi Visual (DKV)\nSMKN 14 Kabupaten Tangerang",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color.White.copy(alpha = 0.8f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "KAPROG",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 8.sp,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Stats summary chips row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                val totalStudents = students.size
                val ongoingCount = students.count { it.status == "Ongoing" || it.status == "Completed" }
                val unassignedCount = students.count { it.status == "Unassigned" }

                StatsCell(
                    title = "Total Siswa",
                    value = totalStudents.toString(),
                    icon = Icons.Default.Face,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatsCell(
                    title = "Sudah PKL",
                    value = ongoingCount.toString(),
                    icon = Icons.Default.Check,
                    color = Color(0xFFC8E6C9), // Light green
                    modifier = Modifier.weight(1f)
                )
                StatsCell(
                    title = "Belum PKL",
                    value = unassignedCount.toString(),
                    icon = Icons.Default.Warning,
                    color = Color(0xFFFFCDD2), // Light red
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Second line of stats (Logbook total, Hours performed, and Approval summary)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                val totalLogs = logbooks.size
                val totalHours = logbooks.sumOf { it.hoursPerformed }
                val dudiApproved = logbooks.count { it.approvedByDudi }
                val approvalRate = if (totalLogs > 0) (dudiApproved * 100 / totalLogs) else 0

                StatsCell(
                    title = "Logs Terkumpul",
                    value = totalLogs.toString(),
                    icon = Icons.Default.List,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatsCell(
                    title = "Total Jam PKL",
                    value = "${totalHours.toInt()}h",
                    icon = Icons.Default.DateRange,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                StatsCell(
                    title = "Persetujuan DUDI",
                    value = "$approvalRate%",
                    icon = Icons.Default.Check,
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    modifier = Modifier.weight(1.1f)
                )
            }
        }

        // AI Productivity Insight Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "AI",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Analisis Produktivitas & Penempatan AI",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(
                            onClick = { viewModel.triggerProductivityAnalysis() },
                            modifier = Modifier.testTag("refresh_productivity_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Muat Ulang AI")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when (val state = productivityState) {
                        is UiState.Idle -> {
                            Text("Tekan tombol muat ulang untuk menganalisis data PKL melalui Gemini.")
                        }
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Error -> {
                            Text(
                                "Gagal menganalisis: ${state.message}",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        is UiState.Success -> {
                            val data = state.data
                            Text(
                                data.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Display mode warning
                            if (!data.isAiGenerated) {
                                Text(
                                    "Mode: Algoritma Luring Cerdas (Kunci API Gemini belum aktif)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            } else {
                                Text(
                                    "Analisis Gemini-3.5-Flash Aktif",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                "Kekuatan & Peluang Terdeteksi:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            data.strengths.forEach { value ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("• ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(value, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Hambatan & Risiko Lapangan:",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                            data.weaknesses.forEach { value ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("• ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text(value, style = MaterialTheme.typography.bodyMedium)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Rekomendasi Tindakan Kaprog:",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            data.recommendations.forEach { value ->
                                Row(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("→ ", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(value, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Companies and setup checklist
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Mitra DUDI Terdaftar (${companies.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = onOpenAddCompany,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("add_company_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah DUDI", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (companies.isEmpty()) {
                        Text("Belum ada mitra industri terdaftar. Silakan tambahkan mitra baru.")
                    } else {
                        companies.take(4).forEach { co ->
                            val currentEnroll = students.count { it.companyId == co.id && it.status != "Unassigned" }
                            val isFull = currentEnroll >= co.slots

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(co.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(co.industry, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isFull) Color(0xFFFFCDD2) else Color(0xFFC8E6C9))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$currentEnroll / ${co.slots} Slot",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isFull) Color(0xFFC62828) else Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                        if (companies.size > 4) {
                            Text(
                                "Dan ${companies.size - 4} perusahaan lainnya...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Security extraction warning message to follow regulations
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF4E5) // Warm light warning orange background
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Peringatan",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Security Warning",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. Do not share this APK file publicly or with unauthorized individuals to prevent potential misuse.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCell(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    // Determine card background, border color, value text color, and primary color
    // to match the requested Bento Grid layout
    val cardBg: Color
    val borderCol: Color
    val valCol: Color
    val titleCol: Color

    if (isSystemDark) {
        when {
            title.contains("Siswa") || title.contains("Jam") -> {
                cardBg = Color(0xFF21005D)
                borderCol = Color(0xFFD6BCFA).copy(alpha = 0.3f)
                valCol = Color(0xFFF3E8FF)
                titleCol = Color(0xFFD6BCFA)
            }
            title.contains("Sudah") || title.contains("Setuju") -> {
                cardBg = Color(0xFF0F5132)
                borderCol = Color(0xFF81C784).copy(alpha = 0.3f)
                valCol = Color(0xFFD1E7DD)
                titleCol = Color(0xFF81C784)
            }
            title.contains("Belum") -> {
                cardBg = Color(0xFF842029)
                borderCol = Color(0xFFE57373).copy(alpha = 0.3f)
                valCol = Color(0xFFF8D7DA)
                titleCol = Color(0xFFE57373)
            }
            title.contains("Log") -> {
                cardBg = Color(0xFF041E49)
                borderCol = Color(0xFF86B2F9).copy(alpha = 0.3f)
                valCol = Color(0xFFD3E3FD)
                titleCol = Color(0xFF86B2F9)
            }
            else -> {
                cardBg = Color(0xFF1F1F24)
                borderCol = Color(0xFF43474E)
                valCol = Color(0xFFE3E2E6)
                titleCol = Color(0xFFC7C6CA)
            }
        }
    } else {
        when {
            title.contains("Siswa") || title.contains("Jam") -> {
                cardBg = BentoPurpleContainer
                borderCol = BentoPurpleBorder
                valCol = BentoPurpleOnContainer
                titleCol = BentoPurple
            }
            title.contains("Sudah") || title.contains("Setuju") -> {
                cardBg = BentoGreenContainer
                borderCol = BentoGreenBorder
                valCol = BentoGreenOnContainer
                titleCol = BentoGreen
            }
            title.contains("Belum") -> {
                cardBg = BentoRedContainer
                borderCol = BentoRedBorder
                valCol = BentoRedOnContainer
                titleCol = BentoRed
            }
            title.contains("Log") -> {
                cardBg = BentoBlueContainer
                borderCol = BentoBlueBorder
                valCol = BentoBlueOnContainer
                titleCol = BentoBlue
            }
            else -> {
                cardBg = Color.White
                borderCol = BentoBorder
                valCol = BentoText
                titleCol = BentoSubText
            }
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderCol),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = titleCol
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Light,
                        color = valCol,
                        fontSize = 32.sp
                    )
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = titleCol,
                        fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ==========================================
// 2. SISWA (STUDENT DIRECTORY) TAB SCREEN
// ==========================================
@Composable
fun StudentsTab(
    viewModel: MainViewModel,
    students: List<Student>,
    companies: List<Company>,
    onOpenAddStudent: () -> Unit,
    onOpenEmail: (Student) -> Unit,
    onOpenAnalysis: (Student) -> Unit,
    onOpenAddCompany: () -> Unit = {},
    onEditCompany: (Company) -> Unit = {},
    onOpenPlacementEmail: (Company) -> Unit = {}
) {
    var nestedTab by remember { mutableStateOf("siswa") } // "siswa" or "dudi"
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("Semua") }
    var companySearchQuery by remember { mutableStateOf("") }

    val filteredStudents = students.filter { st ->
        val matchQuery = st.name.contains(searchQuery, ignoreCase = true) ||
                st.className.contains(searchQuery, ignoreCase = true) ||
                st.skills.any { s -> s.contains(searchQuery, ignoreCase = true) }

        val matchFilter = when (filterStatus) {
            "Semua" -> true
            "Belum PKL" -> st.status == "Unassigned"
            "Pending" -> st.status == "Pending"
            "Sedang/Selesai" -> st.status == "Ongoing" || st.status == "Completed"
            else -> true
        }

        matchQuery && matchFilter
    }

    val filteredCompanies = companies.filter { co ->
        co.name.contains(companySearchQuery, ignoreCase = true) ||
                co.industry.contains(companySearchQuery, ignoreCase = true) ||
                co.emailHRD.contains(companySearchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Nested tab selection Row using M3 Segmented Tabs / TabRow
        TabRow(
            selectedTabIndex = if (nestedTab == "siswa") 0 else 1,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = nestedTab == "siswa",
                onClick = { nestedTab = "siswa" },
                text = { Text("Daftar Siswa Magang", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = nestedTab == "dudi",
                onClick = { nestedTab = "dudi" },
                text = { Text("Mitra Industri (DUDI)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (nestedTab == "siswa") {
            // Student list sub-tab view
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Cari Siswa DKV / Kelas / Kompetensi") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_students_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filters pills row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                val filterOptions = listOf("Semua", "Belum PKL", "Pending", "Sedang/Selesai")
                filterOptions.forEach { opt ->
                    val selected = filterStatus == opt
                    ElevatedFilterChip(
                        selected = selected,
                        onClick = { filterStatus = opt },
                        label = { Text(opt, fontSize = 11.sp) },
                        modifier = Modifier.testTag("filter_chip_$opt")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Daftar Siswa DKV (${filteredStudents.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = onOpenAddStudent,
                    modifier = Modifier.testTag("add_student_fab_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Siswa")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Siswa", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredStudents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Siswa tidak ditemukan atau daftar kosong.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredStudents, key = { it.id }) { student ->
                        val studentLogs = logbooks.filter { it.studentId == student.id }
                        val studentHours = studentLogs.sumOf { it.hoursPerformed }
                        StudentItemCard(
                            student = student,
                            studentHours = studentHours,
                            targetHours = targetRequiredHours,
                            onDelete = { viewModel.deleteStudent(student) },
                            onEmail = { onOpenEmail(student) },
                            onAnalyze = { onOpenAnalysis(student) },
                            modifier = Modifier.testTag("student_card_${student.id}")
                        )
                    }
                }
            }
        } else {
            // Mitra DUDI management sub-tab view
            OutlinedTextField(
                value = companySearchQuery,
                onValueChange = { companySearchQuery = it },
                label = { Text("Cari Mitra DUDI / Sektor Bidang") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (companySearchQuery.isNotEmpty()) {
                        IconButton(onClick = { companySearchQuery = "" }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_companies_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Mitra DUDI Terdaftar (${filteredCompanies.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Button(
                    onClick = onOpenAddCompany,
                    modifier = Modifier.testTag("add_company_btn_student_tab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah DUDI Mitra")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("DUDI Baru", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredCompanies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Mitra perusahaan DUDI tidak ditemukan atau daftar kosong.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredCompanies, key = { it.id }) { company ->
                        // Show DUDI Card with complete info (Name, Sector, Capacity, HRD Email, and enrolled students list)
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().testTag("company_card_${company.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Top row layout: Company name & industry, and edit/delete actions
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = company.name,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Place,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = company.industry,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Action buttons row (Edit / Delete)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onEditCompany(company) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Mitra",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteCompany(company) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Mitra",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Quota and Enrollment Status visualization
                                val enrolledCount = students.count { it.companyId == company.id && it.status != "Unassigned" }
                                val isFull = enrolledCount >= company.slots
                                val remainingSlots = company.slots - enrolledCount

                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Kuota Siswa Magang: $enrolledCount / ${company.slots} Terisi",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isFull) Color(0xFFFFCDD2) else Color(0xFFC8E6C9))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isFull) "Penuh" else "$remainingSlots Tersisa",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isFull) Color(0xFFC62828) else Color(0xFF2E7D32)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Capacity Linear Progress Bar
                                    val progressPct = if (company.slots > 0) enrolledCount.toFloat() / company.slots.toFloat() else 0f
                                    LinearProgressIndicator(
                                        progress = progressPct,
                                        color = if (isFull) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // HRD Contact Section
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Hubungi HRD: ${company.emailHRD}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Interactive Enrolled Student tags inside this Card
                                val enrolledStudents = students.filter { it.companyId == company.id && it.status != "Unassigned" }
                                if (enrolledStudents.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Siswa Magang DKV Aktif:",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    // Flow Row or wrap students
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
                                    ) {
                                        enrolledStudents.forEach { st ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(100.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Face,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "${st.name} | ${st.className}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (company.lastNotifiedAt != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE8F5E9))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Notifikasi Terkirim: ${company.lastNotifiedAt}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            onOpenPlacementEmail(company)
                                        },
                                        modifier = Modifier.height(34.dp).testTag("trigger_batch_email_company_${company.id}"),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Kirim Email Konfirmasi",
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentItemCard(
    student: Student,
    studentHours: Double,
    targetHours: Double,
    onDelete: () -> Unit,
    onEmail: () -> Unit,
    onAnalyze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (student.status) {
        "Unassigned" -> Color(0xFFFFCDD2) // Light red
        "Pending" -> Color(0xFFFFF9C4) // Light yellow
        "Ongoing" -> Color(0xFFC8E6C9) // Light green
        "Completed" -> Color(0xFFB3E5FC) // Light blue
        else -> MaterialTheme.colorScheme.surface
    }

    val statusTextColor = when (student.status) {
        "Unassigned" -> Color(0xFFC62828)
        "Pending" -> Color(0xFFF57F17)
        "Ongoing" -> Color(0xFF2E7D32)
        "Completed" -> Color(0xFF0277BD)
        else -> MaterialTheme.colorScheme.onSurface
    }

    val statusIndonesian = when (student.status) {
        "Unassigned" -> "Belum PKL"
        "Pending" -> "Menunggu HRD"
        "Ongoing" -> "Sedang PKL"
        "Completed" -> "Selesai PKL"
        else -> student.status
    }

    // Milestones tracking
    val progressPercent = if (targetHours > 0) (studentHours / targetHours).coerceIn(0.0, 1.0) else 0.0
    val progressPctIn100 = (progressPercent * 100).toInt()
    
    val reached25 = progressPercent >= 0.25
    val reached50 = progressPercent >= 0.50
    val reached75 = progressPercent >= 0.75

    Card(
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name and deletion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        student.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        student.className,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        statusIndonesian,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = statusTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Skills chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start
            ) {
                student.skills.take(3).forEach { skill ->
                    Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(skill, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Portfolio highlight info
            if (student.portfolioHighlight.isNotBlank()) {
                Text(
                    text = "Aset Karya: ${student.portfolioHighlight}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Company location assigned
            if (student.companyNameAssigned != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Lokasi: ${student.companyNameAssigned}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // --- MOTIVATIONAL PROGRESS SYSTEM ---
            if (student.status == "Ongoing" || student.status == "Completed") {
                Spacer(modifier = Modifier.height(12.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Progres Jam Kerja PKL",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "$progressPctIn100%",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Progress Bar
                        LinearProgressIndicator(
                            progress = { progressPercent.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = when {
                                progressPercent >= 0.75 -> Color(0xFFFFD700) // Gold
                                progressPercent >= 0.50 -> Color(0xFFC0C0C0) // Silver
                                progressPercent >= 0.25 -> Color(0xFFCD7F32) // Bronze
                                else -> MaterialTheme.colorScheme.primary
                            },
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "${studentHours} Jam Terkumpul dari Target ${targetHours} Jam",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Milestone Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                        ) {
                            // 25% Badge
                            MilestoneIndicatorChip(
                                label = "25%",
                                emoji = "🥉",
                                isActive = reached25,
                                activeColor = Color(0xFFCD7F32).copy(alpha = 0.15f),
                                activeTextColor = Color(0xFF8B4513)
                            )
                            
                            // 50% Badge
                            MilestoneIndicatorChip(
                                label = "50%",
                                emoji = "🥈",
                                isActive = reached50,
                                activeColor = Color(0xFFC0C0C0).copy(alpha = 0.2f),
                                activeTextColor = Color(0xFF4F4F4F)
                            )
                            
                            // 75% Badge
                            MilestoneIndicatorChip(
                                label = "75%",
                                emoji = "🥇",
                                isActive = reached75,
                                activeColor = Color(0xFFFFD700).copy(alpha = 0.18f),
                                activeTextColor = Color(0xFFB8860B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action options panel: AI Email, AI Analysis, and deletion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus Siswa",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    // Direct AI Email
                    FilledTonalButton(
                        onClick = onEmail,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim Email", fontSize = 10.sp)
                    }

                    // Direct AI Analysis
                    Button(
                        onClick = onAnalyze,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analisis Jurnal", fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun MilestoneIndicatorChip(
    label: String,
    emoji: String,
    isActive: Boolean,
    activeColor: Color,
    activeTextColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isActive) activeColor else MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
            .border(
                width = 0.5.dp,
                color = if (isActive) activeTextColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.graphicsLayer {
                    alpha = if (isActive) 1.0f else 0.4f
                }
            ) {
                Text(
                    text = emoji, 
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) activeTextColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

// ==========================================
// 3. LOGBOOK (DAILY JURNALS) TAB SCREEN
// ==========================================
@Composable
fun LogbooksTab(
    viewModel: MainViewModel,
    logbooks: List<Logbook>,
    students: List<Student>,
    onOpenAddLogbook: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var filterStudentId by remember { mutableStateOf<Int?>(null) }
    var logbookToDelete by remember { mutableStateOf<Logbook?>(null) }
    val filteredLogs = if (filterStudentId != null) {
        logbooks.filter { it.studentId == filterStudentId }
    } else {
        logbooks
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Logbook Harian Siswa DKV (${filteredLogs.size})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        val selectedStudentName = if (filterStudentId != null) {
                            students.find { it.id == filterStudentId }?.name
                        } else null
                        PdfExporter.exportLogbooksToPdf(context, selectedStudentName, filteredLogs)
                    },
                    modifier = Modifier.testTag("export_pdf_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Ekspor PDF",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ekspor PDF", fontSize = 11.sp)
                }
                Button(
                    onClick = onOpenAddLogbook,
                    modifier = Modifier.testTag("add_logbook_active_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tulis Jurnal")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tulis Jurnal")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Student selector filter chips (Horizontal Scrollable or wrap chips)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pencarian: ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ElevatedFilterChip(
                selected = filterStudentId == null,
                onClick = { filterStudentId = null },
                label = { Text("Semua Siswa", fontSize = 10.sp) }
            )
            students.filter { it.status == "Ongoing" }.forEach { st ->
                Spacer(modifier = Modifier.width(6.dp))
                ElevatedFilterChip(
                    selected = filterStudentId == st.id,
                    onClick = { filterStudentId = st.id },
                    label = { Text(st.name.split(" ").firstOrNull() ?: st.name, fontSize = 10.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Belum ada catatan logbook harian terdata.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                items(filteredLogs, key = { it.id }) { log ->
                    LogbookItemCard(
                        log = log,
                        onDelete = { logbookToDelete = log },
                        onToggleDudiApproval = {
                            viewModel.updateLogbook(
                                log.copy(approvedByDudi = !log.approvedByDudi)
                            )
                        },
                        onToggleTeacherApproval = {
                            viewModel.updateLogbook(
                                log.copy(approvedByTeacher = !log.approvedByTeacher)
                            )
                        }
                    )
                }
            }
        }
    }

    if (logbookToDelete != null) {
        val targetLog = logbookToDelete!!
        AlertDialog(
            onDismissRequest = { logbookToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Hapus Logbook Harian?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Apakah Anda yakin ingin menghapus catatan kegiatan logbook untuk ${targetLog.studentName} pada tanggal ${targetLog.date}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Catatan aktivitas: \"${targetLog.activity}\"",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tindakan ini tidak dapat dibatalkan. Seluruh data jam kerja (${targetLog.hoursPerformed} jam) serta tautan atau foto bukti yang dilampirkan akan dihapus secara permanen.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteLogbook(targetLog)
                        logbookToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_logbook_btn").height(48.dp)
                ) {
                    Text("Ya, Hapus")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { logbookToDelete = null },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun LogbookItemCard(
    log: Logbook,
    onDelete: () -> Unit,
    onToggleDudiApproval: () -> Unit,
    onToggleTeacherApproval: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header date & student
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            log.studentName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // M3 Hours Badge Chip
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${log.hoursPerformed} Jam",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                    Text(
                        log.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Deletion
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus Logbook",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body activity
            Text(
                "Aktivitas Harian:",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                log.activity,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Display Photo Documentation
            if (!log.photoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                coil.compose.AsyncImage(
                    model = log.photoUri,
                    contentDescription = "Foto Jurnal Kegiatan Siswa",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tools used
            if (log.toolsUsed.isNotBlank()) {
                Text(
                    "Software / Tools: ${log.toolsUsed}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Obstacle & Solution
            if (log.obstacle.isNotBlank() || log.solution.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        if (log.obstacle.isNotBlank()) {
                            Text(
                                "Hambatan: ${log.obstacle}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (log.solution.isNotBlank()) {
                            Text(
                                "Solusi: ${log.solution}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }

            // Project link
            if (log.projectLink.isNotBlank()) {
                Text(
                    text = "Link Portofolio: ${log.projectLink}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            // Approvals toggle switches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Dudi / Pembimbing Approval
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onToggleDudiApproval() }
                        .padding(4.dp)
                ) {
                    Icon(
                        if (log.approvedByDudi) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (log.approvedByDudi) Color(0xFF2E7D32) else Color(0xFFFFA000),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "DUDI: ${if (log.approvedByDudi) "Disetujui" else "Belum"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Teacher Approval
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onToggleTeacherApproval() }
                        .padding(4.dp)
                ) {
                    Icon(
                        if (log.approvedByTeacher) Icons.Default.Check else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (log.approvedByTeacher) Color(0xFF2E7D32) else Color(0xFFFFA000),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Guru: ${if (log.approvedByTeacher) "Disetujui" else "Belum"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. MATCHMAKER (AI PLACEMENTS) SCREEN
// ==========================================
@Composable
fun MatchmakingTab(
    viewModel: MainViewModel,
    students: List<Student>,
    companies: List<Company>
) {
    val matchmakingState by viewModel.matchmakingState.collectAsStateWithLifecycle()
    val unassignedCount = students.count { it.status == "Unassigned" }
    var showHistoricalData by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome and triggers
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "AI Matchmaker Penempatan PKL DKV",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Temukan kecocokan industri otomatis untuk $unassignedCount siswa berstatus 'Belum PKL' berdasarkan portofolio, kompetensi, dan kapasitas sisa kuota industri.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.triggerPredictiveMatchmaking() },
                    enabled = unassignedCount > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("run_matchmaker_btn")
                ) {
                    Icon(Icons.Default.Place, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jalankan AI Matchmaker")
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Historical reference accordion
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
            ),
            modifier = Modifier.fillMaxWidth().testTag("historical_metrics_card")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHistoricalData = !showHistoricalData },
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Data Historis Performa Sektor Industri",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        if (showHistoricalData) "Sembunyikan" else "Lihat Data (${SectorPerformanceData.list.size} Sektor)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (showHistoricalData) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Rekam jejak historis penempatan alumni di bawah ini menjadi basis rekomendasi penyesuaian bagi logaritma pencocokan AI:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    SectorPerformanceData.list.forEach { sector ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        sector.sectorName,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "${sector.totalHistoricalStudents} Alumni",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                                        ) {
                                            Text("Selesai Magang:", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${sector.completionRate}%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        LinearProgressIndicator(
                                            progress = { sector.completionRate / 100f },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                                        ) {
                                            Text("Kepuasan Mitra:", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${sector.satisfactionIndex}/5.0", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        LinearProgressIndicator(
                                            progress = { (sector.satisfactionIndex / 5.0).toFloat() },
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.outlineVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Keahlian Selaras: ${sector.keySkillsMatch.joinToString(", ")}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    sector.bestPerformersReason,
                                    fontSize = 9.sp,
                                    lineHeight = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Matchmaker state controller UI
        when (val state = matchmakingState) {
            is UiState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Place,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            if (unassignedCount == 0) "Semua siswa telah terisi penempatan!" else "Tekan tombol di atas untuk menjalankan algoritma pencocokan AI.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sedang meraba kecocokan kompetensi siswa...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Kesalahan: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val data = state.data
                if (data.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Semua siswa unassigned telah berhasil diproses!")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                    ) {
                        items(data, key = { it.studentId }) { predict ->
                            PredictionItemCard(
                                pred = predict,
                                onApply = { rec ->
                                    viewModel.applyPrediction(predict.studentId, rec)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionItemCard(
    pred: PredictionResult,
    onApply: (MatchRecommendation) -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Student basic headers
            Text(
                pred.studentName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "${pred.className} • Keahlian: ${pred.skills.joinToString(", ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (pred.portfolioHighlight.isNotBlank()) {
                Text(
                    "Fokus Portfolio: ${pred.portfolioHighlight}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Rekomendasi Industri:",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            // Loop options
            pred.recommendations.forEach { rec ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                rec.companyName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            // Match badge score
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "${rec.score}% Match",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Suggested role
                        Text(
                            "Peran: ${rec.suggestedRole}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // Match reason
                        Text(
                            rec.matchReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        // Apply Placement button
                        Button(
                            onClick = { onApply(rec) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Terapkan Penempatan", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. CHATBOT TAB SCREEN
// ==========================================
@Composable
fun ChatbotTab(
    viewModel: MainViewModel,
    chatMessages: List<ChatMessage>
) {
    val userInput by viewModel.chatInput.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat headers with clear option
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "SimPKL Asisten AI Sekolah",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    "Konselor Aturan & Prosedur PKL SMKN 14 DKV",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = { viewModel.clearChatMessages() },
                modifier = Modifier.testTag("clear_chat_history_btn")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Bersihkan History", tint = MaterialTheme.colorScheme.error)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message bubbles list box
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            reverseLayout = false // standard chronologic
        ) {
            if (chatMessages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            
                            Text(
                                text = "Halo, Desainer DKV Muda!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Text(
                                text = "Saya adalah SimPKL Asisten AI. Siap membantu menjawab seluruh keraguan Anda tentang regulasi magang industri (PKL) di SMKN 14 Kabupaten Tangerang serta pemandu logbook harian secara real-time.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Pertanyaan Populer Siswa:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                
                val suggestions = listOf(
                    "Bagaimana alur pelaksanaan PKL?" to "Tanya Alur PKL",
                    "Apa aturan seragam wearpack hari Rabu?" to "Aturan Seragam",
                    "Bagaimana cara mengisi logbook harian yang baik?" to "Panduan Logbook",
                    "Bagaimana integrasi Apps Script & Google Sheets?" to "Koneksi Sheets",
                    "Apakah PKL mendapatkan uang saku / gaji?" to "Uang Saku PKL"
                )
                
                suggestions.forEach { (fullPrompt, label) ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setChatInput(fullPrompt)
                                    viewModel.sendChatbotMessage()
                                }
                                .testTag("suggestion_chip_${label.replace(" ", "_").lowercase()}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = fullPrompt,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                items(chatMessages, key = { it.id }) { msg ->
                    ChatBubble(msg = msg)
                }
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                "SimPKL sedang merumuskan jawaban...",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // TextInput send row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { viewModel.setChatInput(it) },
                placeholder = { Text("Tanyakan alur, seragam wearpack, logbook...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    keyboardType = KeyboardType.Text
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        focusManager.clearFocus()
                        viewModel.sendChatbotMessage()
                    }
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        focusManager.clearFocus()
                        viewModel.sendChatbotMessage()
                    }
                    .testTag("send_chat_msg_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.sender == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) androidx.compose.foundation.layout.Arrangement.End else androidx.compose.foundation.layout.Arrangement.Start
    ) {
        val bubbleColor = if (isUser) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        }

        val textColor = if (isUser) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp
                    )
                )
                .background(bubbleColor)
                .padding(14.dp)
        ) {
            Text(
                msg.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}


// ==========================================
// 6. POPUP DIALOGS (INSERT FORMS)
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentDialog(
    companies: List<Company>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, cls: String, status: String, skills: List<String>, highlight: String, companyId: Int?, companyName: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("XII DKV 1") }
    var status by remember { mutableStateOf("Unassigned") }
    var skillsInput by remember { mutableStateOf("") }
    var portfolioHighlight by remember { mutableStateOf("") }
    var selectedCompanyId by remember { mutableStateOf<Int?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Tambah Data Siswa DKV",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap Siswa") },
                        modifier = Modifier.fillMaxWidth().testTag("student_form_name")
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("XII DKV 1", "XII DKV 2").forEach { cName ->
                            val selected = className == cName
                            ElevatedFilterChip(
                                selected = selected,
                                onClick = { className = cName },
                                label = { Text(cName) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    Text("Status Keaktifan Penempatan:", style = MaterialTheme.typography.bodySmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Unassigned" to "Belum", "Pending" to "Pending", "Ongoing" to "PKL").forEach { (v, l) ->
                            val selected = status == v
                            ElevatedFilterChip(
                                selected = selected,
                                onClick = { status = v },
                                label = { Text(l) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = skillsInput,
                        onValueChange = { skillsInput = it },
                        label = { Text("Keahlian (pisahkan koma: Photoshop, Figma)") },
                        placeholder = { Text("Photoshop, Illustrator, After Effects") },
                        modifier = Modifier.fillMaxWidth().testTag("student_form_skills")
                    )
                }

                item {
                    OutlinedTextField(
                        value = portfolioHighlight,
                        onValueChange = { portfolioHighlight = it },
                        label = { Text("Highlight Karya Portofolio") },
                        placeholder = { Text("Desain Kemasan UMKM / Cameraman Profil") },
                        modifier = Modifier.fillMaxWidth().testTag("student_form_portfolio")
                    )
                }

                if (status == "Ongoing") {
                    item {
                        Text("DUDI assigned:", style = MaterialTheme.typography.bodySmall)
                        companies.forEach { co ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCompanyId = co.id }
                                    .padding(vertical = 4.dp),
                                valign = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCompanyId == co.id,
                                    onClick = { selectedCompanyId = co.id }
                                )
                                Text(co.name, fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Batal") }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val skillsList = skillsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    val assignedCoName = companies.find { it.id == selectedCompanyId }?.name
                                    onConfirm(
                                        name,
                                        className,
                                        status,
                                        skillsList,
                                        portfolioHighlight,
                                        selectedCompanyId,
                                        assignedCoName
                                    )
                                }
                            },
                            modifier = Modifier.testTag("student_form_save")
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}

// Row vertically aligned helper
@Composable
fun Row(
    modifier: Modifier = Modifier,
    horizontalArrangement: androidx.compose.foundation.layout.Arrangement.Horizontal = androidx.compose.foundation.layout.Arrangement.Start,
    valign: Alignment.Vertical,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = valign,
        content = content
    )
}

@Composable
fun AddCompanyDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, industry: String, slots: Int, email: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var industry by remember { mutableStateOf("") }
    var slotsInput by remember { mutableStateOf("2") }
    var email by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Tambah Mitra DUDI Baru",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Perusahaan / DUDI") },
                    modifier = Modifier.fillMaxWidth().testTag("company_form_name")
                )

                OutlinedTextField(
                    value = industry,
                    onValueChange = { industry = it },
                    label = { Text("Jenis Industri / Bidang") },
                    placeholder = { Text("Creative Agency, IT Studio...") },
                    modifier = Modifier.fillMaxWidth().testTag("company_form_industry")
                )

                OutlinedTextField(
                    value = slotsInput,
                    onValueChange = { slotsInput = it },
                    label = { Text("Kuota Penerima Slot") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("company_form_slots")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Resmi HRD") },
                    modifier = Modifier.fillMaxWidth().testTag("company_form_email")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && industry.isNotBlank()) {
                                onConfirm(
                                    name,
                                    industry,
                                    slotsInput.toIntOrNull() ?: 2,
                                    email
                                )
                            }
                        },
                        modifier = Modifier.testTag("company_form_save")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun EditCompanyDialog(
    company: Company,
    onDismiss: () -> Unit,
    onConfirm: (company: Company) -> Unit
) {
    var name by remember { mutableStateOf(company.name) }
    var industry by remember { mutableStateOf(company.industry) }
    var slotsInput by remember { mutableStateOf(company.slots.toString()) }
    var email by remember { mutableStateOf(company.emailHRD) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Edit Mitra DUDI",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Perusahaan / DUDI") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_company_form_name"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = industry,
                    onValueChange = { industry = it },
                    label = { Text("Jenis Industri / Bidang") },
                    placeholder = { Text("Creative Agency, IT Studio...") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_company_form_industry"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = slotsInput,
                    onValueChange = { slotsInput = it },
                    label = { Text("Kuota Penerima Slot") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("edit_company_form_slots"),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Resmi HRD") },
                    modifier = Modifier.fillMaxWidth().testTag("edit_company_form_email"),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                    valign = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank() && industry.isNotBlank()) {
                                onConfirm(
                                    company.copy(
                                        name = name,
                                        industry = industry,
                                        slots = slotsInput.toIntOrNull() ?: company.slots,
                                        emailHRD = email
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("edit_company_form_save")
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}

@Composable
fun PlacementConfirmationEmailDialog(
    company: Company,
    students: List<Student>,
    emailState: UiState<EmailResult>,
    onTrigger: (customMessage: String) -> Unit,
    onDismiss: () -> Unit,
    onConfirmSent: () -> Unit
) {
    var customMessage by remember { mutableStateOf("") }
    var dispatchState by remember { mutableStateOf("ready") } // "ready", "sending", "sent"
    var sendingStatusText by remember { mutableStateOf("") }
    
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Konfirmasi Penempatan PKL",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Kirim notifikasi email penempatan resmi untuk mitra DUDI.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Recipient Info
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Penerima HRD / Instansi:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = company.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = company.emailHRD,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // List of Assigned Students
                item {
                    Text(
                        text = "Siswa yang Ditempatkan (${students.size}):",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (students.isEmpty()) {
                        Text(
                            text = "Belum ada siswa yang ditunjuk pada kemitraan ini. Harap pasang siswa atau gunakan sistem matchmaking terlebih dahulu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)) {
                            students.forEach { st ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = st.name,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "Peran: ${st.suggestedRole ?: "DKV Designer"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dialog flows depending on EmailState
                if (emailState is UiState.Idle && students.isNotEmpty()) {
                    item {
                        OutlinedTextField(
                            value = customMessage,
                            onValueChange = { customMessage = it },
                            label = { Text("Pesan Tambahan Hubin (Opsional)") },
                            placeholder = { Text("Mulai magang per tanggal 1 Juli 2026...") },
                            modifier = Modifier.fillMaxWidth().testTag("placement_email_custom_msg"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                        ) {
                            TextButton(onClick = onDismiss) { Text("Batal") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onTrigger(customMessage) },
                                modifier = Modifier.testTag("generate_placement_email_btn")
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Draft Surat Penempatan AI", fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (emailState is UiState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Mengintegrasi roster siswa & meramu surat konfirmasi formal via Gemini AI...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                if (emailState is UiState.Error) {
                    item {
                        Text(
                            text = "Gagal memproses draft email: ${emailState.message}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
                            TextButton(onClick = onDismiss) { Text("Tutup") }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { onTrigger(customMessage) }) { Text("Coba Lagi") }
                        }
                    }
                }

                if (emailState is UiState.Success) {
                    val emailResult = emailState.data
                    
                    if (dispatchState == "ready") {
                        item {
                            OutlinedTextField(
                                value = emailResult.subject,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Subjek Email Notifikasi") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = emailResult.body,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Isi Surat Notifikasi") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 180.dp),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString("${emailResult.subject}\n\n${emailResult.body}"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 11.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            dispatchState = "sending"
                                            sendingStatusText = "Menghubungkan ke SMTP Relai SimPKL..."
                                            delay(1000)
                                            sendingStatusText = "Mengirim surat penempatan ke ${company.emailHRD}..."
                                            delay(1200)
                                            sendingStatusText = "Registrasi log konfirmasi penempatan..."
                                            delay(800)
                                            dispatchState = "sent"
                                            onConfirmSent()
                                        }
                                    },
                                    modifier = Modifier.weight(1.5f).testTag("send_automated_placement_email_btn")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kirim Otomatis", fontSize = 11.sp)
                                }
                            }
                        }
                    } else if (dispatchState == "sending") {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = sendingStatusText,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    } else if (dispatchState == "sent") {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Sistem Notifikasi Terkirim!",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "Email konfirmasi resmi penempatan telah diproses secara otomatis ke HRD ${company.emailHRD}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                                        Text("Selesai")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddLogbookDialog(
    students: List<Student>,
    onDismiss: () -> Unit,
    onConfirm: (studentId: Int, studentName: String, date: String, activity: String, tools: String, obstacle: String, solution: String, link: String, hours: Double, photoUri: String?) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("logbook_draft_pref", android.content.Context.MODE_PRIVATE) }
    val draftStudentId = remember { sharedPref.getInt("draft_student_id", -1) }
    var selectedStudent by remember { 
        mutableStateOf<Student?>(
            if (draftStudentId != -1) students.find { it.id == draftStudentId } else null
        ) 
    }
    var activity by remember { mutableStateOf(sharedPref.getString("draft_activity", "") ?: "") }
    var date by remember { mutableStateOf(sharedPref.getString("draft_date", "2026-06-14") ?: "2026-06-14") }
    var tools by remember { mutableStateOf(sharedPref.getString("draft_tools", "") ?: "") }
    var obstacle by remember { mutableStateOf(sharedPref.getString("draft_obstacle", "") ?: "") }
    var solution by remember { mutableStateOf(sharedPref.getString("draft_solution", "") ?: "") }
    var link by remember { mutableStateOf(sharedPref.getString("draft_link", "") ?: "") }
    var hoursText by remember { mutableStateOf(sharedPref.getString("draft_hours", "8.0") ?: "8.0") }
    var photoUriString by remember { mutableStateOf(sharedPref.getString("draft_photo_uri", null)) }
    var showSamplePhotoChooser by remember { mutableStateOf(false) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }

    val clearDraft = {
        sharedPref.edit().clear().apply()
    }

    LaunchedEffect(
        selectedStudent, activity, date, tools, obstacle, solution, link, hoursText, photoUriString
    ) {
        sharedPref.edit().apply {
            putInt("draft_student_id", selectedStudent?.id ?: -1)
            putString("draft_activity", activity)
            putString("draft_date", date)
            putString("draft_tools", tools)
            putString("draft_obstacle", obstacle)
            putString("draft_solution", solution)
            putString("draft_link", link)
            putString("draft_hours", hoursText)
            putString("draft_photo_uri", photoUriString)
            apply()
        }
    }

    // Launcher for taking photo using native device Camera (low-res thumbnail Bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val file = java.io.File(context.cacheDir, "logbook_cam_${System.currentTimeMillis()}.jpg")
                java.io.FileOutputStream(file).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                photoUriString = file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Launcher for selecting generic photo from Device Gallery content provider URI
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUriString = uri.toString()
        }
    }

    val activeStudents = students.filter { it.status == "Ongoing" }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Tulis Jurnal Logbook Harian",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    val hasCurrentDraft = activity.isNotEmpty() || tools.isNotEmpty() || obstacle.isNotEmpty() || solution.isNotEmpty() || link.isNotEmpty() || photoUriString != null
                    if (hasCurrentDraft) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Draf otomatis tersimpan luring (offline)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text(
                                text = "Reset",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier
                                    .clickable {
                                        // Clear all input states
                                        selectedStudent = null
                                        activity = ""
                                        tools = ""
                                        obstacle = ""
                                        solution = ""
                                        link = ""
                                        hoursText = "8.0"
                                        photoUriString = null
                                        sharedPref.edit().clear().apply()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("reset_draft_btn")
                            )
                        }
                    }
                }

                item {
                    Text("Pilih Siswa Aktif PKL:", style = MaterialTheme.typography.bodySmall)
                    if (activeStudents.isEmpty()) {
                        Text("Belum ada siswa dengan status 'Ongoing PKL'", color = MaterialTheme.colorScheme.error)
                    } else {
                        activeStudents.forEach { st ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedStudent = st }
                                    .padding(vertical = 4.dp),
                                valign = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedStudent?.id == st.id,
                                    onClick = { selectedStudent = st }
                                )
                                Text("${st.name} (${st.className})", fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Tanggal (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth().testTag("logbook_form_date")
                    )
                }

                item {
                    OutlinedTextField(
                        value = activity,
                        onValueChange = { activity = it },
                        label = { Text("Aktivitas / Penugasan") },
                        placeholder = { Text("Sebutkan deskripsi proses teknis DKV...") },
                        modifier = Modifier.fillMaxWidth().testTag("logbook_form_activity")
                    )
                }

                item {
                    OutlinedTextField(
                        value = hoursText,
                        onValueChange = { hoursText = it },
                        label = { Text("Jam Kerja Lapangan (Durasi)") },
                        placeholder = { Text("Contoh: 8.0") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("logbook_form_hours")
                    )
                }

                // Interactive Camera and Photo Attachment Section
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Foto Bukti Kegiatan PKL (Camera):", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        if (photoUriString != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                coil.compose.AsyncImage(
                                    model = photoUriString,
                                    contentDescription = "Foto Jurnal Harian DKV",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                
                                // Remove photo trigger
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable { photoUriString = null }
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Hapus foto",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { cameraLauncher.launch() },
                                        modifier = Modifier.weight(1f).testTag("camera_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                                    ) {
                                        Text("Kamera", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { galleryLauncher.launch("image/*") },
                                        modifier = Modifier.weight(1f).testTag("gallery_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                    ) {
                                        Text("Galeri", fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { showSamplePhotoChooser = true },
                                        modifier = Modifier.weight(1.3f).testTag("sample_photo_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer)
                                    ) {
                                        Text("Sampel DKV", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = tools,
                        onValueChange = { tools = it },
                        label = { Text("Software & Tools Yang Digunakan") },
                        placeholder = { Text("Photoshop, Premiere, Figma...") },
                        modifier = Modifier.fillMaxWidth().testTag("logbook_form_tools")
                    )
                }

                item {
                    OutlinedTextField(
                        value = obstacle,
                        onValueChange = { obstacle = it },
                        label = { Text("Kendala Lapangan") },
                        modifier = Modifier.fillMaxWidth().testTag("logbook_form_obstacle")
                    )
                }

                item {
                    OutlinedTextField(
                        value = solution,
                        onValueChange = { solution = it },
                        label = { Text("Langkah Penanganan Solusi") },
                        modifier = Modifier.fillMaxWidth().testTag("logbook_form_solution")
                    )
                }

                item {
                    OutlinedTextField(
                        value = link,
                        onValueChange = { link = it },
                        label = { Text("Link Karya Google Drive / Behance") },
                        modifier = Modifier.fillMaxWidth().testTag("logbook_form_link")
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) { Text("Batal") }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (selectedStudent != null && activity.isNotBlank()) {
                                    showSubmitConfirmation = true
                                }
                            },
                            modifier = Modifier.testTag("logbook_form_save")
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    if (showSamplePhotoChooser) {
        AlertDialog(
            onDismissRequest = { showSamplePhotoChooser = false },
            title = { Text("Pilih Contoh Model Dokumentasi DKV") },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                    Text("Sistem simulasi menyediakan representasi visual industri berkualitas tinggi:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        val samples = listOf(
                            Pair("Layouting", "https://images.unsplash.com/photo-1541462608141-2ff030de4a24?w=500"),
                            Pair("Video Editing", "https://images.unsplash.com/photo-1622737133809-d95047b9e673?w=500")
                        )
                        samples.forEach { (name, url) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        photoUriString = url
                                        showSamplePhotoChooser = false
                                    }
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                coil.compose.AsyncImage(
                                    model = url,
                                    contentDescription = name,
                                    modifier = Modifier.height(60.dp).fillMaxWidth().clip(RoundedCornerShape(6.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        val samples = listOf(
                            Pair("Photography", "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=500"),
                            Pair("UI/UX Sketch", "https://images.unsplash.com/photo-1581291518633-83b4ebd1d83e?w=500")
                        )
                        samples.forEach { (name, url) ->
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        photoUriString = url
                                        showSamplePhotoChooser = false
                                    }
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                coil.compose.AsyncImage(
                                    model = url,
                                    contentDescription = name,
                                    modifier = Modifier.height(60.dp).fillMaxWidth().clip(RoundedCornerShape(6.dp)),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSamplePhotoChooser = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    if (showSubmitConfirmation) {
        val stName = selectedStudent?.name ?: ""
        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "Kirim Jurnal Logbook PKL?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Apakah Anda yakin ingin mengirim laporan harian magang ini? Pastikan detail data Anda sudah benar:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)) {
                            Text("Siswa: $stName", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                            Text("Tanggal: $date", style = MaterialTheme.typography.bodySmall)
                            Text("Durasi: $hoursText Jam", style = MaterialTheme.typography.bodySmall)
                            Text("Aktivitas: \"$activity\"", style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                        }
                    }
                    Text(
                        text = "Setelah dikirim, laporan ini akan diteruskan ke mentor industri dan pembimbing Hubin untuk divalidasi resmi.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedStudent != null && activity.isNotBlank()) {
                            onConfirm(
                                selectedStudent!!.id,
                                selectedStudent!!.name,
                                date,
                                activity,
                                tools,
                                obstacle,
                                solution,
                                link,
                                hoursText.toDoubleOrNull() ?: 8.0,
                                photoUriString
                            )
                            clearDraft()
                        }
                        showSubmitConfirmation = false
                    },
                    modifier = Modifier.testTag("confirm_submit_logbook_btn").height(48.dp)
                ) {
                    Text("Ya, Kirim Resmi")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSubmitConfirmation = false },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("Batal")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

// Autocrat and Apps Script installation manual tutorial dialog to assist administrators
@Composable
fun AutocratGuideDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Otomatisasi Google Sheets, Apps Script & Autocrat PKL",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item {
                    Text(
                        text = "Sistem SimPKL memanfaatkan integrasi Google Workspace untuk generate otomatis cetakan berkas fisik (Surat Pengantar, Surat BYOD, Biodata Siswa):\n\n" +
                                "1. Buat Template Surat (.docx / Google Docs):\n" +
                                "Gunakan penanda tag data dinamis seperti <<Nama>>, <<Kelas>>, <<LokasiDudi>> di dalam docx Anda.\n\n" +
                                "2. Buat Google Spreadsheet:\n" +
                                "Sesuaikan judul kolom dengan tag data dinamis Sheets Anda.\n\n" +
                                "3. Install Add-on Autocrat:\n" +
                                "Di menu Extension > Add-ons > Get add-ons, instal Autocrat. Buat job baru, map tag <<Nama>> dengan judul kolom Sheets, pilih folder GDrive penampung, tentukan pemicu (trigger) cetak berkas PDF otomatis.\n\n" +
                                "4. Opsional: Pasang Google Apps Script:\n" +
                                "Supaya data web / Android langsung masuk lembaran Google Sheets, gunakan Apps Script generator 'Code.gs' kami di tab asisten / chatbot.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mengerti & Tutup")
                    }
                }
            }
        }
    }
}

// Gemini AI powered PKL email HRD generator dialog
@Composable
fun EmailGeneratorDialog(
    student: Student,
    companies: List<Company>,
    emailState: UiState<EmailResult>,
    onTrigger: (Company, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCompany by remember { mutableStateOf<Company?>(companies.firstOrNull()) }
    var messageAdd by remember { mutableStateOf("") }

    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Kirim Email HRD AI - ${student.name}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                if (emailState is UiState.Idle) {
                    item {
                        Text("Pilih Perusahaan Mitra DUDI:", style = MaterialTheme.typography.bodySmall)
                        companies.forEach { co ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCompany = co }
                                    .padding(vertical = 4.dp),
                                valign = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedCompany?.id == co.id,
                                    onClick = { selectedCompany = co }
                                )
                                Text("${co.name} (${co.industry})", fontSize = 12.sp)
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = messageAdd,
                            onValueChange = { messageAdd = it },
                            label = { Text("Pesan Tambahan Aturan / Permintaan khusus") },
                            placeholder = { Text("Sangat antusias belajar penyiaran video...") },
                            modifier = Modifier.fillMaxWidth().testTag("email_form_custom_msg")
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                if (selectedCompany != null) {
                                    onTrigger(selectedCompany!!, messageAdd)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_email_btn")
                        ) {
                            Text("Generate Surat Email AI")
                        }
                    }
                }

                if (emailState is UiState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Meresapi portofolio kreatif siswa...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (emailState is UiState.Error) {
                    item {
                        Text("Kesalahan generate: ${emailState.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Tutup") }
                    }
                }

                if (emailState is UiState.Success) {
                    val res = emailState.data
                    item {
                        Text(
                            text = if (res.isAiGenerated) "Gemini AI Berhasil Merakit Surat" else "Draft Surat Dirakit Luring",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = res.subject,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Subjek Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = res.body,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Isi Email (Body)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("${res.subject}\n\n${res.body}"))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Surat")
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            TextButton(onClick = onDismiss, modifier = Modifier.weight(0.5f)) {
                                Text("Tutup")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Logbook analysis dialogue sheet
@Composable
fun LogbookAnalysisDialog(
    student: Student,
    analysisState: UiState<LogbookAnalysisResult>,
    onTrigger: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Analisis Jurnal Harian DKV AI",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Evaluasi komparatif logbook siswa harian ${student.name} (${student.className}) mengenai efisiensi software, penulisan, dan penyelesaian masalah.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (analysisState is UiState.Idle) {
                    item {
                        Button(
                            onClick = onTrigger,
                            modifier = Modifier.fillMaxWidth().testTag("run_logbook_analysis_btn")
                        ) {
                            Text("Mulai Analisis Logbook")
                        }
                    }
                }

                if (analysisState is UiState.Loading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Menyelidiki jurnal logbook siswa...", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }

                if (analysisState is UiState.Error) {
                    item {
                        Text("Kesalahan analisa: ${analysisState.message}", color = MaterialTheme.colorScheme.error)
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Tutup") }
                    }
                }

                if (analysisState is UiState.Success) {
                    val data = analysisState.data
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Kualitas: ", style = MaterialTheme.typography.bodySmall)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    data.qualityScore,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            data.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    item {
                        Text(
                            "Saran Pengembangan Pelaporan:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        data.feedbackBullets.forEach { bullet ->
                            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(bullet, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    item {
                        Text(
                            "Saran Teknis Desain / Hardware & Tools:",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary
                        )
                        data.technicalRecommendations.forEach { rec ->
                            Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("→ ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Text(rec, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text("Tutup Evaluasi")
                        }
                    }
                }
            }
        }
    }
}
