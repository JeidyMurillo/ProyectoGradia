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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
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
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia
import kotlinx.coroutines.launch

private enum class SubjectFilter { Todas, Actual, Antiguas }

private enum class SubjectIconType { Math, Palette, Flask, Code, Default }

private fun subjectIconType(name: String): SubjectIconType = when {
    listOf("calculo", "cálculo", "matemat", "algebra", "álgebra", "estadist", "geometr").any { name.contains(it, ignoreCase = true) } -> SubjectIconType.Math
    listOf("diseño", "diseno", "ui", "ux", "arte", "grafic", "pintura").any { name.contains(it, ignoreCase = true) } -> SubjectIconType.Palette
    listOf("fisica", "física", "quimica", "química", "biologia", "biología", "ciencia", "laboratorio").any { name.contains(it, ignoreCase = true) } -> SubjectIconType.Flask
    listOf("programac", "software", "codigo", "código", "desarrollo", "informat").any { name.contains(it, ignoreCase = true) } -> SubjectIconType.Code
    else -> SubjectIconType.Default
}

@Composable
fun SubjectsScreen(onSubjectClick: (Subject) -> Unit = {}) {
    val app = LocalContext.current.applicationContext as GradiaApplication
    val repo = app.subjectRepository
    val subjects by repo.getSubjects().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf(SubjectFilter.Todas) }
    var showAddSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        SubjectsFilterRow(selected = selectedFilter, onSelected = { selectedFilter = it })
        Spacer(modifier = Modifier.height(18.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(subjects, key = { it.id }) { subject ->
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

    if (showAddSheet) {
        AddSubjectSheet(
            onDismiss = { showAddSheet = false },
            onSave = { newSubject ->
                scope.launch {
                    repo.insertSubject(newSubject)
                    showAddSheet = false
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterPill("Todas las asignaturas", SubjectFilter.Todas, selected, onSelected)
        FilterPill("Semestre Actual", SubjectFilter.Actual, selected, onSelected)
        FilterPill("Antiguas", SubjectFilter.Antiguas, selected, onSelected)
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
            SubjectIconRender(subject.name)
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
private fun SubjectIconRender(name: String) {
    when (subjectIconType(name)) {
        SubjectIconType.Math -> {
            Text(
                text = "fx",
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = PurpleGradia,
                fontFamily = InterFontFamily
            )
        }
        SubjectIconType.Palette -> {
            Icon(
                painter = painterResource(id = R.drawable.subject_palette),
                contentDescription = null,
                tint = PurpleGradia,
                modifier = Modifier.size(56.dp)
            )
        }
        SubjectIconType.Flask -> {
            Icon(
                painter = painterResource(id = R.drawable.subject_flask),
                contentDescription = null,
                tint = PurpleGradia,
                modifier = Modifier.size(56.dp)
            )
        }
        SubjectIconType.Code -> {
            Icon(
                painter = painterResource(id = R.drawable.subject_code),
                contentDescription = null,
                tint = PurpleGradia,
                modifier = Modifier.size(56.dp)
            )
        }
        SubjectIconType.Default -> {
            Icon(
                painter = painterResource(id = R.drawable.graduation_cap),
                contentDescription = null,
                tint = PurpleGradia,
                modifier = Modifier.size(56.dp)
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
    onDismiss: () -> Unit,
    onSave: (Subject) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("1") }
    var professor by remember { mutableStateOf("") }
    var classroom by remember { mutableStateOf("") }

    val creditsInt = credits.toIntOrNull()
    val isValid = name.isNotBlank() && creditsInt != null && creditsInt in 1..6

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
                    IconPreview(name = name)
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
                    PillTextField(
                        value = semester,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.toIntOrNull() != null) {
                                semester = input
                            }
                        },
                        placeholder = "1",
                        keyboardType = KeyboardType.Number
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
                    val finalSemester = semester.toIntOrNull() ?: 1
                    val icon = when (subjectIconType(name)) {
                        SubjectIconType.Math -> "fx"
                        SubjectIconType.Palette -> "palette"
                        SubjectIconType.Flask -> "flask"
                        SubjectIconType.Code -> "code"
                        SubjectIconType.Default -> "📚"
                    }
                    onSave(
                        Subject(
                            id = 0L,
                            name = name.trim(),
                            icon = icon,
                            creditHours = finalCredits,
                            semester = finalSemester,
                            professor = professor.trim(),
                            classroom = classroom.trim()
                        )
                    )
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurpleGradia,
                    disabledContainerColor = PurpleGradia.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(50)
            ) {
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
private fun IconPreview(name: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(PurpleGradia, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (subjectIconType(name)) {
            SubjectIconType.Math -> Text(
                text = "fx",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontStyle = FontStyle.Italic,
                color = Color.White,
                fontFamily = InterFontFamily
            )
            SubjectIconType.Palette -> Icon(
                painter = painterResource(id = R.drawable.subject_palette),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            SubjectIconType.Flask -> Icon(
                painter = painterResource(id = R.drawable.subject_flask),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            SubjectIconType.Code -> Icon(
                painter = painterResource(id = R.drawable.subject_code),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            SubjectIconType.Default -> Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
