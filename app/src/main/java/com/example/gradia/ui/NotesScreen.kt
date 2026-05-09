package com.example.gradia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gradia.GradiaApplication
import com.example.gradia.R
import com.example.gradia.domain.model.Note
import com.example.gradia.presentation.viewmodel.NotesViewModel
import com.example.gradia.ui.theme.*
import com.example.gradia.util.RichTextUtil
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File
import java.io.FileOutputStream

@Composable
fun NotesScreen(viewModel: NotesViewModel? = null) {
    val app = LocalContext.current.applicationContext as GradiaApplication
    val vm = viewModel ?: viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return app.provideNotesViewModel() as T
        }
    })
    val state by vm.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            CategoryRow(
                categories = state.allCategories,
                selectedIds = state.selectedCategoryIds,
                onCategoryClick = vm::toggleCategorySelection,
                onCreateCategory = { name, color -> vm.createCategory(name, color) }
            )
        }

        item {
            NoteEditorCard(
                title = state.currentTitle,
                content = state.currentContent,
                noteCategories = state.noteCategories,
                allCategories = state.allCategories,
                onTitleChange = vm::onTitleChange,
                onContentChange = vm::onContentChange,
                onCategoryToggle = vm::toggleNoteCategory,
                onSave = vm::saveNote
            )
        }

        val notes = state.savedNotes
        if (notes.isNotEmpty()) {
            items(notes.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { note ->
                        NoteGridItem(
                            note = note,
                            onEdit = vm::loadNoteForEditing,
                            onDelete = vm::deleteNote,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun CategoryRow(
    categories: List<com.example.gradia.domain.model.Category>,
    selectedIds: Set<Long>,
    onCategoryClick: (Long) -> Unit,
    onCreateCategory: (String, Long) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                label = category.name,
                backgroundColor = Color(category.color),
                isSelected = category.id in selectedIds,
                onClick = { onCategoryClick(category.id) }
            )
        }
        item {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { showCreateDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category", modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
        }
    }

    if (showCreateDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, color ->
                onCreateCategory(name, color)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CategoryChip(label: String, backgroundColor: Color, isSelected: Boolean = false, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) backgroundColor else Color.White,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)) else null,
        modifier = Modifier.height(32.dp).clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = Color.DarkGray,
                    fontFamily = InterFontFamily
                )
            )
        }
    }
}

