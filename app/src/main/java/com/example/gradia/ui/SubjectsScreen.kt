package com.example.gradia.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.GradiaApplication
import com.example.gradia.R
import com.example.gradia.data.local.OnboardingPrefs
import com.example.gradia.domain.model.Subject
import com.example.gradia.domain.validation.SubjectValidation
import com.example.gradia.presentation.viewmodel.SubjectFilter
import com.example.gradia.presentation.viewmodel.SubjectsViewModel
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState


@Composable
fun SubjectsScreen(
    onSubjectClick: (Subject) -> Unit = {},
    externalViewModel: SubjectsViewModel? = null
) {
    val app = LocalContext.current.applicationContext as GradiaApplication
    val viewModel = externalViewModel ?: remember { app.provideSubjectsViewModel() }
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showOnboarding by remember { mutableStateOf(!OnboardingPrefs.isDismissed(context, "subjects")) }

    var showAddSheet by remember { mutableStateOf(false) }
    var subjectToDelete by remember { mutableStateOf<Subject?>(null) }

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
            if (showOnboarding) {
                OnboardingCard(
                    title = stringResource(R.string.onboarding_subjects_title),
                    message = stringResource(R.string.onboarding_subjects_message),
                    onDismiss = {
                        OnboardingPrefs.dismiss(context, "subjects")
                        showOnboarding = false
                    }
                )
            }
            SubjectsFilterRow(
                selected = state.filter,
                onSelected = viewModel::onFilterChange
            )
            if (state.subjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.subjects_hint),
                    fontSize = 11.sp,
                    color = Color(0xFFA098AA),
                    fontFamily = InterFontFamily
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.subjects, key = { it.id }) { subject ->
                    SubjectCard(
                        subject = subject,
                        onClick = { onSubjectClick(subject) },
                        onLongClick = { subjectToDelete = subject }
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
        SubjectFormSheet(
            isSaving = state.isSaving,
            onDismiss = { showAddSheet = false },
            onSave = { newSubject ->
                viewModel.addSubject(newSubject) { showAddSheet = false }
            },
            existingNames = state.allSubjectNames
        )
    }

    subjectToDelete?.let { subject ->
        AlertDialog(
            onDismissRequest = { subjectToDelete = null },
            title = {
                Text(
                    text = stringResource(R.string.dialog_delete_subject_title),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
                    fontFamily = InterFontFamily
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_delete_subject_body, subject.name),
                    fontFamily = InterFontFamily
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubject(subject.id)
                        subjectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) { Text(stringResource(R.string.action_delete), color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { subjectToDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = PurpleGradia)
                }
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
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterPill(stringResource(R.string.filter_all_subjects), SubjectFilter.TODAS, selected, onSelected)
        FilterPill(stringResource(R.string.filter_current_semester), SubjectFilter.ACTUAL, selected, onSelected)
        FilterPill(stringResource(R.string.filter_old_subjects), SubjectFilter.ANTIGUAS, selected, onSelected)
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
        color = if (isSelected) PurpleGradia else MaterialTheme.colorScheme.surfaceVariant,
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubjectCard(
    subject: Subject,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val iconType = resolveSubjectIcon(subject.icon, subject.name)
    Surface(
        modifier = Modifier
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant)
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
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = InterFontFamily,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp,
                modifier = Modifier.fillMaxWidth()
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
        color = MaterialTheme.colorScheme.surfaceVariant
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
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.add_subject_card),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = InterFontFamily,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SubjectFormSheet(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (Subject) -> Unit,
    initial: Subject? = null,
    onDelete: (() -> Unit)? = null,
    existingNames: List<String> = emptyList()
) {
    val isEditing = initial != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var credits by remember { mutableStateOf(initial?.creditHours?.takeIf { it > 0 }?.toString() ?: "") }
    var semester by remember { mutableStateOf((initial?.semester ?: 1).toString()) }
    var professor by remember { mutableStateOf(initial?.professor ?: "") }
    var classroom by remember { mutableStateOf(initial?.classroom ?: "") }
    var selectedIconOverride by remember {
        mutableStateOf<SubjectIconType?>(initial?.let { resolveSubjectIcon(it.icon, it.name) })
    }
    var iconMenuOpen by remember { mutableStateOf(false) }

    val effectiveIcon = selectedIconOverride ?: subjectIconType(name)
    val creditsInt = credits.toIntOrNull()
    val semesterInt = semester.toIntOrNull()

    // Marca si el usuario ya intentó guardar o tocó cada campo, para no mostrar
    // errores en un formulario aún en blanco (recién abierto).
    var attemptedSave by remember { mutableStateOf(false) }
    var nameTouched by remember { mutableStateOf(false) }
    var creditsTouched by remember { mutableStateOf(false) }

    val trimmedName = name.trim()
    val isDuplicateName = existingNames.any { it.trim().equals(trimmedName, ignoreCase = true) }
    val nameError: String? = when {
        trimmedName.isEmpty() -> stringResource(R.string.error_subject_name_required)
        isDuplicateName -> stringResource(R.string.error_duplicate_name)
        else -> null
    }
    val creditsError: String? = when {
        credits.isEmpty() -> stringResource(R.string.error_credits_required, SubjectValidation.MIN_CREDITS, SubjectValidation.MAX_CREDITS)
        creditsInt == null || creditsInt !in SubjectValidation.MIN_CREDITS..SubjectValidation.MAX_CREDITS ->
            stringResource(R.string.error_credits_range, SubjectValidation.MIN_CREDITS, SubjectValidation.MAX_CREDITS)
        else -> null
    }
    val isValid = nameError == null && creditsError == null &&
        semesterInt != null && semesterInt >= 1
    val showNameError = (attemptedSave || nameTouched) && nameError != null
    val showCreditsError = (attemptedSave || creditsTouched) && creditsError != null

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.onSurfaceVariant) }
        ) {
            // Si el teclado está abierto, "atrás" solo lo cierra (no la hoja), para
            // no interrumpir la creación/edición.
            val focusManager = LocalFocusManager.current
            BackHandler(enabled = WindowInsets.isImeVisible) {
                focusManager.clearFocus()
            }
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
                        text = if (isEditing) stringResource(R.string.sheet_title_edit_subject) else stringResource(R.string.sheet_title_add_subject),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontFamily = InterFontFamily
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onDelete != null) {
                            IconButton(onClick = onDelete) {
                                Icon(
                                    painter = painterResource(id = R.drawable.delete),
                                    contentDescription = "Eliminar asignatura",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                                            color = MaterialTheme.colorScheme.onSurface
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
                    FieldLabel(stringResource(R.string.field_subject_name))
                    Spacer(modifier = Modifier.height(8.dp))
                    PillTextField(
                        value = name,
                        onValueChange = {
                            if (it.length <= SubjectValidation.MAX_NAME_LENGTH) {
                                name = it
                                nameTouched = true
                            }
                        },
                        placeholder = stringResource(R.string.placeholder_subject_name)
                    )
                    if (showNameError) {
                        FieldErrorText(nameError!!)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel(stringResource(R.string.field_credits))
                    Spacer(modifier = Modifier.height(8.dp))
                    PillTextField(
                        value = credits,
                        onValueChange = { input ->
                            if (input.isEmpty() || (input.toIntOrNull() != null && input.length <= 1)) {
                                credits = input
                                creditsTouched = true
                            }
                        },
                        placeholder = stringResource(R.string.placeholder_credits),
                        leadingContent = { HashIcon() },
                        keyboardType = KeyboardType.Number
                    )
                    if (showCreditsError) {
                        FieldErrorText(creditsError!!)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    FieldLabel(stringResource(R.string.field_semester))
                    Spacer(modifier = Modifier.height(8.dp))
                    SemesterDropdownField(
                        value = semester,
                        onSelect = { semester = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            FieldLabel(stringResource(R.string.field_professor))
            Spacer(modifier = Modifier.height(8.dp))
            PillTextField(
                value = professor,
                onValueChange = { professor = it },
                placeholder = stringResource(R.string.placeholder_professor),
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

            FieldLabel(stringResource(R.string.field_classroom))
            Spacer(modifier = Modifier.height(8.dp))
            PillTextField(
                value = classroom,
                onValueChange = { classroom = it },
                placeholder = stringResource(R.string.placeholder_classroom),
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
                    attemptedSave = true
                    if (!isValid) return@Button
                    val finalCredits = creditsInt ?: return@Button
                    val finalSemester = semesterInt ?: return@Button
                    onSave(
                        Subject(
                            id = initial?.id ?: 0L,
                            name = name.trim(),
                            icon = iconStringFor(effectiveIcon),
                            passingGrade = initial?.passingGrade ?: 3.0,
                            creditHours = finalCredits,
                            semester = finalSemester,
                            professor = professor.trim(),
                            classroom = classroom.trim()
                        )
                    )
                },
                enabled = !isSaving,
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
                        text = if (isEditing) stringResource(R.string.action_save_changes) else stringResource(R.string.action_save_subject),
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
        color = MaterialTheme.colorScheme.onSurface,
        fontFamily = InterFontFamily
    )
}

@Composable
private fun FieldErrorText(message: String) {
    Text(
        text = message,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.error,
        fontFamily = InterFontFamily,
        modifier = Modifier.padding(top = 4.dp, start = 6.dp)
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
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.dropdown_semester, selected),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
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
                            text = stringResource(R.string.dropdown_semester, sem.toString()),
                            fontFamily = InterFontFamily,
                            fontSize = 14.sp,
                            fontWeight = if (selected == semStr) FontWeight.Bold else FontWeight.Normal,
                            color = if (selected == semStr) PurpleGradia else MaterialTheme.colorScheme.onSurface
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
        color = MaterialTheme.colorScheme.surfaceVariant
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontFamily = InterFontFamily
                ),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(PurpleGradia),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
