package com.example.gradia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.domain.model.NotificationItem
import com.example.gradia.presentation.viewmodel.NotificationsViewModel
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun NotificationsScreen(viewModel: NotificationsViewModel) {
    val state by viewModel.uiState.collectAsState()

    if (state.totalCount == 0) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.notif_empty_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.notif_empty_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    fontFamily = InterFontFamily
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        if (state.reminders.isNotEmpty()) {
            item {
                NotificationSectionHeader(title = stringResource(R.string.notif_reminders_section))
            }
            items(state.reminders, key = { "rem_${it.evento.id}" }) { item ->
                ReminderCard(item)
            }
        }

        if (state.urgentNotifications.isNotEmpty()) {
            item {
                NotificationSectionHeader(title = stringResource(R.string.notif_urgent_section))
            }
            items(state.urgentNotifications, key = { "urg_${it.activityTitle}_${it.dueDateMillis}" }) { item ->
                ProximityCard(item)
            }
        }

        if (state.upcomingNotifications.isNotEmpty()) {
            item {
                NotificationSectionHeader(title = stringResource(R.string.notif_upcoming_section))
            }
            items(state.upcomingNotifications, key = { "upc_${it.activityTitle}_${it.dueDateMillis}" }) { item ->
                ProximityCard(item)
            }
        }

        if (state.completedNotifications.isNotEmpty()) {
            item {
                NotificationSectionHeader(title = stringResource(R.string.notif_completed_section))
            }
            items(state.completedNotifications, key = { "cmp_${it.activityTitle}_${it.dueDateMillis}" }) { item ->
                ProximityCard(item)
            }
        }

        if (state.lowAverages.isNotEmpty()) {
            item {
                NotificationSectionHeader(title = stringResource(R.string.notif_warnings_section))
            }
            items(state.lowAverages, key = { "low_${it.subject.id}" }) { item ->
                SubjectAlertCard(
                    icon = Icons.Default.Warning,
                    iconTint = Color(0xFFE53935),
                    iconBg = Color(0xFFFFEBEE),
                    title = item.subject.name,
                    body = stringResource(R.string.notif_warning_body, item.average)
                )
            }
        }

        if (state.incompleteSubjects.isNotEmpty()) {
            item {
                NotificationSectionHeader(title = stringResource(R.string.notif_incomplete_section))
            }
            items(state.incompleteSubjects, key = { "inc_${it.subject.id}" }) { item ->
                SubjectAlertCard(
                    icon = Icons.Default.Warning,
                    iconTint = Color(0xFFF57C00),
                    iconBg = Color(0xFFFFF3E0),
                    title = item.subject.name,
                    body = stringResource(R.string.notif_incomplete_body, item.remainingPercentage.toInt())
                )
            }
        }

        if (state.passingGrades.isNotEmpty()) {
            item {
                NotificationSectionHeader(title = stringResource(R.string.notif_achievements_section))
            }
            items(state.passingGrades, key = { "pass_${it.subject.id}" }) { item ->
                SubjectAlertCard(
                    icon = Icons.Default.CheckCircle,
                    iconTint = Color(0xFF2E7D32),
                    iconBg = Color(0xFFE8F5E9),
                    title = stringResource(R.string.notif_congratulations),
                    body = stringResource(R.string.notif_passing_body, item.subject.name, item.average)
                )
            }
        }
    }
}

@Composable
private fun NotificationSectionHeader(title: String) {
    Text(
        text = title,
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun ReminderCard(item: NotificationItem.Reminder) {
    val now = System.currentTimeMillis()
    val diff = item.evento.fecha - now
    val timeLabel = formatTimeLeft(diff)
    val dateLabel = SimpleDateFormat("d MMM, HH:mm", Locale("es")).format(Date(item.evento.fecha))
    val reminderBody = stringResource(R.string.notif_reminder_body, timeLabel, dateLabel)

    val title = buildString {
        append(item.evento.titulo)
        if (!item.subjectName.isNullOrBlank()) append(" - ${item.subjectName}")
    }

    NotificationCard(
        icon = Icons.Default.Notifications,
        iconTint = PurpleGradia,
        iconBg = Color(0xFFF3EEF8),
        title = title,
        body = reminderBody
    )
}

@Composable
private fun SubjectAlertCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    body: String
) {
    NotificationCard(icon = icon, iconTint = iconTint, iconBg = iconBg, title = title, body = body)
}

@Composable
private fun NotificationCard(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    body: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = body,
                    fontFamily = InterFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ProximityCard(item: NotificationItem.ActivityProximityNotification) {
    val isDark = isSystemInDarkTheme()
    val (iconTint, iconBg, icon) = when {
        item.isCompleted -> Triple(
            Color(0xFF2E7D32),
            Color(0xFFE8F5E9),
            Icons.Default.CheckCircle
        )
        item.daysRemaining < 0 || item.daysRemaining <= 1 -> Triple(
            Color(0xFFE53935),
            Color(0xFFFFEBEE),
            Icons.Default.Warning
        )
        item.daysRemaining <= 3 -> Triple(
            Color(0xFFF57C00),
            Color(0xFFFFF3E0),
            Icons.Default.Notifications
        )
        else -> Triple(
            if (isDark) Color(0xFFCE93D8) else Color(0xFF7B1FA2),
            if (isDark) Color(0xFF4A235A) else Color(0xFFF3EEF8),
            Icons.Default.Notifications
        )
    }

    val title = buildString {
        append(item.activityTitle)
        if (!item.subjectName.isNullOrBlank()) append(" - ${item.subjectName}")
    }

    NotificationCard(
        icon = icon,
        iconTint = iconTint,
        iconBg = iconBg,
        title = title,
        body = item.message
    )
}

@Composable
private fun formatTimeLeft(diffMs: Long): String {
    if (diffMs <= 0) return stringResource(R.string.notif_now)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
    val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
    val days = TimeUnit.MILLISECONDS.toDays(diffMs)
    return when {
        minutes < 60 -> "${minutes}min"
        hours < 24 -> "${hours}h ${minutes % 60}min"
        else -> "${days}d ${hours % 24}h"
    }
}
