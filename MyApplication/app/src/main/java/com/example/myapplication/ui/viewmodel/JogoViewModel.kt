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

enum class CorLetra {
    VAZIA,
    INCORRETA,
    LUGAR_ERRADO,
    CORRETA
}

data class TentativaComResultado(
    val palavra: String,
    val cores: List<CorLetra>
)

data class JogoUiState(
    val palavraSecreta: String = "",
    val tentativas: List<TentativaComResultado> = emptyList(),
    val tentativaAtual: String = "",
    val status: JogoStatus = JogoStatus.JOGANDO,
    val maxTentativas: Int = 6,
    val tamanhoPalavra: Int = 5,
    val mensagemErro: String? = null
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
                    tamanhoPalavra = novaPalavra.length,
                    maxTentativas = 6,
                    tentativas = emptyList(),
                    status = JogoStatus.JOGANDO,
                    tentativaAtual = ""
                )
            }
        }
    }

    fun onTentativaChange(novaTentativa: String) {
        if (novaTentativa.length <= _uiState.value.tamanhoPalavra) {
            _uiState.update {
                it.copy(
                    tentativaAtual = novaTentativa.uppercase(),
                    mensagemErro = null
                )
            }
        }
    }

    private fun calcularCores(tentativa: String, secreta: String): List<CorLetra> {
        val cores = MutableList(secreta.length) { CorLetra.INCORRETA }
        val secretaChars = secreta.toMutableList()

        for (i in secreta.indices) {
            if (tentativa[i] == secreta[i]) {
                cores[i] = CorLetra.CORRETA
                secretaChars[i] = ' '
            }
        }

        for (i in secreta.indices) {
            if (cores[i] == CorLetra.CORRETA) continue
            if (secretaChars.contains(tentativa[i])) {
                cores[i] = CorLetra.LUGAR_ERRADO
                secretaChars[secretaChars.indexOf(tentativa[i])] = ' '
            }
        }
        return cores
    }

    fun enviarTentativa(nomeJogador: String) {
        val state = _uiState.value
        val palavraTentativa = state.tentativaAtual.trim()

        if (state.status != JogoStatus.JOGANDO) return

        if (state.tentativas.isEmpty() && nomeJogador.isBlank()) {
            _uiState.update { it.copy(mensagemErro = "Por favor, insira seu nome para o ranking.") }
            return
        }

        if (palavraTentativa.length != state.tamanhoPalavra) {
            _uiState.update { it.copy(mensagemErro = "A palavra deve ter exatamente ${state.tamanhoPalavra} letras.") }
            return
        }

        if (state.tentativas.any { it.palavra == palavraTentativa }) {
            _uiState.update { it.copy(mensagemErro = "Você já tentou esta palavra.") }
            return
        }

        if (palavraTentativa.all { it == palavraTentativa[0] }) {
            _uiState.update { it.copy(mensagemErro = "Não é permitido enviar palavras com todas as letras iguais.") }
            return
        }

        val cores = calcularCores(palavraTentativa, state.palavraSecreta)
        val novaTentativa = TentativaComResultado(palavraTentativa, cores)
        val novasTentativas = state.tentativas + novaTentativa

        if (palavraTentativa == state.palavraSecreta) {
            _uiState.update {
                it.copy(
                    tentativas = novasTentativas,
                    status = JogoStatus.VITORIA,
                    tentativaAtual = "",
                    mensagemErro = null
                )
            }
            salvarRanking(nomeJogador.ifBlank { "Jogador" }, novasTentativas.size)
        } else if (novasTentativas.size >= state.maxTentativas) {
            _uiState.update {
                it.copy(
                    tentativas = novasTentativas,
                    status = JogoStatus.DERROTA,
                    tentativaAtual = "",
                    mensagemErro = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    tentativas = novasTentativas,
                    tentativaAtual = "",
                    mensagemErro = null
                )
            }
        }
    }

    fun clearMensagemErro() {
        _uiState.update { it.copy(mensagemErro = null) }
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