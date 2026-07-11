package com.mitron.connect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mitron.connect.data.model.ChatMessage
import com.mitron.connect.data.model.ChatPreview

@Database(entities = [ChatMessage::class, ChatPreview::class], version = 2, exportSchema = false)
abstract class MitronDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: MitronDatabase? = null

        // Migration from v1 to v2: add senderId column to chat_messages
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE chat_messages ADD COLUMN senderId TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): MitronDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MitronDatabase::class.java,
                    "mitron_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
