package com.example.mestredaspalavras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mestredaspalavras.data.local.AppDatabase
import com.example.mestredaspalavras.data.repository.PalavraRepository
import com.example.mestredaspalavras.data.repository.RankingRepository
import com.example.mestredaspalavras.ui.screens.TelaAdminDashboard
import com.example.mestredaspalavras.ui.screens.TelaGerenciarPalavras
import com.example.mestredaspalavras.ui.screens.TelaInicial
import com.example.mestredaspalavras.ui.screens.TelaJogo
import com.example.mestredaspalavras.ui.screens.TelaLoginAdmin
import com.example.mestredaspalavras.ui.screens.TelaRanking
import com.example.mestredaspalavras.ui.theme.MestreDasPalavrasTheme
import com.example.mestredaspalavras.ui.viewmodel.AdminViewModel
import com.example.mestredaspalavras.ui.viewmodel.JogoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MestreDasPalavrasTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    val palavraRepository = PalavraRepository(db.palavraDao())
    val rankingRepository = RankingRepository(db.rankingDao())

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "inicial"
    ) {
        composable("inicial") {
            TelaInicial(navController = navController)
        }

        composable("jogo") {
            val viewModel: JogoViewModel = viewModel(
                factory = JogoViewModel.Factory(palavraRepository, rankingRepository)
            )
            TelaJogo(navController = navController, viewModel = viewModel)
        }

        composable("ranking") {
            TelaRanking(
                navController = navController,
                repository = rankingRepository
            )
        }

        composable("login_admin") {
            TelaLoginAdmin(navController = navController)
        }

        composable("admin_dashboard") {
            val viewModel: AdminViewModel = viewModel(
                factory = AdminViewModel.Factory(palavraRepository)
            )
            TelaAdminDashboard(navController = navController, viewModel = viewModel)
        }

        composable("gerenciar_palavras") {
            val viewModel: AdminViewModel = viewModel(
                factory = AdminViewModel.Factory(palavraRepository)
            )
            TelaGerenciarPalavras(navController = navController, viewModel = viewModel)
        }
    }
}