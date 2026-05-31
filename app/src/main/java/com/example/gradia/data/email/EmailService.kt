package com.example.gradia.data.email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class EmailService(private val apiKey: String) {

    suspend fun sendWelcomeEmail(userName: String, userEmail: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.sendgrid.com/v3/mail/send")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true

                val jsonPayload = buildJson(userName, userEmail)
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonPayload)
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                if (responseCode in 200..299) {
                    Result.success(Unit)
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    Result.failure(Exception("SendGrid error $responseCode: $errorBody"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun buildJson(userName: String, userEmail: String): String {
        val html = buildHtml(userName)
        return """
        {
            "personalizations": [{
                "to": [{"email": "$userEmail", "name": "$userName"}]
            }],
            "from": {"email": "abyssprueba@gmail.com", "name": "Gradia"},
            "subject": "${"¡Bienvenido a Gradia, $userName!"}",
            "content": [{
                "type": "text/html",
                "value": ${jsonEncode(html)}
            }]
        }
        """.trimIndent()
    }

    private fun buildHtml(userName: String): String {
        return """
        <!DOCTYPE html>
        <html>
        <head><meta charset="UTF-8"></head>
        <body style="font-family: Arial, sans-serif; background-color: #f5f5f5; margin: 0; padding: 0;">
            <div style="max-width: 600px; margin: 40px auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.1);">
                <div style="background: linear-gradient(135deg, #6750A4, #7C52A0); padding: 32px; text-align: center;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">${"¡Bienvenido a Gradia!"}</h1>
                </div>
                <div style="padding: 32px; color: #333;">
                    <p style="font-size: 18px;">${"Hola $userName,"}</p>
                    <p style="font-size: 16px; line-height: 1.6;">
                        Gracias por registrarte en <strong>Gradia</strong>, tu gestor inteligente de notas universitarias.
                        Estamos emocionados de tenerte a bordo.
                    </p>
                    <p style="font-size: 16px; line-height: 1.6;">Con Gradia puedes:</p>
                    <ul style="font-size: 16px; line-height: 1.8; padding-left: 20px;">
                        <li>Gestionar tus asignaturas y notas</li>
                        <li>Calcular tu promedio y nota necesaria</li>
                        <li>Tomar notas organizadas por categorías</li>
                        <li>Administrar tu calendario académico</li>
                    </ul>
                    <p style="font-size: 16px; line-height: 1.6;">
                        Empieza ahora y organiza tu vida universitaria de la mejor manera.
                    </p>
                    <p style="font-size: 16px; color: #888; margin-top: 32px;">
                        Saludos,<br>
                        <strong>El equipo de Gradia</strong>
                    </p>
                </div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun jsonEncode(value: String): String {
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }
}
