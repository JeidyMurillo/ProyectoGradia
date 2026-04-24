package com.example.gradia.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gradia.R
import com.example.gradia.ui.theme.*

@Composable
fun NotesScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            CategoryRow()
        }

        item {
            NoteEditorCard()
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NoteGridItem(
                    title = "Lorem Ipsum",
                    content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                    backgroundColor = Color(0xFFFFE0E0),
                    modifier = Modifier.weight(1f)
                )
                NoteGridItem(
                    title = "Lorem Ipsum",
                    content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                    backgroundColor = Color(0xFFD0EFFF),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            NoteGridItem(
                title = "Lorem Ipsum",
                content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                backgroundColor = Color(0xFFD1AFF5),
                modifier = Modifier.fillMaxWidth()
            )
        }

        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun CategoryRow() {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        item {
            CategoryChip("Matemática", Color(0xFFD0EFFF), isSelected = true)
        }
        item {
            CategoryChip("Física", Color(0xFFFFE0E0))
        }
        item {
            CategoryChip("Literatura", Color.White)
        }
        item {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category", modifier = Modifier.size(16.dp), tint = Color.Gray)
            }
        }
    }
}

@Composable
fun CategoryChip(label: String, backgroundColor: Color, isSelected: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) backgroundColor else Color.White,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)) else null,
        modifier = Modifier.height(32.dp).clickable { }
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
fun NoteEditorCard() {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

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
                onValueChange = { title = it },
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

            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { 
                    Text(
                        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus condimentum magna eu augue malesuada, vitae vestibulum odio varius. Suspendisse nec mauris ut arcu venenatis laoreet eget id ex.",
                        fontSize = 16.sp,
                        color = Color.Gray
                    ) 
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp), // Reduced spacing
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "B", 
                        fontWeight = FontWeight.ExtraBold, 
                        color = PurpleGradia, 
                        fontSize = 18.sp, // Smaller size
                        modifier = Modifier.clickable { }
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.italic), 
                        contentDescription = "Italic", 
                        tint = PurpleGradia,
                        modifier = Modifier.size(18.dp).clickable { } // Uniform smaller size
                    )
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete", 
                        tint = PurpleGradia,
                        modifier = Modifier.size(18.dp).clickable { } // Uniform smaller size
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.list), 
                        contentDescription = "Bullets", 
                        tint = PurpleGradia,
                        modifier = Modifier.size(18.dp).clickable { } // Uniform smaller size
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.image),
                        contentDescription = "Image", 
                        tint = PurpleGradia,
                        modifier = Modifier.size(18.dp).clickable { } // Uniform smaller size
                    )
                }

                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleGradia),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text("Guardar nota", fontWeight = FontWeight.Bold, fontSize = 13.sp) // Slightly smaller text
                }
            }
        }
    }
}

@Composable
fun NoteGridItem(title: String, content: String, backgroundColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(4.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = Color.Black.copy(alpha = 0.1f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesScreenPreview() {
    GradiaTheme {
        NotesScreen()
    }
}
