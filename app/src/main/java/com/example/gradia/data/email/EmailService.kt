package com.example.gradia.data.email

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class EmailService(private val apiKey: String) {

    suspend fun sendWelcomeEmail(userName: String, userEmail: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKeyPreview = if (apiKey.length >= 8) "${apiKey.take(8)}..." else "muy corta"
                Log.d("EmailService", "Enviando email a $userEmail | API key: $apiKeyPreview")

                val url = URL("https://api.sendgrid.com/v3/mail/send")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer $apiKey")
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true
                connection.connectTimeout = 15000
                connection.readTimeout = 15000

                val jsonPayload = buildJson(userName, userEmail)
                val writer = OutputStreamWriter(connection.outputStream, "UTF-8")
                writer.write(jsonPayload)
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                Log.d("EmailService", "SendGrid respuesta: $responseCode")

                if (responseCode in 200..299) {
                    Log.d("EmailService", "Email enviado exitosamente a $userEmail")
                    Result.success(Unit)
                } else {
                    val errorBody = try {
                        connection.errorStream?.bufferedReader()?.readText()
                            ?: "(no error body)"
                    } catch (e: Exception) {
                        "(error al leer respuesta: ${e.message})"
                    }
                    Log.e("EmailService", "SendGrid error $responseCode: $errorBody")
                    Result.failure(Exception("SendGrid error $responseCode: $errorBody"))
                }
            } catch (e: Exception) {
                Log.e("EmailService", "Error conectando con SendGrid", e)
                Result.failure(e)
            }
        }
    }

    private fun buildJson(userName: String, userEmail: String): String {
        val html = buildHtml(userName)

        val toObj = JSONObject().apply {
            put("email", userEmail)
            put("name", userName)
        }
        val personalization = JSONObject().apply {
            put("to", JSONArray().put(toObj))
        }
        val contentObj = JSONObject().apply {
            put("type", "text/html")
            put("value", html)
        }
        return JSONObject().apply {
            put("personalizations", JSONArray().put(personalization))
            put("from", JSONObject().apply {
                put("email", "abyssprueba@gmail.com")
                put("name", "Gradia")
            })
            put("subject", "\u00a1Bienvenido a Gradia, $userName!")
            put("content", JSONArray().put(contentObj))
        }.toString()
    }

    private fun buildHtml(userName: String): String {
        return """<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body style="margin: 0; padding: 0; background-color: #f0f0f5; font-family: 'Segoe UI', Arial, Helvetica, sans-serif;">
    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%" style="background-color: #f0f0f5;">
        <tr>
            <td align="center" style="padding: 24px 16px;">
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="600" style="max-width: 600px; width: 100%;">
                    ${buildHeaderSection()}
                    ${buildContentSection(userName)}
                    ${buildFooterSection()}
                </table>
            </td>
        </tr>
    </table>
</body>
</html>""".trimIndent()
    }

    private fun buildHeaderSection(): String {
        val subtitle = "Tu gestor acad\u00e9mico inteligente"
        return """
            <tr>
                <td bgcolor="#6750A4" style="background-color: #6750A4; border-radius: 20px 20px 0 0; padding: 48px 32px 40px; text-align: center;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center">
                        <tr>
                            <td align="center" style="padding-bottom: 20px;">
                                ${svgGradiaLogo()}
                            </td>
                        </tr>
                        <tr>
                            <td align="center">
                                <h1 style="color: #FFFFFF; font-size: 32px; font-weight: 700; margin: 0; letter-spacing: 1px;">Bienvenido a Gradia</h1>
                                <p style="color: #EADDFF; font-size: 16px; margin: 12px 0 0 0;">$subtitle</p>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        """.trimIndent()
    }

    private fun a(text: String): String = text

    private fun buildContentSection(userName: String): String {
        val sd = buildSectionDivider()
        val qs = buildQuoteSection()
        val a = a("Gesti\u00f3n de Asignaturas")

        val p1 = "Gracias por registrarte en Gradia. Estamos muy contentos de darte la bienvenida a nuestra comunidad de estudiantes organizados que, como t\u00fa, buscan llevar su rendimiento acad\u00e9mico al siguiente nivel."

        val p2 = "Sabemos lo desafiante que puede ser la vida universitaria. Entre clases, ex\u00e1menes, trabajos en grupo y actividades extracurriculares, mantener todo en orden no es tarea f\u00e1cil. Por eso creamos Gradia: para que puedas enfocarte en lo que realmente importa aprender y crecer acad\u00e9micamente."

        val qg = "\u00bfQu\u00e9 es Gradia?"
        val pg1 = "Gradia naci\u00f3 como un proyecto universitario con una visi\u00f3n clara: transformar la manera en que los estudiantes gestionan su vida acad\u00e9mica. No somos solo una calculadora de notas o un gestor de tareas; somos una herramienta integral que te acompa\u00f1a en cada paso de tu trayectoria universitaria."

        val pg2 = "Nuestra misi\u00f3n es ayudarte a visualizar tu progreso, identificar \u00e1reas de mejora y tomar decisiones informadas sobre tu rendimiento acad\u00e9mico. Creemos que la organizaci\u00f3n es la clave del \u00e9xito, y queremos proporcionarte todas las herramientas necesarias para que alcances tus metas."

        val t1 = "Agrega tus asignaturas"
        val d1 = "Ingresa las materias que est\u00e1s cursando este semestre con sus respectivos cr\u00e9ditos. As\u00edgnales un color para identificarlas r\u00e1pidamente."

        val t2 = "Registra tus notas y tareas"
        val d2 = "Ingresa tus calificaciones parciales, agrega eventos importantes y toma notas organizadas por categor\u00edas para cada asignatura."

        val t3 = "Sigue tu progreso"
        val d3 = "Revisa tus estad\u00edsticas, calcula tu promedio actual y la nota que necesitas para alcanzar tus metas. Ajusta tu estrategia de estudio seg\u00fan los resultados."

        val sh = "\u00bfNecesitas ayuda?"
        val shd = "Estamos aqu\u00ed para ayudarte. Si tienes alguna duda, sugerencia o encuentras alg\u00fan problema, no dudes en escribirnos a "
        val shd2 = "Nos encanta recibir comentarios de nuestros usuarios para seguir mejorando."

        val c1 = "Bienvenido a bordo, $userName. Estamos seguros de que Gradia se convertir\u00e1 en una herramienta indispensable en tu vida universitaria."
        val c2 = "\u00a1Mucho \u00e9xito en tus estudios!"
        val c3 = "Atentamente,"
        val c4 = "El equipo de Gradia"

        return """
            <tr>
                <td style="background-color: #FFFFFF; padding: 40px 32px;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%">
                        <tr><td style="padding-bottom: 24px; font-size: 18px; color: #1D1B20; line-height: 1.7;">
                            <span style="font-size: 22px; font-weight: 600;">Hola $userName,</span>
                        </td></tr>

                        <tr><td style="padding-bottom: 16px; font-size: 15px; color: #333; line-height: 1.7;">
                            $p1
                        </td></tr>

                        <tr><td style="padding-bottom: 16px; font-size: 15px; color: #333; line-height: 1.7;">
                            $p2
                        </td></tr>

                        $sd

                        <tr><td style="padding-bottom: 20px;">
                            <h2 style="color: #1D1B20; font-size: 22px; margin: 0 0 8px 0;">$qg</h2>
                            <p style="font-size: 15px; color: #333; line-height: 1.7; margin: 0;">
                                $pg1
                            </p>
                        </td></tr>
                        <tr><td style="padding-bottom: 24px; font-size: 15px; color: #333; line-height: 1.7;">
                            $pg2
                        </td></tr>

                        $sd

                        <tr><td style="padding-bottom: 24px;">
                            <h2 style="color: #1D1B20; font-size: 22px; margin: 0 0 24px 0; text-align: center;">Descubre todo lo que puedes hacer</h2>

                            ${buildFeatureCard(svgBook(), "GESTI\u00d3N DE ASIGNATURAS", "Organiza todas tus materias en un solo lugar. Agrega tus asignaturas con sus respectivos cr\u00e9ditos, horarios y colores identificativos. Lleva el control de cada materia de forma independiente y visualiza tu carga acad\u00e9mica completa.")}

                            ${buildFeatureCard(svgChart(), "C\u00c1LCULO DE NOTAS Y PROMEDIOS", "Olv\u00eddate de las hojas de c\u00e1lculo complicadas. Ingresa tus notas parciales y deja que Gradia calcule autom\u00e1ticamente tu promedio actual, el porcentaje restante y la nota que necesitas para aprobar o alcanzar la calificaci\u00f3n que deseas.")}

                            ${buildFeatureCard(svgNote(), "NOTAS Y APUNTES INTELIGENTES", "Toma notas organizadas por categor\u00edas y vinc\u00falalas a tus asignaturas. Nuestro sistema de notas inteligentes te permite crear, editar y categorizar apuntes de manera r\u00e1pida, con soporte para contenido enriquecido y b\u00fasqueda instant\u00e1nea.")}

                            ${buildFeatureCard(svgCalendar(), "CALENDARIO ACAD\u00c9MICO", "No pierdas ninguna fecha importante. Administra tus ex\u00e1menes, entregas de trabajos, feriados y eventos acad\u00e9micos en un calendario integrado. Recibe recordatorios y mant\u00e9n tu semestre perfectamente planificado.")}

                            ${buildFeatureCardLast(svgTarget(), "METAS Y ESTAD\u00cdSTICAS", "Establece metas acad\u00e9micas y da seguimiento a tu progreso con gr\u00e1ficos visuales y estad\u00edsticas detalladas. Visualiza tu evoluci\u00f3n a lo largo del semestre, identifica tendencias y celebra tus logros. Cada peque\u00f1o avance cuenta.")}
                        </td></tr>

                        $sd

                        <tr><td style="padding: 8px 0 24px;">
                            <h2 style="color: #1D1B20; font-size: 22px; margin: 0 0 20px 0; text-align: center;">3 pasos para empezar</h2>

                            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%">
                                <tr>
                                    <td width="40" valign="top" style="padding: 0 12px 16px 0;">
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="36" height="36" style="background-color: #6750A4; border-radius: 50%;">
                                            <tr><td align="center" valign="middle" style="font-size: 16px; font-weight: 700; color: #FFFFFF; text-align: center;">1</td></tr>
                                        </table>
                                    </td>
                                    <td valign="top" style="padding-bottom: 20px;">
                                        <p style="font-size: 15px; font-weight: 600; color: #1D1B20; margin: 0 0 4px 0;">$t1</p>
                                        <p style="font-size: 14px; color: #555; line-height: 1.6; margin: 0;">$d1</p>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="40" valign="top" style="padding: 0 12px 16px 0;">
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="36" height="36" style="background-color: #6750A4; border-radius: 50%;">
                                            <tr><td align="center" valign="middle" style="font-size: 16px; font-weight: 700; color: #FFFFFF; text-align: center;">2</td></tr>
                                        </table>
                                    </td>
                                    <td valign="top" style="padding-bottom: 20px;">
                                        <p style="font-size: 15px; font-weight: 600; color: #1D1B20; margin: 0 0 4px 0;">$t2</p>
                                        <p style="font-size: 14px; color: #555; line-height: 1.6; margin: 0;">$d2</p>
                                    </td>
                                </tr>
                                <tr>
                                    <td width="40" valign="top" style="padding: 0 12px 0 0;">
                                        <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="36" height="36" style="background-color: #6750A4; border-radius: 50%;">
                                            <tr><td align="center" valign="middle" style="font-size: 16px; font-weight: 700; color: #FFFFFF; text-align: center;">3</td></tr>
                                        </table>
                                    </td>
                                    <td valign="top">
                                        <p style="font-size: 15px; font-weight: 600; color: #1D1B20; margin: 0 0 4px 0;">$t3</p>
                                        <p style="font-size: 14px; color: #555; line-height: 1.6; margin: 0;">$d3</p>
                                    </td>
                                </tr>
                            </table>
                        </td></tr>

                        $qs

                        $sd

                        <tr><td style="padding-top: 8px;">
                            <h2 style="color: #1D1B20; font-size: 20px; margin: 0 0 12px 0;">$sh</h2>
                            <p style="font-size: 15px; color: #333; line-height: 1.7; margin: 0;">
                                $shd
                                <a href="mailto:soporte@gradia.app" style="color: #6750A4; text-decoration: underline;">soporte@gradia.app</a>.
                                $shd2
                            </p>
                        </td></tr>

                        <tr><td style="padding-top: 32px;">
                            <p style="font-size: 16px; color: #1D1B20; line-height: 1.7;">
                                $c1
                            </p>
                            <p style="font-size: 16px; color: #333; line-height: 1.7; margin-top: 16px;">
                                $c2
                            </p>
                            <p style="font-size: 16px; color: #1D1B20; line-height: 1.7; margin-top: 24px;">
                                $c3,<br>
                                <strong style="color: #6750A4; font-size: 17px;">$c4</strong>
                            </p>
                        </td></tr>
                    </table>
                </td>
            </tr>
        """.trimIndent()
    }

    private fun buildSectionDivider(): String {
        val star = "\u2726"
        return """
            <tr><td align="center" style="padding: 24px 0;">
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="80" align="center">
                    <tr>
                        <td style="height: 2px; background-color: #6750A4;"></td>
                    </tr>
                </table>
                <p style="color: #6750A4; font-size: 14px; margin: 8px 0 0; letter-spacing: 8px;">$star $star $star</p>
            </td></tr>
        """.trimIndent()
    }

    private fun buildQuoteSection(): String {
        val q1 = "\"El \u00e9xito acad\u00e9mico no es casualidad. Es disciplina, organizaci\u00f3n y la convicci\u00f3n de que cada peque\u00f1o esfuerzo diario construye el camino hacia tus metas m\u00e1s grandes.\""
        return """
            <tr><td style="padding: 24px 0;">
                <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%" style="background-color: #F3EFF9; border-radius: 12px; border-left: 4px solid #6750A4;">
                    <tr><td style="padding: 24px;">
                        <p style="font-size: 16px; color: #1D1B20; line-height: 1.7; font-style: italic; margin: 0 0 8px 0;">
                            $q1
                        </p>
                        <p style="font-size: 14px; color: #6750A4; font-weight: 600; margin: 0;">
                            &mdash; Gradia
                        </p>
                    </td></tr>
                </table>
            </td></tr>
        """.trimIndent()
    }

    private fun buildFeatureCard(svg: String, title: String, description: String): String {
        return """
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%" style="background-color: #FAFAFE; border-radius: 12px; margin-bottom: 16px; border: 1px solid #EDE7F6;">
                <tr>
                    <td width="64" valign="top" style="padding: 20px 0 20px 20px; text-align: center;">
                        $svg
                    </td>
                    <td valign="top" style="padding: 20px 20px 20px 12px;">
                        <p style="font-size: 14px; font-weight: 700; color: #6750A4; margin: 0 0 6px 0; letter-spacing: 0.5px;">$title</p>
                        <p style="font-size: 14px; color: #444; line-height: 1.6; margin: 0;">$description</p>
                    </td>
                </tr>
            </table>
        """.trimIndent()
    }

    private fun buildFeatureCardLast(svg: String, title: String, description: String): String {
        return """
            <table role="presentation" cellspacing="0" cellpadding="0" border="0" width="100%" style="background-color: #FAFAFE; border-radius: 12px; margin-bottom: 0; border: 1px solid #EDE7F6;">
                <tr>
                    <td width="64" valign="top" style="padding: 20px 0 20px 20px; text-align: center;">
                        $svg
                    </td>
                    <td valign="top" style="padding: 20px 20px 20px 12px;">
                        <p style="font-size: 14px; font-weight: 700; color: #6750A4; margin: 0 0 6px 0; letter-spacing: 0.5px;">$title</p>
                        <p style="font-size: 14px; color: #444; line-height: 1.6; margin: 0;">$description</p>
                    </td>
                </tr>
            </table>
        """.trimIndent()
    }

    private fun buildFooterSection(): String {
        val footer1 = "\u00a9 2026 Gradia. Todos los derechos reservados."
        val footer2 = "Hecho con \u2764\ufe0f para estudiantes universitarios"
        return """
            <tr>
                <td bgcolor="#1D1B20" style="background-color: #1D1B20; border-radius: 0 0 20px 20px; padding: 32px; text-align: center;">
                    <table role="presentation" cellspacing="0" cellpadding="0" border="0" align="center">
                        <tr>
                            <td align="center" style="padding-bottom: 16px;">
                                ${svgFooterLogo()}
                            </td>
                        </tr>
                        <tr>
                            <td align="center" style="color: #B0AEB8; font-size: 13px; line-height: 1.6;">
                                <p style="margin: 0 0 4px 0;">$footer1</p>
                                <p style="margin: 0;">$footer2</p>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        """.trimIndent()
    }

    private fun svgGradiaLogo(): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 288 288" width="100" height="100">
                <path d="M172.45 172.57c-1.69 0.86-4.47 2.9-6.13 4.06-2.82 1.94-5.63 3.9-8.42 5.9-2.89 2.07-7.55 5.96-11.3 4.58-2.03-0.75-4.93-2.96-6.74-4.23-2.95-2.06-5.88-4.13-8.8-6.23-12.38-8.63-24.57-17.53-36.57-26.67-3.44-2.63-6.95-5.34-10.26-8.13-1.47-1.33-3.14-2.57-4.3-4.17-3.5-4.78-3.42-10.86 1.95-14.3 1.1-0.7 2.26-1.4 3.36-2.08l8.2-5.05 23.52-14.28 15.47-9.42c2.2-1.33 4.38-2.83 6.69-3.97 1.8-0.8 4.2-0.58 5.84 0.5 2.12 1.38 4.23 2.86 6.38 4.23l21.78 14.14 16.6 10.74c4.46 2.85 8.9 5.73 13.33 8.65 1.07 0.71 2.22 1.8 2.48 3.08 0.12 0.43 0.04 1.65-0.3 1.97-2.65 2.51-6.42 4.77-9.42 6.85l-29.05 20.16-10.01 6.9c-2.65 1.82-5.45 3.67-7.85 5.8-1.39 1.23-1.7 2.27-2.19 3.98-0.56 2.03 1.2 6.05 3.82 4.4 6-4.38 12.5-8.37 18.47-12.76 0.78 0.38 2.84 3.76 3.22 4.72 0.07 0.16 0.02 0.4-0.01 0.57l0.24 0.06Z" fill="#FFFFFF"/>
                <path d="M99.01 157.04c1.34 1.23 4.06 3.08 5.62 4.25 4.74 3.55 9.53 7.04 14.36 10.47 6.36 4.63 12.77 9.18 19.23 13.68 2.45 1.69 4.92 3.45 7.4 5.1 0.67 0.44 1.76 0.96 2.57 0.9 1.05-0.1 2-0.26 2.94-0.78 2.83-1.54 5.47-3.54 8.1-5.39l11.08-7.68c0.32-0.22 3.2-2.07 3.26-2.15-0.44-0.92-0.72-1.92-1.12-2.87l-0.24-0.06c0.03-0.18 0.08-0.4 0.01-0.57-0.38-0.96-2.44-4.34-3.22-4.72-0.8-1-2.37-2.02-3.46-2.79 0.72-0.9 7.66-6 8.61-6.18 0.78 0.22 3.1 2.29 3.75 2.94 0.7 1.05 3.1 4.04 3.3 5.1 0.18 0.37 0.43 0.71 0.64 1.1 0.21 0.39 0.56 1.45 0.74 1.72 1.68-0.91 3.91-2.78 5.97-3.97-0.35 1.58-0.24 4.84-0.24 6.63v14.5c0 2.39 0.25 5.02-0.1 7.38-0.24 1.59-4.73 4.98-6.15 6-9.81 7.07-24.28 9.73-36.25 9.64-4.84-0.06-9.68-0.47-14.47-1.23-10.72-1.6-29.5-7.07-32.11-19.3-0.27-1.25-0.2-3.58-0.2-4.95l0.01-6.54c0-6.68-0.11-13.57-0.03-20.23Z" fill="#FFFFFF"/>
                <path d="M165.54 164.43c0.72-0.9 7.66-6 8.61-6.18 0.78 0.22 3.1 2.29 3.75 2.94 0.7 1.05 3.1 4.04 3.3 5.1 0.18 0.37 0.43 0.71 0.64 1.1 0.21 0.39 0.56 1.45 0.74 1.72l0.02 0.11c0.23 1.2 0.85 2.6 1.05 3.69 0.47 2.6 0.87 5.63 1.07 8.26-1.63-0.35-3.2-0.8-4.8-1.25-0.9 1.53-3.18 6-3.88 7.7-0.47-3.49-0.98-9.07-2.47-12.18-0.44-0.92-0.72-1.92-1.12-2.87l-0.24-0.06c0.03-0.18 0.08-0.4 0.01-0.57-0.38-0.96-2.44-4.34-3.22-4.72-0.8-1-2.37-2.02-3.46-2.79Z" fill="#EADDFF"/>
                <path d="M177.9 161.2c0.6-0.35 1.29-0.96 1.9-1.38 1.98-1.34 3.94-2.7 5.9-4.05 5.9-4.11 11.85-8.18 17.83-12.18 0.93-0.63 4.4-3.2 5.18-3.49 1.49 0.54 2.25 3.95 1.49 5.26-1.5 2.55-5.12 4.56-7.57 6.24l-9.36 6.45-7.56 5.2c-1.3 0.9-3.16 2.32-4.5 3.04-0.21-1.06-2.6-4.05-3.31-5.1Z" fill="#FFFFFF"/>
            </svg>
        """.trimIndent()
    }

    private fun svgBook(): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40" width="44" height="44">
                <path d="M6 8h14v26H6z" fill="none" stroke="#6750A4" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M20 8h14v26H20z" fill="none" stroke="#6750A4" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M20 8v26" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round"/>
                <path d="M9 14h8" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
                <path d="M9 19h8" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
                <path d="M9 24h5" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
                <path d="M23 14h8" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
                <path d="M23 19h8" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
            </svg>
        """.trimIndent()
    }

    private fun svgChart(): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40" width="44" height="44">
                <path d="M4 32h34" fill="none" stroke="#6750A4" stroke-width="2.5" stroke-linecap="round"/>
                <path d="M6 32V10" fill="none" stroke="#6750A4" stroke-width="2.5" stroke-linecap="round"/>
                <rect x="10" y="20" width="6" height="12" rx="1.5" fill="#C7B5E0"/>
                <rect x="19" y="14" width="6" height="18" rx="1.5" fill="#9F7FCF"/>
                <rect x="28" y="8" width="6" height="24" rx="1.5" fill="#6750A4"/>
            </svg>
        """.trimIndent()
    }

    private fun svgNote(): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40" width="44" height="44">
                <path d="M8 4h16l12 12v20a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z" fill="none" stroke="#6750A4" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M24 4v12h12" fill="none" stroke="#6750A4" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 18h16" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
                <path d="M12 24h16" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
                <path d="M12 30h10" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.6"/>
            </svg>
        """.trimIndent()
    }

    private fun svgCalendar(): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40" width="44" height="44">
                <rect x="4" y="6" width="32" height="30" rx="4" fill="none" stroke="#6750A4" stroke-width="2.5"/>
                <path d="M4 14h32" fill="none" stroke="#6750A4" stroke-width="2.5"/>
                <path d="M12 2v8" fill="none" stroke="#6750A4" stroke-width="2.5" stroke-linecap="round"/>
                <path d="M28 2v8" fill="none" stroke="#6750A4" stroke-width="2.5" stroke-linecap="round"/>
                <rect x="14" y="20" width="12" height="10" rx="2" fill="#C7B5E0"/>
                <text x="20" y="28" text-anchor="middle" font-family="Arial, sans-serif" font-size="8" font-weight="bold" fill="#6750A4">HOY</text>
            </svg>
        """.trimIndent()
    }

    private fun svgTarget(): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40" width="44" height="44">
                <circle cx="20" cy="20" r="16" fill="none" stroke="#6750A4" stroke-width="2.5"/>
                <circle cx="20" cy="20" r="10" fill="none" stroke="#9F7FCF" stroke-width="2"/>
                <circle cx="20" cy="20" r="4" fill="#6750A4"/>
                <path d="M20 2v6" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
                <path d="M20 32v6" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
                <path d="M2 20h6" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
                <path d="M32 20h6" fill="none" stroke="#6750A4" stroke-width="2" stroke-linecap="round" opacity="0.5"/>
            </svg>
        """.trimIndent()
    }

    private fun svgFooterLogo(): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 288 288" width="28" height="28">
                <path d="M172.45 172.57c-1.69 0.86-4.47 2.9-6.13 4.06-2.82 1.94-5.63 3.9-8.42 5.9-2.89 2.07-7.55 5.96-11.3 4.58-2.03-0.75-4.93-2.96-6.74-4.23-2.95-2.06-5.88-4.13-8.8-6.23-12.38-8.63-24.57-17.53-36.57-26.67-3.44-2.63-6.95-5.34-10.26-8.13-1.47-1.33-3.14-2.57-4.3-4.17-3.5-4.78-3.42-10.86 1.95-14.3 1.1-0.7 2.26-1.4 3.36-2.08l8.2-5.05 23.52-14.28 15.47-9.42c2.2-1.33 4.38-2.83 6.69-3.97 1.8-0.8 4.2-0.58 5.84 0.5 2.12 1.38 4.23 2.86 6.38 4.23l21.78 14.14 16.6 10.74c4.46 2.85 8.9 5.73 13.33 8.65 1.07 0.71 2.22 1.8 2.48 3.08 0.12 0.43 0.04 1.65-0.3 1.97-2.65 2.51-6.42 4.77-9.42 6.85l-29.05 20.16-10.01 6.9c-2.65 1.82-5.45 3.67-7.85 5.8-1.39 1.23-1.7 2.27-2.19 3.98-0.56 2.03 1.2 6.05 3.82 4.4 6-4.38 12.5-8.37 18.47-12.76 0.78 0.38 2.84 3.76 3.22 4.72 0.07 0.16 0.02 0.4-0.01 0.57l0.24 0.06Z" fill="#B0AEB8" opacity="0.6"/>
                <path d="M99.01 157.04c1.34 1.23 4.06 3.08 5.62 4.25 4.74 3.55 9.53 7.04 14.36 10.47 6.36 4.63 12.77 9.18 19.23 13.68 2.45 1.69 4.92 3.45 7.4 5.1 0.67 0.44 1.76 0.96 2.57 0.9 1.05-0.1 2-0.26 2.94-0.78 2.83-1.54 5.47-3.54 8.1-5.39l11.08-7.68c0.32-0.22 3.2-2.07 3.26-2.15-0.44-0.92-0.72-1.92-1.12-2.87l-0.24-0.06c0.03-0.18 0.08-0.4 0.01-0.57-0.38-0.96-2.44-4.34-3.22-4.72-0.8-1-2.37-2.02-3.46-2.79 0.72-0.9 7.66-6 8.61-6.18 0.78 0.22 3.1 2.29 3.75 2.94 0.7 1.05 3.1 4.04 3.3 5.1 0.18 0.37 0.43 0.71 0.64 1.1 0.21 0.39 0.56 1.45 0.74 1.72 1.68-0.91 3.91-2.78 5.97-3.97-0.35 1.58-0.24 4.84-0.24 6.63v14.5c0 2.39 0.25 5.02-0.1 7.38-0.24 1.59-4.73 4.98-6.15 6-9.81 7.07-24.28 9.73-36.25 9.64-4.84-0.06-9.68-0.47-14.47-1.23-10.72-1.6-29.5-7.07-32.11-19.3-0.27-1.25-0.2-3.58-0.2-4.95l0.01-6.54c0-6.68-0.11-13.57-0.03-20.23Z" fill="#B0AEB8" opacity="0.6"/>
                <path d="M165.54 164.43c0.72-0.9 7.66-6 8.61-6.18 0.78 0.22 3.1 2.29 3.75 2.94 0.7 1.05 3.1 4.04 3.3 5.1 0.18 0.37 0.43 0.71 0.64 1.1 0.21 0.39 0.56 1.45 0.74 1.72l0.02 0.11c0.23 1.2 0.85 2.6 1.05 3.69 0.47 2.6 0.87 5.63 1.07 8.26-1.63-0.35-3.2-0.8-4.8-1.25-0.9 1.53-3.18 6-3.88 7.7-0.47-3.49-0.98-9.07-2.47-12.18-0.44-0.92-0.72-1.92-1.12-2.87l-0.24-0.06c0.03-0.18 0.08-0.4 0.01-0.57-0.38-0.96-2.44-4.34-3.22-4.72-0.8-1-2.37-2.02-3.46-2.79Z" fill="#B0AEB8" opacity="0.4"/>
                <path d="M177.9 161.2c0.6-0.35 1.29-0.96 1.9-1.38 1.98-1.34 3.94-2.7 5.9-4.05 5.9-4.11 11.85-8.18 17.83-12.18 0.93-0.63 4.4-3.2 5.18-3.49 1.49 0.54 2.25 3.95 1.49 5.26-1.5 2.55-5.12 4.56-7.57 6.24l-9.36 6.45-7.56 5.2c-1.3 0.9-3.16 2.32-4.5 3.04-0.21-1.06-2.6-4.05-3.31-5.1Z" fill="#B0AEB8" opacity="0.6"/>
            </svg>
        """.trimIndent()
    }
}
