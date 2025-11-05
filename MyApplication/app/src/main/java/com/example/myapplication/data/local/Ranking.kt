package com.example.mestredaspalavras.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ranking")
data class Ranking(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nomeJogador: String,
    val tentativas: Int
)