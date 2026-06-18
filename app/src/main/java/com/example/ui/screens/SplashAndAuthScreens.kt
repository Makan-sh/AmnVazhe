package com.example.ui.screens

import android.app.Activity
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.CancellationSignal
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IranSans
import com.example.utils.PasswordStrengthEstimator
import com.example.viewmodel.AmnViewModel

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shield lock Logo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(70.dp)
                )
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color(0xFF0F172A),
                    modifier = Modifier.size(30.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "امن‌واژه | AmnVazeh",
                fontFamily = IranSans,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.White
            )
            
            Text(
                text = "Isolated Local Credentials Vault",
                fontFamily = IranSans,
                fontSize = 13.sp,
                color = Color.LightGray.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Rotating loader
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(40.dp)
                    .rotate(angle)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: AmnViewModel,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val strength = PasswordStrengthEstimator.estimate(password)

    // Password guidelines checklist
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasDigit = password.any { it.isDigit() }

    val scrollState = rememberScrollState()
    val lang = viewModel.currentLanguage
    val scale = viewModel.currentFontScale
    val isEn = lang == "en"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Shield / Edit registration Icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AppRegistration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (isEn) "Initialize Master Credentials" else "ثبت‌نام اولیه در امن‌واژه",
                    fontFamily = IranSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = (20 * scale).sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isEn) "Define an offline master username and master password. This encrypted key ensures your absolute client-side local database security." else "برای شروع کار، یک نام کاربری و رمز اصلی تعریف کنید. این اطلاعات برای رمزگذاری محلی دیتابیس بکار رفته و فقط و فقط روی گوشی شما ذخیره خواهند شد.",
                    fontFamily = IranSans,
                    fontSize = (12 * scale).sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = (18 * scale).sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(if (isEn) "Master Username" else "نام کاربری اصلی", fontFamily = IranSans, fontSize = (13 * scale).sp) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_username_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Master Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isEn) "Master Password" else "رمز عبور اصلی (مستر رمز)", fontFamily = IranSans, fontSize = (13 * scale).sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle Visibility"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("register_password_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Real-time strength meter inside register
                if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEn) "Password Strength: " else "قدرت رمز اصلی: ",
                            fontFamily = IranSans,
                            fontSize = (12 * scale).sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = strength.label,
                            fontFamily = IranSans,
                            fontSize = (12 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            color = strength.color
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { strength.score / 4f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = strength.color,
                        trackColor = Color.LightGray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Guidelines checklist UI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isEn) "Password Minimum Parameters:" else "شرایط رمز عبور اصلی:",
                        fontFamily = IranSans,
                        fontSize = (12 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    GuidelineItem(if (isEn) "At least 8 parameters or characters" else "حداقل ۸ کاراکتر برآورده شود", hasMinLength, scale)
                    GuidelineItem(if (isEn) "Contains at least 1 uppercase character" else "شامل حداقل یک حرف بزرگ انگلیسی باشد", hasUppercase, scale)
                    GuidelineItem(if (isEn) "Includes at least 1 digit or number" else "شامل حداقل یک عدد باشد", hasDigit, scale)
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = errorMessage,
                        fontFamily = IranSans,
                        fontSize = (12 * scale).sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Language select fast switcher at registration bottom
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel.changeLanguage(if (isEn) "fa" else "en")
                        }
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = "Language Switcher",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) "فارسی" else "English (US)",
                        fontFamily = IranSans,
                        fontSize = (10 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (username.isBlank()) {
                            errorMessage = if (isEn) "Username cannot be blank" else "لطفاً نام کاربری را وارد کنید"
                        } else if (!hasMinLength || !hasUppercase || !hasDigit) {
                            errorMessage = if (isEn) "Please satisfy all master password parameters" else "شروط رمز عبور هنوز به‌طور کامل برقرار نیست"
                        } else {
                            viewModel.registerUser(username, password) { success ->
                                if (!success) {
                                    errorMessage = if (isEn) "An unexpected storage error occurred" else "خطایی در ذخیره‌سازی رخ داد"
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_register_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isEn) "Initialize & Access" else "ثبت‌نام و ورود",
                        fontFamily = IranSans,
                        fontSize = (15 * scale).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GuidelineItem(text: String, isMet: Boolean, scale: Float) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isMet) Icons.Filled.CheckCircle else Icons.Filled.Close,
            contentDescription = null,
            tint = if (isMet) Color(0xFF43A047) else Color(0xFFE53935),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontFamily = IranSans,
            fontSize = (11 * scale).sp,
            color = if (isMet) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AmnViewModel,
    modifier: Modifier = Modifier
) {
    val config = viewModel.userConfigFlow.collectAsState().value ?: return
    val context = LocalContext.current

    var loginPassword by remember { mutableStateOf("") }
    var loginPin by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf("") }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Password, 1 = PIN

    val scrollState = rememberScrollState()
    val lang = viewModel.currentLanguage
    val scale = viewModel.currentFontScale
    val isEn = lang == "en"

    // Real and simulated biometric trigger
    fun triggerBiometrics() {
        if (!config.useBiometrics) {
            viewModel.showSnackbar(
                if (isEn) "Biometrics login is inactive. Enable it in Settings." 
                else "احراز هویت اثر انگشت فعال نیست. آن را از تنظیمات روشن کنید."
            )
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val activity = context as Activity
                val executor = activity.mainExecutor
                val prompt = BiometricPrompt.Builder(context)
                    .setTitle(if (isEn) "AmnVazeh Authentication" else "ورود به امن‌واژه")
                    .setSubtitle(if (isEn) "Scan your registered fingerprint" else "اثر انگشت ثبت‌شده خود را اسکن کنید")
                    .setNegativeButton(if (isEn) "Cancel" else "انصراف", executor) { _, _ -> /* Cancel */ }
                    .build()

                prompt.authenticate(
                    CancellationSignal(),
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                            activity.runOnUiThread {
                                viewModel.authenticateBiometrics()
                            }
                        }
                        override fun onAuthenticationFailed() {
                            activity.runOnUiThread {
                                loginError = if (isEn) "Fingerprint not verified" else "اثر انگشت معتبر نیست"
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                // Biometrics fallback
                viewModel.authenticateBiometrics()
            }
        } else {
            // Simulated fallback
            viewModel.authenticateBiometrics()
        }
    }

    // Auto-authenticate with biometric if allowed
    LaunchedEffect(config) {
        if (config.useBiometrics) {
            triggerBiometrics()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${if (isEn) "Welcome back" else "خوش آمدید"} ${config.username}",
                    fontFamily = IranSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = (22 * scale).sp
                )

                Text(
                    text = if (isEn) "Credentials vault locked. Use your credentials to decrypt database." else "امن‌واژه قفل است. برای باز کردن ورودتان را تایید کنید.",
                    fontFamily = IranSans,
                    fontSize = (12 * scale).sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Navigation headers for Login methods (Password vs PIN)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .background(
                            MaterialTheme.colorScheme.background,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable {
                                selectedTab = 0
                                loginError = ""
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isEn) "Master Key" else "رمز اصلی",
                            fontFamily = IranSans,
                            fontSize = (13 * scale).sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    if (config.pinCode != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    selectedTab = 1
                                    loginError = ""
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isEn) "4-Digit PIN" else "رمز پین (۴ رقمی)",
                                fontFamily = IranSans,
                                fontSize = (13 * scale).sp,
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }

                // TAB CONTENT
                if (selectedTab == 0) {
                    // Password input login
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text(if (isEn) "Enter Master Password" else "رمز عبور اصلی را وارد کنید", fontFamily = IranSans, fontSize = (13 * scale).sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (loginError.isNotEmpty()) {
                        Text(
                            text = loginError,
                            fontFamily = IranSans,
                            fontSize = (12 * scale).sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.loginWithPassword(loginPassword) { ok ->
                                if (!ok) {
                                    loginError = if (isEn) "Incorrect master password key" else "رمز عبور اصلی نادرست است"
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_password_submit"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isEn) "Decrypt & Open" else "تأیید و ورود", 
                            fontFamily = IranSans, 
                            fontSize = (14 * scale).sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // PIN flow input selection
                    OutlinedTextField(
                        value = loginPin,
                        onValueChange = { input -> 
                            if (input.all { it.isDigit() } && input.length <= 4) {
                                loginPin = input
                            }
                        },
                        label = { Text(if (isEn) "4-Digit PIN Code" else "پین کد ۴ رقمی", fontFamily = IranSans, fontSize = (13 * scale).sp) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Filled.Pin, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_pin_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (loginError.isNotEmpty()) {
                        Text(
                            text = loginError,
                            fontFamily = IranSans,
                            fontSize = (12 * scale).sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (loginPin.length != 4) {
                                loginError = if (isEn) "Fill 4-digit pin completely" else "لطفاً پین ۴ رقمی کامل را وارد کنید"
                            } else {
                                viewModel.loginWithPin(loginPin) { ok ->
                                    if (!ok) {
                                        loginError = if (isEn) "Incorrect lock pin" else "رمز پین نادرست است"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_pin_submit"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(
                            text = if (isEn) "PIN Authenticate" else "ورود با پین", 
                            fontFamily = IranSans, 
                            fontSize = (14 * scale).sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Biometrics button display trigger if enabled
                if (config.useBiometrics) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    IconButton(
                        onClick = { triggerBiometrics() },
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            )
                            .padding(8.dp)
                            .testTag("biometric_login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = if (isEn) "Fingerprint access" else "ورود با اثر انگشت",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                    
                    Text(
                        text = if (isEn) "Tap to scan fingerprint scanner" else "لمس حسگر اثر انگشت",
                        fontFamily = IranSans,
                        fontSize = (11 * scale).sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Fast dynamic language switcher also at LoginScreen bottom
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            viewModel.changeLanguage(if (isEn) "fa" else "en")
                        }
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = "Language switcher",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isEn) "فارسی" else "English (US)",
                        fontFamily = IranSans,
                        fontSize = (10 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
