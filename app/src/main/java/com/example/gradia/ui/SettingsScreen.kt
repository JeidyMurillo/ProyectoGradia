package com.example.gradia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.PurpleGradia
import com.example.gradia.ui.theme.InterFontFamily

@Composable
fun SettingsScreen() {
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
                value = "Español"
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.user_outline),
                title = "Cuenta"
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.help),
                title = "Ayuda y Soporte"
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.faq),
                title = "FAQ (Preguntas Frecuentes)"
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.email),
                title = "Contacto"
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.star),
                title = "Valorar App"
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.share),
                title = "Compartir App"
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.document), // Usando Lock para Políticas de Privacidad
                title = "Políticas de Privacidad"
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.document), // Usando Article para Términos y Condiciones
                title = "Términos y Condiciones"
            )
        }
        item {
            SettingsItem(
                iconPainter = painterResource(id = R.drawable.information),
                title = "Acerca de"
            )
        }
        
        item { Spacer(modifier = Modifier.height(100.dp)) }
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
