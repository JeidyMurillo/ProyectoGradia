package com.example.gradia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.GradiaApplication
import com.example.gradia.R
import com.example.gradia.domain.model.GradeItem
import com.example.gradia.presentation.viewmodel.GradeFilter
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia

@Composable
fun SubjectDetailScreen(subjectId: Long) {
    val app = LocalContext.current.applicationContext as GradiaApplication
    val viewModel = remember(subjectId) { app.provideSubjectDetailViewModel(subjectId) }
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }
            item { CurrentAverageCard(average = state.currentAverage) }
            item {
                FilterChipsRow(
                    selected = state.filter,
                    onSelected = viewModel::onFilterChange
                )
            }
            item {
                Text(
                    text = "Calificaciones detalladas",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    fontFamily = InterFontFamily,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(state.grades, key = { it.id }) { gradeItem ->
                GradeRow(item = gradeItem)
            }
            item {
                HorizontalDivider(
                    color = Color(0xFFE6DDEF),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item { AddGradeButton(onClick = { showAddSheet = true }) }
            item { Spacer(modifier = Modifier.height(90.dp)) }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showAddSheet) {
        AddGradeSheet(
            isSaving = state.isSaving,
            onDismiss = { showAddSheet = false },
            onSave = { newItem ->
                viewModel.addGrade(newItem) { showAddSheet = false }
            }
        )
    }
}

@Composable
private fun CurrentAverageCard(average: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF3EDF7)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "PROMEDIO ACTUAL",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B6B6B),
                        fontFamily = InterFontFamily,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "%.1f".format(average),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PurpleGradia,
                        fontFamily = InterFontFamily
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stats_chart),
                        contentDescription = null,
                        tint = PurpleGradia,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (average / 5.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = PurpleGradia,
                trackColor = Color(0xFFE2D6EE)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Lorem ipsum dolor sit amet.",
                fontSize = 12.sp,
                color = Color.Gray,
                fontFamily = InterFontFamily
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    selected: GradeFilter,
    onSelected: (GradeFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterPill(label = "Todas las notas", value = GradeFilter.TODAS, selected = selected, onSelected = onSelected)
        FilterPill(label = "Parciales", value = GradeFilter.PARCIALES, selected = selected, onSelected = onSelected)
        FilterPill(label = "Talleres", value = GradeFilter.TALLERES, selected = selected, onSelected = onSelected)
    }
}

@Composable
private fun FilterPill(
    label: String,
    value: GradeFilter,
    selected: GradeFilter,
    onSelected: (GradeFilter) -> Unit
) {
    val isSelected = selected == value
    Surface(
        shape = RoundedCornerShape(50),
        color = if (isSelected) PurpleGradia else Color(0xFFF3EDF7),
        modifier = Modifier
            .height(32.dp)
            .clickable { onSelected(value) }
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color.White else PurpleGradia,
                fontFamily = InterFontFamily
            )
        }
    }
}

private fun iconForActivity(name: String): Int = when {
    name.contains("Final", ignoreCase = true) -> R.drawable.calendar
    name.contains("Taller", ignoreCase = true) ||
    name.contains("Tarea", ignoreCase = true) ||
    name.contains("Quiz", ignoreCase = true) ||
    name.contains("Laboratorio", ignoreCase = true) ||
    name.contains("Proyecto", ignoreCase = true) -> R.drawable.list
    else -> R.drawable.document
}

@Composable
private fun GradeRow(item: GradeItem) {
    if (item.grade == null) {
        PendingGradeRow(item = item)
        return
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = Color(0xFFF7F1FB)
    ) {
        GradeRowContent(item = item, isPending = false)
    }
}

@Composable
private fun PendingGradeRow(item: GradeItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(color = Color(0xFFC9BDD6), cornerRadius = 28.dp)
    ) {
        GradeRowContent(item = item, isPending = true)
    }
}

@Composable
private fun GradeRowContent(item: GradeItem, isPending: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    if (isPending) Color(0xFFF3EDF7) else Color.White,
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconForActivity(item.name)),
                contentDescription = null,
                tint = if (isPending) Color(0xFFB0A4BC) else PurpleGradia,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPending) Color(0xFFA098AA) else Color(0xFF4A4A4A),
                fontFamily = InterFontFamily
            )
            Text(
                text = "Peso: ${item.percentage.toInt()}%",
                fontSize = 11.sp,
                color = Color.Gray,
                fontFamily = InterFontFamily
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(end = 6.dp)
        ) {
            if (item.grade != null) {
                Text(
                    text = "%.1f".format(item.grade),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    fontFamily = InterFontFamily
                )
                Spacer(modifier = Modifier.height(3.dp))
                StatusChip(grade = item.grade)
            } else {
                PendingLabel()
            }
        }
    }
}

