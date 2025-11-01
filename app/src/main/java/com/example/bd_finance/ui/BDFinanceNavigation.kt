package com.example.bd_finance.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bd_finance.data.StockAnalysisRepository
import com.example.bd_finance.data.sync.StockMetricsSyncModule
import com.example.bd_finance.ui.portfolio.PortfolioScreen
import com.example.bd_finance.ui.portfolio.PortfolioViewModel
import com.example.bd_finance.ui.portfolio.PortfolioViewModelFactory
import com.example.bd_finance.ui.watchlist.WatchlistScreen
import com.example.bd_finance.ui.watchlist.WatchlistViewModel
import com.example.bd_finance.ui.watchlist.WatchlistViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Watchlist : Screen("watchlist", "Watchlist", Icons.Default.Star)
    object Portfolio : Screen("portfolio", "Portfolio", Icons.Default.AccountBalance)
}

@Composable
fun BDFinanceApp() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Set up repositories
    val database = remember { StockMetricsSyncModule.provideDatabase(context) }
    val watchlistRepository = remember { StockMetricsSyncModule.provideWatchlistRepository(database) }
    val portfolioRepository = remember { StockMetricsSyncModule.providePortfolioRepository(database) }
    val analysisRepository = remember { StockAnalysisRepository.default() }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                StockEvaluatorRoot(
                    watchlistRepository = watchlistRepository,
                    portfolioRepository = portfolioRepository
                )
            }

            composable(Screen.Watchlist.route) {
                val viewModel: WatchlistViewModel = viewModel(
                    factory = WatchlistViewModelFactory(watchlistRepository, analysisRepository)
                )
                WatchlistScreen(
                    viewModel = viewModel,
                    onNavigateToAnalysis = { ticker ->
                        // Navigate to home and trigger analysis
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        // TODO: Pass ticker to trigger analysis
                    }
                )
            }

            composable(Screen.Portfolio.route) {
                val viewModel: PortfolioViewModel = viewModel(
                    factory = PortfolioViewModelFactory(portfolioRepository, analysisRepository)
                )
                PortfolioScreen(
                    viewModel = viewModel,
                    onNavigateToAnalysis = { ticker ->
                        // Navigate to home and trigger analysis
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        // TODO: Pass ticker to trigger analysis
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        Screen.Home,
        Screen.Watchlist,
        Screen.Portfolio
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
