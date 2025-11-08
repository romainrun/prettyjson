package com.prettyjson.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.prettyjson.android.ui.screens.*

/**
 * Navigation graph for the app
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = NavigationRoutes.MAIN,
    onSupportUs: () -> Unit,
    onShowRewardedAd: (onRewardEarned: () -> Unit, onAdFailed: (String) -> Unit) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationRoutes.MAIN) {
            MainScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                },
                onShowRewardedAd = onShowRewardedAd
            )
        }
        
        composable(NavigationRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onSupportUs = onSupportUs,
                onNavigateToUpgrade = {
                    navController.navigate(NavigationRoutes.UPGRADE_TO_PRO)
                }
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
                initialJsonContent = jsonContent,
                onShowRewardedAd = onShowRewardedAd
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
                    // Navigate to main screen with the selected object content
                    val jsonContent = android.net.Uri.encode(reusableObject.content)
                    navController.navigate("${NavigationRoutes.MAIN}?jsonContent=$jsonContent") {
                        popUpTo(NavigationRoutes.MAIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(NavigationRoutes.DATA_BUCKETS) {
            DataBucketsScreen(
                onNavigateBack = { navController.popBackStack() },
                onBucketSelected = { dataBucket ->
                    // Navigate to main screen with the selected bucket content
                    val jsonContent = android.net.Uri.encode(dataBucket.value)
                    navController.navigate("${NavigationRoutes.MAIN}?jsonContent=$jsonContent") {
                        popUpTo(NavigationRoutes.MAIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(NavigationRoutes.HELP) {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(NavigationRoutes.UPGRADE_TO_PRO) {
            UpgradeToProScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

