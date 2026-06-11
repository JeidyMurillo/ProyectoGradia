package com.example.gradia.domain.model

import com.example.gradia.data.local.entity.Evento

sealed class NotificationItem {
    data class Reminder(
        val evento: Evento,
        val subjectName: String?
    ) : NotificationItem()

    data class IncompleteSubject(
        val subject: Subject,
        val remainingPercentage: Double
    ) : NotificationItem()

    data class LowAverage(
        val subject: Subject,
        val average: Double
    ) : NotificationItem()

    data class PassingGrade(
        val subject: Subject,
        val average: Double
    ) : NotificationItem()

    data class ActivityProximityNotification(
        val activityTitle: String,
        val activityType: String,
        val subjectName: String?,
        val dueDateMillis: Long,
        val daysRemaining: Long,
        val isCompleted: Boolean,
        val message: String
    ) : NotificationItem()
}
