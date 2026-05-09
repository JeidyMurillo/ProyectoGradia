package com.example.gradia.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gradia.GradiaApplication
import com.example.gradia.R
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.usecase.RequiredGradeResult
import com.example.gradia.presentation.viewmodel.FinalGradeViewModel
import com.example.gradia.ui.theme.GradiaTheme
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalGradeScreen(viewModel: FinalGradeViewModel = viewModel(
    factory = (LocalContext.current.applicationContext as GradiaApplication).let { app ->
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return app.provideFinalGradeViewModel() as T
            }
        }
    }
)) {
    val uiState by viewModel.uiState.collectAsState()

    var showSubjectDropdown by remember { mutableStateOf(false) }
    var showActivityDropdown by remember { mutableStateOf(false) }
    var metaInput by remember { mutableStateOf("") }
    var selectedActivityIndex by remember { mutableStateOf(0) }

    LaunchedEffect(uiState.targetGrade) {
        if (metaInput.isEmpty() && uiState.targetGrade > 0) {
            metaInput = "%.1f".format(uiState.targetGrade)
        }
    }

    LaunchedEffect(uiState.selectedSubject) {
        selectedActivityIndex = 0
        showActivityDropdown = false
    }

    val pendingActivities = uiState.activities.filter { it.grade == null }
    val currentActivity = pendingActivities.getOrNull(selectedActivityIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

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
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "\u00bfQue necesito sacar?",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                fontFamily = InterFontFamily
            )
        )

        Text(
            text = "Calcula la nota minima para alcanzar\ntu meta academica",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray,
                textAlign = TextAlign.Center,
                fontFamily = InterFontFamily
            ),
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        ExposedDropdownMenuBox(
            expanded = showSubjectDropdown,
            onExpandedChange = { showSubjectDropdown = it }
        ) {
            Column(modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)) {
                Text(
                    text = "Asignatura:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
                OutlinedTextField(
                    value = uiState.selectedSubject?.name ?: "Seleccionar...",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                    trailingIcon = {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = PurpleGradia)
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleGradia,
                        unfocusedBorderColor = PurpleGradia,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = Color.Black)
                )
            }
            ExposedDropdownMenu(
                expanded = showSubjectDropdown,
                onDismissRequest = { showSubjectDropdown = false }
            ) {
                uiState.subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject.name) },
                        onClick = {
                            viewModel.selectSubject(subject)
                            showSubjectDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nota objetivo (Meta):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
                OutlinedTextField(
                    value = metaInput,
                    onValueChange = {
                        if (it.isEmpty() || it.toDoubleOrNull() != null) metaInput = it
                    },
                    placeholder = { Text("Ej: 4.5", color = Color.Gray.copy(alpha = 0.4f), fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.goal),
                            contentDescription = null,
                            tint = PurpleGradia,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurpleGradia,
                        unfocusedBorderColor = PurpleGradia,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 15.sp, color = Color.Black),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Promedio actual:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, PurpleGradia)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.sum),
                            contentDescription = null,
                            tint = PurpleGradia,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "%.1f".format(uiState.currentAverage),
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (pendingActivities.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Actividad pendiente:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1.6f)
                )
                Text(
                    text = "Porcentaje (%)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black.copy(alpha = 0.7f),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            ExposedDropdownMenuBox(
                expanded = showActivityDropdown,
                onExpandedChange = { showActivityDropdown = it }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color.Black.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(1.5.dp, PurpleGradia)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.rename_box),
                            contentDescription = null,
                            tint = PurpleGradia,
                            modifier = Modifier.size(20.dp)
                        )

                        Text(
                            text = currentActivity?.name ?: "Seleccionar...",
                            modifier = Modifier.weight(1.6f).padding(horizontal = 8.dp),
                            fontSize = 15.sp,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .width(1.dp)
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        ) {
                            Text(
                                text = "%.0f".format(currentActivity?.percentage ?: 0),
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleGradia,
                                    textAlign = TextAlign.End
                                )
                            )
                            Text(
                                "%",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleGradia
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                }
                ExposedDropdownMenu(
                    expanded = showActivityDropdown,
                    onDismissRequest = { showActivityDropdown = false }
                ) {
                    pendingActivities.forEachIndexed { index, activity ->
                        DropdownMenuItem(
                            text = { Text(activity.name) },
                            trailingIcon = { Text("%.0f%%".format(activity.percentage), color = PurpleGradia) },
                            onClick = {
                                selectedActivityIndex = index
                                showActivityDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                metaInput.toDoubleOrNull()?.let { viewModel.updateTargetGrade(it) }
                viewModel.calculateRequiredGrade()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = PurpleGradia.copy(alpha = 0.4f)),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.calculator_white),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    "Calcular Resultado",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        uiState.requiredGradeResult?.let { result ->
            ResultCard(
                result = result,
                targetGrade = uiState.targetGrade,
                activityName = currentActivity?.name ?: "actividad"
            )
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun ResultCard(result: RequiredGradeResult, targetGrade: Double, activityName: String) {
    val resultText = when (result) {
        is RequiredGradeResult.Success -> buildAnnotatedString {
            append("Necesitas un ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = PurpleGradia, fontSize = 20.sp)) {
                append("%.1f".format(result.grade))
            }
            append(" en ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                append(activityName)
            }
            append(" para alcanzar ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                append("%.1f".format(targetGrade))
            }
        }
        RequiredGradeResult.AlreadyAchieved -> buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = PurpleGradia, fontSize = 18.sp)) {
                append("\u00a1Ya alcanzaste tu meta!")
            }
            append("\nTu promedio actual ya supera ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                append("%.1f".format(targetGrade))
            }
        }
        RequiredGradeResult.Impossible -> buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color.Red, fontSize = 18.sp)) {
                append("No es posible alcanzar ")
            }
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                append("%.1f".format(targetGrade))
            }
            append(" con el porcentaje restante")
        }
        RequiredGradeResult.NoRemainingPercentage -> buildAnnotatedString {
            append("No hay ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.Black)) {
                append("porcentaje restante")
            }
            append(" por evaluar")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PurpleGradia.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = resultText,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp, color = Color.DarkGray)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Basado en el sistema de evaluacion estandar de la asignatura.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start
                ),
                modifier = Modifier.padding(start = 66.dp)
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun FinalGradeScreenPreview() {
    GradiaTheme {
        FinalGradeScreen()
    }
}
