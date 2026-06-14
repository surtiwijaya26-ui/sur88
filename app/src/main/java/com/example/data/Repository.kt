package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import org.json.JSONArray

// --- Gemini API Models ---

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class ContentItem(
    val parts: List<Part>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class ResponseSchema(
    val type: String,
    val properties: Map<String, ResponseSchema>? = null,
    val required: List<String>? = null,
    val items: ResponseSchema? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    val mimeType: String,
    val schema: ResponseSchema? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: ResponseSchema? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<ContentItem>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: ContentItem? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: ContentItem
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

// --- Retrofit API Service ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

// --- Retrofit Client Singleton ---

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

// --- Domain Models for UI Parsed Result ---

data class EmailResult(
    val subject: String,
    val body: String,
    val isAiGenerated: Boolean
)

data class ProductivityResult(
    val summary: String,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val recommendations: List<String>,
    val isAiGenerated: Boolean
)

data class LogbookAnalysisResult(
    val qualityScore: String, // "Sangat Baik", "Baik", "Cukup", "Kurang Detail"
    val summary: String,
    val feedbackBullets: List<String>,
    val technicalRecommendations: List<String>,
    val isAiGenerated: Boolean
)

data class MatchRecommendation(
    val companyId: Int,
    val companyName: String,
    val score: Int,
    val suggestedRole: String,
    val matchReason: String
)

data class PredictionResult(
    val studentId: Int,
    val studentName: String,
    val className: String,
    val skills: List<String>,
    val portfolioHighlight: String,
    val recommendations: List<MatchRecommendation>
)

// --- App Repository implementation ---

class AppRepository(
    private val studentDao: StudentDao,
    private val companyDao: CompanyDao,
    private val logbookDao: LogbookDao,
    private val chatDao: ChatDao
) {
    // Flow observables for DB updates
    val students: Flow<List<Student>> = studentDao.getAllStudents()
    val companies: Flow<List<Company>> = companyDao.getAllCompanies()
    val logbooks: Flow<List<Logbook>> = logbookDao.getAllLogbooks()
    val chatMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages()

    // --- DB Mutators ---

    suspend fun addStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) = withContext(Dispatchers.IO) {
        studentDao.deleteStudent(student)
    }

    suspend fun addCompany(company: Company) = withContext(Dispatchers.IO) {
        companyDao.insertCompany(company)
    }

    suspend fun updateCompany(company: Company) = withContext(Dispatchers.IO) {
        companyDao.updateCompany(company)
    }

    suspend fun deleteCompany(company: Company) = withContext(Dispatchers.IO) {
        companyDao.deleteCompany(company)
    }

    suspend fun addLogbook(logbook: Logbook) = withContext(Dispatchers.IO) {
        logbookDao.insertLogbook(logbook)
    }

    suspend fun updateLogbook(logbook: Logbook) = withContext(Dispatchers.IO) {
        logbookDao.updateLogbook(logbook)
    }

    suspend fun deleteLogbook(logbook: Logbook) = withContext(Dispatchers.IO) {
        logbookDao.deleteLogbook(logbook)
    }

    suspend fun addChatMessage(message: ChatMessage) = withContext(Dispatchers.IO) {
        chatDao.insertMessage(message)
    }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        chatDao.clearHistory()
        // Seeds a welcoming message after clearing
        chatDao.insertMessage(
            ChatMessage(
                sender = "bot",
                text = "Halo! History obrolan dibersihkan. Sampaikan pertanyaan apa saja seputar PKL DKV di sini!"
            )
        )
    }

    fun getLogbooksForStudent(studentId: Int): Flow<List<Logbook>> {
        return logbookDao.getLogbooksForStudent(studentId)
    }

    // --- Gemini API Implementations with Resilient Local Fallbacks ---

    private fun getApiKey(): String {
        return BuildConfig.GEMINI_API_KEY
    }

    /**
     * F-1: Generate unique PKL application email using Gemini-3.5-flash
     */
    suspend fun generateHRDEmail(
        student: Student,
        companyName: String,
        companyIndustry: String,
        customMessage: String
    ): EmailResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", true)) {
            return@withContext runLocalEmailFallback(student, companyName, companyIndustry, customMessage)
        }

        val prompt = """
            Make an email request letter for PKL (Internship) in Bahasa Indonesia from a Vocational High School (SMK) student.
            School: SMK Negeri 14 Kabupaten Tangerang
            Department: Desain Komunikasi Visual (DKV)
            
            Student Detail:
            - Name: ${student.name}
            - Class: ${student.className}
            - Key Skills: ${student.skills.joinToString(", ")}
            - Portfolio: ${student.portfolioHighlight}
            
            Company Detail:
            - Target Company: $companyName
            - Target Industry: $companyIndustry
            - Custom Notes from Student: $customMessage
            
            Provide the response strictly in JSON format matching this schema:
            {
              "subject": "Email Subject",
              "body": "Greeting and complete body of the application letter..."
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(ContentItem(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = ResponseSchema(
                    type = "OBJECT",
                    properties = mapOf(
                        "subject" to ResponseSchema(type = "STRING", description = "Email subject line"),
                        "body" to ResponseSchema(type = "STRING", description = "Email full body text")
                    ),
                    required = listOf("subject", "body")
                ),
                temperature = 0.7f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrBlank()) {
                val jsonObject = JSONObject(jsonText)
                EmailResult(
                    subject = jsonObject.optString("subject", "Permohonan PKL - ${student.name}"),
                    body = jsonObject.optString("body", "Isi surat permohonan..."),
                    isAiGenerated = true
                )
            } else {
                throw Exception("Received empty response text from Gemini API")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runLocalEmailFallback(student, companyName, companyIndustry, customMessage, true)
        }
    }

    private fun runLocalEmailFallback(
        student: Student,
        companyName: String,
        companyIndustry: String,
        customMessage: String,
        hasError: Boolean = false
    ): EmailResult {
        val subject = "Permohonan Praktek Kerja Lapangan (PKL) DKV - ${student.name} - SMKN 14 Kab. Tangerang"
        val errorSuffix = if (hasError) "\n\n*(Catatan: Layanan AI sibuk, menggunakan penulisan pintar luring)" else ""
        val body = """
            Kepada Yth.
            Bapak/Ibu HRD Pimpinan $companyName ($companyIndustry)
            di tempat
            
            Dengan hormat,
            
            Saya yang bertanda tangan di bawah ini, siswa aktif SMK Negeri 14 Kabupaten Tangerang:
            Nama: ${student.name}
            Kelas: ${student.className}
            Jurusan: Desain Komunikasi Visual (DKV)
            
            Mengajukan surat minat dan permohonan magang Praktek Kerja Lapangan (PKL) di perusahaan yang Bapak/Ibu pimpin selama periode 6 bulan ke depan. Sebagai penunjang operasional, sekolah kami menerapkan prinsip BYOD (Bring Your Own Device) sehingga saya akan membawa laptop pribadi berspesifikasi desainer untuk memproses penugasan desain harian.
            
            Kompetensi bidang keahlian DKV saya:
            - ${student.skills.joinToString("\n- ")}
            
            Sorotan Portofolio Portofolio / Karya Unggulan:
            ${student.portfolioHighlight}
            
            ${if (customMessage.isNotBlank()) "Catatan Tambahan:\n$customMessage\n" else ""}
            Besar harapan saya diberikan kesempatan wawancara serta bergabung menimba ilmu di lingkungan kreatif perusahaan Bapak/Ibu. Atas perhatian dan kerjasamanya, saya ucapkan banyak terima kasih.
            
            Hormat saya,
            ${student.name}
            Program Keahlian DKV - SMKN 14 Kab. Tangerang$errorSuffix
        """.trimIndent()

        return EmailResult(subject, body, isAiGenerated = false)
    }

    /**
     * Generate placement confirmation email to DUDI HRD contact
     */
    suspend fun generatePlacementConfirmationEmail(
        students: List<Student>,
        companyName: String,
        companyIndustry: String,
        customMessage: String
    ): EmailResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", true)) {
            return@withContext runLocalPlacementConfirmationFallback(students, companyName, companyIndustry, customMessage)
        }

        val roster = students.joinToString("\n") { s ->
            "- ${s.name} (${s.className}), Peran yang disarankan: ${s.suggestedRole ?: "DKV Designer"} (Keahlian: ${s.skills.joinToString(", ")})"
        }

        val prompt = """
            Make an official placement confirmation letter (Surat Konfirmasi Penempatan PKL Resmi) from the head of public relations / hubin SMK Negeri 14 Kabupaten Tangerang to a DUDI Partner HRD.
            Target Company: $companyName ($companyIndustry)
            
            Students Placed:
            $roster
            
            Custom Admin Message: $customMessage
            
            Format the response as a formal Indonesian school letter with:
            - Appropriate formal letter heading, subject: 'Konfirmasi Penempatan Praktik Kerja Lapangan (PKL) Siswa SMKN 14 Kabupaten Tangerang', and content.
            - Remind the HRD that SMKN 14 DKV has a BYOD policy, so students will bring laptops with professional specifications.
            - Express appreciation for the partnership and request confirmation / acknowledgment of readiness.
            
            Provide the response strictly in JSON format matching this schema:
            {
              "subject": "Email Subject",
              "body": "Formal greeting, complete body of the confirmation letter, school signature block..."
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(ContentItem(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = ResponseSchema(
                    type = "OBJECT",
                    properties = mapOf(
                        "subject" to ResponseSchema(type = "STRING", description = "Email subject line"),
                        "body" to ResponseSchema(type = "STRING", description = "Email full body text")
                    ),
                    required = listOf("subject", "body")
                ),
                temperature = 0.7f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrBlank()) {
                val jsonObject = JSONObject(jsonText)
                EmailResult(
                    subject = jsonObject.optString("subject", "Konfirmasi Penempatan PKL - SMKN 14 Kab. Tangerang"),
                    body = jsonObject.optString("body", "Isi surat konfirmasi penempatan..."),
                    isAiGenerated = true
                )
            } else {
                throw Exception("Received empty response text from Gemini API")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runLocalPlacementConfirmationFallback(students, companyName, companyIndustry, customMessage, true)
        }
    }

    private fun runLocalPlacementConfirmationFallback(
        students: List<Student>,
        companyName: String,
        companyIndustry: String,
        customMessage: String,
        hasError: Boolean = false
    ): EmailResult {
        val subject = "SURAT KONFIRMASI PENEMPATAN PKL RESMI - SMKN 14 KAB. TANGERANG"
        val roster = students.joinToString("\n") { s ->
            "- Nama: ${s.name}\n  Kelas: ${s.className}\n  Kompetensi: ${s.skills.joinToString(", ")}\n  Usulan Peran: ${s.suggestedRole ?: "DKV Designer"}"
        }
        val errorSuffix = if (hasError) "\n\n*(Catatan: Generasi AI dialihkan ke penulisan cepat luring)*" else ""
        val body = """
            Nomor  : 421.5/Hubin-SMK14/VI/2026
            Lamp.  : Roster Penempatan Siswa
            Hal    : Konfirmasi Penempatan Praktik Kerja Lapangan (PKL) Resmi
            
            Kepada Yth.
            Bapak/Ibu HRD Pimpinan $companyName ($companyIndustry)
            di Tempat
            
            Dengan hormat,
            
            Menindaklanjuti program kemitraan Dunia Usaha Dunia Industri (DUDI) dan hasil kurasi kecocokan kompetensi, bersama surat ini kami dari Hubungan Industri (Hubin) SMK Negeri 14 Kabupaten Tangerang resmi menempatkan siswa-siswi terbaik kami untuk melaksanakan Praktik Kerja Lapangan (PKL) di Instansi/Perusahaan yang Bapak/Ibu pimpin:
            
            Daftar Roster Siswa Magang Terpilih:
            $roster
            
            Siswa di atas diproyeksikan melaksanakan PKL selama periode 6 bulan dengan membawa perangkat kerja laptop pribadi (Bring Your Own Device/BYOD) berspesifikasi desain grafis profesional guna mendukung kelancaran operasional harian di tempat Bapak/Ibu.
            
            ${if (customMessage.isNotBlank()) "Catatan Tambahan Hubin:\n$customMessage\n" else ""}
            Kami memohon konfirmasi penerimaan dan pengaturan kedatangan perdana siswa dengan merefresh lembar penempatan atau menghubungi narahubung Hubin sekolah melalui portal SimPKL ini.
            
            Atas perhatian, dedikasi, serta kesediaan Bapak/Ibu memberikan ruang bertumbuh bagi calon desainer visual masa depan, kami haturkan terima kasih sebesar-besarnya.
            
            Hormat kami,
            Kepala Pokja Hubungan Industri (Hubin)
            SMK Negeri 14 Kabupaten Tangerang$errorSuffix
        """.trimIndent()
        return EmailResult(subject, body, isAiGenerated = false)
    }

    /**
     * F-2: Analyze student productivity & PKL approval rates using Gemini-3.5-flash
     */
    suspend fun analyzeOverallProductivity(
        studentsList: List<Student>,
        logbooksList: List<Logbook>,
        companiesList: List<Company>
    ): ProductivityResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", true)) {
            return@withContext runLocalProductivityFallback(studentsList, logbooksList)
        }

        val totalStudents = studentsList.size
        val totalLogs = logbooksList.size
        val ongoingOrCompleted = studentsList.count { it.status == "Ongoing" || it.status == "Completed" }
        val unassignedCount = studentsList.count { it.status == "Unassigned" }
        val pendingCount = studentsList.count { it.status == "Pending" }
        val dudiApproved = logbooksList.count { it.approvedByDudi }
        val teacherApproved = logbooksList.count { it.approvedByTeacher }

        val statsSummaryString = """
            Stats Dashboard:
            Total Registered: $totalStudents students
            At Work (Ongoing+Completed): $ongoingOrCompleted
            Awaiting Placement: $unassignedCount (Unassigned), $pendingCount (Pending)
            Total Logs generated: $totalLogs harian
            DUDI / Merchant Approved Logs: $dudiApproved
            Teacher Approved Logs: $teacherApproved
        """.trimIndent()

        val prompt = """
            You are the senior PKL Coordinator for the department of Graphic Design (DKV) at SMK Negeri 14 Kabupaten Tangerang.
            Analyze the following statistics and create a strategic and tactical report in Bahasa Indonesia targeted at the Head of Department, Mrs. Surti Wijaya.
            
            $statsSummaryString
            
            Provide a complete and highly professional assessment in Bahasa Indonesia. 
            Output strictly a valid JSON conforming to this schema:
            {
              "summary": "3-4 sentences high-level strategic executive summary...",
              "strengths": ["Strength point 1", "Strength point 2", "Strength point 3"],
              "weaknesses": ["Risk point 1", "Risk point 2", "Risk point 3"],
              "recommendations": ["Action item 1", "Action item 2", "Action item 3"]
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(ContentItem(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = ResponseSchema(
                    type = "OBJECT",
                    properties = mapOf(
                        "summary" to ResponseSchema(type = "STRING", description = "3-4 sentences executive summary"),
                        "strengths" to ResponseSchema(type = "ARRAY", items = ResponseSchema(type = "STRING")),
                        "weaknesses" to ResponseSchema(type = "ARRAY", items = ResponseSchema(type = "STRING")),
                        "recommendations" to ResponseSchema(type = "ARRAY", items = ResponseSchema(type = "STRING"))
                    ),
                    required = listOf("summary", "strengths", "weaknesses", "recommendations")
                ),
                temperature = 0.5f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrBlank()) {
                val jsonObject = JSONObject(jsonText)
                val strengthsArr = jsonObject.optJSONArray("strengths")
                val weaknessesArr = jsonObject.optJSONArray("weaknesses")
                val recommendationsArr = jsonObject.optJSONArray("recommendations")

                ProductivityResult(
                    summary = jsonObject.optString("summary"),
                    strengths = parseJsonArray(strengthsArr),
                    weaknesses = parseJsonArray(weaknessesArr),
                    recommendations = parseJsonArray(recommendationsArr),
                    isAiGenerated = true
                )
            } else {
                throw Exception("Received empty response text from Gemini API")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runLocalProductivityFallback(studentsList, logbooksList, true)
        }
    }

    private fun runLocalProductivityFallback(
        studentsList: List<Student>,
        logbooksList: List<Logbook>,
        hasError: Boolean = false
    ): ProductivityResult {
        val totalStudents = studentsList.size
        val totalLogs = logbooksList.size
        val ongoingOrCompleted = studentsList.count { it.status == "Ongoing" || it.status == "Completed" }
        val unassignedCount = studentsList.count { it.status == "Unassigned" }
        val pendingCount = studentsList.count { it.status == "Pending" }
        val dudiApproved = logbooksList.count { it.approvedByDudi }

        val ratePlacement = if (totalStudents > 0) (ongoingOrCompleted * 100 / totalStudents) else 0
        val rateApproval = if (totalLogs > 0) (dudiApproved * 100 / totalLogs) else 0

        val errorHeader = if (hasError) "(Analisis Offline) " else ""
        val summary = "${errorHeader}Berdasarkan analisis logistik PKL DKV SMKN 14 Tangerang, pencapaian penempatan siswa berada di angka $ratePlacement%. Sebanyak $ongoingOrCompleted dari $totalStudents siswa telah berada di lokasi magang. Total telah terkumpul $totalLogs laporan logbook mandiri dengan tingkat persetujuan (approval rate) dari pembimbing industri lapangan sebesar $rateApproval%. Tersisa $unassignedCount siswa berstatus 'Unassigned' dan $pendingCount 'Pending' yang butuh optimalisasi cepat."

        val strengths = listOf(
            "Tingkat serapan penempatan siswa ke DUDI terpantau mumpuni ($ratePlacement% siswa telah aktif di lapangan).",
            "Disiplin pelaporan mandiri terbentuk, tercatat $totalLogs pengisian jurnal harian aktif terdokumentasi.",
            "Hubungan industri komunikatif, terbukti dari approval rate DUDI mencapai $rateApproval%."
        )

        val weaknesses = listOf(
            "Sebanyak $unassignedCount siswa DKV masih berstatus 'Belum PKL' dan membutuhkan bantuan penempatan langsung.",
            "Terdapat $pendingCount penawaran magang yang tertunda dan rentan kadaluwarsa di pihak HRD.",
            "Sekitar ${totalLogs - dudiApproved} laporan logbook masih menggantung belum diverifikasi oleh mentor lapangan."
        )

        val recommendations = listOf(
            "Segera selaraskan portofolio siswa yang belum penempatan serta hubungi pembimbing Hubinmas untuk menembus slot DUDI tersisa.",
            "Implementasi metode PBL (Project-Based Learning) internal berbobot di sekolah bagi siswa yang belum mendapatkan mitra DUDI formal.",
            "Kirimkan pesan rekapitulasi sheets berkala kepada pembimbing industri untuk segera meng-approve logbook menggantung."
        )

        return ProductivityResult(summary, strengths, weaknesses, recommendations, isAiGenerated = false)
    }

    /**
     * F-3: Evaluate selected student logbooks & provide advice using Gemini-3.5-flash
     */
    suspend fun analyzeStudentLogbooks(
        student: Student,
        studentLogbooks: List<Logbook>
    ): LogbookAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", true)) {
            return@withContext runLocalLogbookFallback(student, studentLogbooks)
        }

        if (studentLogbooks.isEmpty()) {
            return@withContext runLocalLogbookFallback(student, studentLogbooks)
        }

        val logsText = studentLogbooks.take(10).joinToString("\n") { log ->
            "Mulai Tanggal ${log.date}: Aktivitas: ${log.activity}. Tools: ${log.toolsUsed}. Hambatan: ${log.obstacle}. Solusi: ${log.solution}"
        }

        val prompt = """
            Evaluate these Graphic Design internship daily logs (Bahasa Indonesia logbook) of student '${student.name}' under SMKN 14.
            The department is Desain Komunikasi Visual (DKV).
            
            Logs records:
            $logsText
            
            Analyze the quality and provide structural feedback.
            Return strictly a valid JSON conforming to this schema:
            {
              "qualityScore": "One of: Sangat Baik, Baik, Cukup, Kurang Detail",
              "summary": "2-3 sentences evaluating the technical layout, software usage, and troubleshooting skills...",
              "feedbackBullets": ["Feedback 1 to improve log book detail", "Feedback 2 to improve log book detail", "Feedback 3 to improve log book detail"],
              "technicalRecommendations": ["DKV tech suggestion 1", "DKV tech suggestion 2"]
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(ContentItem(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = ResponseSchema(
                    type = "OBJECT",
                    properties = mapOf(
                        "qualityScore" to ResponseSchema(type = "STRING", description = "Must be: Sangat Baik, Baik, Cukup, or Kurang Detail"),
                        "summary" to ResponseSchema(type = "STRING", description = "2-3 sentences evaluation summary"),
                        "feedbackBullets" to ResponseSchema(type = "ARRAY", items = ResponseSchema(type = "STRING")),
                        "technicalRecommendations" to ResponseSchema(type = "ARRAY", items = ResponseSchema(type = "STRING"))
                    ),
                    required = listOf("qualityScore", "summary", "feedbackBullets", "technicalRecommendations")
                ),
                temperature = 0.5f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrBlank()) {
                val jsonObject = JSONObject(jsonText)
                LogbookAnalysisResult(
                    qualityScore = jsonObject.optString("qualityScore", "Baik"),
                    summary = jsonObject.optString("summary", "Laporan terisi cukup..."),
                    feedbackBullets = parseJsonArray(jsonObject.optJSONArray("feedbackBullets")),
                    technicalRecommendations = parseJsonArray(jsonObject.optJSONArray("technicalRecommendations")),
                    isAiGenerated = true
                )
            } else {
                throw Exception("Received empty response text from Gemini API")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runLocalLogbookFallback(student, studentLogbooks, true)
        }
    }

    private fun runLocalLogbookFallback(
        student: Student,
        studentLogbooks: List<Logbook>,
        hasError: Boolean = false
    ): LogbookAnalysisResult {
        if (studentLogbooks.isEmpty()) {
            return LogbookAnalysisResult(
                qualityScore = "Kurang Detail",
                summary = "Siswa ${student.name} belum mengisi jurnal harian logbook di sistem kami. Penilaian kualitas pelaporan tidak dapat dilakukan secara memadai.",
                feedbackBullets = listOf(
                    "Segera catat aktivitas PKL setiap sore setelah kembali dari tempat magang.",
                    "Sebutkan software kreatif apa saja yang membantu tugas harian Anda di industri (DUDI).",
                    "Konsultasikan hambatan pengerjaan kepada guru pembimbing sekolah."
                ),
                technicalRecommendations = listOf(
                    "Siapkan portofolio aset mentah (bila diperbolehkan perusahaan) untuk direkap akhir bulan.",
                    "Manfaatkan bank aset gratisan legal seperti Freepik / Flaticon untuk efisiensi pengerjaan."
                ),
                isAiGenerated = false
            )
        }

        val totalLogs = studentLogbooks.size
        val hasObstacles = studentLogbooks.any { it.obstacle.isNotBlank() }
        val hasLinks = studentLogbooks.any { it.projectLink.isNotBlank() }

        val score = when {
            totalLogs >= 3 && hasObstacles && hasLinks -> "Sangat Baik"
            totalLogs >= 2 -> "Baik"
            else -> "Cukup"
        }

        val summary = "Evaluasi jurnal harian ${student.name} berjalan dengan status $score. Siswa telah mendokumentasikan kegiatan teknis pengerjaan visual secara berkala dengan rincian alat (tools) pengerjaan yang relevan dengan DKV.${if(hasError) " (AI sedaya luring)" else ""}"

        val feedback = listOf(
            "Tuliskan deskripsi pengerjaan yang lebih komparatif, misalnya menjabarkan format cetak, ukuran, atau revisi proyek.",
            "Tingkatkan pelaporan kolom solusi taktis setiap kali menemui hambatan pengerjaan.",
            "Cantumkan tautan karya visual yang telah di-upload ke Behance / GDrive."
        )

        val techRecs = listOf(
            "Pelajari teknik masking dan selection presisi guna mempercepat isolasi aset foto di Photoshop.",
            "Sempurnakan penggunaan dynamic layout grid dan tipografi hi-contrast agar brosur promosi tidak terpotong saat naik cetak."
        )

        return LogbookAnalysisResult(score, summary, feedback, techRecs, isAiGenerated = false)
    }

    /**
     * F-4: Match unassigned students with companies using skills, slots, and Gemini-3.5-flash
     */
    suspend fun predictModelPlacements(
        unassignedStudents: List<Student>,
        companiesList: List<Company>,
        allStudents: List<Student>
    ): List<PredictionResult> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", true)) {
            return@withContext runLocalMatchmakerFallback(unassignedStudents, companiesList, allStudents)
        }

        if (unassignedStudents.isEmpty()) return@withContext emptyList()

        // Compress lists for token safety
        val studentsMini = unassignedStudents.map {
            mapOf("id" to it.id, "name" to it.name, "skills" to it.skills, "portfolio" to it.portfolioHighlight)
        }

        val companiesMini = companiesList.map { co ->
            val countAssigned = allStudents.count { it.companyId == co.id && it.status != "Unassigned" }
            mapOf("id" to co.id, "name" to co.name, "industry" to co.industry, "totalSlots" to co.slots, "slotsLeft" to (co.slots - countAssigned))
        }

        val sectorsPerformanceMini = SectorPerformanceData.list.map {
            mapOf(
                "sector" to it.sectorName,
                "completionRate" to "${it.completionRate}%",
                "satisfactionIndex" to "${it.satisfactionIndex}/5.0",
                "trustScore" to "${it.industryTrustScore}/100",
                "keySkills" to it.keySkillsMatch,
                "reason" to it.bestPerformersReason
            )
        }

        val prompt = """
            Matchmaker AI Role: DKV Lead Teacher at SMK Negeri 14.
            Assign these students ('unassigned') to the available target companies.
            
            Siswa need placement:
            ${JSONObject().put("students", studentsMini)}
            
            DUDI / Companies:
            ${JSONObject().put("companies", companiesMini)}

            Historical Industry Sector Performance Data (Use this for dynamic weighting & context):
            ${JSONObject().put("historicalPerformance", sectorsPerformanceMini)}
            
            Match them. Max 2 company options recommended per student.
            Please factor the Historical Sector Performance into the match 'score' and 'matchReason'. 
            If a student's skills align closely with a sector's historical preferred skills ('keySkills'), apply a positive weight (boosting the score up to 85-98%) and describe how this aligns with the sector's historical success rate (e.g. mention that sector's historical completionRate and satisfactionIndex in Indonesian language as part of the 'matchReason' to support this strategic decision).

            Provide response strictly as valid JSON conforming exactly to this structure:
            {
              "predictions": [
                {
                  "studentId": "student id as string",
                  "studentName": "student name",
                  "className": "XII DKV 1",
                  "skills": ["skill1", "skill2"],
                  "portfolioHighlight": "...",
                  "recommendations": [
                    {
                      "companyId": "company id as integer",
                      "companyName": "company name",
                      "score": 90, 
                      "suggestedRole": "Intern role title (e.g. Assitant Video Editor)",
                      "matchReason": "Indonesian logical paragraph explaining why the student's portfolio and skills match..."
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(ContentItem(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                responseSchema = ResponseSchema(
                    type = "OBJECT",
                    properties = mapOf(
                        "predictions" to ResponseSchema(
                            type = "ARRAY",
                            items = ResponseSchema(
                                type = "OBJECT",
                                properties = mapOf(
                                    "studentId" to ResponseSchema(type = "STRING"),
                                    "studentName" to ResponseSchema(type = "STRING"),
                                    "className" to ResponseSchema(type = "STRING"),
                                    "skills" to ResponseSchema(type = "ARRAY", items = ResponseSchema(type = "STRING")),
                                    "portfolioHighlight" to ResponseSchema(type = "STRING"),
                                    "recommendations" to ResponseSchema(
                                        type = "ARRAY",
                                        items = ResponseSchema(
                                            type = "OBJECT",
                                            properties = mapOf(
                                                "companyId" to ResponseSchema(type = "INTEGER"),
                                                "companyName" to ResponseSchema(type = "STRING"),
                                                "score" to ResponseSchema(type = "INTEGER"),
                                                "suggestedRole" to ResponseSchema(type = "STRING"),
                                                "matchReason" to ResponseSchema(type = "STRING")
                                            ),
                                            required = listOf("companyId", "companyName", "score", "suggestedRole", "matchReason")
                                        )
                                    )
                                ),
                                required = listOf("studentId", "studentName", "className", "skills", "portfolioHighlight", "recommendations")
                            )
                        )
                    ),
                    required = listOf("predictions")
                ),
                temperature = 0.4f
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!jsonText.isNullOrBlank()) {
                val jsonObject = JSONObject(jsonText)
                val predictionsArr = jsonObject.optJSONArray("predictions") ?: return@withContext emptyList()
                val results = mutableListOf<PredictionResult>()

                for (i in 0 until predictionsArr.length()) {
                    val pObj = predictionsArr.optJSONObject(i) ?: continue
                    val recsArr = pObj.optJSONArray("recommendations")
                    val recommendationsList = mutableListOf<MatchRecommendation>()

                    if (recsArr != null) {
                        for (j in 0 until recsArr.length()) {
                            val rObj = recsArr.optJSONObject(j) ?: continue
                            recommendationsList.add(
                                MatchRecommendation(
                                    companyId = rObj.optInt("companyId"),
                                    companyName = rObj.optString("companyName"),
                                    score = rObj.optInt("score", 70),
                                    suggestedRole = rObj.optString("suggestedRole", "Asisten Desainer Komunikasi Visual"),
                                    matchReason = rObj.optString("matchReason")
                                )
                            )
                        }
                    }

                    results.add(
                        PredictionResult(
                            studentId = pObj.optString("studentId").toIntOrNull() ?: pObj.optInt("studentId"),
                            studentName = pObj.optString("studentName"),
                            className = pObj.optString("className", "XII DKV"),
                            skills = parseJsonArray(pObj.optJSONArray("skills")),
                            portfolioHighlight = pObj.optString("portfolioHighlight"),
                            recommendations = recommendationsList
                        )
                    )
                }

                results
            } else {
                throw Exception("Received empty response text from Gemini API")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runLocalMatchmakerFallback(unassignedStudents, companiesList, allStudents)
        }
    }

    private fun runLocalMatchmakerFallback(
        unassigned: List<Student>,
        companiesList: List<Company>,
        allStudents: List<Student>
    ): List<PredictionResult> {
        return unassigned.map { student ->
            val skillsString = student.skills.joinToString(" ").lowercase()
            val recommendations = companiesList.map { co ->
                val assignedCount = allStudents.count { it.companyId == co.id && it.status != "Unassigned" }
                val slotsLeft = co.slots - assignedCount
                var score = 60

                if (slotsLeft > 0) score += 12 else score -= 15

                val perf = SectorPerformanceData.getPerformanceForSector(co.industry)
                if (perf != null) {
                    // completion rate bonus (completion rates are 85-95%, let's add 1% for every 1% above 80%)
                    val completionBonus = (perf.completionRate - 80).coerceAtLeast(0) / 2
                    score += completionBonus
                    
                    // satisfaction rate bonus (ranges 4.2-4.9, add 10 points for every 0.1 above 4.0)
                    val satisfactionBonus = ((perf.satisfactionIndex - 4.0) * 10).toInt().coerceAtLeast(0)
                    score += satisfactionBonus

                    // Check skill compatibility with historical success factors
                    var matchCount = 0
                    for (prefSkill in perf.keySkillsMatch) {
                        if (skillsString.contains(prefSkill.lowercase())) {
                            matchCount++
                        }
                    }
                    score += (matchCount * 8)
                }

                val indStr = co.industry.lowercase()
                val coName = co.name.lowercase()

                // Logic matching
                var matched = false
                val role: String
                val reason: String

                when {
                    indStr.contains("video") || indStr.contains("motion") || coName.contains("motion") || coName.contains("studio") -> {
                        role = "intern Assistant Video Editor & Motion Designer"
                        if (skillsString.contains("video") || skillsString.contains("after") || skillsString.contains("premiere")) {
                            score += 15
                            matched = true
                        }
                        reason = "Keahlian editing video dan render multimedia siswa klop dengan PT Creative Studio yang berfokus di bidang penyiaran digital dan After Effects."
                    }
                    indStr.contains("agency") || indStr.contains("brand") || coName.contains("agency") || coName.contains("visual") -> {
                        role = "Creative Graphic Designer & Branding Intern"
                        if (skillsString.contains("illustr") || skillsString.contains("photoshop") || skillsString.contains("brand")) {
                            score += 15
                            matched = true
                        }
                        reason = "Agensi Visual Karya Utama sangat membutuhkan ide-ide segar desain ilustrasi karakter dan digital painting siswa DKV guna meracik pamflet branding media sosial klien."
                    }
                    indStr.contains("cetak") || indStr.contains("layout") -> {
                        role = "Asisten Pracetak & Pengarah Layout Majalah"
                        if (skillsString.contains("corel") || skillsString.contains("photoshop") || skillsString.contains("layout")) {
                            score += 15
                            matched = true
                        }
                        reason = "Tuntutan pengerjaan layout brosur promosi di CV Cetak Cepat Digital selaras dengan keahlian CorelDRAW dan olah layout pracetak siswa."
                    }
                    indStr.contains("ui") || indStr.contains("ux") || indStr.contains("tech") -> {
                        role = "Junior UI/UX & Web Designer Intern"
                        if (skillsString.contains("figma") || skillsString.contains("xd") || skillsString.contains("web")) {
                            score += 15
                            matched = true
                        }
                        reason = "Tech Apps Nusantara berfokus di bidang desain antarmuka aplikasi. Keterampilan wireframing Figma siswa akan mempercepat mockup aset interaktif mereka."
                    }
                    else -> {
                        role = "Junior Graphic Designer"
                        reason = "Faktor penempatan prioritas slot bebas dan keseimbangan kurikulum DKV di SMKN 14."
                    }
                }

                // Normalizing matches score
                score = Math.max(40, Math.min(98, score))

                val historicalNote = if (perf != null) {
                    " [Analisis Historis Sektor: Kelulusan Magang ${perf.completionRate}%, Indeks Kepuasan Mitra ${perf.satisfactionIndex}/5.0. ${perf.bestPerformersReason}]"
                } else ""

                MatchRecommendation(
                    companyId = co.id,
                    companyName = co.name,
                    score = score,
                    suggestedRole = role,
                    matchReason = reason + historicalNote + (if (slotsLeft > 0) " Sisa slot penerima aktif ($slotsLeft/$co.slots) tersedia." else " Kuota utama penuh.")
                )
            }.sortedByDescending { it.score }.take(3)

            PredictionResult(
                studentId = student.id,
                studentName = student.name,
                className = student.className,
                skills = student.skills,
                portfolioHighlight = student.portfolioHighlight,
                recommendations = recommendations
            )
        }
    }

    /**
     * F-5: Dialog chat with a stateful model using custom handbook system rules
     */
    suspend fun getChatbotReply(
        userMessage: String,
        historyList: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", true)) {
            return@withContext runLocalChatbotFallback(userMessage)
        }

        // Prepare context
        val sysInstruction = """
            Anda adalah SimPKL Chatbot, asisten cerdas untuk program magang (PKL) Program Studi Desain Komunikasi Visual (DKV) di SMK Negeri 14 Kabupaten Tangerang.
            Ketua program studi DKV di sini bernama Ibu Surti Wijaya, S.Kom., Gr.
            Bantulah menjawab pertanyaan siswa mengenai Alur Pelaksanaan PKL, Kebijakan Seragam (Wearpack DKV hari Rabu), Aturan Kedisiplinan Disiplin, pengisian Logbook Harian yang bermutu, serta integrasi Apps Script Google Sheets.
            Jawab menggunakan bahasa Indonesia yang ramah remaja SMK, sopan, taktis, dan informatif.
        """.trimIndent()

        // Construct history contents format
        val chatTurns = mutableListOf<ContentItem>()
        historyList.takeLast(10).forEach { msg ->
            val roleName = if (msg.sender == "user") "user" else "model"
            chatTurns.add(
                ContentItem(
                    parts = listOf(Part(text = msg.text)),
                    role = roleName
                )
            )
        }
        
        // Add current turn
        chatTurns.add(
            ContentItem(
                parts = listOf(Part(text = userMessage)),
                role = "user"
            )
        )

        val request = GenerateContentRequest(
            contents = chatTurns,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = ContentItem(parts = listOf(Part(text = sysInstruction)))
        )

        try {
            val response = RetrofitClient.service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!replyText.isNullOrBlank()) {
                replyText
            } else {
                throw Exception("Received empty response text")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runLocalChatbotFallback(userMessage) + " *(Koneksi AI terhambat, dilayani dengan kecerdasan lokal)*"
        }
    }

    private fun runLocalChatbotFallback(message: String): String {
        val msg = message.lowercase()
        return when {
            msg.contains("alur") || msg.contains("langkah") || msg.contains("tahap") || msg.contains("proses") -> {
                "**Alur PKL SMKN 14 Kab. Tangerang (DKV):**\n\n1. **Persiapan:** Melengkapi profil di SimPKL, mengumpulkan Surat Pernyataan Orang Tua & Bring Your Own Device (BYOD), serta menyiapkan portofolio Behance.\n2. **Pengajuan:** Memilih tempat magang aktif di tab Perusahaan, lalu mengajukan cetak Surat Pengantar TU sekolah.\n3. **Pelaksanaan:** Magang di mitra selama 6 bulan, wajib mengisi jurnal harian logbook SimPKL setiap hari kerja.\n4. **Pelaporan:** Menyerahkan Laporan PKL, mengikuti Sidang Presentasi PKL, dan menerima Sertifikat Industri."
            }
            msg.contains("aturan") || msg.contains("seragam") || msg.contains("disiplin") || msg.contains("baju") || msg.contains("rambut") -> {
                "**Aturan Kedisiplinan & Seragam PKL:**\n\n* **Pakaian:** Menggunakan seragam harian sekolah (wearpack jurusan DKV pada hari Rabu) atau mengenakan seragam khusus perusahaan apabila disediakan.\n* **Rambut:** Rapi, tidak gondrong (untuk siswa laki-laki), rambut hitam alami tidak dicat warna mencolok.\n* **Kehadiran:** Mengikuti jam kerja industri DUDI. Jika sakit wajib mengirimkan Surat Dokter ke pembimbing industri dan guru pendamping.\n* **Sanksi:** Pelanggaran berat atau absen bolos tanpa kabar >10 hari dapat ditarik langsung, dinyatakan gagal PKL, dan menunda kelulusan."
            }
            msg.contains("logbook") || msg.contains("jurnal") || msg.contains("isi") || msg.contains("harian") -> {
                "**Panduan Mengisi Logbook SimPKL:**\n\nWajib diisi setiap hari kerja. Logbook yang ideal wajib merinci:\n* **Aktivitas:** Contoh bagus: *'Membuat draf ide kasar layout brosur lipat agensi menggunakan Adobe Illustrator'*, dibanding sekadar menulis *'Desain pamflet'*.\n* **Tools:** Sebutkan software (Photoshop, Figma, Illustrator).\n* **Kendala & Solusi:** Sebutkan jika ada lag laptop / revisi dan solusi kreatif yang Anda kerjakan."
            }
            msg.contains("autocrat") || msg.contains("apps script") || msg.contains("sheet") -> {
                "**Panduan Sistem Apps Script & Sheets:**\n\nSistem otomatisasi administrasi SimPKL ini menggunakan integrasi template Google Sheets dan Google Apps Script untuk cetak otomatis. Anda dapat membuka panduan penulisan script di dashboard tab utama serta menyalin script Code.gs via menu Script Generator kami."
            }
            msg.contains("gaji") || msg.contains("uang") || msg.contains("bayar") || msg.contains("ongkos") -> {
                "PKL adalah program kurikulum sekolah wajib untuk belajar di industri. Secara aturan tidak diwajibkan perusahaan bayar uang saku, namun beberapa industri yang ramah dan dermawan memberikan uang transport/makan secara sukarela. Fokuslah pada pengalaman dan relasi profesional DKV."
            }
            msg.contains("guru") || msg.contains("kaprog") || msg.contains("surti wijaya") -> {
                "Kepala Program Keahlian Desain Komunikasi Visual (DKV) SMKN 14 Kabupaten Tangerang adalah **Ibu Surti Wijaya, S.Kom., Gr.** Beliau yang mengoordinasikan seluruh perizinan dan standar kelulusan PKL Anda."
            }
            else -> {
                "Halo! Saya mengerti pesan Anda. Tanyakan kepada saya lebih spesifik mengenai alur instalan Apps Script, tata cara pengisian logbook harian DKV, kedisiplinan seragam baju hari Rabu, atau kebijakan sanksi PKL DKV."
            }
        }
    }

    // Helper conversion from JSONArray to List<String>
    private fun parseJsonArray(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            list.add(arr.optString(i))
        }
        return list
    }
}
