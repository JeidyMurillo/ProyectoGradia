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
import androidx.compose.material.icons.filled.Edit
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
import com.example.gradia.domain.model.Subject
import com.example.gradia.presentation.viewmodel.GradeFilter
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia

@Composable
fun SubjectDetailScreen(
    subjectId: Long,
    onSubjectDeleted: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as GradiaApplication
    val viewModel = remember(subjectId) { app.provideSubjectDetailViewModel(subjectId) }
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showAddSheet by remember { mutableStateOf(false) }
    var showEditSubjectSheet by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }
    var editingGrade by remember { mutableStateOf<GradeItem?>(null) }
    var gradeToDelete by remember { mutableStateOf<GradeItem?>(null) }

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
            state.subject?.let { subject ->
                item {
                    SubjectInfoCard(
                        subject = subject,
                        onEdit = { showEditSubjectSheet = true }
                    )
                }
            }
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
                GradeRow(item = gradeItem, onClick = { editingGrade = gradeItem })
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

    // ── Agregar nota ──
    if (showAddSheet) {
        GradeFormSheet(
            isSaving = state.isSaving,
            onDismiss = { showAddSheet = false },
            onSave = { newItem ->
                viewModel.addGrade(newItem) { showAddSheet = false }
            }
        )
    }

    // ── Editar nota (toca una nota para editarla) ──
    editingGrade?.let { grade ->
        GradeFormSheet(
            isSaving = state.isSaving,
            initial = grade,
            onDismiss = { editingGrade = null },
            onSave = { updated ->
                viewModel.updateGrade(updated) { editingGrade = null }
            },
            onDelete = {
                editingGrade = null
                gradeToDelete = grade
            }
        )
    }

    // ── Editar asignatura ──
    if (showEditSubjectSheet) {
        state.subject?.let { subject ->
            SubjectFormSheet(
                isSaving = state.isSaving,
                initial = subject,
                onDismiss = { showEditSubjectSheet = false },
                onSave = { updated ->
                    viewModel.updateSubject(updated) { showEditSubjectSheet = false }
                },
                onDelete = {
                    showEditSubjectSheet = false
                    showDeleteSubjectDialog = true
                }
            )
        }
    }

    // ── Confirmar eliminación de asignatura ──
    if (showDeleteSubjectDialog) {
        ConfirmDeleteDialog(
            title = "Eliminar asignatura",
            message = "¿Seguro que quieres eliminar \"${state.subject?.name ?: ""}\"? También se eliminarán todas sus calificaciones. Esta acción no se puede deshacer.",
            onConfirm = {
                showDeleteSubjectDialog = false
                viewModel.deleteSubject { onSubjectDeleted() }
            },
            onDismiss = { showDeleteSubjectDialog = false }
        )
    }

    // ── Confirmar eliminación de nota ──
    gradeToDelete?.let { grade ->
        ConfirmDeleteDialog(
            title = "Eliminar nota",
            message = "¿Seguro que quieres eliminar \"${grade.name}\"?",
            onConfirm = {
                viewModel.deleteGrade(grade)
                gradeToDelete = null
            },
            onDismiss = { gradeToDelete = null }
        )
    }
}

@Composable
private fun SubjectInfoCard(subject: Subject, onEdit: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE0D4EE))
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(PurpleGradia, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    SubjectIconRender(
                        type = resolveSubjectIcon(subject.icon, subject.name),
                        tint = Color.White,
                        size = 26.dp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F),
                        fontFamily = InterFontFamily
                    )
                    Text(
                        text = "Semestre ${subject.semester} · ${subject.creditHours} créditos",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontFamily = InterFontFamily
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF3EDF7),
                    modifier = Modifier.clickable(onClick = onEdit)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar asignatura",
                            tint = PurpleGradia,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Editar",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PurpleGradia,
                            fontFamily = InterFontFamily
                        )
                    }
                }
            }
            if (subject.professor.isNotBlank() || subject.classroom.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (subject.professor.isNotBlank()) {
                        InfoChip(text = subject.professor)
                    }
                    if (subject.classroom.isNotBlank()) {
                        InfoChip(text = subject.classroom)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFF7F1FB)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            fontSize = 12.sp,
            color = Color(0xFF6B6B6B),
            fontFamily = InterFontFamily
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(title, fontWeight = FontWeight.Bold, color = Color(0xFFC62828), fontFamily = InterFontFamily)
        },
        text = { Text(message, fontFamily = InterFontFamily) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) { Text("Eliminar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = PurpleGradia) }
        }
    )
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

