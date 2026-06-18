package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IranSans
import com.example.viewmodel.AmnViewModel
import com.example.utils.Localization

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AmnViewModel,
    modifier: Modifier = Modifier
) {
    val config = viewModel.userConfigFlow.collectAsState().value ?: return
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    
    val lang = viewModel.currentLanguage
    val scale = viewModel.currentFontScale

    // Dialog & Form expanded states
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    // Forms temp states
    var nameInput by remember { mutableStateOf(config.username) }
    var passwordError by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf(config.pinCode ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = Localization.getString("settings_title", lang), 
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Section 1: Security Controls
            Text(
                text = Localization.getString("set_account_security", lang),
                fontFamily = IranSans,
                fontWeight = FontWeight.Bold,
                fontSize = (14 * scale).sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // 1. Username Selector
            SettingsItemCard(
                title = if (lang == "en") "Change Native Username" else "تغییر نام کاربری",
                desc = "${Localization.getString("lbl_username", lang)}: ${config.username}",
                icon = Icons.Filled.Person,
                scale = scale,
                onClick = { 
                    nameInput = config.username
                    showUsernameDialog = true 
                }
            )

            // 2. Changing Password Row
            SettingsItemCard(
                title = if (lang == "en") "Change Master Password" else "تغییر رمز عبور اصلی (مستر)",
                desc = if (lang == "en") "Update vault log-in code & master PBKDF2 seed" else "تغییر گذرواژه ورودی برنامه و کلید پشتیبان‌گیری",
                icon = Icons.Filled.Lock,
                scale = scale,
                onClick = { showPasswordDialog = true }
            )

            // 3. Changing PIN Code Row
            SettingsItemCard(
                title = Localization.getString("set_pin", lang),
                desc = if (config.pinCode != null) Localization.getString("set_pin_desc_on", lang) else Localization.getString("set_pin_desc_off", lang),
                icon = Icons.Filled.Dialpad,
                scale = scale,
                onClick = { 
                    pinInput = config.pinCode ?: ""
                    showPinDialog = true 
                }
            )

            HorizontalDivider()

            // Section 2: App Preferences
            Text(
                text = if (lang == "en") "Appearance & Localization Preferences" else "سوییچ‌های امنیتی و ظاهری",
                fontFamily = IranSans,
                fontWeight = FontWeight.Bold,
                fontSize = (14 * scale).sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 4. In-App language switching option selector card
            SettingsSelectCard(
                title = Localization.getString("set_language", lang),
                desc = Localization.getString("set_language_desc", lang),
                icon = Icons.Filled.Language,
                options = listOf("fa" to "فارسی (Persian)", "en" to "English"),
                selected = viewModel.currentLanguage,
                onOptionSelected = { viewModel.changeLanguage(it) },
                scale = scale
            )

            // 5. In-App Premium color palette theme options card
            SettingsSelectCard(
                title = Localization.getString("set_theme_color", lang),
                desc = Localization.getString("set_theme_color_desc", lang),
                icon = Icons.Filled.Brush,
                options = listOf(
                    "BENTO_SLATE" to Localization.getString("theme_bento", lang),
                    "ROYAL_AMETHYST" to Localization.getString("theme_amethyst", lang),
                    "EMERALD_FOREST" to Localization.getString("theme_emerald", lang),
                    "SOLAR_AMBER" to Localization.getString("theme_solar", lang)
                ),
                selected = viewModel.currentThemeName,
                onOptionSelected = { viewModel.changeThemeName(it) },
                scale = scale
            )

            // 6. Font Selector
            SettingsSelectCard(
                title = Localization.getString("set_font_size", lang),
                desc = Localization.getString("set_font_size_desc", lang),
                icon = Icons.Filled.TextFields,
                options = listOf(
                    "1.0" to Localization.getString("font_normal", lang),
                    "1.2" to Localization.getString("font_medium", lang),
                    "1.4" to Localization.getString("font_large", lang),
                    "1.6" to Localization.getString("font_senior", lang)
                ),
                selected = viewModel.currentFontScale.toString(),
                onOptionSelected = { viewModel.changeFontScale(it.toFloat()) },
                scale = scale
            )

            // 7. Biometrics Lock Toggle
            SettingsToggleCard(
                title = Localization.getString("set_biometric", lang),
                desc = Localization.getString("set_biometric_desc", lang),
                icon = Icons.Filled.Fingerprint,
                scale = scale,
                checked = config.useBiometrics,
                onCheckedChange = { viewModel.toggleBiometrics(it) }
            )

            // 8. Auto-Lock 5 Mins Toggle
            SettingsToggleCard(
                title = Localization.getString("set_autolock", lang),
                desc = Localization.getString("set_autolock_desc", lang),
                icon = Icons.Filled.Timer,
                scale = scale,
                checked = config.useAutoLock,
                onCheckedChange = { viewModel.toggleAutoLock(it) }
            )

            // 9. Day / Night mode live switch
            SettingsToggleCard(
                title = Localization.getString("set_darktheme", lang),
                desc = Localization.getString("set_darktheme_desc", lang),
                icon = Icons.Filled.BrightnessMedium,
                scale = scale,
                checked = viewModel.isDarkTheme,
                onCheckedChange = { viewModel.toggleTheme() }
            )

            HorizontalDivider()

            // Section 3: Backup & Resets
            Text(
                text = Localization.getString("set_backup", lang),
                fontFamily = IranSans,
                fontWeight = FontWeight.Bold,
                fontSize = (14 * scale).sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 10. Backup Export Row
            SettingsItemCard(
                title = if (lang == "en") "Export XML/JSON Cryptographic Backup" else "پشتیبان‌گیری رمزنگاری‌شده",
                desc = Localization.getString("set_backup_desc", lang),
                icon = Icons.Filled.CloudUpload,
                scale = scale,
                onClick = { showBackupDialog = true }
            )

            // 11. Backup Import Row
            SettingsItemCard(
                title = if (lang == "en") "Import Cryptographic Backup" else "بازیابی اطلاعات پشتیبان",
                desc = Localization.getString("set_restore_desc", lang),
                icon = Icons.Filled.CloudDownload,
                scale = scale,
                onClick = { showRestoreDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 12. Emergency Reset Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showResetDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = Localization.getString("set_reset", lang),
                            fontFamily = IranSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = (14 * scale).sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = Localization.getString("set_reset_desc", lang),
                            fontFamily = IranSans,
                            fontSize = (11 * scale).sp,
                            color = Color.Gray,
                            lineHeight = (16 * scale).sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            // Software Version Details
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "AmnVazeh Offline Secure Version 1.5.0",
                fontFamily = IranSans,
                fontSize = (11 * scale).sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // ================== DIALOG WORKER PORTAL ==================

    // A. USERNAME DIALOG
    if (showUsernameDialog) {
        var localError by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showUsernameDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (nameInput.isBlank()) {
                        localError = if (lang == "en") "Username cannot be empty" else "نام کاربری نمی‌تواند خالی باشد"
                    } else {
                        viewModel.updateUsername(nameInput)
                        showUsernameDialog = false
                    }
                }) {
                    Text(if (lang == "en") "Save" else "ثبت نام جدید", fontFamily = IranSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUsernameDialog = false }) {
                    Text(Localization.getString("btn_cancel", lang), fontFamily = IranSans)
                }
            },
            title = { Text(if (lang == "en") "Change Username" else "تغییر نام کاربری", fontFamily = IranSans, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text(Localization.getString("lbl_username", lang), fontFamily = IranSans) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("settings_username_input")
                    )
                    if (localError.isNotEmpty()) {
                        Text(localError, fontFamily = IranSans, fontSize = 11.sp, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        )
    }

    // B. MASTER PASSWORD DIALOG
    if (showPasswordDialog) {
        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }
        var localError by remember { mutableStateOf("") }

        val passesCheck = newPass.length >= 8 && newPass.any { it.isUpperCase() } && newPass.any { it.isDigit() }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (oldPass.isEmpty()) {
                        localError = if (lang == "en") "Enter your current master password" else "رمز عبور اصلی فعلی را وارد کنید"
                    } else if (!passesCheck) {
                        localError = if (lang == "en") "New password must be >= 8 chars and contain 1 uppercase letter and 1 digit" else "گذرواژه جدید باید حداقل ۸ کاراکتر و دارای یک حرف بزرگ انگلیسی و یک عدد باشد"
                    } else {
                        viewModel.updateMasterPassword(oldPass, newPass) { success ->
                            if (success) {
                                showPasswordDialog = false
                            } else {
                                localError = if (lang == "en") "Incorrect current password" else "رمز عبور فعلی اشتباه است یا شروط برقرار نیست"
                            }
                        }
                    }
                }) {
                    Text(if (lang == "en") "Save Password" else "تغییر گذرواژه", fontFamily = IranSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
                    Text(Localization.getString("btn_cancel", lang), fontFamily = IranSans)
                }
            },
            title = { Text(if (lang == "en") "Change Master Password" else "تغییر رمز عبور اصلی (مستر مربوط)", fontFamily = IranSans, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (lang == "en") "Note: Changing your master password changes the AES cipher seed." else "نکته: تغییر رمز اصلی کلید رمزنگاری پشتیبان‌های قدیمی را تغییر می‌دهد.", 
                        fontFamily = IranSans, 
                        fontSize = 11.sp, 
                        color = Color.Gray, 
                        lineHeight = 16.sp
                    )
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text(if (lang == "en") "Current Master Password" else "رمز عبور فعلی اصلی", fontFamily = IranSans) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text(if (lang == "en") "New Master Password" else "رمز عبور جدید اصلی (مستر)", fontFamily = IranSans) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (localError.isNotEmpty()) {
                        Text(localError, fontFamily = IranSans, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }

    // C. PIN LOCK DIALOG
    if (showPinDialog) {
        var localError by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (pinInput.isNotEmpty() && pinInput.length != 4) {
                        localError = if (lang == "en") "PIN must be exactly 4 digits" else "پین باید دقیقاً ۴ رقم یا کاملاً خالی برای غیرفعال‌سازی باشد"
                    } else {
                        viewModel.updatePin(if (pinInput.isEmpty()) null else pinInput)
                        showPinDialog = false
                    }
                }) {
                    Text(if (lang == "en") "Save PIN" else "ثبت پین", fontFamily = IranSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) {
                    Text(Localization.getString("btn_cancel", lang), fontFamily = IranSans)
                }
            },
            title = { Text(Localization.getString("set_pin", lang), fontFamily = IranSans, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = if (lang == "en") "Set a 4-digit fast-entry lock. Leave empty to disable PIN block." else "یک رمز ۴ رقمی و عددی برای ورود سریع به برنامه تعریف کنید. برای غیرفعال کردن پین، کادر را کاملاً خالی بگذارید و تایید کنید.", 
                        fontFamily = IranSans, 
                        fontSize = 12.sp, 
                        color = Color.Gray, 
                        lineHeight = 18.sp, 
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                pinInput = input
                            }
                        },
                        label = { Text(if (lang == "en") "4-Digit Electronic PIN Code" else "پین کد عددی (۴ رقمی)", fontFamily = IranSans) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (localError.isNotEmpty()) {
                        Text(localError, fontFamily = IranSans, fontSize = 11.sp, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        )
    }

    // D. CRYPTOGRAPHIC BACKUP DIALOG
    if (showBackupDialog) {
        var passInput by remember { mutableStateOf("") }
        var errorBackupMsg by remember { mutableStateOf("") }
        var exportResultBase64 by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            confirmButton = {
                if (exportResultBase64.isEmpty()) {
                    Button(onClick = {
                        viewModel.exportBackup(passInput) { output ->
                            if (output != null) {
                                exportResultBase64 = output
                                clipboardManager.setText(AnnotatedString(output))
                                viewModel.showSnackbar(if (lang == "en") "Backup cipher code copied!" else "فایل پشتیبان رمزگذاری شده در کلیپ بورد موقتا کپی شد!")
                            } else {
                                errorBackupMsg = if (lang == "en") "Incorrect master password" else "رمز عبور اصلی اشتباه است"
                            }
                        }
                    }) {
                        Text(if (lang == "en") "Generate Backup" else "تولید پشتیبان رمزنگاری‌شده", fontFamily = IranSans)
                    }
                } else {
                    Button(onClick = { showBackupDialog = false }) {
                        Text(if (lang == "en") "Done & Close" else "متوجه شدم و بستن", fontFamily = IranSans)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text(if (lang == "en") "Close" else "بستن", fontFamily = IranSans)
                }
            },
            title = { Text(if (lang == "en") "Military secure Backup AES-GCM" else "پشتیبان‌گیری رمزنگاری‌شده GCM", fontFamily = IranSans, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (exportResultBase64.isEmpty()) {
                        Text(
                            text = if (lang == "en") "Creates an offline AES-256 GCM encrypted text package. Enter master password to derive PBKDF2 seed:" else "این ابزار اطلاعات فعال شما را به شکل فایل یا بلوک کد رمزشده با تکنولوژی AES-GCM 256خروجی می‌دهد. برای ساخت کلید PBKDF2 نیاز به تایید مستر رمز شما می‌باشد:", 
                            fontFamily = IranSans, 
                            fontSize = 12.sp, 
                            lineHeight = 18.sp
                        )
                        OutlinedTextField(
                            value = passInput,
                            onValueChange = { passInput = it },
                            label = { Text(if (lang == "en") "Your Master Password" else "مستر رمز عبور اصلی شما", fontFamily = IranSans) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (errorBackupMsg.isNotEmpty()) {
                            Text(errorBackupMsg, fontFamily = IranSans, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Text(
                            text = if (lang == "en") "✅ Success! Encrypted text has been produced and copied into clipboard." else "✅ پشتیبان‌گیری موفق! کد رمزگذاری‌شده تولید شد و در حافظه موقت (کلیپ بورد) گوشی قرار گرفت. می‌توانید آن را ذخیره کنید:", 
                            fontFamily = IranSans, 
                            fontSize = 12.sp, 
                            color = Color(0xFF43A047), 
                            lineHeight = 18.sp
                        )
                        OutlinedTextField(
                            value = exportResultBase64,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (lang == "en") "Encrypted Hash String" else "کد رمزنگاری پشتیبان امن", fontFamily = IranSans) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(exportResultBase64))
                                viewModel.showSnackbar(if (lang == "en") "Copied back into clipboard!" else "کدی پشتیبان مجدداً کپی گردید")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (lang == "en") "Recopy Code" else "کپی مجدد کد خروجی", fontFamily = IranSans)
                        }
                    }
                }
            }
        )
    }

    // E. RESTORE BACKUP DIALOG
    if (showRestoreDialog) {
        var base64PayloadInput by remember { mutableStateOf("") }
        var masterPassInput by remember { mutableStateOf("") }
        var restoreErrorMsg by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            confirmButton = {
                Button(onClick = {
                    if (base64PayloadInput.isBlank() || masterPassInput.isBlank()) {
                        restoreErrorMsg = if (lang == "en") "All fields are required" else "پر کردن کدهای کادر اطلاعات الزامی است"
                    } else {
                        viewModel.importBackup(base64PayloadInput.trim(), masterPassInput) { ok ->
                            if (ok) {
                                showRestoreDialog = false
                            } else {
                                restoreErrorMsg = if (lang == "en") "Invalid password or corrupted hash" else "رمز عبور اصلی نادرست است یا اطلاعات فایل ورودی معیوب است"
                            }
                        }
                    }
                }) {
                    Text(if (lang == "en") "Authenticate & Import" else "تأیید و بازیابی اطلاعات", fontFamily = IranSans)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text(Localization.getString("btn_cancel", lang), fontFamily = IranSans)
                }
            },
            title = { Text(if (lang == "en") "Decrypt & Restore" else "بازیابی امن اطلاعات", fontFamily = IranSans, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (lang == "en") "Paste your cryptographic backup token below and enter the matching master password to recover parameters:" else "کد پشتیبان رمز فشرده‌شده تولیدی در بخش قبل را در کادر زیر قرار دهید و برای شکستن رمز عبور، مستر رمز همان زمان را تایپ کنید تا داده‌ها بازیابی شوند:", 
                        fontFamily = IranSans, 
                        fontSize = 11.sp, 
                        color = Color.Gray, 
                        lineHeight = 16.sp
                    )
                    OutlinedTextField(
                        value = base64PayloadInput,
                        onValueChange = { base64PayloadInput = it },
                        label = { Text(if (lang == "en") "Cryptographic Token Block" else "کد رمزشده حاصل از پشتیبان", fontFamily = IranSans) },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                    OutlinedTextField(
                        value = masterPassInput,
                        onValueChange = { masterPassInput = it },
                        label = { Text(if (lang == "en") "Companion Master Password" else "رمز عبور مستر مربوطه", fontFamily = IranSans) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (restoreErrorMsg.isNotEmpty()) {
                        Text(restoreErrorMsg, fontFamily = IranSans, fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }

    // F. EMERGENCY RESET DIALOG
    if (showResetDialog) {
        var localConfirmInput by remember { mutableStateOf("") }
        val verificationTextConfirm = if (lang == "en") "confirm" else "تایید"
        
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (localConfirmInput == verificationTextConfirm) {
                            viewModel.emergencyResetApp()
                            showResetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = localConfirmInput == verificationTextConfirm
                ) {
                    Text(if (lang == "en") "Purge Everything" else "حذف همه‌چیز و بازنشانی", fontFamily = IranSans, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(if (lang == "en") "Cancel" else "لغو عملیات", fontFamily = IranSans)
                }
            },
            title = { 
                Text(
                    text = if (lang == "en") "⚠️ Emergency Reset Warning" else "⚠️ هشدار بازنشانی اضطراری برنامه", 
                    fontFamily = IranSans, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.error
                ) 
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (lang == "en") "This permanently purges your vault credentials, configurations, PIN locker, and active themes. There is absolutely NO undo!" else "با فعال کردن این گزینه کل رمزهای ذخیره شده، سطل آشغال و تنظیمات هویتی و پین شما کاملاً پاک شده و برنامه به حالت صفر اولیه باز می‌گردد.", 
                        fontFamily = IranSans, 
                        fontSize = 12.sp, 
                        lineHeight = 18.sp
                    )
                    Text(
                        text = if (lang == "en") "To proceed, write the word 'confirm' below:" else "برای تأیید، کلمه «تایید» را در فیلد زیر بنویسید:", 
                        fontFamily = IranSans, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = localConfirmInput,
                        onValueChange = { localConfirmInput = it },
                        placeholder = { Text(if (lang == "en") "Type 'confirm'" else "کلمه «تایید» را تایپ کنید", fontFamily = IranSans, fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }
}

@Composable
fun SettingsItemCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    scale: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = IranSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = (14 * scale).sp
                )
                Text(
                    text = desc,
                    fontFamily = IranSans,
                    fontSize = (11 * scale).sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronLeft,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun SettingsToggleCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    scale: Float,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = IranSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = (14 * scale).sp
                )
                Text(
                    text = desc,
                    fontFamily = IranSans,
                    fontSize = (11 * scale).sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun SettingsSelectCard(
    title: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    options: List<Pair<String, String>>,
    selected: String,
    onOptionSelected: (String) -> Unit,
    scale: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontFamily = IranSans, fontWeight = FontWeight.Bold, fontSize = (14 * scale).sp)
                    Text(desc, fontFamily = IranSans, color = Color.Gray, fontSize = (11 * scale).sp, lineHeight = (16 * scale).sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                options.forEach { (key, label) ->
                    val isSelected = key == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
                            .clickable { onOptionSelected(key) }
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onOptionSelected(key) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label, 
                            fontFamily = IranSans, 
                            fontSize = (13 * scale).sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
