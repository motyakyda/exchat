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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import ru.fromchat.ui.components.Text
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EXChatScreen(onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    var quickReplies by remember { mutableStateOf(Settings.readQuickReplies()) }
    var showDialog by remember { mutableStateOf(false) }
    var editingReply by remember { mutableStateOf<QuickReply?>(null) }

    var shortcutText by remember { mutableStateOf("") }
    var messageText by remember { mutableStateOf("") }

    var shortcutError by remember { mutableStateOf<String?>(null) }
    var messageError by remember { mutableStateOf<String?>(null) }

    val errorEmpty = stringResource(Res.string.settings_quick_replies_error_empty)
    val errorSpace = stringResource(Res.string.settings_quick_replies_error_space)
    val errorDuplicate = stringResource(Res.string.settings_quick_replies_error_duplicate)

    fun openAddDialog() {
        editingReply = null
        shortcutText = ""
        messageText = ""
        shortcutError = null
        messageError = null
        showDialog = true
    }

    fun openEditDialog(reply: QuickReply) {
        editingReply = reply
        shortcutText = reply.shortcut
        messageText = reply.message
        shortcutError = null
        messageError = null
        showDialog = true
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
            showDialog = false
        }
    }

    fun deleteQuickReply(reply: QuickReply) {
        val updatedList = quickReplies.filter { it.id != reply.id }
        Settings.writeQuickReplies(updatedList)
        quickReplies = updatedList
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
            Category(
                Modifier.padding(top = 16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                ListItem(
                    headline = stringResource(Res.string.settings_quick_replies_title),
                    supportingText = stringResource(Res.string.settings_quick_replies_desc),
                )
            }

            Category(
                Modifier.padding(top = 16.dp),
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
                            onClick = { openEditDialog(reply) },
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

            Category(
                Modifier.padding(top = 16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                ListItem(
                    headline = stringResource(Res.string.settings_quick_replies_add),
                    onClick = { openAddDialog() },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                )
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
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
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}