@Composable
private fun GradeRow(item: GradeItem, onClick: () -> Unit) {
    if (item.grade == null) {
        PendingGradeRow(item = item, onClick = onClick)
        return
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = Color(0xFFF7F1FB)
    ) {
        GradeRowContent(item = item, isPending = false)
    }
}

@Composable
private fun PendingGradeRow(item: GradeItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .dashedBorder(color = Color(0xFFC9BDD6), cornerRadius = 28.dp)
            .clickable(onClick = onClick)
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
            GradeIconRender(
                type = resolveGradeIcon(item.icon, item.name),
                tint = if (isPending) Color(0xFFB0A4BC) else PurpleGradia,
                size = 22.dp
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
private fun GradeFormSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (GradeItem) -> Unit,
    initial: GradeItem? = null,
    onDelete: (() -> Unit)? = null
) {
    val isEditing = initial != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var grade by remember { mutableStateOf(initial?.grade?.let { gradeToInput(it) } ?: "") }
    var percentage by remember { mutableStateOf(initial?.percentage?.toInt()?.toString() ?: "") }
    var selectedIconOverride by remember {
        mutableStateOf<GradeIconType?>(initial?.let { resolveGradeIcon(it.icon, it.name) })
    }
    var iconMenuOpen by remember { mutableStateOf(false) }

    val effectiveIcon = selectedIconOverride ?: gradeIconType(name)
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
                    text = if (isEditing) "Editar nota" else "Agregar nota",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1F1F1F),
                    fontFamily = InterFontFamily
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = "Eliminar nota",
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF4A4A4A)
                        )
                    }
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
                    Box {
                        GradeIconPreview(
                            type = effectiveIcon,
                            onClick = { iconMenuOpen = true }
                        )
                        DropdownMenu(
                            expanded = iconMenuOpen,
                            onDismissRequest = { iconMenuOpen = false }
                        ) {
                            GradeIconType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = gradeIconLabel(type),
                                            fontFamily = InterFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1F1F1F)
                                        )
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(PurpleGradia),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            GradeIconRender(
                                                type = type,
                                                tint = Color.White,
                                                size = 18.dp
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedIconOverride = type
                                        iconMenuOpen = false
                                    }
                                )
                            }
                        }
                    }
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
                            id = initial?.id ?: 0L,
                            subjectId = initial?.subjectId ?: 0L,
                            name = name.trim(),
                            percentage = pct,
                            grade = gradeDouble,
                            icon = gradeIconStringFor(effectiveIcon)
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
                        text = if (isEditing) "Guardar cambios" else "Guardar nota",
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
private fun GradeIconPreview(type: GradeIconType, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(PurpleGradia)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        GradeIconRender(type = type, tint = Color.White, size = 22.dp)
    }
}

/**
 * Convierte una nota (Double) al texto que espera el campo de edición:
 * usa siempre punto decimal (independiente del locale), redondea a 2 decimales
 * y elimina ceros sobrantes conservando al menos un decimal. Ej: 4.0 → "4.0",
 * 3.50 → "3.5", 4.25 → "4.25".
 */
private fun gradeToInput(grade: Double): String {
    val rounded = kotlin.math.round(grade * 100.0) / 100.0
    var text = String.format(java.util.Locale.US, "%.2f", rounded)
    while (text.endsWith("0") && !text.endsWith(".0")) {
        text = text.dropLast(1)
    }
    return text
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
