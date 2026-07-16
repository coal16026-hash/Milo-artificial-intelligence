package com.example

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val timestamp: Long,
    val isPinned: Boolean = false
)

@Entity(tableName = "messages", 
    foreignKeys = [
        ForeignKey(entity = ConversationEntity::class, parentColumns = ["id"], childColumns = ["conversationId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val conversationId: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val thinking: String? = null,
    val imageUri: String? = null
)

@Entity(tableName = "user_preferences")
data class UserPreferenceEntity(
    @PrimaryKey val id: String = "default",
    val theme: String = "Follow system",
    val textSize: String = "Default",
    val notificationsEnabled: Boolean = true,
    val hapticFeedback: Boolean = true,
    val displayName: String = "",
    val avatarPath: String = ""
)

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, timestamp DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    suspend fun getAllConversationsSync(): List<ConversationEntity>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    suspend fun getMessagesForConversation(conversationId: String): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert
    suspend fun insertMessage(message: MessageEntity)

    @Query("UPDATE conversations SET title = :title, timestamp = :timestamp WHERE id = :id")
    suspend fun updateConversation(id: String, title: String, timestamp: Long)

    @Query("UPDATE conversations SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: String, title: String)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("DELETE FROM conversations")
    suspend fun clearAll()

    @Query("SELECT * FROM user_preferences WHERE id = 'default'")
    fun getUserPreferences(): Flow<UserPreferenceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(prefs: UserPreferenceEntity)
}

@Database(entities = [ConversationEntity::class, MessageEntity::class, UserPreferenceEntity::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "chat_db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
