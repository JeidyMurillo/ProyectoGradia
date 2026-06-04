package com.example.gradia.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.gradia.GradiaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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

                val notas = app.notaRepository.getAllNotasConRecordatorioPendientesSync(now)
                val currentUser = app.userRepository.getCurrentUser().first()
                val userId = currentUser?.id
                if (userId != null) {
                    notas.forEach { nota ->
                        app.reminderScheduler.scheduleReminder(
                            eventId = nota.id + ReminderScheduler.NOTA_OFFSET,
                            title = nota.nombre,
                            eventTimeMs = nota.fecha ?: return@forEach,
                            minutesBefore = nota.recordatorioMinutosAntes,
                            userId = userId,
                            esNota = true
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
