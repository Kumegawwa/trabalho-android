package com.example.mestredaspalavras.data.repository

import com.example.mestredaspalavras.data.local.Ranking
import com.example.mestredaspalavras.data.local.RankingDao
import kotlinx.coroutines.flow.Flow

class RankingRepository(private val rankingDao: RankingDao) {

    val rankingFlow: Flow<List<Ranking>> = rankingDao.getAllFlow()

    suspend fun insert(ranking: Ranking) {
        rankingDao.insert(ranking)
    }

    suspend fun update(ranking: Ranking) {
        rankingDao.update(ranking)
    }

    suspend fun delete(ranking: Ranking) {
        rankingDao.delete(ranking)
    }
}