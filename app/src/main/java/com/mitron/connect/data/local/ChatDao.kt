package com.mitron.connect.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mitron.connect.data.model.ChatMessage
import com.mitron.connect.data.model.ChatPreview
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_previews")
    fun getChatPreviewsFlow(): Flow<List<ChatPreview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatPreviews(chats: List<ChatPreview>)

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun getChatMessagesFlow(chatId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessage>)

    @Query("DELETE FROM chat_messages WHERE chatId = :chatId")
    suspend fun clearMessagesForChat(chatId: String)

    @Query("DELETE FROM chat_messages WHERE id LIKE 'temp_%'")
    suspend fun clearTempMessages()
}
