package com.example.gradia.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.InterFontFamily
import com.example.gradia.ui.theme.PurpleGradia

@Composable
fun StatsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FF))
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AverageCard(
                    modifier = Modifier.weight(1.5f)
                )
                CreditsCard(
                    modifier = Modifier.weight(1f)
                )
            }

            DistributionChartCard()

            RecentMilestonesCard()

            CTAButton()

            Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun AverageCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .border(2.dp, PurpleGradia, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Promedio General",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = PurpleGradia,
                    fontFamily = InterFontFamily
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "4.2",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PurpleGradia,
                        fontSize = 32.sp,
                        fontFamily = InterFontFamily
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        "+0.2",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = Color(0xFF34C759),
                            fontFamily = InterFontFamily
                        ),
                        modifier = Modifier.offset(x = 25.dp, y = 12.dp)
                    )
                    Icon(
                        painter = painterResource(R.drawable.ic_average),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp),
                        tint = Color(0xFF34C759)
                    )
                }
            }
        }
    }
}

@Composable
fun CreditsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Créditos",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A4A4A),
                    fontFamily = InterFontFamily
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "95",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PurpleGradia,
                        fontSize = 32.sp,
                        fontFamily = InterFontFamily
                    )
                )
                Text(
                    " / 154",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF4A4A4A),
                        fontFamily = InterFontFamily
                    )
                )
            }
        }
    }
}

@Composable
fun DiagonalProgressBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE9E4F0))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(6.dp))
                .background(PurpleGradia)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-2).dp)
                    .background(
                        color = Color(0xFFE9E4F0),
                        shape = RoundedCornerShape(topStart = 4.dp)
                    )
            )
        }
    }
}

@Composable
fun DistributionChartCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Distribución de notas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    fontFamily = InterFontFamily
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DonutChart()
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendItem(
                        color = Color(0xFF4CAF50),
                        label = "Excelente (30%)"
                    )
                    LegendItem(
                        color = Color(0xFF81C784),
                        label = "Aprobado (50%)"
                    )
                    LegendItem(
                        color = Color(0xFFFFF176),
                        label = "Regular (10%)"
                    )
                    LegendItem(
                        color = Color(0xFFEF9A9A),
                        label = "Reprobado (10%)"
                    )
                }
            }
        }
    }
}

@Composable
fun DonutChart() {
    val animatedSweep by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000),
        label = "donut"
    )

    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            val strokeWidth = 20.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            val colors = listOf(
                Color(0xFF4CAF50),
                Color(0xFF81C784),
                Color(0xFFFFF176),
                Color(0xFFEF9A9A)
            )
            val percentages = listOf(0.30f, 0.50f, 0.10f, 0.10f)

            var startAngle = -90f

            percentages.forEachIndexed { index, percentage ->
                drawArc(
                    color = colors[index],
                    startAngle = startAngle,
                    sweepAngle = 360f * percentage * animatedSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += 360f * percentage
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "6",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    fontSize = 24.sp,
                    fontFamily = InterFontFamily
                )
            )
            Text(
                "Asignaturas",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.Gray,
                    fontFamily = InterFontFamily
                )
            )
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFF4A4A4A),
                fontFamily = InterFontFamily
            )
        )
    }
}

@Composable
fun RecentMilestonesCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Hitos recientes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    fontFamily = InterFontFamily
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.statistics_icon),
                    contentDescription = "Estadísticas",
                    modifier = Modifier.size(50.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Mejor promedio: Base de datos",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A4A4A),
                            fontFamily = InterFontFamily
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Has alcanzado un 4.8 en el último parcial",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.Gray,
                            fontFamily = InterFontFamily
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CTAButton() {
    Button(
        onClick = { /* Navigate to performance by subject */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia)
    ) {
        Text(
            "Ver rendimiento por asignatura",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontFamily = InterFontFamily
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color.White
        )
    }
}