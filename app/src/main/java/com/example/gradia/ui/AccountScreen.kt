package com.example.gradia.ui

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.GradiaApplication
import com.example.gradia.R
import com.example.gradia.data.firebase.FacebookSignInUtil
import com.example.gradia.data.firebase.GoogleSignInUtil
import com.example.gradia.data.firebase.getFirebaseErrorMessage
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.example.gradia.data.local.entity.Asignatura
import com.example.gradia.data.local.entity.Evento
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountScreen(
    onNavigateToProfile: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as GradiaApplication
    val scope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }
    var isExporting by remember { mutableStateOf(false) }
    var exportError by remember { mutableStateOf<String?>(null) }

    var showImportDialog by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<String?>(null) }
    var importError by remember { mutableStateOf<String?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleteError by remember { mutableStateOf<String?>(null) }

    var showMethodDialog by remember { mutableStateOf(false) }
    var isLinking by remember { mutableStateOf(false) }
    var linkError by remember { mutableStateOf<String?>(null) }
    var linkSuccess by remember { mutableStateOf<String?>(null) }
    var showEmailLinkDialog by remember { mutableStateOf(false) }
    var linkEmail by remember { mutableStateOf("") }
    var linkPassword by remember { mutableStateOf("") }
    var linkEmailError by remember { mutableStateOf<String?>(null) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isImporting = true
                importError = null
                importResult = null
                try {
                    val json = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                            ?: throw Exception("No se pudo leer el archivo")
                    }
                    val userId = app.authRepository.getCurrentUserId()
                        ?: throw Exception("No hay sesión activa")
                    importData(app, json, userId)
                    importResult = "Datos importados correctamente"
                } catch (e: Exception) {
                    importError = e.message ?: "Error al importar"
                } finally {
                    isImporting = false
                }
            }
        }
    }

    val googleLinkLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val accountResult = GoogleSignInUtil.getSignedInAccountFromIntent(result.data)
        if (accountResult.isSuccessful) {
            val googleAccount = accountResult.result ?: return@rememberLauncherForActivityResult
            val idToken = googleAccount.idToken ?: return@rememberLauncherForActivityResult
            scope.launch {
                isLinking = true
                linkError = null
                try {
                    app.authRepository.linkWithGoogle(idToken).fold(
                        onSuccess = {
                            linkSuccess = "Cuenta de Google vinculada correctamente"
                        },
                        onFailure = { e ->
                            linkError = e.message ?: "Error al vincular Google"
                        }
                    )
                } catch (e: Exception) {
                    linkError = e.message ?: "Error inesperado"
                } finally {
                    isLinking = false
                }
            }
        }
    }

    val providers = app.authRepository.getLinkedProviders()
    val hasPassword = providers.contains("password")
    val hasGoogle = providers.contains("google.com")
    val hasFacebook = providers.contains("facebook.com")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            AccountSettingsItem(
                iconPainter = painterResource(id = R.drawable.user_outline),
                title = "Perfil",
                onClick = onNavigateToProfile
            )
        }

        item {
            AccountSettingsItem(
                iconPainter = painterResource(id = R.drawable.download),
                title = "Exportar datos",
                subtitle = "Guarda tus datos en formato JSON",
                onClick = { showExportDialog = true }
            )
        }

        item {
            AccountSettingsItem(
                iconPainter = painterResource(id = R.drawable.ic_google),
                title = "Importar datos",
                subtitle = "Restaura datos desde un archivo JSON",
                onClick = { showImportDialog = true }
            )
        }

        item {
            AccountSettingsItem(
                iconPainter = painterResource(id = R.drawable.delete),
                title = "Eliminar cuenta",
                subtitle = "Borra todos tus datos y cierra sesión",
                onClick = { showDeleteDialog = true }
            )
        }

        item {
            AccountSettingsItem(
                iconPainter = painterResource(id = R.drawable.ic_google),
                title = "Cambiar método de inicio",
                subtitle = "Vincular Google o correo",
                onClick = { showMethodDialog = true; linkError = null; linkSuccess = null }
            )
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }

    // ── Export Dialog ──
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isExporting) showExportDialog = false },
            title = { Text("Exportar datos", fontWeight = FontWeight.Bold) },
            text = {
                when {
                    exportError != null -> Text("Error: $exportError", color = Color.Red)
                    else -> Text("Se exportarán tus materias, notas, eventos y notas personales en formato JSON.")
                }
            },
            confirmButton = {
                if (isExporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    TextButton(onClick = {
                        scope.launch {
                            isExporting = true
                            exportError = null
                            try {
                                val userId = app.authRepository.getCurrentUserId()
                                if (userId == null) { exportError = "No hay sesión activa"; isExporting = false; return@launch }
                                val json = generateExportJson(app, userId)
                                saveJsonToDownloads(context, json)
                                showExportDialog = false
                            } catch (e: Exception) { exportError = e.message ?: "Error desconocido"
                            } finally { isExporting = false }
                        }
                    }) { Text("Exportar") }
                }
            },
            dismissButton = {
                if (!isExporting) TextButton(onClick = { showExportDialog = false; exportError = null }) { Text("Cancelar") }
            }
        )
    }

    // ── Import Dialog ──
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isImporting) { showImportDialog = false; importResult = null; importError = null } },
            title = { Text("Importar datos", fontWeight = FontWeight.Bold) },
            text = {
                when {
                    importResult != null -> Text(importResult!!, color = Color(0xFF4CAF50))
                    importError != null -> Text("Error: $importError", color = Color.Red)
                    isImporting -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Importando datos...", color = Color.Gray)
                        }
                    }
                    else -> Text("Selecciona un archivo JSON exportado previamente para restaurar tus datos.")
                }
            },
            confirmButton = {
                if (importResult != null || importError != null) {
                    TextButton(onClick = { showImportDialog = false; importResult = null; importError = null }) { Text("Cerrar") }
                } else if (!isImporting) {
                    TextButton(onClick = {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    }) { Text("Seleccionar archivo") }
                }
            },
            dismissButton = {
                if (!isImporting && importResult == null) {
                    TextButton(onClick = { showImportDialog = false; importError = null }) { Text("Cancelar") }
                }
            }
        )
    }

    // ── Delete Account Dialog ──
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("Eliminar cuenta", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                when {
                    deleteError != null -> Text("Error: $deleteError", color = Color.Red)
                    isDeleting -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Eliminando datos...", color = Color.Gray)
                        }
                    }
                    else -> {
                        Column {
                            Text("¿Estás seguro de que quieres eliminar tu cuenta? Esta acción es irreversible y se borrarán todos tus datos.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Se eliminarán:", fontWeight = FontWeight.Medium)
                            Text("• Materias y notas")
                            Text("• Eventos y tareas")
                            Text("• Notas personales")
                            Text("• Tu cuenta de Firebase")
                        }
                    }
                }
            },
            confirmButton = {
                if (isDeleting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else if (deleteError == null) {
                    Button(
                        onClick = {
                            scope.launch {
                                isDeleting = true
                                deleteError = null
                                try {
                                    val result = app.authRepository.deleteAccount {
                                        withContext(Dispatchers.IO) {
                                            app.database.clearAllTables()
                                        }
                                    }
                                    result.fold(
                                        onSuccess = {
                                            app.authRepository.signOut()
                                            showDeleteDialog = false
                                            onDeleteAccount()
                                        },
                                        onFailure = { e ->
                                            deleteError = e.message ?: "Error al eliminar cuenta"
                                        }
                                    )
                                } catch (e: Exception) {
                                    deleteError = e.message ?: "Error inesperado"
                                } finally {
                                    isDeleting = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Eliminar todo", color = Color.White) }
                } else {
                    TextButton(onClick = { showDeleteDialog = false; deleteError = null }) { Text("Cerrar") }
                }
            },
            dismissButton = {
                if (!isDeleting && deleteError == null) {
                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                }
            }
        )
    }

    // ── Change Login Method Dialog ──
    if (showMethodDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLinking) { showMethodDialog = false; linkError = null; linkSuccess = null } },
            title = { Text("Métodos de inicio", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (linkSuccess != null) {
                        Text(linkSuccess!!, color = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (linkError != null) {
                        Text("Error: $linkError", color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text("Métodos vinculados actualmente:", fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (hasPassword) "✔" else "○", color = if (hasPassword) Color(0xFF4CAF50) else Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Correo electrónico", color = if (hasPassword) Color(0xFF4CAF50) else Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (hasGoogle) "✔" else "○", color = if (hasGoogle) Color(0xFF4CAF50) else Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google", color = if (hasGoogle) Color(0xFF4CAF50) else Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (hasFacebook) "✔" else "○", color = if (hasFacebook) Color(0xFF4CAF50) else Color.Gray)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Facebook", color = if (hasFacebook) Color(0xFF4CAF50) else Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!hasPassword) {
                        Button(
                            onClick = { showEmailLinkDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia),
                            enabled = !isLinking
                        ) { Text("Vincular correo electrónico") }
                    }
                    if (!hasGoogle) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    GoogleSignInUtil.signOut()
                                    googleLinkLauncher.launch(GoogleSignInUtil.getSignInIntent())
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia),
                            enabled = !isLinking
                        ) { Text("Vincular Google") }
                    }
                    if (!hasFacebook) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                LoginManager.getInstance().registerCallback(FacebookSignInUtil.callbackManager, object : FacebookCallback<LoginResult> {
                                    override fun onSuccess(result: LoginResult) {
                                        scope.launch {
                                            isLinking = true
                                            linkError = null
                                            try {
                                                app.authRepository.linkWithFacebook(result.accessToken.token).fold(
                                                    onSuccess = {
                                                        linkSuccess = "Cuenta de Facebook vinculada correctamente"
                                                    },
                                                    onFailure = { e ->
                                                        linkError = getFirebaseErrorMessage(e)
                                                    }
                                                )
                                            } catch (e: Exception) {
                                                linkError = e.message ?: "Error inesperado"
                                            } finally {
                                                isLinking = false
                                            }
                                        }
                                    }
                                    override fun onCancel() {}
                                    override fun onError(error: FacebookException) {
                                        linkError = error.message ?: "Error al vincular Facebook"
                                    }
                                })
                                FacebookSignInUtil.loginWithAccountPicker(
                                    context as androidx.activity.ComponentActivity,
                                    listOf("email", "public_profile")
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia),
                            enabled = !isLinking
                        ) { Text("Vincular Facebook") }
                    }
                    if (providers.size > 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tienes múltiples métodos. Para desvincular uno, elimina la cuenta y vuelve a registrarte.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMethodDialog = false; linkError = null; linkSuccess = null }) { Text("Cerrar") }
            }
        )
    }

    // ── Link Email Dialog ──
    if (showEmailLinkDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLinking) { showEmailLinkDialog = false; linkEmailError = null } },
            title = { Text("Vincular correo", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    if (linkEmailError != null) {
                        Text(linkEmailError!!, color = Color.Red)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    OutlinedTextField(
                        value = linkEmail,
                        onValueChange = { linkEmail = it },
                        label = { Text("Correo") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = linkPassword,
                        onValueChange = { linkPassword = it },
                        label = { Text("Contraseña") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                if (isLinking) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    TextButton(onClick = {
                        if (linkEmail.isBlank() || linkPassword.isBlank()) {
                            linkEmailError = "Completa todos los campos"
                            return@TextButton
                        }
                        scope.launch {
                            isLinking = true
                            linkEmailError = null
                            try {
                                app.authRepository.linkWithEmail(linkEmail, linkPassword).fold(
                                    onSuccess = {
                                        showEmailLinkDialog = false
                                        linkSuccess = "Correo vinculado correctamente"
                                    },
                                    onFailure = { e ->
                                        linkEmailError = e.message ?: "Error al vincular"
                                    }
                                )
                            } catch (e: Exception) {
                                linkEmailError = e.message ?: "Error inesperado"
                            } finally {
                                isLinking = false
                            }
                        }
                    }) { Text("Vincular") }
                }
            },
            dismissButton = {
                if (!isLinking) {
                    TextButton(onClick = { showEmailLinkDialog = false; linkEmailError = null }) { Text("Cancelar") }
                }
            }
        )
    }
}

// ───────── Export ─────────

private suspend fun generateExportJson(app: GradiaApplication, userId: String): String = withContext(Dispatchers.IO) {
    val user = app.userRepository.getUserById(userId).first()
    val asignaturas = app.asignaturaRepository.getAsignaturasByUser(userId).first()
    val eventos = app.eventoRepository.getEventosByUser(userId).first()
    val notasContenido = app.noteRepository.getNotes(userId).first()

    val root = JSONObject().apply {
        put("app", "Gradia")
        put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        put("version", 1)

        user?.let {
            put("usuario", JSONObject().apply {
                put("id", it.id)
                put("nombre", it.nombre)
                put("email", it.email)
                put("metaPromedio", it.metaPromedio)
            })
        }

        put("asignaturas", JSONArray().apply {
            asignaturas.forEach { asig ->
                put(JSONObject().apply {
                    put("originalId", asig.id)
                    put("nombre", asig.nombre)
                    put("icono", asig.icono)
                    put("profesor", asig.profesor)
                    put("salon", asig.salon)
                    put("creditos", asig.creditos)
                    put("semestre", asig.semestre)
                    put("notaAprobacion", asig.notaAprobacion)
                })
            }
        })

        put("eventos", JSONArray().apply {
            eventos.forEach { ev ->
                put(JSONObject().apply {
                    put("originalId", ev.id)
                    put("asignaturaId", ev.asignaturaId)
                    put("titulo", ev.titulo)
                    put("descripcion", ev.descripcion)
                    put("fecha", ev.fecha)
                    put("tipo", ev.tipo)
                    put("completado", ev.completado)
                })
            }
        })

        put("notas", JSONArray().apply {
            notasContenido.forEach { nota ->
                put(JSONObject().apply {
                    put("originalId", nota.id)
                    put("titulo", nota.title)
                    put("contenido", nota.content)
                    put("color", nota.color)
                })
            }
        })
    }

    root.toString(2)
}

private fun saveJsonToDownloads(context: Context, json: String) {
    val fileName = "Gradia_respaldo_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
        put(MediaStore.Downloads.MIME_TYPE, "application/json")
        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }
    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        context.contentResolver.openOutputStream(it)?.use { os ->
            os.write(json.toByteArray())
        }
    }
}

