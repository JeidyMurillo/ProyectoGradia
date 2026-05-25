package com.example.gradia.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.gradia.GradiaApplication
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import com.example.gradia.R
import com.example.gradia.data.local.entity.User
import com.example.gradia.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userId: String = "",
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as GradiaApplication
    val userRepository = app.userRepository
    val authRepository = app.authRepository
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var career by remember { mutableStateOf("") }
    var semester by remember { mutableStateOf("") }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var pendingNewPassword by remember { mutableStateOf<String?>(null) }
    var originalUser by remember { mutableStateOf<User?>(null) }
    var photoUri by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            userRepository.getUserById(userId).collect { user ->
                if (user != null) {
                    name = user.nombre
                    email = user.email
                    career = user.carrera
                    semester = user.semestre
                    photoUri = user.fotoUrl
                    originalUser = user
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val localPath = withContext(Dispatchers.IO) {
                    copyImageToInternalStorage(it, userId, context)
                }
                photoUri = localPath
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FF))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(240.dp)
                .clickable(enabled = isEditing) { imagePickerLauncher.launch("image/*") }
        ) {
            if (!photoUri.isNullOrBlank()) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.White),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.splash_logo)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.White),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(52.dp)
                    .background(PurpleGradia, CircleShape)
                    .clickable(enabled = isEditing) { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.image),
                    contentDescription = "Edit Photo",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        ProfileField(
            label = "Nombre",
            value = name,
            isEditing = isEditing,
            onValueChange = { name = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        ProfileField(
            label = "Correo",
            value = email,
            isEditing = false,
            onValueChange = { email = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        ProfileField(
            label = "Carrera",
            value = career,
            isEditing = isEditing,
            onValueChange = { career = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Semestre actual - Número:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A4A4A),
                    fontSize = 16.sp
                )
            )

Box(
                modifier = Modifier
                    .width(140.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(30.dp),
                        ambientColor = Color.Black.copy(alpha = 0.15f),
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    )
                    .background(
                        Color.White,
                        RoundedCornerShape(30.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = PurpleGradia,
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (isEditing) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = semester,
                            onValueChange = { semester = it },
                            modifier = Modifier
                                .width(80.dp)
                                .heightIn(min = 20.dp),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = InterFontFamily,
                                color = Color(0xFF4A4A4A),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(PurpleGradia)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = semester,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = InterFontFamily,
                                color = Color(0xFF4A4A4A),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PasswordField(
            label = "Contraseña",
            value = "",
            isEditing = isEditing,
            onEditClick = { showChangePasswordDialog = true }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (isEditing) {
                        scope.launch {
                            try {
                                originalUser?.let { user ->
                                    userRepository.updateUser(
                                        user.copy(
                                            nombre = name,
                                            email = email,
                                            carrera = career,
                                            semestre = semester,
                                            fotoUrl = photoUri
                                        )
                                    )
                                }
                                pendingNewPassword?.let { newPassword ->
                                    authRepository.updatePassword(newPassword)
                                    pendingNewPassword = null
                                }
                                isEditing = false
                            } catch (e: Exception) {
                                // Error al guardar, permanece en modo edición
                            }
                        }
                    } else {
                        isEditing = true
                    }
                },
                modifier = Modifier
                    .width(160.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.edit_save),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isEditing) "Guardar" else "Editar",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = InterFontFamily,
                        color = Color.White
                    )
                )
            }

            if (isEditing) {
                Button(
                    onClick = {
                        pendingNewPassword = null
                        originalUser?.let { user ->
                            name = user.nombre
                            email = user.email
                            career = user.carrera
                            semester = user.semestre
                            photoUri = user.fotoUrl
                        }
                        isEditing = false
                    },
                    modifier = Modifier
                        .width(160.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9E9E9E)
                    ),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.close),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = InterFontFamily,
                            color = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            userEmail = email,
            onDismiss = { showChangePasswordDialog = false },
            onPasswordChanged = { newPassword ->
                pendingNewPassword = newPassword
                showChangePasswordDialog = false
            },
            authRepository = authRepository
        )
    }
}

