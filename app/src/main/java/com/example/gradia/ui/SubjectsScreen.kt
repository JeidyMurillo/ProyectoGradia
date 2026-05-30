package com.example.gradia.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.GradiaApplication
import com.example.gradia.R
import com.example.gradia.domain.model.Subject
import com.example.gradia.presentation.viewmodel.SubjectFilter
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState


@Composable
fun SubjectsScreen(onSubjectClick: (Subject) -> Unit = {}) {
    val app = LocalContext.current.applicationContext as GradiaApplication
    val viewModel = remember { app.provideSubjectsViewModel() }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            SubjectsFilterRow(
                selected = state.filter,
                onSelected = viewModel::onFilterChange
            )
            Spacer(modifier = Modifier.height(18.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.subjects, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        onClick = { onSubjectClick(subject) }
                    )
                }
                item {
                    AddSubjectCard(onClick = { showAddSheet = true })
                }
                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showAddSheet) {
        AddSubjectSheet(
            isSaving = state.isSaving,
            onDismiss = { showAddSheet = false },
            onSave = { newSubject ->
                viewModel.addSubject(newSubject) { showAddSheet = false }
            }
        )
    }
}

@Composable
private fun SubjectsFilterRow(
    selected: SubjectFilter,
    onSelected: (SubjectFilter) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterPill("Todas las asignaturas", SubjectFilter.TODAS, selected, onSelected)
        FilterPill("Semestre Actual", SubjectFilter.ACTUAL, selected, onSelected)
        FilterPill("Antiguas", SubjectFilter.ANTIGUAS, selected, onSelected)
    }
}

@Composable
private fun FilterPill(
    label: String,
    value: SubjectFilter,
    selected: SubjectFilter,
    onSelected: (SubjectFilter) -> Unit
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
private fun SubjectCard(subject: Subject, onClick: () -> Unit) {
    val iconType = resolveSubjectIcon(subject.icon, subject.name)
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, Color(0xFFD9C7E8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SubjectIconRender(type = iconType, tint = PurpleGradia, size = 56.dp)
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = subject.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A),
                fontFamily = InterFontFamily,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddSubjectCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFEAE0F2)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Añadir asignatura",
                tint = Color(0xFF9C8AAB),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Añadir\nAsignatura",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF9C8AAB),
                fontFamily = InterFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSubjectSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Subject) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("1") }
    var professor by remember { mutableStateOf("") }
    var classroom by remember { mutableStateOf("") }
    var selectedIconOverride by remember { mutableStateOf<SubjectIconType?>(null) }
    var iconMenuOpen by remember { mutableStateOf(false) }

    val effectiveIcon = selectedIconOverride ?: subjectIconType(name)
    val creditsInt = credits.toIntOrNull()
    val semesterInt = semester.toIntOrNull()
    val isValid = name.isNotBlank() &&
        creditsInt != null && creditsInt in 1..6 &&
        semesterInt != null && semesterInt >= 1

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
                    text = "Agregar Asignatura",
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
                    FieldLabel("Icon")
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        IconPreview(
                            type = effectiveIcon,
                            onClick = { iconMenuOpen = true }
                        )
                        DropdownMenu(
                            expanded = iconMenuOpen,
                            onDismissRequest = { iconMenuOpen = false }
                        ) {
                            SubjectIconType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = iconLabel(type),
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
                                            SubjectIconRender(
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
                    FieldLabel("Nombre de la Asignatura")
                    Spacer(modifier = Modifier.height(8.dp))
                    PillTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Ej: Español"
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("N° de Creditos")
                    Spacer(modifier = Modifier.height(8.dp))
                    PillTextField(
                        value = credits,
                        onValueChange = { input ->
                            if (input.isEmpty() || (input.toIntOrNull() != null && input.length <= 1)) {
                                credits = input
                            }
                        },
                        placeholder = "min: 1 - max:6",
                        leadingContent = { HashIcon() },
                        keyboardType = KeyboardType.Number
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel("Semestre")
                    Spacer(modifier = Modifier.height(8.dp))
                    SemesterDropdownField(
                        value = semester,
                        onSelect = { semester = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel("Nombre del profesor")
            Spacer(modifier = Modifier.height(8.dp))
            PillTextField(
                value = professor,
                onValueChange = { professor = it },
                placeholder = "Ej: Mauricio",
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel("Salon de Clases")
            Spacer(modifier = Modifier.height(8.dp))
            PillTextField(
                value = classroom,
                onValueChange = { classroom = it },
                placeholder = "Ej: Sala de sistemas 1",
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val finalCredits = creditsInt ?: return@Button
                    val finalSemester = semesterInt ?: return@Button
                    onSave(
                        Subject(
                            id = 0L,
                            name = name.trim(),
                            icon = iconStringFor(effectiveIcon),
                            creditHours = finalCredits,
                            semester = finalSemester,
                            professor = professor.trim(),
                            classroom = classroom.trim()
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
                        text = "Guardar Asignatura",
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
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1F1F1F),
        fontFamily = InterFontFamily
    )
}

@Composable
private fun SemesterDropdownField(
    value: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = value.ifBlank { "1" }

    Box(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .clickable { expanded = true },
            shape = RoundedCornerShape(50),
            color = Color(0xFFF3EDF7)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Semestre $selected",
                    fontSize = 14.sp,
                    color = Color(0xFF1F1F1F),
                    fontFamily = InterFontFamily
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Seleccionar semestre",
                    tint = PurpleGradia,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 280.dp)
        ) {
            (1..10).forEach { sem ->
                val semStr = sem.toString()
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Semestre $sem",
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            fontWeight = if (selected == semStr) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected == semStr) PurpleGradia else Color(0xFF1F1F1F)
                        )
                    },
                    onClick = {
                        onSelect(semStr)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingContent: (@Composable () -> Unit)? = null,
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
                cursorBrush = androidx.compose.ui.graphics.SolidColor(PurpleGradia),
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
                    .padding(end = 14.dp)
            )
        }
    }
}

@Composable
private fun HashIcon() {
    Text(
        text = "#",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        fontFamily = InterFontFamily
    )
}

@Composable
private fun IconPreview(type: SubjectIconType, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(PurpleGradia)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        SubjectIconRender(type = type, tint = Color.White, size = 22.dp)
    }
}
