package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Universal UI States for AI Workflows
sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    // --- Database Streams ---
    val students: StateFlow<List<Student>> = repository.students
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val companies: StateFlow<List<Company>> = repository.companies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logbooks: StateFlow<List<Logbook>> = repository.logbooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Feature States ---
    private val _emailState = MutableStateFlow<UiState<EmailResult>>(UiState.Idle)
    val emailState: StateFlow<UiState<EmailResult>> = _emailState.asStateFlow()

    private val _placementEmailState = MutableStateFlow<UiState<EmailResult>>(UiState.Idle)
    val placementEmailState: StateFlow<UiState<EmailResult>> = _placementEmailState.asStateFlow()

    private val _productivityState = MutableStateFlow<UiState<ProductivityResult>>(UiState.Idle)
    val productivityState: StateFlow<UiState<ProductivityResult>> = _productivityState.asStateFlow()

    private val _logbookAnalysisState = MutableStateFlow<UiState<LogbookAnalysisResult>>(UiState.Idle)
    val logbookAnalysisState: StateFlow<UiState<LogbookAnalysisResult>> = _logbookAnalysisState.asStateFlow()

    private val _matchmakingState = MutableStateFlow<UiState<List<PredictionResult>>>(UiState.Idle)
    val matchmakingState: StateFlow<UiState<List<PredictionResult>>> = _matchmakingState.asStateFlow()

    // --- Chat Input & Loading States ---
    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // --- Internship Goal & Notification States ---
    private val _targetRequiredHours = MutableStateFlow(60.0)
    val targetRequiredHours: StateFlow<Double> = _targetRequiredHours.asStateFlow()

    private val _milestoneNotifications = MutableStateFlow<List<MilestoneNotification>>(
        listOf(
            MilestoneNotification(
                studentId = 1,
                studentName = "Andi Saputra",
                milestonePercentage = 25,
                hoursReached = 15.5,
                targetHours = 60.0,
                message = "Berhasil mencapai 25%! Andi Saputra telah mengumpulkan 15.5 jam kerja dari total target 60.0 jam. Awal yang fantastis! Langkah pertama menuju keahlian profesional! Hajar terus! 🥉",
                timestamp = System.currentTimeMillis() - 7200000 // 2 hours ago
            )
        )
    )
    val milestoneNotifications: StateFlow<List<MilestoneNotification>> = _milestoneNotifications.asStateFlow()

    private val _activeMilestoneAlert = MutableStateFlow<MilestoneNotification?>(null)
    val activeMilestoneAlert: StateFlow<MilestoneNotification?> = _activeMilestoneAlert.asStateFlow()

    fun setTargetRequiredHours(hours: Double) {
        _targetRequiredHours.value = hours
        // Recalculate or clear notifications if needed, we'll keep them as is or reset
    }

    fun dismissMilestoneAlert() {
        _activeMilestoneAlert.value = null
    }

    fun clearNotifications() {
        _milestoneNotifications.value = emptyList()
    }

    fun addManualNotification(notif: MilestoneNotification) {
        _milestoneNotifications.update { listOf(notif) + it }
    }

    fun checkAndTriggerMilestones(
        studentId: Int,
        studentName: String,
        prevHours: Double,
        addedHours: Double
    ) {
        val target = targetRequiredHours.value
        val newHours = prevHours + addedHours
        
        val pPrev = prevHours / target
        val pNew = newHours / target
        
        val milestones = listOf(25, 50, 75)
        for (m in milestones) {
            val threshold = m / 100.0
            if (pPrev < threshold && pNew >= threshold) {
                val emoji = when (m) {
                    25 -> "🥉"
                    50 -> "🥈"
                    else -> "🥇"
                }
                val encouragement = when (m) {
                    25 -> "Awal yang fantastis! Langkah pertama menuju keahlian profesional! Hajar terus!"
                    50 -> "Mantap luar biasa! Setengah perjuangan telah terlampaui, masa depan cerah menanti!"
                    else -> "Luar biasa hebat! Tinggal selangkah lagi menuju kelulusan magang. Tunjukkan karya terbaikmu!"
                }
                
                val msg = "Berhasil mencapai $m%! $studentName telah mengumpulkan ${newHours} jam kerja dari total target ${target} jam. $encouragement $emoji"
                
                val notif = MilestoneNotification(
                    studentId = studentId,
                    studentName = studentName,
                    milestonePercentage = m,
                    hoursReached = newHours,
                    targetHours = target,
                    message = msg
                )
                
                _milestoneNotifications.update { list -> (listOf(notif) + list).take(30) }
                _activeMilestoneAlert.value = notif
            }
        }
    }

    // --- UI Modifiers and Input Handlers ---
    fun setChatInput(input: String) {
        _chatInput.value = input
    }

    // --- DB Mutator Actions (Delegating to Repository) ---

    fun insertStudent(student: Student) {
        viewModelScope.launch {
            repository.addStudent(student)
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun insertCompany(company: Company) {
        viewModelScope.launch {
            repository.addCompany(company)
        }
    }

    fun updateCompany(company: Company) {
        viewModelScope.launch {
            repository.updateCompany(company)
        }
    }

    fun deleteCompany(company: Company) {
        viewModelScope.launch {
            repository.deleteCompany(company)
        }
    }

    fun insertLogbook(logbook: Logbook) {
        viewModelScope.launch {
            val studentLogs = logbooks.value.filter { it.studentId == logbook.studentId }
            val prevHours = studentLogs.sumOf { it.hoursPerformed }
            
            repository.addLogbook(logbook)
            
            checkAndTriggerMilestones(
                studentId = logbook.studentId,
                studentName = logbook.studentName,
                prevHours = prevHours,
                addedHours = logbook.hoursPerformed
            )
            
            // Refresh productivity analysis on new logbook to keep Dashboard fresh
            triggerProductivityAnalysis()
        }
    }

    fun updateLogbook(logbook: Logbook) {
        viewModelScope.launch {
            val studentLogs = logbooks.value.filter { it.studentId == logbook.studentId }
            val prevHours = studentLogs.sumOf { it.hoursPerformed }
            val originalLog = studentLogs.find { it.id == logbook.id }
            val originalHours = originalLog?.hoursPerformed ?: 0.0
            val addedHours = logbook.hoursPerformed - originalHours
            
            repository.updateLogbook(logbook)
            
            if (addedHours > 0) {
                checkAndTriggerMilestones(
                    studentId = logbook.studentId,
                    studentName = logbook.studentName,
                    prevHours = prevHours - originalHours,
                    addedHours = logbook.hoursPerformed
                )
            }
            
            triggerProductivityAnalysis()
        }
    }

    fun deleteLogbook(logbook: Logbook) {
        viewModelScope.launch {
            repository.deleteLogbook(logbook)
            triggerProductivityAnalysis()
        }
    }

    // --- AI Trigger Actions ---

    fun triggerEmailGeneration(
        student: Student,
        companyName: String,
        companyIndustry: String,
        customMessage: String
    ) {
        viewModelScope.launch {
            _emailState.value = UiState.Loading
            try {
                val result = repository.generateHRDEmail(student, companyName, companyIndustry, customMessage)
                _emailState.value = UiState.Success(result)
            } catch (e: Exception) {
                _emailState.value = UiState.Error(e.localizedMessage ?: "Gagal menghasilkan email lamaran")
            }
        }
    }

    fun clearEmailState() {
        _emailState.value = UiState.Idle
    }

    fun triggerPlacementEmailGeneration(
        students: List<Student>,
        company: Company,
        customMessage: String
    ) {
        viewModelScope.launch {
            _placementEmailState.value = UiState.Loading
            try {
                val result = repository.generatePlacementConfirmationEmail(students, company.name, company.industry, customMessage)
                _placementEmailState.value = UiState.Success(result)
            } catch (e: Exception) {
                _placementEmailState.value = UiState.Error(e.localizedMessage ?: "Gagal menghasilkan email konfirmasi penempatan")
            }
        }
    }

    fun markCompanyAsNotified(company: Company) {
        viewModelScope.launch {
            val df = java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm", java.util.Locale("id"))
            val formattedDate = df.format(java.util.Date()) + " WIB"
            val updatedCompany = company.copy(lastNotifiedAt = formattedDate)
            repository.updateCompany(updatedCompany)
        }
    }

    fun clearPlacementEmailState() {
        _placementEmailState.value = UiState.Idle
    }

    fun triggerProductivityAnalysis() {
        viewModelScope.launch {
            _productivityState.value = UiState.Loading
            try {
                val result = repository.analyzeOverallProductivity(
                    students.value,
                    logbooks.value,
                    companies.value
                )
                _productivityState.value = UiState.Success(result)
            } catch (e: Exception) {
                _productivityState.value = UiState.Error(e.localizedMessage ?: "Gagal menganalisis produktivitas")
            }
        }
    }

    fun triggerLogbookAnalysis(student: Student) {
        viewModelScope.launch {
            _logbookAnalysisState.value = UiState.Loading
            try {
                // Get logs for the selected student
                val filteredLogs = logbooks.value.filter { it.studentId == student.id }
                val result = repository.analyzeStudentLogbooks(student, filteredLogs)
                _logbookAnalysisState.value = UiState.Success(result)
            } catch (e: Exception) {
                _logbookAnalysisState.value = UiState.Error(e.localizedMessage ?: "Gagal menganalisis logbook")
            }
        }
    }

    fun clearLogbookAnalysisState() {
        _logbookAnalysisState.value = UiState.Idle
    }

    fun triggerPredictiveMatchmaking() {
        viewModelScope.launch {
            _matchmakingState.value = UiState.Loading
            try {
                val unassigned = students.value.filter { it.status == "Unassigned" }
                val result = repository.predictModelPlacements(unassigned, companies.value, students.value)
                _matchmakingState.value = UiState.Success(result)
            } catch (e: Exception) {
                _matchmakingState.value = UiState.Error(e.localizedMessage ?: "Gagal melakukan pencocokan")
            }
        }
    }

    fun applyPrediction(studentId: Int, rec: MatchRecommendation) {
        viewModelScope.launch {
            val student = students.value.find { it.id == studentId }
            if (student != null) {
                val updatedStudent = student.copy(
                    status = "Ongoing",
                    companyId = rec.companyId,
                    companyNameAssigned = rec.companyName,
                    suggestedRole = rec.suggestedRole,
                    matchReason = rec.matchReason
                )
                repository.updateStudent(updatedStudent)
                
                // Recalculate metrics
                triggerProductivityAnalysis()
                
                // Refresh matchmaking state
                val updatedMatchList = matchmakingState.value
                if (updatedMatchList is UiState.Success) {
                    val remaining = updatedMatchList.data.filter { it.studentId != studentId }
                    _matchmakingState.value = UiState.Success(remaining)
                }
            }
        }
    }

    fun sendChatbotMessage() {
        val messageText = _chatInput.value.trim()
        if (messageText.isBlank()) return

        viewModelScope.launch {
            // Add user message to local db
            val userMsg = ChatMessage(sender = "user", text = messageText)
            repository.addChatMessage(userMsg)
            _chatInput.value = ""
            _isChatLoading.value = true

            try {
                // Get AI response
                val responseText = repository.getChatbotReply(messageText, chatMessages.value)
                val botMsg = ChatMessage(sender = "bot", text = responseText)
                repository.addChatMessage(botMsg)
            } catch (e: Exception) {
                val errorMsg = ChatMessage(sender = "bot", text = "Koneksi terhambat: ${e.localizedMessage}")
                repository.addChatMessage(errorMsg)
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun clearChatMessages() {
        viewModelScope.launch {
            repository.clearChatHistory()
        }
    }
}

// Custom ViewModel Factory class
class ViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
