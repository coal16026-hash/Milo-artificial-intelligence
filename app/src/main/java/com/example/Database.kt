package com.example

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
}

@Database(entities = [ConversationEntity::class, MessageEntity::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        private fun safeAddColumn(db: SupportSQLiteDatabase, table: String, column: String, type: String) {
            try {
                db.execSQL("ALTER TABLE `$table` ADD COLUMN `$column` $type")
            } catch (e: Exception) {
                // Ignore if column already exists
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeAddColumn(db, "conversations", "isPinned", "INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeAddColumn(db, "messages", "thinking", "TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeAddColumn(db, "messages", "imageUri", "TEXT")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ensure user_preferences table existed (it's dropped in 5->6 anyway, but needed if migrating directly)
                db.execSQL("CREATE TABLE IF NOT EXISTS `user_preferences` (`id` TEXT NOT NULL, `theme` TEXT NOT NULL, `textSize` TEXT NOT NULL, `notificationsEnabled` INTEGER NOT NULL, `hapticFeedback` INTEGER NOT NULL, `displayName` TEXT NOT NULL, `avatarPath` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `user_preferences`")
            }
        }

        fun getDatabase(context: android.content.Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "chat_db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
