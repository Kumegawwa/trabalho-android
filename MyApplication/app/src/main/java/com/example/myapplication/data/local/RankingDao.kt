package com.example.mestredaspalavras.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RankingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ranking: Ranking)

    @Update
    suspend fun update(ranking: Ranking)

    @Delete
    suspend fun delete(ranking: Ranking)

    @Query("SELECT * FROM ranking ORDER BY tentativas ASC LIMIT 10")
    fun getAllFlow(): Flow<List<Ranking>>
}