@Composable
private fun StatusChip(grade: Double) {
    val (label, bg, fg) = when {
        grade < 3.0 -> Triple("REPROBADO", Color(0xFFFFE0E0), Color(0xFFC62828))
        grade < 4.0 -> Triple("REGULAR", Color(0xFFFFF3B0), Color(0xFF8A6D00))
        grade < 4.5 -> Triple("APROBADO", Color(0xFFCDEFCD), Color(0xFF2E7D32))
        else -> Triple("EXCELENTE", Color(0xFFBCEFC4), Color(0xFF1B5E20))
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = fg,
            fontFamily = InterFontFamily
        )
    }
}

@Composable
private fun PendingLabel() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color(0xFFA098AA), CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "Pendiente",
            fontSize = 12.sp,
            color = Color(0xFFA098AA),
            fontWeight = FontWeight.SemiBold,
            fontFamily = InterFontFamily
        )
    }
}

@Composable
private fun AddGradeButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .dashedBorder(color = Color(0xFFC9BDD6), cornerRadius = 25.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Agregar nota",
            tint = Color(0xFFA098AA),
            modifier = Modifier.size(28.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGradeSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (GradeItem) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var percentage by remember { mutableStateOf("") }

    val percentageDouble = percentage.toDoubleOrNull()
    val gradeDouble = grade.toDoubleOrNull()
    val isValid = name.isNotBlank() &&
        percentageDouble != null && percentageDouble in 1.0..100.0 &&
        (grade.isEmpty() || (gradeDouble != null && gradeDouble in 0.0..5.0))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFCFC2DC)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Agregar nota",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F1F1F),
                    fontFamily = InterFontFamily
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color(0xFF4A4A4A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    SheetFieldLabel("Icon")
                    Spacer(modifier = Modifier.height(8.dp))
                    GradeIconPreview(name = name)
                }
                Column(modifier = Modifier.weight(1f)) {
                    SheetFieldLabel("Nombre de la actividad")
                    Spacer(modifier = Modifier.height(8.dp))
                    SheetPillTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Ej: Parcial"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SheetFieldLabel("Nota (0.0 - 5.0)")
                    Spacer(modifier = Modifier.height(8.dp))
                    SheetPillTextField(
                        value = grade,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d{0,1}(\\.\\d{0,2})?$"))) {
                                grade = input
                            }
                        },
                        placeholder = "2.0",
                        keyboardType = KeyboardType.Decimal
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    SheetFieldLabel("Porcentaje (%)")
                    Spacer(modifier = Modifier.height(8.dp))
                    SheetPillTextField(
                        value = percentage,
                        onValueChange = { input ->
                            if (input.isEmpty() || (input.toIntOrNull() != null && input.length <= 3)) {
                                percentage = input
                            }
                        },
                        placeholder = "25",
                        keyboardType = KeyboardType.Number,
                        trailingContent = {
                            Text(
                                text = "%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontFamily = InterFontFamily
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val pct = percentageDouble ?: return@Button
                    onSave(
                        GradeItem(
                            id = 0L,
                            subjectId = 0L,
                            name = name.trim(),
                            percentage = pct,
                            grade = gradeDouble
                        )
                    )
                },
                enabled = isValid && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleGradia,
                    disabledContainerColor = PurpleGradia.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(50)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "Guardar nota",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = InterFontFamily
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetFieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1F1F1F),
        fontFamily = InterFontFamily
    )
}

@Composable
private fun SheetPillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(50),
        color = Color(0xFFF3EDF7)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingContent != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PurpleGradia, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    leadingContent()
                }
                Spacer(modifier = Modifier.width(10.dp))
            } else {
                Spacer(modifier = Modifier.width(14.dp))
            }

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF1F1F1F),
                    fontFamily = InterFontFamily
                ),
                cursorBrush = SolidColor(PurpleGradia),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color(0xFFB3A8C4),
                                fontSize = 14.sp,
                                fontFamily = InterFontFamily
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = if (trailingContent == null) 14.dp else 8.dp)
            )

            if (trailingContent != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PurpleGradia, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    trailingContent()
                }
            }
        }
    }
}

@Composable
private fun GradeIconPreview(name: String) {
    val iconRes = when {
        name.contains("Final", ignoreCase = true) -> R.drawable.calendar
        name.contains("Taller", ignoreCase = true) ||
        name.contains("Tarea", ignoreCase = true) ||
        name.contains("Quiz", ignoreCase = true) ||
        name.contains("Laboratorio", ignoreCase = true) ||
        name.contains("Proyecto", ignoreCase = true) -> R.drawable.list
        name.contains("Parcial", ignoreCase = true) ||
        name.contains("Examen", ignoreCase = true) -> R.drawable.document
        else -> null
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(PurpleGradia, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.5.dp,
    dashOn: Float = 14f,
    dashOff: Float = 10f
): Modifier = this.drawBehind {
    val stroke = Stroke(
        width = strokeWidth.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashOn, dashOff), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx())
    )
}
