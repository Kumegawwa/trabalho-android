package com.example.mestredaspalavras.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mestredaspalavras.data.local.Palavra
import com.example.mestredaspalavras.data.repository.PalavraRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val palavras: List<Palavra> = emptyList(),
    val mensagem: String? = null
)

class AdminViewModel(private val repository: PalavraRepository) : ViewModel() {

    private val _mensagemFlow = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AdminUiState> = repository.todasPalavrasFlow
        .map { AdminUiState(it, _mensagemFlow.value) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AdminUiState()
        )

    private fun setMensagem(msg: String?) {
        _mensagemFlow.update { msg }
    }

    fun addPalavra(palavra: String) {
        if (palavra.isBlank()) {
            setMensagem("Palavra não pode estar em branco")
            return
        }
        if (palavra.length != 5) {
            setMensagem("Palavra deve ter 5 letras")
            return
        }

        viewModelScope.launch {
            try {
                repository.addPalavra(palavra)
                setMensagem("Palavra '$palavra' adicionada e sincronizada.")
            } catch (e: Exception) {
                setMensagem("Erro: ${e.message}")
            }
        }
    }

    fun updatePalavra(palavraAntiga: Palavra, palavraNova: String) {
         if (palavraNova.isBlank()) {
            setMensagem("Palavra não pode estar em branco")
            return
        }
        if (palavraNova.length != 5) {
            setMensagem("Palavra deve ter 5 letras")
            return
        }
        if (palavraAntiga.palavra == palavraNova.uppercase()) {
            setMensagem("A nova palavra é igual à antiga.")
            return
        }

        viewModelScope.launch {
            try {
                repository.updatePalavra(palavraAntiga, palavraNova)
                setMensagem("Palavra atualizada e sincronizada.")
            } catch (e: Exception) {
                setMensagem("Erro: ${e.message}")
            }
        }
    }

    fun deletePalavra(palavra: Palavra) {
        viewModelScope.launch {
            try {
                repository.deletePalavra(palavra)
                setMensagem("Palavra removida")
            } catch (e: Exception) {
                setMensagem("Erro: ${e.message}")
            }
        }
    }

    fun syncPalavras() {
        viewModelScope.launch {
            setMensagem("Sincronizando...")
            try {
                repository.syncFirebaseToRoom()
                setMensagem("Sincronização concluída")
            } catch (e: Exception) {
                setMensagem("Erro: ${e.message}")
            }
        }
    }

    fun clearMensagem() {
        setMensagem(null)
    }

    companion object {
        fun Factory(repository: PalavraRepository): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
                        return AdminViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}