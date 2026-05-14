package com.example.gradia.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.*

@Composable
fun ProfileScreen() {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("Sophia Rose") }
    var email by remember { mutableStateOf("sophia.rose@university.edu") }
    var career by remember { mutableStateOf("Diseño UX/UI") }
    var semester by remember { mutableStateOf("7") }
    var password by remember { mutableStateOf("password123") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FF))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(200.dp)
                .clickable { }
        ) {
            Image(
                painter = painterResource(id = R.drawable.splash_logo),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.White),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(52.dp)
                    .background(PurpleGradia, CircleShape)
                    .clickable { },
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
            isEditing = isEditing,
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
                text = "Semestre actual:",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = InterFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    fontSize = 14.sp
                )
            )

            Box(
                modifier = Modifier
                    .width(120.dp)
                    .background(
                        Color.White,
                        RoundedCornerShape(30.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0xFFD8CDF0),
                        shape = RoundedCornerShape(30.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
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
                                .width(60.dp)
                                .heightIn(min = 24.dp),
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
                                color = Color(0xFF6B6B6B),
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        PasswordField(
            label = "Contraseña",
            value = password,
            isEditing = isEditing,
            onValueChange = { password = it },
            passwordVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { isEditing = !isEditing },
            modifier = Modifier
                .wrapContentWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.rename_box),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEditing) "Guardar" else "Editar Perfil",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = InterFontFamily,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
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
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A),
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(30.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFD8CDF0),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            if (isEditing) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 24.dp),
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
                        color = Color(0xFF6B6B6B),
                        fontSize = 16.sp
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
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = InterFontFamily,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A),
                fontSize = 14.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.White,
                    RoundedCornerShape(30.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFD8CDF0),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditing) {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 24.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = InterFontFamily,
                            color = Color(0xFF4A4A4A),
                            fontSize = 16.sp
                        ),
                        cursorBrush = SolidColor(PurpleGradia)
                    )
                } else {
                    Text(
                        text = "••••••••",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = InterFontFamily,
                            color = Color(0xFF6B6B6B),
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                IconButton(
                    onClick = onToggleVisibility,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.visibility_off else R.drawable.visibility
                        ),
                        contentDescription = "Toggle password visibility",
                        tint = PurpleGradia,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}