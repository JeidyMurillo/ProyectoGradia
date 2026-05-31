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
                                ${svgGraduationCap()}
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

    private fun svgGraduationCap(): String {
        return """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" width="80" height="80">
                <path d="M21.783,3.987a6.98,6.98 0,0 1,4.434 0c1.997,0.668 5.656,1.966 10.979,4.136c3.817,1.557 6.537,2.766 8.378,3.628c1.208,0.565 1.924,1.69 1.924,2.91c0,1.222 -0.716,2.346 -1.924,2.911a171,171 0,0 1,-6.895 3.018c0.04,0.969 0.066,2.07 0.066,3.31c0,3.1 -0.158,5.098 -0.326,6.349c-0.216,1.608 -1.155,3.033 -2.645,3.814c-1.998,1.049 -5.806,2.464 -11.748,2.464c-5.943,0 -9.75,-1.415 -11.75,-2.464c-1.49,-0.781 -2.428,-2.206 -2.644,-3.814c-0.168,-1.251 -0.326,-3.25 -0.326,-6.348c0,-1.232 0.025,-2.326 0.065,-3.29q-1.027,-0.426 -1.936,-0.813V34.91a4.95,4.95 0,0 1,2.936 4.52a4.94,4.94 0,0 1,-4.936 4.943A4.94,4.94 0,0 1,0.5 39.431a4.95,4.95 0,0 1,2.935 -4.52V18.04q-0.543,-0.248 -1.009,-0.467C1.22,17.007 0.502,15.883 0.502,14.662s0.717,-2.346 1.924,-2.911c1.842,-0.862 4.561,-2.071 8.379,-3.628c5.322,-2.17 8.981,-3.468 10.978,-4.136m-8.46,18.225q-0.017,0.791 -0.017,1.689c0,2.97 0.152,4.784 0.29,5.816c0.052,0.384 0.252,0.654 0.54,0.804c1.438,0.755 4.61,2.006 9.89,2.006s8.451,-1.25 9.89,-2.006c0.287,-0.15 0.487,-0.42 0.538,-0.804c0.139,-1.032 0.291,-2.845 0.291,-5.816q0,-0.91 -0.018,-1.709c-4.002,1.584 -6.838,2.584 -8.51,3.144a6.98,6.98 0,0 1,-4.434 0c-1.664,-0.557 -4.484,-1.551 -8.46,-3.124" fill="#FFFFFF" fill-rule="evenodd"/>
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
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" width="32" height="32">
                <path d="M21.783,3.987a6.98,6.98 0,0 1,4.434 0c1.997,0.668 5.656,1.966 10.979,4.136c3.817,1.557 6.537,2.766 8.378,3.628c1.208,0.565 1.924,1.69 1.924,2.91c0,1.222 -0.716,2.346 -1.924,2.911a171,171 0,0 1,-6.895 3.018c0.04,0.969 0.066,2.07 0.066,3.31c0,3.1 -0.158,5.098 -0.326,6.349c-0.216,1.608 -1.155,3.033 -2.645,3.814c-1.998,1.049 -5.806,2.464 -11.748,2.464c-5.943,0 -9.75,-1.415 -11.75,-2.464c-1.49,-0.781 -2.428,-2.206 -2.644,-3.814c-0.168,-1.251 -0.326,-3.25 -0.326,-6.348c0,-1.232 0.025,-2.326 0.065,-3.29q-1.027,-0.426 -1.936,-0.813V34.91a4.95,4.95 0,0 1,2.936 4.52a4.94,4.94 0,0 1,-4.936 4.943A4.94,4.94 0,0 1,0.5 39.431a4.95,4.95 0,0 1,2.935 -4.52V18.04q-0.543,-0.248 -1.009,-0.467C1.22,17.007 0.502,15.883 0.502,14.662s0.717,-2.346 1.924,-2.911c1.842,-0.862 4.561,-2.071 8.379,-3.628c5.322,-2.17 8.981,-3.468 10.978,-4.136m-8.46,18.225q-0.017,0.791 -0.017,1.689c0,2.97 0.152,4.784 0.29,5.816c0.052,0.384 0.252,0.654 0.54,0.804c1.438,0.755 4.61,2.006 9.89,2.006s8.451,-1.25 9.89,-2.006c0.287,-0.15 0.487,-0.42 0.538,-0.804c0.139,-1.032 0.291,-2.845 0.291,-5.816q0,-0.91 -0.018,-1.709c-4.002,1.584 -6.838,2.584 -8.51,3.144a6.98,6.98 0,0 1,-4.434 0c-1.664,-0.557 -4.484,-1.551 -8.46,-3.124" fill="#B0AEB8" fill-rule="evenodd" opacity="0.6"/>
            </svg>
        """.trimIndent()
    }
}
