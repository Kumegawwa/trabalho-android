package com.example.mestredaspalavras.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Palavra::class, Ranking::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun palavraDao(): PalavraDao
    abstract fun rankingDao(): RankingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mestre_palavras_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}