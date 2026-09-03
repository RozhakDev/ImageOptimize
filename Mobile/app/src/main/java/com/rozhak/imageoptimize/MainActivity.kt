package com.rozhak.imageoptimize

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rozhak.imageoptimize.core.designsystem.theme.ImageOptimizeTheme
import com.rozhak.imageoptimize.ui.history.HistoryScreen
import com.rozhak.imageoptimize.ui.optimization.OptimizationScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ImageOptimizeTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.height(72.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp
                        ) {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination

                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.Home, contentDescription = stringResource(R.string.nav_optimizer)) },
                                label = { Text(stringResource(R.string.nav_optimizer)) },
                                selected = currentDestination?.hierarchy?.any { it.route == "optimization" } == true,
                                onClick = {
                                    navController.navigate("optimization") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )

                            NavigationBarItem(
                                icon = { Icon(Icons.Filled.DateRange, contentDescription = stringResource(R.string.nav_history)) },
                                label = { Text(stringResource(R.string.nav_history)) },
                                selected = currentDestination?.hierarchy?.any { it.route == "history" } == true,
                                onClick = {
                                    navController.navigate("history") {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = "optimization"
                        ) {
                            composable("optimization") { OptimizationScreen() }
                            composable("history") { HistoryScreen() }
                        }
                    }
                }
            }
        }
    }
}
