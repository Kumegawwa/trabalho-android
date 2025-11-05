package com.example.mestredaspalavras.data.repository

import android.util.Log
import com.example.mestredaspalavras.data.local.Palavra
import com.example.mestredaspalavras.data.local.PalavraDao
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class PalavraRepository(private val palavraDao: PalavraDao) {

    private val firestore = Firebase.firestore
    private val firestoreCollection = firestore.collection("palavras")

    val todasPalavrasFlow: Flow<List<Palavra>> = palavraDao.getAllFlow()

    suspend fun getPalavraAleatoria(): Palavra? {
        return palavraDao.getRandomWord()
    }

    suspend fun syncFirebaseToRoom() {
        try {
            val snapshot = firestoreCollection.get().await()
            val palavrasFirebase = snapshot.documents.mapNotNull {
                it.data?.get("palavra")?.toString()
            }

            palavraDao.nukeTable()
            palavrasFirebase.forEach {
                palavraDao.insert(Palavra(palavra = it.uppercase()))
            }
        } catch (e: Exception) {
            Log.e("PalavraRepository", "Erro ao sincronizar Firebase", e)
            throw e
        }
    }

    suspend fun addPalavra(palavra: String) {
        val palavraUpper = palavra.uppercase()
        val doc = firestoreCollection.document(palavraUpper)
        val data = hashMapOf("palavra" to palavraUpper)

        try {
            doc.set(data).await()
            palavraDao.insert(Palavra(palavra = palavraUpper))
        } catch (e: Exception) {
            Log.e("PalavraRepository", "Erro ao adicionar palavra", e)
            throw e
        }
    }

    suspend fun deletePalavra(palavra: Palavra) {
        val palavraUpper = palavra.palavra.uppercase()
        val doc = firestoreCollection.document(palavraUpper)

        try {
            doc.delete().await()
            val localWord = palavraDao.getWordByString(palavraUpper)
            localWord?.let {
                palavraDao.delete(it)
            }
        } catch (e: Exception) {
            Log.e("PalavraRepository", "Erro ao deletar palavra", e)
            throw e
        }
    }
}