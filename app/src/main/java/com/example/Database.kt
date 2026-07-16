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
    val isPinned: Boolean = false,
    val folderId: String? = null
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
    val imageUri: String? = null,
    val parentMessageId: String? = null,
    val branchIndex: Int = 0,
    val promptTokens: Int? = null,
    val completionTokens: Int? = null
)


@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val imageUrl: String,
    val timestamp: Long,
    val style: String = "Default",
    val size: String = "1024x1024"
)

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorTag: String,
    val createdAt: Long
)

@Dao
interface GeneratedImageDao {
    @Query("SELECT * FROM generated_images ORDER BY timestamp DESC")
    fun getAllGeneratedImages(): kotlinx.coroutines.flow.Flow<List<GeneratedImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedImage(image: GeneratedImageEntity)

    @Delete
    suspend fun deleteGeneratedImage(image: GeneratedImageEntity)
}

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE conversations SET title = :title, timestamp = :timestamp WHERE id = :id")
    suspend fun updateConversation(id: String, title: String, timestamp: Long)

    @Query("UPDATE conversations SET title = :title WHERE id = :id")
    suspend fun updateTitle(id: String, title: String)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinned(id: String, isPinned: Boolean)

    @Query("UPDATE conversations SET folderId = :folderId WHERE id = :id")
    suspend fun updateConversationFolder(id: String, folderId: String?)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("DELETE FROM conversations")
    suspend fun clearAll()

    // Folder Dao methods
    @Query("SELECT * FROM folders ORDER BY createdAt ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Query("UPDATE folders SET name = :name WHERE id = :id")
    suspend fun updateFolderName(id: String, name: String)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteFolder(id: String)

    @Query("UPDATE conversations SET folderId = NULL WHERE folderId = :folderId")
    suspend fun removeFolderFromConversations(folderId: String)

    @Query("SELECT COUNT(*) FROM messages WHERE timestamp >= :since")
    suspend fun getMessageCountSince(since: Long): Int

    @Query("SELECT SUM(promptTokens) FROM messages WHERE timestamp >= :since")
    suspend fun getTotalPromptTokensSince(since: Long): Int?

    @Query("SELECT SUM(completionTokens) FROM messages WHERE timestamp >= :since")
    suspend fun getTotalCompletionTokensSince(since: Long): Int?
}

@Database(entities = [ConversationEntity::class, MessageEntity::class, GeneratedImageEntity::class, FolderEntity::class], version = 9, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun generatedImageDao(): GeneratedImageDao

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
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `generated_images` (`id` TEXT NOT NULL, `prompt` TEXT NOT NULL, `imageUrl` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeAddColumn(db, "generated_images", "style", "TEXT NOT NULL DEFAULT 'Default'")
                safeAddColumn(db, "generated_images", "size", "TEXT NOT NULL DEFAULT '1024x1024'")
            }
        }
        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeAddColumn(db, "messages", "parentMessageId", "TEXT")
                safeAddColumn(db, "messages", "branchIndex", "INTEGER NOT NULL DEFAULT 0")
                safeAddColumn(db, "messages", "promptTokens", "INTEGER")
                safeAddColumn(db, "messages", "completionTokens", "INTEGER")
                
                db.execSQL("CREATE TABLE IF NOT EXISTS `folders` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `colorTag` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                
                safeAddColumn(db, "conversations", "folderId", "TEXT")
            }
        }

        fun getDatabase(context: android.content.Context): AppDatabase {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "chat_db")
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