@Composable
fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFFD0EFFF) }
    val colorOptions = listOf(
        0xFFD0EFFF to "Azul",
        0xFFFFE0E0 to "Rojo",
        0xFFD1AFF5 to "Morado",
        0xFFE0FFD0 to "Verde",
        0xFFFFF5CC to "Amarillo"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva categoría", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Color:", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorOptions.forEach { (color, _) ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(color))
                                .border(
                                    if (selectedColor == color) 2.dp else 0.dp,
                                    if (selectedColor == color) Color.DarkGray else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name, selectedColor) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia)
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun NoteEditorCard(
    title: String = "",
    content: String = "",
    noteCategories: List<com.example.gradia.domain.model.Category> = emptyList(),
    allCategories: List<com.example.gradia.domain.model.Category> = emptyList(),
    onTitleChange: (String) -> Unit = {},
    onContentChange: (String) -> Unit = {},
    onCategoryToggle: (Long) -> Unit = {},
    onSave: () -> Unit = {}
) {
    val data = remember(content) { RichTextUtil.deserialize(content) }
    var tfValue by remember(content) { mutableStateOf(TextFieldValue(data.text)) }
    var boldRanges by remember(content) { mutableStateOf(data.boldRanges) }
    var italicRanges by remember(content) { mutableStateOf(data.italicRanges) }
    var imagePaths by remember(content) { mutableStateOf(data.imagePaths) }
    var isBoldActive by remember { mutableStateOf(false) }
    var isItalicActive by remember { mutableStateOf(false) }
    var isBulletActive by remember { mutableStateOf(false) }

    val visualTransformation = remember(boldRanges, italicRanges) {
        RichTextUtil.visualTransformation(boldRanges, italicRanges)
    }

    fun adjustRanges(
        ranges: List<IntRange>,
        oldLen: Int,
        newLen: Int,
        isActive: Boolean
    ): List<IntRange> {
        if (newLen > oldLen) {
            val result = ranges.toMutableList()
            if (isActive) {
                result.add(oldLen until newLen)
            }
            return result.distinct()
        } else if (newLen < oldLen) {
            return ranges.filter { it.last < newLen }
        }
        return ranges
    }

    fun toggleFormat(
        ranges: List<IntRange>,
        target: IntRange,
        setRanges: (List<IntRange>) -> Unit
    ) {
        val result = ranges.toMutableList()
        val existing = result.indexOfFirst { it == target }
        if (existing >= 0) {
            result.removeAt(existing)
        } else {
            result.removeAll { target.contains(it.first) && target.contains(it.last) }
            result.add(target)
        }
        setRanges(result)
    }

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            try {
                val fileDir = File(context.filesDir, "images").also { it.mkdirs() }
                val file = File(fileDir, "note_img_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(it)?.use { input ->
                    FileOutputStream(file).use { output -> input.copyTo(output) }
                }
                val path = file.absolutePath
                imagePaths = imagePaths + path
                val text = tfValue.text
                val cursor = tfValue.selection.start
                val newText = text.substring(0, cursor) + " 🖼 " + text.substring(cursor)
                tfValue = TextFieldValue(newText, TextRange(cursor + 3))
            } catch (_: Exception) { }
        }
    }

    fun handleSave() {
        val json = RichTextUtil.serialize(tfValue.text, boldRanges, italicRanges, imagePaths)
        onContentChange(json)
        onSave()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleGradia.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            TextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text("Title", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Gray) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 1.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
            ) {
                BasicTextField(
                    value = tfValue,
                    onValueChange = { newValue ->
                        val oldText = tfValue.text
                        val newText = newValue.text
                        val oldLen = oldText.length
                        val newLen = newText.length
                        if (isBulletActive && newText.length > oldText.length) {
                            val common = oldText.commonPrefixWith(newText)
                            val inserted = newText.substring(common.length)
                            if (inserted.contains("\n")) {
                                val nlPos = common.length + inserted.indexOf("\n")
                                val after = newText.substring(nlPos + 1)
                                val modified = if (after.isEmpty() || !after.startsWith("• ")) {
                                    newText.substring(0, nlPos + 1) + "• " + after
                                } else {
                                    newText
                                }
                                val cursorShift = if (newValue.selection.start > nlPos && modified != newText) 2 else 0
                                boldRanges = adjustRanges(boldRanges, oldLen, modified.length, isBoldActive)
                                italicRanges = adjustRanges(italicRanges, oldLen, modified.length, isItalicActive)
                                tfValue = TextFieldValue(modified, TextRange(newValue.selection.start + cursorShift))
                            } else {
                                boldRanges = adjustRanges(boldRanges, oldLen, newLen, isBoldActive)
                                italicRanges = adjustRanges(italicRanges, oldLen, newLen, isItalicActive)
                                tfValue = newValue
                            }
                        } else {
                            boldRanges = adjustRanges(boldRanges, oldLen, newLen, isBoldActive)
                            italicRanges = adjustRanges(italicRanges, oldLen, newLen, isItalicActive)
                            tfValue = newValue
                        }
                    },
                    visualTransformation = visualTransformation,
                    cursorBrush = SolidColor(PurpleGradia),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.Black,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (tfValue.text.isEmpty()) {
                            Text(
                                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus condimentum magna eu augue malesuada, vitae vestibulum odio varius. Suspendisse nec mauris ut arcu venenatis laoreet eget id ex.",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                        innerTextField()
                    }
                )
            }

            if (allCategories.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.heightIn(max = 32.dp)
                ) {
                    items(allCategories) { category ->
                        val isSelected = noteCategories.any { it.id == category.id }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(category.color) else Color.White,
                            border = if (!isSelected) androidx.compose.foundation.BorderStroke(
                                1.dp, Color.LightGray.copy(alpha = 0.5f)
                            ) else null,
                            modifier = Modifier.height(28.dp).clickable { onCategoryToggle(category.id) }
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = Color.DarkGray,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "B",
                        fontWeight = if (isBoldActive) FontWeight.ExtraBold else FontWeight.Bold,
                        color = if (isBoldActive) Color(0xFF4A0072) else PurpleGradia,
                        fontSize = 18.sp,
                        modifier = Modifier.clickable {
                            val sel = tfValue.selection
                            if (sel.collapsed) {
                                isBoldActive = !isBoldActive
                            } else {
                                toggleFormat(boldRanges, sel.min until sel.max, { boldRanges = it })
                            }
                        }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.italic),
                        contentDescription = "Italic",
                        tint = if (isItalicActive) Color(0xFF4A0072) else PurpleGradia,
                        modifier = Modifier.size(18.dp).clickable {
                            val sel = tfValue.selection
                            if (sel.collapsed) {
                                isItalicActive = !isItalicActive
                            } else {
                                toggleFormat(italicRanges, sel.min until sel.max, { italicRanges = it })
                            }
                        }
                    )
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = PurpleGradia,
                        modifier = Modifier.size(18.dp).clickable {
                            tfValue = TextFieldValue("")
                            boldRanges = emptyList()
                            italicRanges = emptyList()
                            isBoldActive = false
                            isItalicActive = false
                            isBulletActive = false
                            imagePaths = emptyList()
                            onContentChange("")
                            onTitleChange("")
                        }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.list),
                        contentDescription = "Bullets",
                        tint = if (isBulletActive) Color(0xFF4A0072) else PurpleGradia,
                        modifier = Modifier.size(18.dp).clickable {
                            isBulletActive = !isBulletActive
                        }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.image),
                        contentDescription = "Image",
                        tint = PurpleGradia,
                        modifier = Modifier.size(18.dp).clickable {
                            launcher.launch("image/*")
                        }
                    )
                }

                Button(
                    onClick = { handleSave() },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Guardar nota", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun NoteGridItem(
    note: Note,
    modifier: Modifier = Modifier,
    onEdit: (Note) -> Unit = {},
    onDelete: (Long) -> Unit = {}
) {
    var showFullNote by remember { mutableStateOf(false) }
    val previewText = RichTextUtil.extractPlainText(note.content)
    val palette = listOf(0xFFFFE0E0L, 0xFFC6E6FFL, 0xFFC59FE3L)
    val bgColor = remember(note.id) { Color(palette[note.id.toInt().mod(3).coerceIn(0, 2)]) }

    Card(
        modifier = modifier
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { showFullNote = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title.ifEmpty { "Sin título" },
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = Color.Black.copy(alpha = 0.1f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = previewText.ifEmpty { "Sin contenido" },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (showFullNote) {
        val fullData = remember(note.content) { RichTextUtil.deserialize(note.content) }

        AlertDialog(
            onDismissRequest = { showFullNote = false },
            title = {
                Text(
                    text = note.title.ifEmpty { "Sin título" },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    val annotatedText = remember(fullData.text, fullData.boldRanges, fullData.italicRanges) {
                        buildAnnotatedString {
                            append(fullData.text)
                            for (range in fullData.boldRanges) {
                                val start = range.first.coerceIn(0, fullData.text.length)
                                val end = (range.last + 1).coerceIn(start, fullData.text.length)
                                if (start < end) {
                                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                                }
                            }
                            for (range in fullData.italicRanges) {
                                val start = range.first.coerceIn(0, fullData.text.length)
                                val end = (range.last + 1).coerceIn(start, fullData.text.length)
                                if (start < end) {
                                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                                }
                            }
                        }
                    }

                    Text(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (fullData.imagePaths.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        fullData.imagePaths.forEach { path ->
                            val bitmap = remember(path) {
                                try { BitmapFactory.decodeFile(path)?.asImageBitmap() } catch (_: Exception) { null }
                            }
                            bitmap?.let {
                                Image(
                                    bitmap = it,
                                    contentDescription = "Imagen",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
                        }
                    }

                    if (note.categories.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Categorías:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            note.categories.forEach { category ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(category.color),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)) {
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = {
                            onDelete(note.id)
                            showFullNote = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) { Text("Eliminar") }
                    TextButton(
                        onClick = {
                            onEdit(note)
                            showFullNote = false
                        }
                    ) { Text("Editar") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showFullNote = false }) { Text("Cerrar") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    GradiaTheme {
        NotesScreen()
    }
}
