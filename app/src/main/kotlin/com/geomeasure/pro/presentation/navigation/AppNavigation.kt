package com.geomeasure.pro.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.geomeasure.pro.presentation.screens.drive.DriveSettingsScreen
import com.geomeasure.pro.presentation.screens.map.MapScreen
import com.geomeasure.pro.presentation.screens.projects.ProjectListScreen
import com.geomeasure.pro.presentation.screens.tools.ToolsScreen
import com.geomeasure.pro.presentation.screens.settings.SettingsScreen

data class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val items = listOf(
        NavItem("map", Icons.Filled.Map, "Map"),
        NavItem("projects", Icons.Filled.Folder, "Projects"),
        NavItem("tools", Icons.Filled.Build, "Tools"),
        NavItem("drive", Icons.Filled.Cloud, "Drive"),
        NavItem("settings", Icons.Filled.Settings, "Settings")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val current by navController.currentBackStackEntryAsState()
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                        selected = current?.destination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        alwaysShowLabel = true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "map",
            modifier = Modifier.padding(padding)
        ) {
            composable("map") { MapScreen() }
            composable("projects") { ProjectListScreen() }
            composable("tools") { ToolsScreen() }
            composable("drive") { DriveSettingsScreen() }
            composable("settings") { SettingsScreen() }
        }
    }
}
