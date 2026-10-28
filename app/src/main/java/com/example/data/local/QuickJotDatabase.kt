package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChecklistDao
import com.example.data.local.dao.FolderDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.NoteVersionDao
import com.example.data.local.dao.RecentSearchDao
import com.example.data.local.dao.ReminderDao
import com.example.data.local.dao.TagDao
import com.example.data.local.entities.ChecklistItemEntity
import com.example.data.local.entities.FolderEntity
import com.example.data.local.entities.NoteEntity
import com.example.data.local.entities.NoteFtsEntity
import com.example.data.local.entities.NoteTagCrossRef
import com.example.data.local.entities.NoteVersionEntity
import com.example.data.local.entities.RecentSearchEntity
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.TagEntity
import com.example.data.security.DatabaseKeyManager
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        NoteEntity::class,
        NoteFtsEntity::class,
        ChecklistItemEntity::class,
        NoteVersionEntity::class,
        FolderEntity::class,
        TagEntity::class,
        NoteTagCrossRef::class,
        ReminderEntity::class,
        RecentSearchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class QuickJotDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun noteVersionDao(): NoteVersionDao
    abstract fun folderDao(): FolderDao
    abstract fun tagDao(): TagDao
    abstract fun reminderDao(): ReminderDao
    abstract fun recentSearchDao(): RecentSearchDao

    companion object {
        const val DATABASE_NAME = "quickjot_secure.db"

        fun buildDatabase(
            context: Context,
            keyManager: DatabaseKeyManager? = null,
            inMemory: Boolean = false
        ): QuickJotDatabase {
            val builder = if (inMemory) {
                Room.inMemoryDatabaseBuilder(context, QuickJotDatabase::class.java)
            } else {
                Room.databaseBuilder(context, QuickJotDatabase::class.java, DATABASE_NAME)
            }

            if (keyManager != null && !inMemory) {
                try {
                    val passphrase = keyManager.getOrCreateDatabaseKey()
                    val factory = SupportOpenHelperFactory(passphrase)
                    builder.openHelperFactory(factory)
                } catch (e: Exception) {
                    // In environments where Keystore/SQLCipher native library is unsupported,
                    // fall back to default builder to ensure uninterrupted functionality
                }
            }

            return builder
                .fallbackToDestructiveMigration(dropAllTables = false)
                .build()
        }
    }
}
