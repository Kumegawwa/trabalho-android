package com.example.mestredaspalavras.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mestredaspalavras.ui.viewmodel.CorLetra
import com.example.mestredaspalavras.ui.viewmodel.JogoStatus
import com.example.mestredaspalavras.ui.viewmodel.JogoViewModel
import com.example.mestredaspalavras.ui.viewmodel.TentativaComResultado
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaJogo(
    navController: NavController,
    viewModel: JogoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var nomeJogador by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = viewModel) {
        viewModel.uiState.collectLatest { state ->
            state.mensagemErro?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMensagemErro()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mestre das Palavras") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            GradeJogo(
                tentativas = uiState.tentativas,
                maxTentativas = uiState.maxTentativas,
                tamanhoPalavra = uiState.tamanhoPalavra
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.status == JogoStatus.JOGANDO) {
                if (uiState.tentativas.isEmpty()) {
                    OutlinedTextField(
                        value = nomeJogador,
                        onValueChange = { nomeJogador = it },
                        label = { Text("Seu nome (para o ranking)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = uiState.mensagemErro?.contains("nome") == true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                TextField(
                    value = uiState.tentativaAtual,
                    onValueChange = viewModel::onTentativaChange,
                    label = { Text("Sua tentativa") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.mensagemErro != null && uiState.mensagemErro?.contains("nome") == false
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.enviarTentativa(nomeJogador) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Enviar")
                }
            } else {
                ResultadoJogo(status = uiState.status, palavraSecreta = uiState.palavraSecreta)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        nomeJogador = ""
                        viewModel.novoJogo()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Novo Jogo")
                }
            }
        }
    }
}

@Composable
private fun GradeJogo(
    tentativas: List<TentativaComResultado>,
    maxTentativas: Int,
    tamanhoPalavra: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(maxTentativas) { index ->
            val tentativa = tentativas.getOrNull(index)
            LinhaTentativa(
                tentativa = tentativa,
                tamanhoPalavra = tamanhoPalavra
            )
        }
    }
}

@Composable
private fun LinhaTentativa(
    tentativa: TentativaComResultado?,
    tamanhoPalavra: Int
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(tamanhoPalavra) { index ->
            val char = tentativa?.palavra?.getOrNull(index)
            val corLetra = tentativa?.cores?.getOrNull(index) ?: CorLetra.VAZIA
            
            val cor = when (corLetra) {
                CorLetra.VAZIA -> Color.LightGray.copy(alpha = 0.3f)
                CorLetra.CORRETA -> Color(0xFF4CAF50)
                CorLetra.LUGAR_ERRADO -> Color(0xFFFFEB3B)
                CorLetra.INCORRETA -> Color.Gray
            }
            CaixaLetra(char = char, color = cor)
        }
    }
}

@Composable
private fun CaixaLetra(char: Char?, color: Color) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(color)
            .border(1.dp, Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char?.toString() ?: "",
            fontSize = 24.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun ResultadoJogo(status: JogoStatus, palavraSecreta: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when (status) {
            JogoStatus.VITORIA -> {
                Text(
                    text = "Você Venceu!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF4CAF50)
                )
            }
            JogoStatus.DERROTA -> {
                Text(
                    text = "Você Perdeu!",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A palavra era: $palavraSecreta",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            JogoStatus.JOGANDO -> {}
        }
    }
}