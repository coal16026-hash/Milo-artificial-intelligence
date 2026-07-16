import re

with open("app/src/main/java/com/example/Database.kt", "r") as f:
    content = f.read()

old_entity = """@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val imageUrl: String,
    val timestamp: Long
)"""

new_entity = """@Entity(tableName = "generated_images")
data class GeneratedImageEntity(
    @PrimaryKey val id: String,
    val prompt: String,
    val imageUrl: String,
    val timestamp: Long,
    val style: String = "Default",
    val size: String = "1024x1024"
)"""
content = content.replace(old_entity, new_entity)

old_db_anno = "@Database(entities = [ConversationEntity::class, MessageEntity::class, GeneratedImageEntity::class], version = 7, exportSchema = false)"
new_db_anno = "@Database(entities = [ConversationEntity::class, MessageEntity::class, GeneratedImageEntity::class], version = 8, exportSchema = false)"
content = content.replace(old_db_anno, new_db_anno)

old_mig = """        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `generated_images` (`id` TEXT NOT NULL, `prompt` TEXT NOT NULL, `imageUrl` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }"""
new_mig = """        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `generated_images` (`id` TEXT NOT NULL, `prompt` TEXT NOT NULL, `imageUrl` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }
        }
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                safeAddColumn(db, "generated_images", "style", "TEXT NOT NULL DEFAULT 'Default'")
                safeAddColumn(db, "generated_images", "size", "TEXT NOT NULL DEFAULT '1024x1024'")
            }
        }"""
content = content.replace(old_mig, new_mig)

old_add = ".addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)"
new_add = ".addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)"
content = content.replace(old_add, new_add)

with open("app/src/main/java/com/example/Database.kt", "w") as f:
    f.write(content)