private fun copyImageToInternalStorage(uri: Uri, userId: String, context: Context): String {
    val dir = File(context.filesDir, "profile_pictures")
    if (!dir.exists()) dir.mkdirs()
    dir.listFiles()?.filter { it.name.startsWith(userId) }?.forEach { it.delete() }
    val file = File(dir, "${userId}_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
        }
    } ?: throw java.io.IOException("Cannot open input stream for $uri")
    return file.absolutePath
}

@Composable
fun ProfileField(
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4A4A4A),
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .background(
                    Color.White,
                    RoundedCornerShape(30.dp)
                )
                .border(
                    width = 1.dp,
                    color = PurpleGradia,
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            if (isEditing) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 20.dp),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = InterFontFamily,
                        color = Color(0xFF4A4A4A),
                        fontSize = 16.sp
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(PurpleGradia)
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = InterFontFamily,
                        color = Color(0xFF4A4A4A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    }
}

@Composable
fun PasswordField(
    label: String,
    value: String,
    isEditing: Boolean,
    onEditClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4A4A4A),
                fontSize = 16.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(30.dp),
                    ambientColor = Color.Black.copy(alpha = 0.15f),
                    spotColor = Color.Black.copy(alpha = 0.3f)
                )
                .background(
                    Color.White,
                    RoundedCornerShape(30.dp)
                )
                .border(
                    width = 1.dp,
                    color = PurpleGradia,
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifEmpty { "••••••••" },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = InterFontFamily,
                        color = Color(0xFF4A4A4A),
                        fontSize = 16.sp
                    )
                )

                if (isEditing) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Cambiar contraseña",
                            tint = PurpleGradia,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChangePasswordDialog(
    userEmail: String,
    onDismiss: () -> Unit,
    onPasswordChanged: (String) -> Unit,
    authRepository: com.example.gradia.data.repository.AuthRepository
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Cambiar contraseña",
                fontWeight = FontWeight.Bold,
                fontFamily = InterFontFamily,
                color = Color(0xFF4A4A4A)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                errorMessage?.let {
                    Text(
                        it,
                        color = Color.Red,
                        fontSize = 13.sp,
                        fontFamily = InterFontFamily
                    )
                }

                DialogPasswordField(
                    label = "Contraseña actual",
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    visible = showCurrentPassword,
                    onToggleVisibility = { showCurrentPassword = !showCurrentPassword }
                )

                DialogPasswordField(
                    label = "Nueva contraseña",
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    visible = showNewPassword,
                    onToggleVisibility = { showNewPassword = !showNewPassword }
                )

                DialogPasswordField(
                    label = "Confirmar nueva contraseña",
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    visible = showConfirmPassword,
                    onToggleVisibility = { showConfirmPassword = !showConfirmPassword }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    errorMessage = null
                    when {
                        currentPassword.isBlank() -> errorMessage = "Ingresa tu contraseña actual"
                        newPassword.length < 6 -> errorMessage = "La nueva contraseña debe tener al menos 6 caracteres"
                        newPassword != confirmPassword -> errorMessage = "Las nuevas contraseñas no coinciden"
                        else -> {
                            isLoading = true
                            scope.launch {
                                val reauthResult = authRepository.reauthenticate(userEmail, currentPassword)
                                reauthResult.fold(
                                    onSuccess = {
                                        onPasswordChanged(newPassword)
                                    },
                                    onFailure = { e ->
                                        errorMessage = e.message ?: "Contraseña actual incorrecta"
                                    }
                                )
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia)
            ) {
                Text(
                    if (isLoading) "Cambiando..." else "Cambiar contraseña",
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar", color = Color(0xFF9E9E9E))
            }
        }
    )
}

@Composable
fun DialogPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    visible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4A4A4A),
            fontFamily = InterFontFamily
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = PurpleGradia,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = InterFontFamily,
                        color = Color(0xFF4A4A4A),
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(PurpleGradia)
                )
                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (visible) R.drawable.visibility else R.drawable.visibility_off
                        ),
                        contentDescription = "Toggle visibility",
                        tint = PurpleGradia,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}