package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PasswordEntity
import com.example.ui.theme.IranSans
import com.example.viewmodel.AmnViewModel
import com.example.utils.Localization
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: AmnViewModel,
    modifier: Modifier = Modifier
) {
    val deletedPasswords by viewModel.deletedPasswordsFlow.collectAsState()
    var itemToDeleteForever by remember { mutableStateOf<PasswordEntity?>(null) }
    
    val lang = viewModel.currentLanguage
    val scale = viewModel.currentFontScale

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = Localization.getString("trash_title", lang), 
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Information details banner about the 30-day auto-purge policy
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (lang == "en") "30-Day Automated Recycle Purge" else "سیاست پایش خودکار ۳۰ روزه",
                        fontFamily = IranSans,
                        fontSize = (12 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = Localization.getString("trash_info", lang),
                        fontFamily = IranSans,
                        fontSize = (11 * scale).sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = (18 * scale).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Deleted list flow
            if (deletedPasswords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = null,
                            tint = Color.Gray.copy(alpha = 0.4f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = Localization.getString("trash_empty", lang),
                            fontFamily = IranSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = (15 * scale).sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(deletedPasswords, key = { it.id }) { passwordItem ->
                        TrashCard(
                            password = passwordItem,
                            lang = lang,
                            scale = scale,
                            onRestore = { 
                                viewModel.restorePassword(passwordItem)
                            },
                            onDeleteForever = { 
                                itemToDeleteForever = passwordItem
                            }
                        )
                    }
                }
            }
        }
    }

    // Double-action secure alert dialog confirmation
    if (itemToDeleteForever != null) {
        val currentItem = itemToDeleteForever!!
        AlertDialog(
            onDismissRequest = { itemToDeleteForever = null },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.permanentlyDeletePassword(currentItem)
                        itemToDeleteForever = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = if (lang == "en") "Permanently Delete" else "حذف دائمی بی‌بازگشت", 
                        fontFamily = IranSans, 
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDeleteForever = null }) {
                    Text(Localization.getString("btn_cancel", lang), fontFamily = IranSans)
                }
            },
            title = {
                Text(
                    text = if (lang == "en") "Delete Credentials Permanently" else "حذف دائمی گذرواژه", 
                    fontFamily = IranSans, 
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (lang == "en") "Are you absolutely sure you want to delete the credentials for '${currentItem.siteName}' forever? This operation is irreversible." else "آیا کاملاً مایلید رمز عبور سایت «${currentItem.siteName}» را برای همیشه و بدون امکان بازیابی حذف نمایید؟ این عملیات غیرقابل بازگشت است.",
                    fontFamily = IranSans,
                    fontSize = 13.sp,
                    lineHeight = 22.sp
                )
            }
        )
    }
}

@Composable
fun TrashCard(
    password: PasswordEntity,
    lang: String,
    scale: Float,
    onRestore: () -> Unit,
    onDeleteForever: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine deletion date representation
    val formatDeletedDate = remember(password.deletedAt) {
        if (password.deletedAt != null) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = password.deletedAt
            DateFormat.format("yyyy/MM/dd", calendar).toString()
        } else {
            "--"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("trash_card_${password.siteName}"),
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
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                val charLabel = password.siteName.firstOrNull()?.toString() ?: "S"
                Text(
                    text = charLabel.uppercase(),
                    fontFamily = IranSans,
                    fontSize = (15 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = password.siteName,
                    fontFamily = IranSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = (15 * scale).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${Localization.getString("lbl_username", lang)}: ${password.username}",
                        fontFamily = IranSans,
                        fontSize = (11 * scale).sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${if (lang == "en") "Purge: " else "حذف: "} $formatDeletedDate",
                        fontFamily = IranSans,
                        fontSize = (11 * scale).sp,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action triggers (Restore or Delete forever)
            IconButton(
                onClick = onRestore,
                modifier = Modifier.testTag("restore_button_${password.siteName}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Restore,
                    contentDescription = Localization.getString("restore_btn", lang),
                    tint = Color(0xFF43A047)
                )
            }

            IconButton(
                onClick = onDeleteForever,
                modifier = Modifier.testTag("delete_forever_button_${password.siteName}")
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = Localization.getString("delete_forever", lang),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
