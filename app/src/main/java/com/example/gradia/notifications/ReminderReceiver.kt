package com.example.gradia.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_USER_ID = "extra_user_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val userId = intent.getStringExtra(EXTRA_USER_ID) ?: return
        if (eventId == -1L) return
        NotificationHelper(context).showReminder(eventId, title, userId)
    }
}
