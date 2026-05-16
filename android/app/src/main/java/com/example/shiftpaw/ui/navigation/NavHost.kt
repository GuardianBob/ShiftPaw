package com.example.shiftpaw.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.shiftpaw.ui.screens.calendar.CalendarScreen
import com.example.shiftpaw.ui.screens.employees.EmployeesScreen
import com.example.shiftpaw.ui.screens.importschedule.ImportScheduleScreen
import com.example.shiftpaw.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Calendar : Screen("calendar", "Calendar", Icons.Filled.CalendarMonth)
    object Employees : Screen("employees", "Employees", Icons.Filled.People)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

// Non-tab route constant
object ImportRoute {
    const val ROUTE = "import"
}

private val bottomNavItems = listOf(Screen.Calendar, Screen.Employees, Screen.Settings)

@Composable
fun ShiftPawNavHost(
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {}
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { ShiftPawBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Calendar.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Calendar.route) {
                CalendarScreen(onNavigateToImport = { navController.navigate(ImportRoute.ROUTE) })
            }
            composable(Screen.Employees.route) { EmployeesScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToImport = { navController.navigate(ImportRoute.ROUTE) },
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = onToggleDarkMode
                )
            }
            composable(ImportRoute.ROUTE) {
                ImportScheduleScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToCalendar = {
                        navController.navigate(Screen.Calendar.route) {
                            popUpTo(ImportRoute.ROUTE) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ShiftPawBottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) }
            )
        }
    }
}
