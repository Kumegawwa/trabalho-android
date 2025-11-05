package com.example.mestredaspalavras.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mestredaspalavras.ui.viewmodel.AdminViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaAdminDashboard(
    navController: NavController,
    viewModel: AdminViewModel
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel Admin") },
                navigationIcon = {
                    IconButton(onClick = { 
                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("inicial") {
                            popUpTo("inicial") { inclusive = true }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Sair"
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
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { navController.navigate("gerenciar_palavras") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Gerenciar Palavras")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}