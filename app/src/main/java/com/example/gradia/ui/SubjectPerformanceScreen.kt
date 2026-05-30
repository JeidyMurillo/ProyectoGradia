package com.example.gradia.ui



import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia

// ─── Data model ────────────────────────────────────────────────────────────────

data class SubjectPerformanceData(
    val subjectName: String,
    val currentAverage: Double,
    val trend: Double,               // e.g. +4.0 or -2.0
    val grades: List<Double>,        // one value per activity
    val activityLabels: List<String> // same length as grades
)

// ─── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectPerformanceScreen(onBackClick: () -> Unit = {}) {
    // ── Mock data – replace with ViewModel / repository data ──────────────────
    val subjects = listOf(
        SubjectPerformanceData(
            subjectName    = "Calculo IV",
            currentAverage = 4.5,
            trend          = 4.0,
            grades         = listOf(3.8, 4.0, 3.5, 4.2, 4.8),
            activityLabels = listOf("EVA 1", "TALLER", "PROY", "EVA 2", "EXAM")
        ),
        SubjectPerformanceData(
            subjectName    = "Física II",
            currentAverage = 3.2,
            trend          = -2.0,
            grades         = listOf(3.5, 3.8, 3.2, 2.9, 2.8),
            activityLabels = listOf("EVA 1", "ENSAYO", "PROY", "MAPA", "EXAM")
        ),
        SubjectPerformanceData(
            subjectName    = "Base de Datos",
            currentAverage = 4.1,
            trend          = 1.5,
            grades         = listOf(3.9, 4.0, 4.1, 4.3, 4.5),
            activityLabels = listOf("QUIZ 1", "TALLER", "PROY", "QUIZ 2", "FINAL")
        ),
        SubjectPerformanceData(
            subjectName    = "Programación III",
            currentAverage = 3.7,
            trend          = -0.5,
            grades         = listOf(3.9, 4.1, 3.8, 3.5, 3.2),
            activityLabels = listOf("EVA 1", "TALLER", "PROY", "EVA 2", "EXAM")
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "Mis asignaturas",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = InterFontFamily,
                    color = Color(0xFF1A1A1A)
                ),
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(subjects) { subject ->
            SubjectPerformanceCard(subject = subject)
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

// ─── Card ──────────────────────────────────────────────────────────────────────

@Composable
private fun SubjectPerformanceCard(subject: SubjectPerformanceData) {
    // Choose accent color based on trend
    val isPositive = subject.trend >= 0
    val lineColor  = if (isPositive) Color(0xFF5B9BD5) else Color(0xFFE8845A)
    val fillColor  = if (isPositive) Color(0xFF5B9BD5) else Color(0xFFE8845A)
    val badgeBg    = if (isPositive) Color(0xFFE6F4EA) else Color(0xFFFFEBEE)
    val badgeFg    = if (isPositive) Color(0xFF2E7D32) else Color(0xFFC62828)
    val trendText  = if (isPositive) "+${subject.trend.toInt()}%" else "${subject.trend.toInt()}%"

    // Animation progress (0f → 1f)
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(1f, animationSpec = tween(durationMillis = 1200))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE8DFF2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 14.dp)
        ) {
            // ── Header row ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subject.subjectName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily,
                        color = PurpleGradia,
                        fontSize = 17.sp
                    )
                )

                // Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeBg
                ) {
                    Text(
                        text = trendText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = badgeFg,
                            fontFamily = InterFontFamily,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Average row ─────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "Promedio actual: ",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF6B6B6B),
                        fontFamily = InterFontFamily
                    )
                )
                Text(
                    text = "%.1f".format(subject.currentAverage),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A),
                        fontFamily = InterFontFamily,
                        fontSize = 28.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Sparkline chart ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                SparklineChart(
                    grades       = subject.grades,
                    lineColor    = lineColor,
                    fillColor    = fillColor,
                    animProgress = animProgress.value,
                    modifier     = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── X-axis labels ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                subject.activityLabels.forEach { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF9E9E9E),
                            fontFamily = InterFontFamily,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

// ─── Sparkline (Canvas) ────────────────────────────────────────────────────────

@Composable
private fun SparklineChart(
    grades: List<Double>,
    lineColor: Color,
    fillColor: Color,
    animProgress: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (grades.size < 2) return@Canvas

        val minGrade = 0.0
        val maxGrade = 5.0
        val w = size.width
        val h = size.height
        val pad = 4.dp.toPx()

        // Map grade value → canvas Y (inverted: 0 at bottom)
        fun yOf(grade: Double): Float =
            (h - pad) - ((grade - minGrade) / (maxGrade - minGrade) * (h - pad * 2)).toFloat()

        // Map index → canvas X
        fun xOf(index: Int): Float =
            if (grades.size == 1) w / 2f
            else pad + index * (w - pad * 2) / (grades.size - 1)

        // Build full path points
        val points = grades.mapIndexed { i, g -> Offset(xOf(i), yOf(g)) }

        // Animate: only draw up to animProgress fraction of the path
        val totalPoints  = points.size
        val endIndex     = ((totalPoints - 1) * animProgress).toInt().coerceIn(0, totalPoints - 2)
        val partialFrac  = ((totalPoints - 1) * animProgress) - endIndex
        val drawnPoints  = points.take(endIndex + 1).toMutableList()
        if (endIndex < totalPoints - 1) {
            val p1 = points[endIndex]
            val p2 = points[endIndex + 1]
            drawnPoints.add(Offset(p1.x + (p2.x - p1.x) * partialFrac, p1.y + (p2.y - p1.y) * partialFrac))
        }

        if (drawnPoints.size < 2) return@Canvas

        // ── Build smooth cubic spline path ───────────────────────────────────
        val linePath = buildSmoothPath(drawnPoints)

        // ── Fill area under line ─────────────────────────────────────────────
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(drawnPoints.last().x, h)
            lineTo(drawnPoints.first().x, h)
            close()
        }
        drawPath(
            path  = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(fillColor.copy(alpha = 0.25f), fillColor.copy(alpha = 0.02f)),
                startY = 0f,
                endY   = h
            )
        )

        // ── Draw stroke line ─────────────────────────────────────────────────
        drawPath(
            path  = linePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // ── Draw endpoint dot ────────────────────────────────────────────────
        val last = drawnPoints.last()
        drawCircle(color = lineColor, radius = 4.dp.toPx(), center = last)
        drawCircle(color = Color.White, radius = 2.dp.toPx(), center = last)
    }
}

// ─── Smooth cubic spline helper ───────────────────────────────────────────────

private fun buildSmoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points[0].x, points[0].y)
    if (points.size == 1) return path

    for (i in 0 until points.size - 1) {
        val p0 = if (i > 0) points[i - 1] else points[i]
        val p1 = points[i]
        val p2 = points[i + 1]
        val p3 = if (i + 2 < points.size) points[i + 2] else points[i + 1]

        val cp1x = p1.x + (p2.x - p0.x) / 6f
        val cp1y = p1.y + (p2.y - p0.y) / 6f
        val cp2x = p2.x - (p3.x - p1.x) / 6f
        val cp2y = p2.y - (p3.y - p1.y) / 6f

        path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
    }
    return path
}