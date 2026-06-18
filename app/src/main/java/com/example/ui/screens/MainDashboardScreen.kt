package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PasswordEntity
import com.example.ui.theme.IranSans
import com.example.utils.PasswordStrength
import com.example.utils.PasswordStrengthEstimator
import com.example.viewmodel.AmnViewModel
import com.example.viewmodel.SortMode
import com.example.utils.Localization
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: AmnViewModel,
    onNavigateToAddEdit: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val passwords by viewModel.displayedPasswords.collectAsState()
    val allPasswords by viewModel.activePasswordsFlow.collectAsState()
    
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()
    val filterFavoritesOnly by viewModel.filterFavoritesOnly.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var passwordToDelete by remember { mutableStateOf<PasswordEntity?>(null) }
    
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val lang = viewModel.currentLanguage
    val scale = viewModel.currentFontScale

    // Real-time local filtering for customized category pills
    val categorizedPasswords = remember(passwords, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            passwords
        } else {
            passwords.filter { (it.category ?: "PUBLIC").uppercase() == selectedCategoryFilter }
        }
    }

    // Fast computations for Security check
    val duplicateHashes = remember(allPasswords) {
        allPasswords.groupBy { it.passwordHash }
            .filter { it.value.size > 1 }
            .keys
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddEdit(0) }, // 0 = New entry
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_password_fab")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add, 
                    contentDescription = Localization.getString("add_edit_new", lang)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search input field with RTL/LTR alignment depending on language
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { 
                    Text(
                        text = Localization.getString("search_hint", lang), 
                        fontFamily = IranSans, 
                        fontSize = (13 * scale).sp
                    ) 
                },
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_input"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Bento Grid Security Dashboard Statistics
            val totalCount = allPasswords.size
            val weakCount = allPasswords.count { PasswordStrengthEstimator.estimate(it.passwordHash) == PasswordStrength.WEAK }
            val favoritesCount = allPasswords.count { it.isFavorite }
            val isDarkTheme = viewModel.isDarkTheme

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left bento tile (Total passwords)
                Box(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(112.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.82f)
                                )
                            )
                        )
                        .clickable { viewModel.filterFavoritesOnly.value = false }
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "$totalCount",
                                fontFamily = IranSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = (22 * scale).sp,
                                color = Color.White
                            )
                            Text(
                                text = Localization.getString("stat_total", lang),
                                fontFamily = IranSans,
                                fontWeight = FontWeight.Medium,
                                fontSize = (10 * scale).sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Right column containing two smaller bento tiles
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(112.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tile 1: Weak Passwords
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isDarkTheme) Color(0xFF334155) else Color(0xFFE2E8F0))
                            .clickable { viewModel.filterFavoritesOnly.value = false }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (weakCount > 0) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$weakCount",
                                fontFamily = IranSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = (12 * scale).sp,
                                color = if (weakCount > 0) Color(0xFFEF4444) else Color(0xFF16A34A)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Localization.getString("stat_weak", lang),
                            fontFamily = IranSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = (11 * scale).sp,
                            color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                        )
                    }

                    // Tile 2: Favorites
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { viewModel.filterFavoritesOnly.value = !filterFavoritesOnly }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFCCFBF1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${Localization.getString("stat_favorites", lang)} ($favoritesCount)",
                            fontFamily = IranSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = (11 * scale).sp,
                            color = if (isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // In-App Category filter horizontal scroll row
            val categoriesFilterMenu = listOf(
                "ALL" to if (lang == "en") "All" else "همه",
                "SOCIAL" to Localization.getString("cat_social", lang),
                "BANKING" to Localization.getString("cat_banking", lang),
                "PERSONAL" to Localization.getString("cat_personal", lang),
                "PUBLIC" to Localization.getString("cat_public", lang),
                "OTHER" to Localization.getString("cat_other", lang)
            )

            Row(
                modifier = Modifier
                    .fillHorizontalScrollable()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                categoriesFilterMenu.forEach { (catCode, catLabel) ->
                    val filterSelected = selectedCategoryFilter == catCode
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (filterSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            }
                        ),
                        modifier = Modifier
                            .clickable { selectedCategoryFilter = catCode }
                            .padding(2.dp)
                    ) {
                        Text(
                            text = catLabel,
                            fontFamily = IranSans,
                            fontWeight = if (filterSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (filterSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = (11 * scale).sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sort and Filters Toggle Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter Favorite Trigger Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.filterFavoritesOnly.value = !filterFavoritesOnly }
                        .background(
                            if (filterFavoritesOnly) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (filterFavoritesOnly) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.getString("stat_favorites", lang),
                        fontFamily = IranSans,
                        fontSize = (11 * scale).sp,
                        color = if (filterFavoritesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Sort menu activator
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showSortMenu = true }
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sort,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (sortMode == SortMode.NEWEST_TO_OLDEST) {
                                if (lang == "en") "Newest" else "جدیدترین‌ها"
                            } else {
                                if (lang == "en") "Oldest" else "قدیمی‌ترین‌ها"
                            },
                            fontFamily = IranSans,
                            fontSize = (11 * scale).sp
                        )
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (lang == "en") "Newest to Oldest" else "جدیدترین به قدیمی‌ترین", fontFamily = IranSans, fontSize = (12 * scale).sp) },
                            onClick = {
                                viewModel.sortMode.value = SortMode.NEWEST_TO_OLDEST
                                showSortMenu = false
                            },
                            leadingIcon = { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                        DropdownMenuItem(
                            text = { Text(if (lang == "en") "Oldest to Newest" else "قدیمی‌ترین به جدیدترین", fontFamily = IranSans, fontSize = (12 * scale).sp) },
                            onClick = {
                                viewModel.sortMode.value = SortMode.OLDEST_TO_NEWEST
                                showSortMenu = false
                            },
                            leadingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main passwords list view
            if (categorizedPasswords.isEmpty()) {
                Box(
                    modifier = Modifier
                         .fillMaxWidth()
                         .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderOpen,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(90.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                if (lang == "en") "No credentials found" else "هیچ رمزی یافت نشد"
                            } else {
                                if (lang == "en") "Vault category is empty" else "دفترچه رمزها خالی است"
                            },
                            fontFamily = IranSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = (16 * scale).sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                if (lang == "en") "Try refining search keys or category" else "نام سایت یا اطلاعات ورودی را تغییر دهید"
                            } else {
                                if (lang == "en") "Tap '+' to add your first secure local credentials entry" else "روی دکمه + کلیک کنید تا اولین رمز عبور امن خود را اضافه کنید"
                            },
                            fontFamily = IranSans,
                            fontSize = (12 * scale).sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            lineHeight = (18 * scale).sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categorizedPasswords, key = { it.id }) { passwordItem ->
                        val isDuplicate = passwordItem.passwordHash in duplicateHashes
                        PasswordCard(
                            password = passwordItem,
                            isDuplicate = isDuplicate,
                            lang = lang,
                            scale = scale,
                            context = context,
                            onToggleFavorite = { viewModel.toggleFavorite(passwordItem) },
                            onEdit = { onNavigateToAddEdit(passwordItem.id) },
                            onDelete = { passwordToDelete = passwordItem },
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    // Confirmation delete Dialog
    if (passwordToDelete != null) {
        val currentItem = passwordToDelete!!
        AlertDialog(
            onDismissRequest = { passwordToDelete = null },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.softDeletePassword(currentItem)
                        passwordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = if (lang == "en") "Purge to Trash" else "انتقال به سطل زباله", 
                        fontFamily = IranSans, 
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { passwordToDelete = null }) {
                    Text(Localization.getString("btn_cancel", lang), fontFamily = IranSans)
                }
            },
            title = {
                Text(
                    text = if (lang == "en") "Send to Soft Trash" else "حذف رمز عبور", 
                    fontFamily = IranSans, 
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (lang == "en") "Are you sure you want to move account '${currentItem.username}' for site '${currentItem.siteName}' to the soft trash recycle bin?" else "آیا مطمئن هستید که می‌خواهید رمز حساب کاربری «${currentItem.username}» در سایت «${currentItem.siteName}» را حذف کرده و به سطل زباله بفرستید؟",
                    fontFamily = IranSans,
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                )
            }
        )
    }
}

@Composable
fun PasswordCard(
    password: PasswordEntity,
    isDuplicate: Boolean,
    lang: String,
    scale: Float,
    context: android.content.Context,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    viewModel: AmnViewModel,
    modifier: Modifier = Modifier
) {
    var showRawPassword by remember { mutableStateOf(false) }
    val strength = remember(password.passwordHash) { PasswordStrengthEstimator.estimate(password.passwordHash) }

    // Date formatting helper
    val formattedCreateDate = remember(password.createdAt) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = password.createdAt
        DateFormat.format("yyyy/MM/dd HH:mm", calendar).toString()
    }

    val formattedEditDate = remember(password.lastEditedAt) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = password.lastEditedAt
        DateFormat.format("yyyy/MM/dd HH:mm", calendar).toString()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("password_card_${password.siteName}"),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // First Row: Website/Service Title + Favorite Star + Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    val firstChar = password.siteName.firstOrNull()?.toString() ?: "S"
                    Text(
                        text = firstChar.uppercase(),
                        fontFamily = IranSans,
                        fontSize = (18 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = password.siteName,
                        fontFamily = IranSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = (16 * scale).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = password.username,
                        fontFamily = IranSans,
                        fontSize = (12 * scale).sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Favorite toggler
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Starred Item",
                        tint = if (password.isFavorite) Color(0xFFFFB74D) else Color.Gray.copy(alpha = 0.3f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Options control buttons
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Item", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Filled.DeleteOutline, contentDescription = "Trash soft purge", tint = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Middle Block: Password text block itself
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                     imageVector = Icons.Filled.VpnKey,
                     contentDescription = null,
                     tint = Color.Gray,
                     modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = if (showRawPassword) password.passwordHash else "••••••••••••",
                    fontFamily = IranSans,
                    fontSize = (14 * scale).sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                IconButton(
                    onClick = { showRawPassword = !showRawPassword },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (showRawPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Toggle Visibility",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        // Secure copying flow using the 20S auto-clean coroutine!
                        viewModel.copyToClipboardAndClearLater(context, password.passwordHash, isPasswordContent = true)
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Secure Copy",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick Copy Username anchor
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        viewModel.copyToClipboardAndClearLater(context, password.username, isPasswordContent = false)
                    }
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.ContentCopy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (lang == "en") "Secure copy username" else "کپی کردن نام کاربری",
                    fontFamily = IranSans,
                    fontSize = (11 * scale).sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category tag and datetime
            val categoryDisplayString = when ((password.category ?: "PUBLIC").uppercase()) {
                "SOCIAL" -> Localization.getString("cat_social", lang)
                "BANKING" -> Localization.getString("cat_banking", lang)
                "PERSONAL" -> Localization.getString("cat_personal", lang)
                "PUBLIC" -> Localization.getString("cat_public", lang)
                else -> Localization.getString("cat_other", lang)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = categoryDisplayString,
                        fontFamily = IranSans,
                        fontSize = (10 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = if (password.createdAt != password.lastEditedAt) {
                        "${if (lang == "en") "Edit" else "ویرایش"}: $formattedEditDate"
                    } else {
                        "${if (lang == "en") "Create" else "ایجاد"}: $formattedCreateDate"
                    },
                    fontFamily = IranSans,
                    fontSize = (10 * scale).sp,
                    color = Color.Gray
                )
            }

            // Third Block: Visual Strength value
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(strength.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                
                val visualLabel = when (strength) {
                    PasswordStrength.EMPTY -> if (lang == "en") "Empty" else "بدون رمز"
                    PasswordStrength.WEAK -> Localization.getString("strength_weak", lang)
                    PasswordStrength.MEDIUM -> Localization.getString("strength_medium", lang)
                    PasswordStrength.STRONG -> Localization.getString("strength_strong", lang)
                    PasswordStrength.VERY_STRONG -> if (lang == "en") "Excellent Strong" else "بسیار قوی"
                }
                
                Text(
                    text = "${if (lang == "en") "Safety Factor: " else "ضریب ایمنی: "} $visualLabel",
                    fontFamily = IranSans,
                    fontSize = (11 * scale).sp,
                    color = strength.color,
                    fontWeight = FontWeight.Bold
                )
            }

            // Optional description detail preview
            if (password.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${if (lang == "en") "Notes: " else "یادداشت: "} ${password.description}",
                    fontFamily = IranSans,
                    fontSize = (11 * scale).sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Unsafe duplicate password Warning Indicator
            if (isDuplicate) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Warning info",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lang == "en") "Warning: Vulnerable reused password!" else "هشدار: پاسپورت موازی! این رمز عبور تکراری است.",
                        fontFamily = IranSans,
                        fontSize = (10 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

// Inline helper for scrolling list horizontal row
@Composable
fun Modifier.fillHorizontalScrollable(): Modifier {
    val scroll = rememberScrollState()
    return this.horizontalScroll(scroll)
}
