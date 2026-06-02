package com.example.gradia.notifications

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.gradia.GradiaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CompleteTaskReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_EVENT_ID = "extra_event_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra(EXTRA_EVENT_ID, -1L)
        if (eventId == -1L) return

        val app = context.applicationContext as GradiaApplication
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.eventoRepository.updateEstadoCompletadoById(eventId, true)
                app.reminderScheduler.cancelReminder(eventId)
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(eventId.toInt())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
