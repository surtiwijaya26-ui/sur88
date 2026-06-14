package com.example.data

data class MilestoneNotification(
    val id: String = java.util.UUID.randomUUID().toString(),
    val studentId: Int,
    val studentName: String,
    val milestonePercentage: Int, // 25, 50, 75
    val hoursReached: Double,
    val targetHours: Double,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
