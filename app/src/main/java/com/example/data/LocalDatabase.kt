package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

// --- Type Converters ---
class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return value.split(",")
    }

    @TypeConverter
    fun fromList(list: List<String>?): String {
        return list?.joinToString(",") ?: ""
    }
}

// --- Entities ---

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val className: String,
    val status: String, // "Unassigned", "Pending", "Ongoing", "Completed"
    val skills: List<String>,
    val portfolioHighlight: String,
    val companyId: Int? = null,
    val suggestedRole: String? = null,
    val matchReason: String? = null,
    val companyNameAssigned: String? = null
)

@Entity(tableName = "companies")
data class Company(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val industry: String,
    val slots: Int,
    val emailHRD: String,
    val lastNotifiedAt: String? = null
)

@Entity(tableName = "logbooks")
data class Logbook(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val studentName: String,
    val date: String,
    val activity: String,
    val toolsUsed: String,
    val obstacle: String,
    val solution: String,
    val approvedByDudi: Boolean = false,
    val approvedByTeacher: Boolean = false,
    val projectLink: String = "",
    val hoursPerformed: Double = 0.0,
    val photoUri: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user" or "bot"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Int): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)
}

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies ORDER BY name ASC")
    fun getAllCompanies(): Flow<List<Company>>

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: Int): Company?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: Company): Long

    @Update
    suspend fun updateCompany(company: Company)

    @Delete
    suspend fun deleteCompany(company: Company)
}

@Dao
interface LogbookDao {
    @Query("SELECT * FROM logbooks ORDER BY date DESC, id DESC")
    fun getAllLogbooks(): Flow<List<Logbook>>

    @Query("SELECT * FROM logbooks WHERE studentId = :studentId ORDER BY date DESC")
    fun getLogbooksForStudent(studentId: Int): Flow<List<Logbook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogbook(logbook: Logbook): Long

    @Update
    suspend fun updateLogbook(logbook: Logbook)

