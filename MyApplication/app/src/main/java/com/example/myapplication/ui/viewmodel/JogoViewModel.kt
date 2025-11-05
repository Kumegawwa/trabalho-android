package com.example.mestredaspalavras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mestredaspalavras.data.local.Ranking
import com.example.mestredaspalavras.data.repository.PalavraRepository
import com.example.mestredaspalavras.data.repository.RankingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class JogoStatus { JOGANDO, VITORIA, DERROTA }

data class JogoUiState(
    val palavraSecreta: String = "",
    val tentativas: List<String> = emptyList(),
    val tentativaAtual: String = "",
    val status: JogoStatus = JogoStatus.JOGANDO,
    val maxTentativas: Int = 6,
    val tamanhoPalavra: Int = 5
)

class JogoViewModel(
    private val palavraRepository: PalavraRepository,
    private val rankingRepository: RankingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JogoUiState())
    val uiState: StateFlow<JogoUiState> = _uiState.asStateFlow()

    init {
        novoJogo()
    }

    fun novoJogo() {
        viewModelScope.launch {
            palavraRepository.syncFirebaseToRoom()
            val novaPalavra = palavraRepository.getPalavraAleatoria()?.palavra ?: "ANDROID"

            _uiState.update {
                JogoUiState(
                    palavraSecreta = novaPalavra.uppercase(),
                    tamanhoPalavra = novaPalavra.length
                )
            }
        }
    }

    fun onTentativaChange(novaTentativa: String) {
        if (novaTentativa.length <= _uiState.value.tamanhoPalavra) {
            _uiState.update {
                it.copy(tentativaAtual = novaTentativa.uppercase())
            }
        }
    }

    fun enviarTentativa(nomeJogador: String) {
        val state = _uiState.value
        if (state.tentativaAtual.length != state.tamanhoPalavra) return
        if (state.status != JogoStatus.JOGANDO) return

        val novasTentativas = state.tentativas + state.tentativaAtual

        if (state.tentativaAtual == state.palavraSecreta) {
            _uiState.update {
                it.copy(
                    tentativas = novasTentativas,
                    status = JogoStatus.VITORIA,
                    tentativaAtual = ""
                )
            }
            salvarRanking(nomeJogador.ifBlank { "Jogador" }, novasTentativas.size)
        } else if (novasTentativas.size >= state.maxTentativas) {
            _uiState.update {
                it.copy(
                    tentativas = novasTentativas,
                    status = JogoStatus.DERROTA,
                    tentativaAtual = ""
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    tentativas = novasTentativas,
                    tentativaAtual = ""
                )
            }
        }
    }

    private fun salvarRanking(nome: String, tentativas: Int) {
        viewModelScope.launch {
            rankingRepository.insert(Ranking(nomeJogador = nome, tentativas = tentativas))
        }
    }

    companion object {
        fun Factory(
            palavraRepo: PalavraRepository,
            rankingRepo: RankingRepository
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(JogoViewModel::class.java)) {
                        return JogoViewModel(palavraRepo, rankingRepo) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}