package com.example.gradia.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.BuildConfig
import com.example.gradia.R
import com.example.gradia.ui.theme.PurpleGradia
import com.example.gradia.ui.theme.InterFontFamily

@Composable
fun SettingsScreen(
    onNavigateToProfile: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {}
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showComingSoonDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.language),
                title = "Idioma",
                value = "Español",
                onClick = { showComingSoonDialog = true }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.user_outline),
                title = "Cuenta",
                onClick = onNavigateToProfile
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.help),
                title = "Ayuda y Soporte",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:soporte@gradia.app")
                        putExtra(Intent.EXTRA_SUBJECT, "Ayuda y Soporte - Gradia")
                    }
                    context.startActivity(intent)
                }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.faq),
                title = "FAQ (Preguntas Frecuentes)",
                onClick = { showFaqDialog = true }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.email),
                title = "Contacto",
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:soporte@gradia.app")
                        putExtra(Intent.EXTRA_SUBJECT, "Contacto - Gradia")
                    }
                    context.startActivity(intent)
                }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.star),
                title = "Valorar App",
                onClick = {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
                    } catch (_: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                    }
                }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.share),
                title = "Compartir App",
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Descarga Gradia y controla tus notas: https://play.google.com/store/apps/details?id=${context.packageName}")
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir Gradia"))
                }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.document),
                title = "Políticas de Privacidad",
                onClick = { showPrivacyDialog = true }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.document),
                title = "Términos y Condiciones",
                onClick = onNavigateToTerms
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.information),
                title = "Acerca de",
                onClick = { showAboutDialog = true }
            )
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("Acerca de Gradia", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Versión: ${BuildConfig.VERSION_NAME}", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Gradia es una aplicación móvil diseñada para que estudiantes universitarios puedan llevar el control de sus notas, materias, metas académicas y progreso personal.",
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Integrantes:", fontWeight = FontWeight.Medium)
                    Text("- Karol T. Burbano N.", color = Color.Gray)
                    Text("- Sebastian Castro R.", color = Color.Gray)
                    Text("- Verónica L. Mujica G.", color = Color.Gray)
                    Text("- Sofía C. Quenoran", color = Color.Gray)
                    Text("- Andrés F. Salcedo B.", color = Color.Gray)
                    Text("- Jeidy N. Murillo M.", color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showComingSoonDialog) {
        AlertDialog(
            onDismissRequest = { showComingSoonDialog = false },
            title = {
                Text("Idioma", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Próximamente estarán disponibles más idiomas.", color = Color.Gray)
            },
            confirmButton = {
                TextButton(onClick = { showComingSoonDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = {
                Text("Preguntas Frecuentes", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    FAQItem("¿Cómo agrego una materia?", "Ve a la pestaña Materias y presiona el botón + para añadir una nueva materia.")
                    FAQItem("¿Cómo calculo mi nota final?", "Usa la calculadora de Nota Final desde el menú principal.")
                    FAQItem("¿Los datos se guardan en la nube?", "Tus datos se sincronizan con Firebase cuando inicias sesión.")
                    FAQItem("¿Puedo usar Gradia sin internet?", "Sí, los datos se almacenan localmente y se sincronizan cuando tengas conexión.")
                    FAQItem("¿Cómo elimino mi cuenta?", "Ve a Perfil y selecciona la opción de eliminar cuenta.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showFaqDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text("Políticas de Privacidad", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "En Gradia, valoramos tu privacidad. Los datos académicos que ingresas (notas, materias, metas) se almacenan localmente en tu dispositivo y, si utilizas sincronización, en servidores protegidos de Firebase.\n\n" +
                    "No vendemos ni compartimos tu información académica personal con terceros. Podemos recopilar datos de uso anónimos para mejorar la experiencia de la aplicación.\n\n" +
                    "Puedes solicitar la eliminación de tus datos en cualquier momento contactando a soporte@gradia.app.",
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun FAQItem(question: String, answer: String) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = question,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = PurpleGradia
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = answer,
            fontSize = 13.sp,
            color = Color.Gray
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), color = Color.LightGray.copy(alpha = 0.3f))
    }
}

@Composable
fun SettingsItem(
    iconPainter: Painter,
    title: String,
    value: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = iconPainter,
            contentDescription = null,
            tint = PurpleGradia,
            modifier = Modifier.size(28.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = InterFontFamily,
                color = PurpleGradia,
                fontWeight = FontWeight.Medium,
                fontSize = 17.sp
            ),
            modifier = Modifier.weight(1f)
        )

        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = InterFontFamily,
                    color = Color.LightGray
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = PurpleGradia,
            modifier = Modifier.size(24.dp)
        )
    }
}
