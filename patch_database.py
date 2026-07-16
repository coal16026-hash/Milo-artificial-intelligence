import re

with open("app/src/main/java/com/example/Database.kt", "r") as f:
    content = f.read()

# Add new entity and DAO
new_entity = """
@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val imageUrl: String,
    val timestamp: Long
)

@Dao
interface GeneratedImageDao {
    @Query("SELECT * FROM generated_images ORDER BY timestamp DESC")
    fun getAllGeneratedImages(): kotlinx.coroutines.flow.Flow<List<GeneratedImageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedImage(image: GeneratedImageEntity)
}

@Dao
interface ChatDao {"""
content = content.replace("@Dao\ninterface ChatDao {", new_entity)

# Update database annotation
old_db = "@Database(entities = [ConversationEntity::class, MessageEntity::class], version = 6, exportSchema = false)"
new_db = "@Database(entities = [ConversationEntity::class, MessageEntity::class, GeneratedImageEntity::class], version = 7, exportSchema = false)"
content = content.replace(old_db, new_db)

# Add abstract dao
old_abs = "abstract class AppDatabase : RoomDatabase() {\n    abstract fun chatDao(): ChatDao"
new_abs = "abstract class AppDatabase : RoomDatabase() {\n    abstract fun chatDao(): ChatDao\n    abstract fun generatedImageDao(): GeneratedImageDao"
content = content.replace(old_abs, new_abs)

# Add migration
mig_old = "val MIGRATION_5_6 = object : Migration(5, 6) {\n            override fun migrate(db: SupportSQLiteDatabase) {\n                db.execSQL(\"DROP TABLE IF EXISTS `user_preferences`\")\n            }\n        }"
mig_new = mig_old + "\n        val MIGRATION_6_7 = object : Migration(6, 7) {\n            override fun migrate(db: SupportSQLiteDatabase) {\n                db.execSQL(\"CREATE TABLE IF NOT EXISTS `generated_images` (`id` TEXT NOT NULL, `prompt` TEXT NOT NULL, `imageUrl` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))\")\n            }\n        }"
content = content.replace(mig_old, mig_new)

# Add to addMigrations
old_add = ".addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)"
new_add = ".addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)"
content = content.replace(old_add, new_add)

with open("app/src/main/java/com/example/Database.kt", "w") as f:
    f.write(content)
