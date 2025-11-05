package com.example.mestredaspalavras.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.navigation.NavController
import com.example.mestredaspalavras.data.local.Palavra
import com.example.mestredaspalavras.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaGerenciarPalavras(
    navController: NavController,
    viewModel: AdminViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var novaPalavra by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    var palavraParaExcluir by remember { mutableStateOf<Palavra?>(null) }
    var palavraParaEditar by remember { mutableStateOf<Palavra?>(null) }

    LaunchedEffect(key1 = true) {
        viewModel.uiState.collectLatest { state ->
            state.mensagem?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMensagem()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Gerenciar Palavras") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.syncPalavras() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sincronizar com Nuvem"
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = novaPalavra,
                    onValueChange = { novaPalavra = it },
                    label = { Text("Nova palavra (5 letras)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(onClick = {
                    viewModel.addPalavra(novaPalavra)
                    novaPalavra = ""
                }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar")
                }
            }

            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(uiState.palavras) { palavra ->
                    PalavraItem(
                        palavra = palavra,
                        onEdit = { palavraParaEditar = it },
                        onDelete = { palavraParaExcluir = it }
                    )
                }
            }
        }
    }

    palavraParaExcluir?.let { palavra ->
        AlertDialog(
            onDismissRequest = { palavraParaExcluir = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja mesmo excluir a palavra '${palavra.palavra}'? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePalavra(palavra)
                        palavraParaExcluir = null
                    }
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { palavraParaExcluir = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    palavraParaEditar?.let { palavra ->
        var palavraEditada by remember { mutableStateOf(palavra.palavra) }
        
        AlertDialog(
            onDismissRequest = { palavraParaEditar = null },
            title = { Text("Editar Palavra") },
            text = {
                OutlinedTextField(
                    value = palavraEditada,
                    onValueChange = { palavraEditada = it },
                    label = { Text("Palavra (5 letras)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updatePalavra(palavra, palavraEditada)
                        palavraParaEditar = null
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { palavraParaEditar = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun PalavraItem(
    palavra: Palavra,
    onEdit: (Palavra) -> Unit,
    onDelete: (Palavra) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEdit(palavra) },
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = palavra.palavra,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Editar",
                modifier = Modifier.clickable { onEdit(palavra) },
                tint = Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Deletar",
                modifier = Modifier.clickable { onDelete(palavra) },
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}