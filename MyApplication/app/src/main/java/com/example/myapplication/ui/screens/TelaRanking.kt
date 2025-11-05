package com.example.mestredaspalavras.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mestredaspalavras.data.repository.RankingRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaRanking(
    navController: NavController,
    repository: RankingRepository
) {
    val rankingList by repository.rankingFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ranking") },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (rankingList.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma pontuação registrada.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    )
                }
            } else {
                itemsIndexed(rankingList) { index, item ->
                    val color = if (index == 0) Color(0xFFFFD700) else MaterialTheme.colorScheme.onBackground
                    val fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${index + 1}. ${item.nomeJogador}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = color,
                            fontWeight = fontWeight
                        )
                        Text(
                            text = "Ganhou na ${item.tentativas}º tentativas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = color,
                            fontWeight = fontWeight
                        )
                    }
                }
            }
        }
    }
}