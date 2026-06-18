package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.IranSans
import com.example.viewmodel.AmnViewModel
import com.example.utils.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    viewModel: AmnViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val lang = viewModel.currentLanguage
    val scale = viewModel.currentFontScale

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = Localization.getString("about_title", lang), 
                        fontFamily = IranSans, 
                        fontWeight = FontWeight.Bold,
                        fontSize = (18 * scale).sp
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_about_shield),
                    contentDescription = "Shield Guard",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // App title block
            Text(
                text = "امن‌واژه (AmnVazeh)",
                fontFamily = IranSans,
                fontWeight = FontWeight.Bold,
                fontSize = (24 * scale).sp,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = Localization.getString("about_desc", lang),
                fontFamily = IranSans,
                fontSize = (12 * scale).sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Dynamic privacy assurance banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = Localization.getString("about_security_desc", lang),
                        fontFamily = IranSans,
                        fontSize = (11 * scale).sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                        lineHeight = (18 * scale).sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Metadata card listing educational stats
            Text(
                text = Localization.getString("about_tech", lang),
                fontFamily = IranSans,
                fontWeight = FontWeight.Bold,
                fontSize = (14 * scale).sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AboutDetailItem(
                        label = if (lang == "en") "Developer & Architect Name:" else "نام توسعه‌دهنده و سازنده:",
                        value = if (lang == "en") "Makan Shadkhast" else "ماکان شادخواست",
                        icon = Icons.Filled.Person,
                        scale = scale
                    )
                    AboutDetailItem(
                        label = if (lang == "en") "Academic Mobile Dev Course:" else "درس برنامه‌سازی موبایل:",
                        value = if (lang == "en") "Advanced Mobile Programming" else "برنامه‌سازی پیشرفته موبایل",
                        icon = Icons.Filled.Assignment,
                        scale = scale
                    )
                    AboutDetailItem(
                        label = if (lang == "en") "Guiding Professor & Mentor:" else "استاد راهنما:",
                        value = if (lang == "en") "Dr. Askarzadeh" else "دکتر عسکرزاده",
                        icon = Icons.Filled.AccountBalance,
                        scale = scale
                    )
                    AboutDetailItem(
                        label = if (lang == "en") "Academic Project Term / Year:" else "سال تحصیلی ساخت:",
                        value = if (lang == "en") "1405 (2026 AD)" else "۱۴۰۵ (۲۰۲۶)",
                        icon = Icons.Filled.DateRange,
                        scale = scale
                    )
                    AboutDetailItem(
                        label = if (lang == "en") "In-Vault Architecture Tech:" else "تکنولوژی‌های توسعه:",
                        value = "Kotlin + Jetpack Compose + MVVM + Room Local SQLite + AES-GCM + PBKDF2 (No Internet)",
                        icon = Icons.Filled.Build,
                        scale = scale
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Copyright sign
            Text(
                text = if (lang == "en") "All rights reserved for AmnVazeh offline vaults. © 2026" else "تمامی حقوق مادی و معنوی برای امن‌واژه محفوظ است. © ۱۴۰۵",
                fontFamily = IranSans,
                fontSize = (10 * scale).sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun AboutDetailItem(
    label: String,
    value: String,
    icon: ImageVector,
    scale: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontFamily = IranSans,
                fontSize = (11 * scale).sp,
                color = Color.Gray
            )
            Text(
                text = value,
                fontFamily = IranSans,
                fontWeight = FontWeight.Bold,
                fontSize = (13 * scale).sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