    @Delete
    suspend fun deleteLogbook(logbook: Logbook)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

// --- Database Configuration & Seeding ---

@Database(
    entities = [Student::class, Company::class, Logbook::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun companyDao(): CompanyDao
    abstract fun logbookDao(): LogbookDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "simpkl_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            val studentDao = db.studentDao()
            val companyDao = db.companyDao()
            val logbookDao = db.logbookDao()
            val chatDao = db.chatDao()

            // Seed Companies
            val idCo1 = companyDao.insertCompany(
                Company(
                    name = "PT Creative Studio Tangerang",
                    industry = "Video Production & Motion",
                    slots = 3,
                    emailHRD = "hr@creativemotiontng.id"
                )
            ).toInt()

            val idCo2 = companyDao.insertCompany(
                Company(
                    name = "Agensi Visual Karya Utama",
                    industry = "Creative Agency & Branding",
                    slots = 2,
                    emailHRD = "vacancy@karyautamaagency.co.id"
                )
            ).toInt()

            val idCo3 = companyDao.insertCompany(
                Company(
                    name = "CV Cetak Cepat Digital",
                    industry = "Percetakan & Layout",
                    slots = 2,
                    emailHRD = "cetakcepat@gmail.com"
                )
            ).toInt()

            val idCo4 = companyDao.insertCompany(
                Company(
                    name = "Tech Apps Nusantara",
                    industry = "IT & UI/UX Studio",
                    slots = 2,
                    emailHRD = "recruitment@techappsnusantara.com"
                )
            ).toInt()

            // Seed Students
            val idS1 = studentDao.insertStudent(
                Student(
                    name = "Andi Saputra",
                    className = "XII DKV 1",
                    status = "Ongoing",
                    skills = listOf("Adobe Photoshop", "Illustrator", "Premiere Pro"),
                    portfolioHighlight = "Desain logo UMKM & editing konten video promosi.",
                    companyId = idCo1,
                    companyNameAssigned = "PT Creative Studio Tangerang"
                )
            ).toInt()

            val idS2 = studentDao.insertStudent(
                Student(
                    name = "Budi Prasetyo",
                    className = "XII DKV 1",
                    status = "Ongoing",
                    skills = listOf("Adobe After Effects", "Premiere Pro", "DaVinci Resolve"),
                    portfolioHighlight = "Slicing video sinematik / motion grafik drone.",
                    companyId = idCo2,
                    companyNameAssigned = "Agensi Visual Karya Utama"
                )
            ).toInt()

            val idS3 = studentDao.insertStudent(
                Student(
                    name = "Cici Lestari",
                    className = "XII DKV 2",
                    status = "Pending",
                    skills = listOf("Figma", "Adobe XD", "HTML/CSS"),
                    portfolioHighlight = "Redesain landing page website sekolah & mockup apps.",
                    companyId = idCo4,
                    companyNameAssigned = "Tech Apps Nusantara"
                )
            ).toInt()

            studentDao.insertStudent(
                Student(
                    name = "Dedi Kurniawan",
                    className = "XII DKV 2",
                    status = "Unassigned",
                    skills = listOf("CorelDRAW", "Adobe Photoshop", "Fotografi"),
                    portfolioHighlight = "Fotografi produk komersil & cetak banner baliho."
                )
            )

            studentDao.insertStudent(
                Student(
                    name = "Eka Wahyuni",
                    className = "XII DKV 2",
                    status = "Unassigned",
                    skills = listOf("Adobe Illustrator", "Procreate", "Digital Painting"),
                    portfolioHighlight = "Ilustrasi karakter fiksi / desain stiker kemasan kustom."
                )
            )

            // Seed Logbooks
            logbookDao.insertLogbook(
                Logbook(
                    studentId = idS1,
                    studentName = "Andi Saputra",
                    date = "2026-06-12",
                    activity = "Membuat draf ide kasar layout brosur promosi cetak agensi sesuai arahan pembimbing lapangan.",
                    toolsUsed = "Adobe Illustrator",
                    obstacle = "Lag rendering aset grafis beresolusi tinggi",
                    solution = "Melakukan kompresi proxy pada background gambar sementara",
                    approvedByDudi = true,
                    approvedByTeacher = true,
                    projectLink = "https://behance.net/andisaputra",
                    hoursPerformed = 8.0,
                    photoUri = null
                )
            )

            logbookDao.insertLogbook(
                Logbook(
                    studentId = idS1,
                    studentName = "Andi Saputra",
                    date = "2026-06-13",
                    activity = "Melakukan penelusuran (tracing) logo UMKM makanan lokal dan menentukan warna dasar cetak CMYK.",
                    toolsUsed = "Adobe Illustrator & Photoshop",
                    obstacle = "Siklus warna layar RGB berbeda dengan cetakan fisik",
                    solution = "Melakukan kalibrasi printer dan warna profil ke Coated FOGRA39",
                    approvedByDudi = true,
                    approvedByTeacher = false,
                    projectLink = "https://behance.net/andisaputra",
                    hoursPerformed = 7.5,
                    photoUri = null
                )
            )

            logbookDao.insertLogbook(
                Logbook(
                    studentId = idS2,
                    studentName = "Budi Prasetyo",
                    date = "2026-06-14",
                    activity = "Mengedit bumper video motion graphic intro berdurasi 5 detik untuk profil klien Karya Utama.",
                    toolsUsed = "Adobe After Effects",
                    obstacle = "Proses caching memori RAM laptop penuh",
                    solution = "Membersihkan disk cache AE dan menuruti resolusi pratinjau ke Third/Quarter",
                    approvedByDudi = true,
                    approvedByTeacher = true,
                    projectLink = "https://youtube.com/budipras",
                    hoursPerformed = 8.0,
                    photoUri = null
                )
            )

            // Seed initial chatbot message
            chatDao.insertMessage(
                ChatMessage(
                    sender = "bot",
                    text = "Halo! Saya adalah Asisten AI SimPKL DKV SMKN 14 Kabupaten Tangerang. Silakan tanyakan kepada saya mengenai Alur PKL, Aturan Seragam, Kebijakan Izin, tata cara menulis Logbook harian, atau integrasi Google Sheets administrasi!"
                )
            )
        }
    }
}
