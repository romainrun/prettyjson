package re.weare.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import re.weare.app.ui.screens.*

/**
 * Navigation graph for the app
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = NavigationRoutes.MAIN,
    onSupportUs: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationRoutes.MAIN) {
            MainScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }
        
        composable(NavigationRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSupportUs = onSupportUs
            )
        }
        
        composable("${NavigationRoutes.SAVED_JSONS}?jsonContent={jsonContent}") { backStackEntry ->
            SavedJsonScreen(
                onNavigateBack = { navController.popBackStack() },
                onJsonSelected = { savedJson ->
                    // Navigate to main screen with the selected JSON
                    navController.navigate("${NavigationRoutes.MAIN}?jsonContent=${android.net.Uri.encode(savedJson.content)}") {
                        popUpTo(NavigationRoutes.MAIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable("${NavigationRoutes.MAIN}?jsonContent={jsonContent}") { backStackEntry ->
            val jsonContent = backStackEntry.arguments?.getString("jsonContent")?.let { 
                android.net.Uri.decode(it)
            }
            MainScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                initialJsonContent = jsonContent
            )
        }
        
        composable(NavigationRoutes.JSON_BUILDER) {
            JsonBuilderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(NavigationRoutes.REUSABLE_OBJECTS) {
            ReusableObjectScreen(
                onNavigateBack = { navController.popBackStack() },
                onObjectSelected = { reusableObject ->
                    // Insert object into current JSON
                    navController.popBackStack()
                    // TODO: Pass object to main screen
                }
            )
        }
        
        composable(NavigationRoutes.HELP) {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