// ───────── Import ─────────

private suspend fun importData(app: GradiaApplication, json: String, userId: String) = withContext(Dispatchers.IO) {
    val root = JSONObject(json)

    val oldToNewId = mutableMapOf<Long, Long>()

    // Import asignaturas
    val asignaturasArray = root.optJSONArray("asignaturas")
    if (asignaturasArray != null) {
        for (i in 0 until asignaturasArray.length()) {
            val obj = asignaturasArray.getJSONObject(i)
            val newId = app.asignaturaRepository.insertAsignatura(
                Asignatura(
                    userId = userId,
                    nombre = obj.getString("nombre"),
                    icono = obj.optString("icono", "\uD83D\uDCDA"),
                    profesor = obj.optString("profesor", ""),
                    salon = obj.optString("salon", ""),
                    creditos = obj.optInt("creditos", 0),
                    semestre = obj.optInt("semestre", 1),
                    notaAprobacion = obj.optDouble("notaAprobacion", 3.0).toFloat()
                )
            )
            oldToNewId[obj.getLong("originalId")] = newId
        }
    }

    // Import eventos
    val eventosArray = root.optJSONArray("eventos")
    if (eventosArray != null) {
        for (i in 0 until eventosArray.length()) {
            val obj = eventosArray.getJSONObject(i)
            val oldAsigId = obj.optLong("asignaturaId", -1L)
            val newAsigId = if (oldAsigId != -1L) oldToNewId[oldAsigId] else null
            app.eventoRepository.insertEvento(
                Evento(
                    userId = userId,
                    asignaturaId = newAsigId,
                    titulo = obj.getString("titulo"),
                    descripcion = obj.optString("descripcion", ""),
                    fecha = obj.getLong("fecha"),
                    tipo = obj.optString("tipo", "TAREA"),
                    completado = obj.optBoolean("completado", false)
                )
            )
        }
    }

    // Import notas (rich text notes)
    val notasArray = root.optJSONArray("notas")
    if (notasArray != null) {
        for (i in 0 until notasArray.length()) {
            val obj = notasArray.getJSONObject(i)
            app.noteRepository.saveNote(
                com.example.gradia.domain.model.Note(
                    title = obj.getString("titulo"),
                    content = obj.getString("contenido"),
                    color = obj.optLong("color", 0xFFFFE0E0),
                    userId = userId
                )
            )
        }
    }
}

    // ───────── Common UI ─────────

@Composable
fun AccountSettingsItem(
    iconPainter: Painter,
    title: String,
    subtitle: String? = null,
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

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = InterFontFamily,
                    color = PurpleGradia,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp
                )
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = InterFontFamily,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = PurpleGradia,
            modifier = Modifier.size(24.dp)
        )
    }
}
