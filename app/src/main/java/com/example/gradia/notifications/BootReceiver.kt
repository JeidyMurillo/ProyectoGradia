package com.example.gradia.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.gradia.GradiaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val app = context.applicationContext as GradiaApplication
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val now = System.currentTimeMillis()
                val eventos = app.eventoRepository.getAllEventosPendientesSync(now)
                eventos.forEach { evento ->
                    app.reminderScheduler.scheduleReminder(
                        evento.id,
                        evento.titulo,
                        evento.fecha,
                        evento.recordatorioMinutosAntes,
                        evento.userId
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
