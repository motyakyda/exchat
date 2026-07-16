package ru.fromchat.config

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import ru.fromchat.AppForeground
import ru.fromchat.api.ApiClient
import ru.fromchat.api.local.messages.generateClientMessageId
import ru.fromchat.api.local.messages.nowMessageTimestampIso
import ru.fromchat.api.local.send.OutgoingMessageCoordinator
import ru.fromchat.api.schema.messages.Message

object AutoResponder {
    private val lastReplyTimes = mutableMapOf<Int, Long>()
    private const val COOLDOWN_MS = 5 * 60 * 1000L // 5 minutes

    suspend fun onIncomingMessage(otherUserId: Int) {
        if (!Settings.autoResponderEnabled) return
        
        val currentUserId = ApiClient.user?.id ?: return
        if (otherUserId == currentUserId) return

        val now = Clock.System.now().toEpochMilliseconds()
        val lastTime = lastReplyTimes[otherUserId] ?: 0L
        if (now - lastTime < COOLDOWN_MS) return

        // 1. If only-offline is enabled, check if the app is in the foreground. If so, skip.
        if (Settings.autoResponderOnlyOffline && AppForeground.isInForeground.value) return

        // 2. Otherwise, check if the user is actively viewing this specific chat. If so, skip.
        if (ru.fromchat.api.local.messages.ActiveDmChatTracker.isActive(otherUserId)) return

        // Update cooldown time
        lastReplyTimes[otherUserId] = now

        val rawText = Settings.autoResponderText
        val replyText = rawText.ifBlank {
            // Default Russian/English friendly text
            "Привет! Сейчас я занят, отвечу позже."
        }

        withContext(Dispatchers.Default) {
            val tempId = -kotlin.random.Random.nextInt(1, Int.MAX_VALUE)
            val clientMsgId = generateClientMessageId()
            val optimisticMessage = Message(
                id = tempId,
                user_id = currentUserId,
                content = replyText,
                timestamp = nowMessageTimestampIso(),
                is_read = false,
                is_edited = false,
                username = "You",
                client_message_id = clientMsgId
            )
            OutgoingMessageCoordinator.enqueueDmMessage(
                recipientId = otherUserId,
                plaintext = replyText,
                clientMessageId = clientMsgId,
                replyToId = null,
                optimisticMessage = optimisticMessage
            )
        }
    }
}
