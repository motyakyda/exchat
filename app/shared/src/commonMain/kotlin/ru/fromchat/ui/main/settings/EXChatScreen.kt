package ru.fromchat.ui.main.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pr0gramm3r101.components.Category
import com.pr0gramm3r101.components.ListItem
import com.pr0gramm3r101.components.SwitchListItem
import org.jetbrains.compose.resources.stringResource
import ru.fromchat.Res
import ru.fromchat.action_save
import ru.fromchat.back
import ru.fromchat.cancel
import ru.fromchat.config.QuickReply
import ru.fromchat.config.Settings
import ru.fromchat.settings_category_exchat
import ru.fromchat.settings_category_exchat_d
import ru.fromchat.settings_quick_replies_add
import ru.fromchat.settings_quick_replies_delete
import ru.fromchat.settings_quick_replies_desc
import ru.fromchat.settings_quick_replies_empty
import ru.fromchat.settings_quick_replies_error_duplicate
import ru.fromchat.settings_quick_replies_error_empty
import ru.fromchat.settings_quick_replies_error_space
import ru.fromchat.settings_quick_replies_message
import ru.fromchat.settings_quick_replies_message_placeholder
import ru.fromchat.settings_quick_replies_shortcut
import ru.fromchat.settings_quick_replies_shortcut_placeholder
import ru.fromchat.settings_quick_replies_title
import ru.fromchat.settings_quick_replies_category
import ru.fromchat.settings_auto_responder_category
import ru.fromchat.settings_auto_responder_enabled
import ru.fromchat.settings_auto_responder_enabled_desc
import ru.fromchat.settings_auto_responder_text_title
import ru.fromchat.settings_auto_responder_text_desc
import ru.fromchat.settings_auto_responder_text_placeholder
import ru.fromchat.settings_auto_responder_text_dialog_title
import ru.fromchat.settings_auto_responder_only_offline
import ru.fromchat.settings_auto_responder_only_offline_desc
import ru.fromchat.ui.components.Text
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EXChatScreen(onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    // Quick Replies state
    var quickReplies by remember { mutableStateOf(Settings.readQuickReplies()) }
    var showQuickReplyDialog by remember { mutableStateOf(false) }
    var editingReply by remember { mutableStateOf<QuickReply?>(null) }
    var shortcutText by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }
    var shortcutError by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf<String?>(null) }

    // Auto-responder state
    var autoResponderEnabled by remember { mutableStateOf(Settings.autoResponderEnabled) }
    var autoResponderText by remember { mutableStateOf(Settings.autoResponderText) }
    var autoResponderOnlyOffline by remember { mutableStateOf(Settings.autoResponderOnlyOffline) }
    var showAutoResponderDialog by remember { mutableStateOf(false) }
    var tempAutoResponderText by remember { mutableStateOf("") }

    val errorEmpty = stringResource(Res.string.settings_quick_replies_error_empty)
    val errorSpace = stringResource(Res.string.settings_quick_replies_error_space)
    val errorDuplicate = stringResource(Res.string.settings_quick_replies_error_duplicate)

    fun openAddQuickReplyDialog() {
        editingReply = null
        shortcutText = ""
        messageText = ""
        shortcutError = null
        messageError = null
        showQuickReplyDialog = true
    }

    fun openEditQuickReplyDialog(reply: QuickReply) {
        editingReply = reply
        shortcutText = reply.shortcut
        messageText = reply.message
        shortcutError = null
        messageError = null
        showQuickReplyDialog = true
    }

    fun saveQuickReply() {
        val cleanShortcut = shortcutText.trim().removePrefix("/").lowercase()
        val cleanMessage = messageText.trim()

        var hasError = false

        if (cleanShortcut.isEmpty()) {
            shortcutError = errorEmpty
            hasError = true
        } else if (cleanShortcut.contains(" ")) {
            shortcutError = errorSpace
            hasError = true
        } else {
            val isDuplicate = quickReplies.any { 
                it.shortcut.lowercase() == cleanShortcut && it.id != editingReply?.id 
            }
            if (isDuplicate) {
                shortcutError = errorDuplicate
                hasError = true
            } else {
                shortcutError = null
            }
        }

        if (cleanMessage.isEmpty()) {
            messageError = errorEmpty
            hasError = true
        } else {
            messageError = null
        }

        if (!hasError) {
            val updatedList = if (editingReply != null) {
                quickReplies.map { 
                    if (it.id == editingReply!!.id) {
                        it.copy(shortcut = cleanShortcut, message = cleanMessage)
                    } else {
                        it
                    }
                }
            } else {
                quickReplies + QuickReply(
                    id = Clock.System.now().toEpochMilliseconds().toString(),
                    shortcut = cleanShortcut,
                    message = cleanMessage
                )
            }
            Settings.writeQuickReplies(updatedList)
            quickReplies = updatedList
            showQuickReplyDialog = false
        }
    }

    fun deleteQuickReply(reply: QuickReply) {
        val updatedList = quickReplies.filter { it.id != reply.id }
        Settings.writeQuickReplies(updatedList)
        quickReplies = updatedList
    }

    fun openAutoResponderDialog() {
        tempAutoResponderText = autoResponderText
        showAutoResponderDialog = true
    }

    fun saveAutoResponderText() {
        val cleanText = tempAutoResponderText.trim()
        autoResponderText = cleanText
        Settings.autoResponderText = cleanText
        showAutoResponderDialog = false
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(stringResource(Res.string.settings_category_exchat)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            // ================== CATEGORY: QUICK REPLIES ==================
            Text(
                text = stringResource(Res.string.settings_quick_replies_category),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
            )

            Category(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                ListItem(
                    headline = stringResource(Res.string.settings_quick_replies_title),
                    supportingText = stringResource(Res.string.settings_quick_replies_desc),
                )
            }

            Spacer(Modifier.height(8.dp))

            Category(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                if (quickReplies.isEmpty()) {
                    ListItem(
                        headline = stringResource(Res.string.settings_quick_replies_empty)
                    )
                } else {
                    quickReplies.forEachIndexed { index, reply ->
                        ListItem(
                            headline = "/${reply.shortcut}",
                            supportingText = reply.message,
                            onClick = { openEditQuickReplyDialog(reply) },
                            leadingContent = {
                                Text(
                                    text = "/",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(16.dp)
                                )
                            },
                            trailingContent = {
                                IconButton(onClick = { deleteQuickReply(reply) }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = stringResource(Res.string.settings_quick_replies_delete),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            divider = index < quickReplies.size - 1
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Category(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                ListItem(
                    headline = stringResource(Res.string.settings_quick_replies_add),
                    onClick = { openAddQuickReplyDialog() },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }

            // ================== CATEGORY: AUTO-RESPONDER ==================
            Text(
                text = stringResource(Res.string.settings_auto_responder_category),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 28.dp, bottom = 8.dp)
            )

            Category(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                // Auto-responder on/off toggle
                SwitchListItem(
                    headline = stringResource(Res.string.settings_auto_responder_enabled),
                    supportingText = stringResource(Res.string.settings_auto_responder_enabled_desc),
                    checked = autoResponderEnabled,
                    onCheckedChange = {
                        autoResponderEnabled = it
                        Settings.autoResponderEnabled = it
                    },
                    divider = autoResponderEnabled,
                    leadingContent = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null,
                            tint = if (autoResponderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                // Only when offline toggle (shown only when auto-responder is enabled)
                if (autoResponderEnabled) {
                    SwitchListItem(
                        headline = stringResource(Res.string.settings_auto_responder_only_offline),
                        supportingText = stringResource(Res.string.settings_auto_responder_only_offline_desc),
                        checked = autoResponderOnlyOffline,
                        onCheckedChange = {
                            autoResponderOnlyOffline = it
                            Settings.autoResponderOnlyOffline = it
                        },
                        divider = true,
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = null,
                                tint = if (autoResponderOnlyOffline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                // Customize reply message (shown only when enabled)
                if (autoResponderEnabled) {
                    val displayMessage = autoResponderText.ifBlank {
                        stringResource(Res.string.settings_auto_responder_text_placeholder)
                    }
                    ListItem(
                        headline = stringResource(Res.string.settings_auto_responder_text_title),
                        supportingText = displayMessage,
                        onClick = { openAutoResponderDialog() },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }

    // Dialog: Add/Edit Quick Reply
    if (showQuickReplyDialog) {
        AlertDialog(
            onDismissRequest = { showQuickReplyDialog = false },
            title = {
                Text(
                    text = if (editingReply != null) {
                        "/${editingReply!!.shortcut}"
                    } else {
                        stringResource(Res.string.settings_quick_replies_add)
                    }
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = shortcutText,
                        onValueChange = { 
                            shortcutText = it
                            shortcutError = null
                        },
                        label = { Text(stringResource(Res.string.settings_quick_replies_shortcut)) },
                        placeholder = { Text(stringResource(Res.string.settings_quick_replies_shortcut_placeholder)) },
                        isError = shortcutError != null,
                        supportingText = {
                            shortcutError?.let { Text(it) }
                        },
                        singleLine = true,
                        prefix = { Text("/") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { 
                            messageText = it
                            messageError = null
                        },
                        label = { Text(stringResource(Res.string.settings_quick_replies_message)) },
                        placeholder = { Text(stringResource(Res.string.settings_quick_replies_message_placeholder)) },
                        isError = messageError != null,
                        supportingText = {
                            messageError?.let { Text(it) }
                        },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { saveQuickReply() }) {
                    Text(stringResource(Res.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickReplyDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    // Dialog: Edit Auto-responder message text
    if (showAutoResponderDialog) {
        AlertDialog(
            onDismissRequest = { showAutoResponderDialog = false },
            title = {
                Text(stringResource(Res.string.settings_auto_responder_text_dialog_title))
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(Res.string.settings_auto_responder_text_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = tempAutoResponderText,
                        onValueChange = { tempAutoResponderText = it },
                        placeholder = { Text(stringResource(Res.string.settings_auto_responder_text_placeholder)) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { saveAutoResponderText() }) {
                    Text(stringResource(Res.string.action_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAutoResponderDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}
