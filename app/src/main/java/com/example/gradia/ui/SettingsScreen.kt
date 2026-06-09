package com.example.gradia.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.BuildConfig
import com.example.gradia.R
import com.example.gradia.ui.theme.PurpleGradia
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.util.LocaleHelper

@Composable
fun SettingsScreen(
    onNavigateToAccount: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {}
) {
    val context = LocalContext.current
    var showAboutDialog by remember { mutableStateOf(false) }
    var showFaqDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    val currentLanguage = remember { LocaleHelper.getCurrentLanguageName(context) }

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
                title = stringResource(R.string.settings_language),
                value = currentLanguage,
                onClick = { showLanguageDialog = true }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.user_outline),
                title = stringResource(R.string.settings_account),
                onClick = onNavigateToAccount
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.help),
                title = stringResource(R.string.settings_help),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:soporte@gradia.app")
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject_help))
                    }
                    context.startActivity(intent)
                }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.faq),
                title = stringResource(R.string.settings_faq),
                onClick = { showFaqDialog = true }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.email),
                title = stringResource(R.string.settings_contact),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:soporte@gradia.app")
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject_contact))
                    }
                    context.startActivity(intent)
                }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.star),
                title = stringResource(R.string.settings_rate),
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
                title = stringResource(R.string.settings_share),
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_text))
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_subject)))
                }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.document),
                title = stringResource(R.string.settings_privacy),
                onClick = { showPrivacyDialog = true }
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.document),
                title = stringResource(R.string.settings_terms),
                onClick = onNavigateToTerms
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.information),
                title = stringResource(R.string.settings_about),
                onClick = { showAboutDialog = true }
            )
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(stringResource(R.string.about_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("${stringResource(R.string.about_version)}: ${BuildConfig.VERSION_NAME}", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.about_description),
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.about_team), fontWeight = FontWeight.Medium)
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
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (showLanguageDialog) {
        val languages = listOf("es", "en")
        LanguagePickerDialog(
            currentLanguage = LocaleHelper.getLanguage(context),
            languages = languages,
            onSelect = { code ->
                LocaleHelper.setLanguage(context, code)
                showLanguageDialog = false
                (context as? ComponentActivity)?.recreate()
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showFaqDialog) {
        AlertDialog(
            onDismissRequest = { showFaqDialog = false },
            title = {
                Text(stringResource(R.string.faq_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    FAQItem(stringResource(R.string.faq_q1), stringResource(R.string.faq_a1))
                    FAQItem(stringResource(R.string.faq_q2), stringResource(R.string.faq_a2))
                    FAQItem(stringResource(R.string.faq_q3), stringResource(R.string.faq_a3))
                    FAQItem(stringResource(R.string.faq_q4), stringResource(R.string.faq_a4))
                    FAQItem(stringResource(R.string.faq_q5), stringResource(R.string.faq_a5))
                }
            },
            confirmButton = {
                TextButton(onClick = { showFaqDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(stringResource(R.string.privacy_title), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    stringResource(R.string.privacy_body),
                    color = Color.Gray
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(stringResource(R.string.close))
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
fun LanguagePickerDialog(
    currentLanguage: String,
    languages: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.select_language), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                languages.forEach { code ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(code) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = code == currentLanguage,
                            onClick = { onSelect(code) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = LocaleHelper.getLanguageDisplayName(code),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
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
