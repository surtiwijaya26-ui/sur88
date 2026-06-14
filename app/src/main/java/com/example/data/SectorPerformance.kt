package com.example.data

data class SectorPerformance(
    val sectorName: String,
    val completionRate: Int,            // percentage, e.g. 96
    val satisfactionIndex: Double,      // out of 5.0, e.g. 4.8
    val industryTrustScore: Int,        // out of 100, e.g. 95
    val totalHistoricalStudents: Int,   // e.g. 18
    val keySkillsMatch: List<String>,   // preferred skills
    val bestPerformersReason: String    // description of performance
)

object SectorPerformanceData {
    val list = listOf(
        SectorPerformance(
            sectorName = "Video Production & Motion",
            completionRate = 95,
            satisfactionIndex = 4.8,
            industryTrustScore = 96,
            totalHistoricalStudents = 14,
            keySkillsMatch = listOf("Premiere Pro", "After Effects", "DaVinci Resolve"),
            bestPerformersReason = "Siswa berkemampuan After Effects & rendering video memiliki rekam jejak terbaik di sektor ini, menghasilkan draf promosi cepat dengan penyelesaian proyek tepat waktu sebesar 95%."
        ),
        SectorPerformance(
            sectorName = "Creative Agency & Branding",
            completionRate = 90,
            satisfactionIndex = 4.7,
            industryTrustScore = 92,
            totalHistoricalStudents = 12,
            keySkillsMatch = listOf("Adobe Illustrator", "Photoshop", "Procreate"),
            bestPerformersReason = "Agensi kreatif berkinerja tinggi menyerap asisten desainer yang mandiri dalam digital painting & desain kemasan sosial media."
        ),
        SectorPerformance(
            sectorName = "Percetakan & Layout",
            completionRate = 85,
            satisfactionIndex = 4.2,
            industryTrustScore = 88,
            totalHistoricalStudents = 20,
            keySkillsMatch = listOf("CorelDRAW", "Adobe Photoshop", "Layout"),
            bestPerformersReason = "Industri cetak memiliki kestabilan output tinggi untuk melatih presisi pemisahan warna CMYK siswa dalam pembuatan baliho/flyer."
        ),
        SectorPerformance(
            sectorName = "IT & UI/UX Studio",
            completionRate = 92,
            satisfactionIndex = 4.9,
            industryTrustScore = 94,
            totalHistoricalStudents = 8,
            keySkillsMatch = listOf("Figma", "Adobe XD", "HTML/CSS"),
            bestPerformersReason = "Sektor teknologi UI/UX ini sangat menyukai portfolio redesain web interaktif dan adaptif dengan kepuasan mentor rata-rata 4.9 dari 5.0."
        )
    )

    fun getPerformanceForSector(industry: String): SectorPerformance? {
        val lowerIndustry = industry.lowercase()
        return list.find { 
            val lowerSector = it.sectorName.lowercase()
            lowerIndustry.contains(lowerSector) || 
            lowerSector.contains(lowerIndustry) ||
            (lowerIndustry.contains("video") && lowerSector.contains("video")) ||
            (lowerIndustry.contains("motion") && lowerSector.contains("video")) ||
            (lowerIndustry.contains("agency") && lowerSector.contains("agency")) ||
            (lowerIndustry.contains("branding") && lowerSector.contains("agency")) ||
            (lowerIndustry.contains("cetak") && lowerSector.contains("percetakan")) ||
            (lowerIndustry.contains("layout") && lowerSector.contains("percetakan")) ||
            (lowerIndustry.contains("ui") && lowerSector.contains("ui")) ||
            (lowerIndustry.contains("ux") && lowerSector.contains("ui")) ||
            (lowerIndustry.contains("tech") && lowerSector.contains("ui"))
        }
    }
}
