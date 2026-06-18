package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IranSans
import com.example.utils.PasswordStrength
import com.example.utils.PasswordStrengthEstimator
import com.example.viewmodel.AmnViewModel
import com.example.utils.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: AmnViewModel,
    id: Int, // 0 = Add New, >0 = Edit Existing
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var siteName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("PUBLIC") }

    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val strength = remember(password) { PasswordStrengthEstimator.estimate(password) }
    val scrollState = rememberScrollState()

    val lang = viewModel.currentLanguage
    val scale = viewModel.currentFontScale

    // Pre-populate data if we are editing an existing item
    LaunchedEffect(id) {
        if (id > 0) {
            viewModel.updateActivity()
            val entry = viewModel.activePasswordsFlow.value.find { it.id == id }
                ?: viewModel.deletedPasswordsFlow.value.find { it.id == id }
            if (entry != null) {
                siteName = entry.siteName
                username = entry.username
                password = entry.passwordHash
                description = entry.description
                isFavorite = entry.isFavorite
                selectedCategory = entry.category ?: "PUBLIC"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (id == 0) {
                            Localization.getString("add_edit_new", lang)
                        } else {
                            Localization.getString("add_edit_edit", lang)
                        },
                        fontFamily = IranSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = (18 * scale).sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = Localization.getString("btn_cancel", lang)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Service Name field
            OutlinedTextField(
                value = siteName,
                onValueChange = { siteName = it },
                label = { Text(Localization.getString("lbl_site", lang), fontFamily = IranSans, fontSize = (14 * scale).sp) },
                singleLine = true,
                placeholder = { 
                    Text(
                        text = if (lang == "en") "e.g., Google, Instagram, Bank" else "مثال: گوگل، اینستاگرام، کارت بانکی", 
                        fontFamily = IranSans, 
                        fontSize = (12 * scale).sp
                    ) 
                },
                leadingIcon = { Icon(Icons.Filled.Web, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_site_name"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(Localization.getString("lbl_username", lang), fontFamily = IranSans, fontSize = (14 * scale).sp) },
                singleLine = true,
                placeholder = { 
                    Text(
                        text = "username@domain.com", 
                        fontFamily = IranSans, 
                        fontSize = (12 * scale).sp
                    ) 
                },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_username"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field with Strength and Random Generator button
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(Localization.getString("lbl_password", lang), fontFamily = IranSans, fontSize = (14 * scale).sp) },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        Row(modifier = Modifier.padding(end = 4.dp)) {
                            // Random Password Generator Button
                            IconButton(
                                onClick = {
                                    password = viewModel.generateRandomPassword()
                                    val feedback = if (lang == "en") "An excellent 12-char secure password has been generated" else "یک رمز عبور بسیار قوی و ۱۲ رقمی تولید شد"
                                    viewModel.showSnackbar(feedback)
                                },
                                modifier = Modifier.testTag("generate_password_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = Localization.getString("btn_generate", lang),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Show-Hide visibility button
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Show/Hide Password"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("form_password"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Real-time strength visual meter inside form fields
                if (password.isNotEmpty()) {
                    val labelStrength = when (strength) {
                        PasswordStrength.EMPTY -> if (lang == "en") "Empty" else "بدون رمز"
                        PasswordStrength.WEAK -> Localization.getString("strength_weak", lang)
                        PasswordStrength.MEDIUM -> Localization.getString("strength_medium", lang)
                        PasswordStrength.STRONG -> Localization.getString("strength_strong", lang)
                        PasswordStrength.VERY_STRONG -> if (lang == "en") "Excellent Strong" else "بسیار قوی"
                    }
                    val percentStrength = when (strength) {
                        PasswordStrength.EMPTY -> 0.0f
                        PasswordStrength.WEAK -> 0.33f
                        PasswordStrength.MEDIUM -> 0.66f
                        PasswordStrength.STRONG, PasswordStrength.VERY_STRONG -> 1.0f
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (lang == "en") "Estimated Strength: " else "وضعیت قدرت رمز عبور: ",
                            fontFamily = IranSans,
                            fontSize = (11 * scale).sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = labelStrength,
                            fontFamily = IranSans,
                            fontSize = (11 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = strength.color
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { percentStrength },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = strength.color,
                        trackColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Secured Category Selection Group
            Text(
                text = Localization.getString("lbl_category", lang),
                fontFamily = IranSans,
                fontWeight = FontWeight.Bold,
                fontSize = (13 * scale).sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val categoriesList = listOf(
                Triple("SOCIAL", Localization.getString("cat_social", lang), Icons.Filled.Group),
                Triple("BANKING", Localization.getString("cat_banking", lang), Icons.Filled.CreditCard),
                Triple("PERSONAL", Localization.getString("cat_personal", lang), Icons.Filled.Fingerprint),
                Triple("PUBLIC", Localization.getString("cat_public", lang), Icons.Filled.AccountBalance),
                Triple("OTHER", Localization.getString("cat_other", lang), Icons.Filled.FolderOpen)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categoriesList.forEach { (catCode, catLabel, catIcon) ->
                    val isCatSelected = selectedCategory == catCode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCatSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { selectedCategory = catCode }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isCatSelected,
                            onClick = { selectedCategory = catCode }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = if (isCatSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = catLabel,
                            fontFamily = IranSans,
                            fontSize = (13 * scale).sp,
                            fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCatSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(Localization.getString("lbl_description", lang), fontFamily = IranSans, fontSize = (14 * scale).sp) },
                placeholder = { 
                    Text(
                        text = if (lang == "en") "Secret question, recovery email, card CVV2..." else "آدرس ردیابی، سوال امنیتی، شماره کارت، CVV2...", 
                        fontFamily = IranSans, 
                        fontSize = (12 * scale).sp
                    ) 
                },
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("form_description"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Star item as Favorite switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { isFavorite = !isFavorite }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (isFavorite) Color(0xFFFFB74D) else Color.Gray.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == "en") "Star as Vault Favorite" else "نشان کردن به عنوان علاقه‌مندی",
                        fontFamily = IranSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = (13 * scale).sp
                    )
                    Text(
                        text = if (lang == "en") "Pinned passwords stay clearly accessible on dashboard stats" else "این رمز عبور ستاره‌دار شده و در تب علاقه‌مندی‌ها آسان‌تر یافته می‌شود",
                        fontFamily = IranSans,
                        fontSize = (11 * scale).sp,
                        color = Color.Gray
                    )
                }
                Checkbox(
                    checked = isFavorite,
                    onCheckedChange = { isFavorite = it }
                )
            }

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    fontFamily = IranSans,
                    fontSize = (12 * scale).sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save and Submit Button
            Button(
                onClick = {
                    if (siteName.isBlank()) {
                        errorMessage = if (lang == "en") "Service / Site name is required" else "لطفاً نام سایت یا سرویس را وارد کنید"
                    } else if (username.isBlank()) {
                        errorMessage = if (lang == "en") "Username field cannot be left blank" else "لطفاً نام کاربری را وارد کنید"
                    } else if (password.isBlank()) {
                        errorMessage = if (lang == "en") "Password field cannot be empty" else "لطفاً رمز عبور را وارد کنید"
                    } else {
                        viewModel.saveOrUpdatePassword(
                            id = id,
                            siteName = siteName,
                            username = username,
                            passwordRaw = password,
                            description = description,
                            isFavorite = isFavorite,
                            category = selectedCategory
                        )
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_form_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = Localization.getString("btn_save", lang),
                    fontFamily = IranSans,
                    fontSize = (15 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
