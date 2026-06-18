package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.IranSans
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AmnViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    
    private val viewModel: AmnViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme(
                darkTheme = viewModel.isDarkTheme,
                languageCode = viewModel.currentLanguage,
                themeName = viewModel.currentThemeName
            ) {
                val snackbarHostState = remember { SnackbarHostState() }
                
                // Collect snackbar messages from ViewModel Event Flow
                LaunchedEffect(Unit) {
                    viewModel.snackbarMessage.collectLatest { msg ->
                        snackbarHostState.showSnackbar(msg)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val configState = viewModel.userConfigFlow.collectAsState().value
                    
                    // Route switcher depending on global state
                    when {
                        viewModel.isSplashActive -> {
                            SplashScreen()
                        }
                        configState == null -> {
                            RegisterScreen(viewModel = viewModel)
                        }
                        !viewModel.isUserAuthenticated -> {
                            LoginScreen(viewModel = viewModel)
                        }
                        else -> {
                            // User is fully authenticated, show the secure main app shell
                            // Capture any tap on the screen to update inactivity locks!
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onPress = { viewModel.updateActivity() },
                                            onTap = { viewModel.updateActivity() }
                                        )
                                    }
                            ) {
                                MainApplicationShell(
                                    viewModel = viewModel,
                                    snackbarHostState = snackbarHostState
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApplicationShell(
    viewModel: AmnViewModel,
    snackbarHostState: SnackbarHostState
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Filter screens where we want to hide the bottom NavigationBar (e.g. AddEditScreen)
    val showBottomBar = currentRoute != "add_edit/{id}"

    val lang = viewModel.currentLanguage
    val scale = viewModel.currentFontScale

    Scaffold(
        topBar = {
            if (showBottomBar) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                "dashboard" -> com.example.utils.Localization.getString("app_title", lang)
                                "security" -> com.example.utils.Localization.getString("nav_security", lang)
                                "trash" -> com.example.utils.Localization.getString("nav_trash", lang)
                                "settings" -> com.example.utils.Localization.getString("nav_settings", lang)
                                "about" -> com.example.utils.Localization.getString("nav_about", lang)
                                else -> com.example.utils.Localization.getString("app_title", lang)
                            },
                            fontFamily = IranSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = (20 * scale).sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        // Manual lock trigger
                        IconButton(
                            onClick = { 
                                viewModel.lockApp()
                                viewModel.showSnackbar("برنامه قفل شد.")
                            },
                            modifier = Modifier.testTag("manual_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "قفل کردن برنامه",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = currentRoute == "dashboard",
                        onClick = {
                            navController.navigate("dashboard") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Lock, contentDescription = null) },
                        label = { Text(com.example.utils.Localization.getString("nav_passwords", lang), fontFamily = IranSans, fontSize = (11 * scale).sp) }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "security",
                        onClick = {
                            navController.navigate("security") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Info, contentDescription = null) },
                        label = { Text(com.example.utils.Localization.getString("nav_security", lang), fontFamily = IranSans, fontSize = (11 * scale).sp) }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "trash",
                        onClick = {
                            navController.navigate("trash") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Delete, contentDescription = null) },
                        label = { Text(com.example.utils.Localization.getString("nav_trash", lang), fontFamily = IranSans, fontSize = (11 * scale).sp) }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "settings",
                        onClick = {
                            navController.navigate("settings") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Settings, contentDescription = null) },
                        label = { Text(com.example.utils.Localization.getString("nav_settings", lang), fontFamily = IranSans, fontSize = (11 * scale).sp) }
                    )

                    NavigationBarItem(
                        selected = currentRoute == "about",
                        onClick = {
                            navController.navigate("about") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(imageVector = Icons.Filled.Info, contentDescription = null) },
                        label = { Text(com.example.utils.Localization.getString("nav_about", lang), fontFamily = IranSans, fontSize = (11 * scale).sp) }
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                MainDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToAddEdit = { id -> 
                        navController.navigate("add_edit/$id")
                    }
                )
            }
            
            composable("security") {
                SecurityScreen(viewModel = viewModel)
            }
            
            composable("trash") {
                TrashScreen(viewModel = viewModel)
            }
            
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
            
            composable("about") {
                AboutScreen(viewModel = viewModel)
            }
            
            composable(
                route = "add_edit/{id}",
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                AddEditScreen(
                    viewModel = viewModel,
                    id = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
