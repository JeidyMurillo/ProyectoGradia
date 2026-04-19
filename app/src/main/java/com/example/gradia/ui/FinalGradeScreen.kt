package com.example.gradia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.*

@Composable
fun FinalGradeScreen() {
    var asignatura by remember { mutableStateOf("Calculo IV") }
    var meta by remember { mutableStateOf("") }
    var acumulado by remember { mutableStateOf("") }
    var actividad by remember { mutableStateOf("") }
    var porcentaje by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Graduation Cap Icon
        Box(
            modifier = Modifier
                .size(110.dp)
                .background(PurpleGradia, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.graduation_cap_white),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(65.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "¿Qué necesito sacar?",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                fontFamily = InterFontFamily,
                fontSize = 24.sp
            )
        )

        Text(
            text = "Calcula la nota mínima para alcanzar\ntu meta académica",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontFamily = InterFontFamily
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Asignatura
        GradeInputField(
            label = "Asignatura:",
            value = asignatura,
            onValueChange = { asignatura = it },
            placeholder = "Calculo IV",
            painter = painterResource(id = R.drawable.signature),
            trailingIcon = Icons.Default.KeyboardArrowDown,
            fullWidth = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Meta and Acumulado Row
        Row(modifier = Modifier.fillMaxWidth()) {
            GradeInputField(
                label = "Nota objetivo (Meta):",
                value = meta,
                onValueChange = { meta = it },
                placeholder = "Ej: 4.5",
                painter = painterResource(id = R.drawable.goal),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            GradeInputField(
                label = "Nota actual (acumulada):",
                value = acumulado,
                onValueChange = { acumulado = it },
                placeholder = "Ej: 4.5",
                painter = painterResource(id = R.drawable.sum),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actividad and Porcentaje Row
        Row(modifier = Modifier.fillMaxWidth()) {
            GradeInputField(
                label = "Nombre de la actividad:",
                value = actividad,
                onValueChange = { actividad = it },
                placeholder = "Ej: Examen Final",
                painter = painterResource(id = R.drawable.rename_box),
                modifier = Modifier.weight(1.2f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            GradeInputField(
                label = "Porcentaje (%):",
                value = porcentaje,
                onValueChange = { porcentaje = it },
                placeholder = "Ej: 35%",
                painter = painterResource(id = R.drawable.percent),
                modifier = Modifier.weight(0.8f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Calculate Button
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.calculator_white),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Calcular Resultado",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    fontFamily = InterFontFamily
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, PurpleGradia.copy(alpha = 0.1f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(PurpleGradia, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Necesitas un ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = PurpleGradia, fontSize = 20.sp)) {
                                append("4.2")
                            }
                            append(" en el ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                                append("Examen Final (35%)")
                            }
                            append(" para pasar con ")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                                append("3.0")
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontFamily = InterFontFamily
                        ),
                        color = Color.DarkGray
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Basado en el sistema de evaluación estándar de la asignatura.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = InterFontFamily,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    painter: Painter,
    modifier: Modifier = Modifier,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    fullWidth: Boolean = false
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = Color.Black,
            fontFamily = InterFontFamily,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f), fontSize = 15.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            leadingIcon = { Icon(painter = painter, contentDescription = null, tint = PurpleGradia, modifier = Modifier.size(24.dp)) },
            trailingIcon = trailingIcon?.let { { Icon(it, contentDescription = null, tint = Color.Gray) } },
            shape = RoundedCornerShape(25.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleGradia,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                focusedContainerColor = Color(0xFFFBF8FF),
                unfocusedContainerColor = Color(0xFFFBF8FF)
            ),
            singleLine = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun FinalGradeScreenPreview() {
    GradiaTheme {
        FinalGradeScreen()
    }
}
