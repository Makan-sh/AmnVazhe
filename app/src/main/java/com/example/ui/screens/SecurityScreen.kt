package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PasswordEntity
import com.example.ui.theme.IranSans
import com.example.utils.PasswordStrength
import com.example.utils.PasswordStrengthEstimator
import com.example.viewmodel.AmnViewModel
import com.example.utils.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    viewModel: AmnViewModel,
    modifier: Modifier = Modifier
) {
    val passwords by viewModel.activePasswordsFlow.collectAsState()
    val scrollState = rememberScrollState()

    val lang = viewModel.currentLanguage
    val scale = viewModel.currentFontScale

    // Dynamically calculate metrics
    val totalCount = passwords.size
    val weakCount = passwords.count { PasswordStrengthEstimator.estimate(it.passwordHash) == PasswordStrength.WEAK }
    val favoritesCount = passwords.count { it.isFavorite }

    // Map and trace duplicates
    val duplicateGroups = remember(passwords) {
        passwords.groupBy { it.passwordHash }.filter { it.value.size > 1 }
    }
    val duplicateKeys = duplicateGroups.keys
    val duplicateCount = passwords.count { it.passwordHash in duplicateKeys }

    // Aggregate credentials that reuse passwords
    val duplicatedList = passwords.filter { it.passwordHash in duplicateKeys }
    val weakList = passwords.filter { PasswordStrengthEstimator.estimate(it.passwordHash) == PasswordStrength.WEAK }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = Localization.getString("security_title", lang), 
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
                .padding(16.dp)
        ) {
            Text(
                text = if (lang == "en") "Security Health Checker" else "داشبورد ارزیابی امنیت رمزها",
                fontFamily = IranSans,
                fontWeight = FontWeight.Bold,
                fontSize = (16 * scale).sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Grid of Security metrics
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricWidget(
                        title = Localization.getString("stat_total", lang),
                        value = totalCount.toString(),
                        icon = Icons.Filled.Lock,
                        scale = scale,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    MetricWidget(
                        title = Localization.getString("stat_favorites", lang),
                        value = favoritesCount.toString(),
                        icon = Icons.Filled.Star,
                        scale = scale,
                        color = Color(0xFFFFB74D) // Amber gold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.weight(1f)) {
                    MetricWidget(
                        title = Localization.getString("stat_weak", lang),
                        value = weakCount.toString(),
                        icon = Icons.Filled.LockOpen,
                        scale = scale,
                        color = if (weakCount > 0) Color(0xFFE53935) else Color(0xFF43A047) // Red / Green
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    MetricWidget(
                        title = if (lang == "en") "Duplicated" else "رمزهای تکراری",
                        value = duplicateCount.toString(),
                        icon = Icons.Filled.Warning,
                        scale = scale,
                        color = if (duplicateCount > 0) Color(0xFFF57C00) else Color(0xFF43A047) // Orange / Green
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // REUSE DUPLICATE PASSWORDS SECTION
            if (duplicatedList.isNotEmpty()) {
                Text(
                    text = if (lang == "en") "🚨 Alert: Reused Passwords (${duplicatedList.size})" else "🚨 هشدار رمزهای تکراری (${duplicatedList.size})",
                    fontFamily = IranSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = (14 * scale).sp,
                    color = Color(0xFFF57C00),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (lang == "en") "The following accounts map to the same passwords. Create unique passwords to bypass rolling leaks:" else "رمزهای زیر در بیش از یک سایت یا سرویس استفاده شده‌اند. برای جلوگیری از هک زنجیره‌ای، رمزهای متمایز انتخاب کنید:",
                    fontFamily = IranSans,
                    fontSize = (11 * scale).sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp),
                    lineHeight = (18 * scale).sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    duplicatedList.forEach { duplicateItem ->
                        AlertSecurityCard(
                            item = duplicateItem,
                            alertType = if (lang == "en") "Reused" else "تکراری",
                            scale = scale,
                            alertColor = Color(0xFFF57C00)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // WEAK PASSWORDS SECTION
            if (weakList.isNotEmpty()) {
                Text(
                    text = if (lang == "en") "⚠️ Resolve Weak Credentials (${weakList.size})" else "⚠️ ضرورت اصلاح رمزهای ضعیف (${weakList.size})",
                    fontFamily = IranSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = (14 * scale).sp,
                    color = Color(0xFFE53935),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = if (lang == "en") "The following details represent weak passwords. Please use our vault secure random generator to enforce encryption safety:" else "رمزهای زیر امنیت کافی ندارند (بسیار کوتاه هستند یا از ترکیب ضعیفی استفاده می‌کنند). لطفا از کلید تولید رمز تصادفی امن این برنامه بهره بگیرید:",
                    fontFamily = IranSans,
                    fontSize = (11 * scale).sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp),
                    lineHeight = (18 * scale).sp
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    weakList.forEach { weakItem ->
                        AlertSecurityCard(
                            item = weakItem,
                            alertType = Localization.getString("strength_weak", lang),
                            scale = scale,
                            alertColor = Color(0xFFE53935)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // EXCELLENT ALL SECURE OUTCOME
            if (weakCount == 0 && duplicateCount == 0 && totalCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success Verified",
                            tint = Color(0xFF43A047),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Localization.getString("security_ok", lang),
                            fontFamily = IranSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = (15 * scale).sp,
                            color = Color(0xFF1B5E20)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Localization.getString("security_ok_desc", lang),
                            fontFamily = IranSans,
                            fontSize = (11 * scale).sp,
                            color = Color(0xFF33691E),
                            textAlign = TextAlign.Center,
                            lineHeight = (18 * scale).sp
                        )
                    }
                }
            } else if (totalCount == 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Localization.getString("no_passwords", lang),
                        fontFamily = IranSans,
                        fontSize = (13 * scale).sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun MetricWidget(
    title: String,
    value: String,
    icon: ImageVector,
    scale: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                fontFamily = IranSans,
                fontWeight = FontWeight.Bold,
                fontSize = (24 * scale).sp,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontFamily = IranSans,
                fontSize = (11 * scale).sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun AlertSecurityCard(
    item: PasswordEntity,
    alertType: String,
    scale: Float,
    alertColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(alertColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val head = item.siteName.firstOrNull()?.toString() ?: "S"
                Text(
                    text = head.uppercase(),
                    fontFamily = IranSans,
                    fontSize = (14 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = alertColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.siteName,
                    fontFamily = IranSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = (13 * scale).sp
                )
                Text(
                    text = "${Localization.getString("lbl_username", scale.toString())}: ${item.username}",
                    fontFamily = IranSans,
                    fontSize = (11 * scale).sp,
                    color = Color.Gray
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(alertColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = alertType,
                    fontFamily = IranSans,
                    fontSize = (10 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = alertColor
                )
            }
        }
    }
}
