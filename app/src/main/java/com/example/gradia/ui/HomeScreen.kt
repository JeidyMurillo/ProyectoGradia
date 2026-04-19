package com.example.gradia.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onLogout: () -> Unit = {}) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedDrawerItem by remember { mutableStateOf("Home") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = Color(0xCC453284), // #453284 with 80% visibility
        drawerContent = {
            GradiaDrawerContent(
                selectedItem = selectedDrawerItem,
                onClose = { scope.launch { drawerState.close() } },
                onItemClick = { item ->
                    if (item == "Log Out") {
                        onLogout()
                    } else {
                        selectedDrawerItem = item
                        scope.launch { drawerState.close() }
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (selectedTab == 0) "Home" else if (selectedTab == 1) "Nota Final" else "Gradia",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4A4A4A)
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = PurpleGradia,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    actions = {
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            IconButton(
                                onClick = { /* TODO */ },
                                modifier = Modifier
                                    .background(SocialIconBg, CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = PurpleGradia,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            // Badge rojo
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(Color.Red, CircleShape)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                GradiaBottomBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            },
            containerColor = Color(0xFFFBF8FF)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedTab) {
                    0 -> HomeContent()
                    1 -> FinalGradeScreen()
                    else -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Próximamente", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GradiaDrawerContent(
    selectedItem: String,
    onClose: () -> Unit,
    onItemClick: (String) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
        modifier = Modifier.width(330.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Close Button
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = PurpleGradia,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Profile Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(SocialIconBg),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Sophia Rose",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = InterFontFamily,
                        fontSize = 20.sp
                    )
                )
                Text(
                    text = "UX/UI Designer",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.Gray,
                        fontFamily = InterFontFamily,
                        fontSize = 15.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(32.dp))

            // Menu Items
            DrawerMenuItem(
                label = "Perfil",
                outlineIcon = R.drawable.user_outline,
                boldIcon = R.drawable.user_bold,
                isSelected = selectedItem == "Perfil",
                onClick = { onItemClick("Perfil") }
            )
            DrawerMenuItem(
                label = "Estadísticas",
                outlineIcon = R.drawable.stats_chart_outline,
                boldIcon = R.drawable.stats_chart,
                isSelected = selectedItem == "Estadísticas",
                onClick = { onItemClick("Estadísticas") }
            )
            DrawerMenuItem(
                label = "Ajustes",
                outlineIcon = R.drawable.settings_linear,
                boldIcon = R.drawable.settings_rounded,
                isSelected = selectedItem == "Ajustes",
                onClick = { onItemClick("Ajustes") }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Items
            DrawerMenuItem(
                label = "Tema Oscuro",
                outlineIcon = R.drawable.sun_light,
                boldIcon = R.drawable.sun_light, // Use same if bold version doesn't exist
                isSelected = selectedItem == "Tema Oscuro",
                onClick = { onItemClick("Tema Oscuro") }
            )
            DrawerMenuItem(
                label = "Log Out",
                outlineIcon = R.drawable.log_out,
                boldIcon = R.drawable.logout_bold,
                isSelected = selectedItem == "Log Out",
                onClick = { onItemClick("Log Out") }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DrawerMenuItem(
    label: String,
    outlineIcon: Int,
    boldIcon: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFEDEDED) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = if (isSelected) boldIcon else outlineIcon),
            contentDescription = null,
            tint = PurpleGradia,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = PurpleGradia,
                fontFamily = InterFontFamily,
                fontSize = 18.sp
            )
        )
    }
}

@Composable
fun HomeContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                "Mi Semestre",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A),
                    fontFamily = InterFontFamily
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Promedio General",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A4A4A)
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressChart(progress = 0.85f, score = "4.5", status = "Excellent")
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "¡Excelente rendimiento este ciclo!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Materias",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A4A4A),
                        fontFamily = InterFontFamily
                    )
                )
                Text(
                    "Ver todas",
                    color = PurpleGradia,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        items(courseList) { course ->
            CourseItem(course)
        }
        
        item { Spacer(modifier = Modifier.height(80.dp)) } // Espacio para la bottom bar
    }
}

@Composable
fun CircularProgressChart(progress: Float, score: String, status: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        Canvas(modifier = Modifier.size(160.dp)) {
            drawArc(
                color = Color(0xFFE9E4F0),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = PurpleGradia,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4A4A4A)
            )
            Text(
                text = status,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = PurpleGradia
            )
        }
    }
}

data class Course(
    val name: String,
    val progress: Float,
    val percentage: Int,
    val info: String,
    val icon: ImageVector,
    val infoIcon: ImageVector
)

val courseList = listOf(
    Course("Calculo IV", 0.8f, 80, "Próximo Examen: Lunes", Icons.Default.Info, Icons.Default.Info),
    Course("Diseño UI", 0.6f, 60, "Entrega: Miércoles", Icons.Default.Edit, Icons.Default.Info),
    Course("Física II", 0.9f, 90, "12 Tareas Completas", Icons.Default.Star, Icons.Default.CheckCircle)
)

@Composable
fun CourseItem(course: Course) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(SocialIconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = course.icon,
                    contentDescription = null,
                    tint = PurpleGradia,
                    modifier = Modifier.size(34.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = Color(0xFF4A4A4A)
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { course.progress },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PurpleGradia,
                        trackColor = Color(0xFFE9E4F0)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "${course.percentage}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A4A4A)
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = course.infoIcon,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = course.info,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun GradiaBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(20.dp),
        color = PurpleGradia,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarItem(
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                whiteIconId = R.drawable.home_white,
                purpleIconId = R.drawable.home_purple,
                contentDescription = "Home"
            )
            BottomBarItem(
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                whiteIconId = R.drawable.calculator_white,
                purpleIconId = R.drawable.calculator_purple,
                contentDescription = "Calculator"
            )
            BottomBarItem(
                isSelected = selectedTab == 2,
                onClick = { onTabSelected(2) },
                whiteIconId = R.drawable.plus_white,
                purpleIconId = R.drawable.plus_purple,
                contentDescription = "Plus"
            )
            BottomBarItem(
                isSelected = selectedTab == 3,
                onClick = { onTabSelected(3) },
                whiteIconId = R.drawable.books_add_white,
                purpleIconId = R.drawable.books_add_purple,
                contentDescription = "Books"
            )
            BottomBarItem(
                isSelected = selectedTab == 4,
                onClick = { onTabSelected(4) },
                whiteIconId = R.drawable.calendar_white,
                purpleIconId = R.drawable.calendar_purple,
                contentDescription = "Calendar"
            )
        }
    }
}

@Composable
fun BottomBarItem(
    isSelected: Boolean,
    onClick: () -> Unit,
    whiteIconId: Int,
    purpleIconId: Int,
    contentDescription: String
) {
    IconButton(
        onClick = onClick,
        modifier = if (isSelected) {
            Modifier
                .background(Color.White, RoundedCornerShape(12.dp))
                .size(35.dp)
        } else {
            Modifier.size(40.dp)
        }
    ) {
        Icon(
            painter = painterResource(id = if (isSelected) purpleIconId else whiteIconId),
            contentDescription = contentDescription,
            tint = if (isSelected) PurpleGradia else Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    GradiaTheme {
        HomeScreen()
    }
}
