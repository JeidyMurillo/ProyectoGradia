package com.example.gradia.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.InterFontFamily

enum class SubjectIconType { Math, Palette, Flask, Code, Default }

fun subjectIconType(name: String): SubjectIconType = when {
    listOf("calculo", "cálculo", "matemat", "algebra", "álgebra", "estadist", "geometr")
        .any { name.contains(it, ignoreCase = true) } -> SubjectIconType.Math
    listOf("diseño", "diseno", "ui", "ux", "arte", "grafic", "pintura")
        .any { name.contains(it, ignoreCase = true) } -> SubjectIconType.Palette
    listOf("fisica", "física", "quimica", "química", "biologia", "biología", "ciencia", "laboratorio")
        .any { name.contains(it, ignoreCase = true) } -> SubjectIconType.Flask
    listOf("programac", "software", "codigo", "código", "desarrollo", "informat")
        .any { name.contains(it, ignoreCase = true) } -> SubjectIconType.Code
    else -> SubjectIconType.Default
}

fun iconStringFor(type: SubjectIconType): String = when (type) {
    SubjectIconType.Math -> "fx"
    SubjectIconType.Palette -> "palette"
    SubjectIconType.Flask -> "flask"
    SubjectIconType.Code -> "code"
    SubjectIconType.Default -> "📚"
}

fun iconTypeFromString(icon: String): SubjectIconType = when (icon) {
    "fx" -> SubjectIconType.Math
    "palette" -> SubjectIconType.Palette
    "flask" -> SubjectIconType.Flask
    "code" -> SubjectIconType.Code
    else -> SubjectIconType.Default
}

fun iconLabel(type: SubjectIconType): String = when (type) {
    SubjectIconType.Math -> "Matemáticas"
    SubjectIconType.Palette -> "Diseño y Arte"
    SubjectIconType.Flask -> "Ciencias"
    SubjectIconType.Code -> "Programación"
    SubjectIconType.Default -> "General"
}

fun resolveSubjectIcon(savedIcon: String, name: String): SubjectIconType =
    if (savedIcon.isNotBlank()) iconTypeFromString(savedIcon) else subjectIconType(name)

@Composable
fun SubjectIconRender(type: SubjectIconType, tint: Color, size: Dp) {
    when (type) {
        SubjectIconType.Math -> Text(
            text = "fx",
            fontSize = (size.value * 0.65f).sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = tint,
            fontFamily = InterFontFamily
        )
        SubjectIconType.Palette -> Icon(
            painter = painterResource(id = R.drawable.subject_palette),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
        SubjectIconType.Flask -> Icon(
            painter = painterResource(id = R.drawable.subject_flask),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
        SubjectIconType.Code -> Icon(
            painter = painterResource(id = R.drawable.subject_code),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
        SubjectIconType.Default -> Icon(
            painter = painterResource(id = R.drawable.graduation_cap),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}
