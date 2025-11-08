package com.prettyjson.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel
import com.prettyjson.android.data.billing.ProManager
import com.prettyjson.android.ui.components.RewardedAdHelper
import com.prettyjson.android.ui.navigation.NavGraph
import com.prettyjson.android.ui.theme.PrettyJSONTheme
import com.prettyjson.android.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val rewardedAdHelper = RewardedAdHelper()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Explicitly disable edge-to-edge to prevent app content from drawing behind system bars
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, true)
        
        // Initialize AdMob
        com.google.android.gms.ads.MobileAds.initialize(this) {}
        
        setContent {
            // Initialize ProManager billing
            val proManager: ProManager = org.koin.androidx.compose.get()
            LaunchedEffect(Unit) {
                proManager.initializeBilling { ready ->
                    if (ready) {
                        // Billing initialized successfully
                    }
                }
            }
            
            val navController = rememberNavController()
            val context = LocalContext.current
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val themePreference by settingsViewModel.theme.collectAsState(initial = "system")
            val themeStyle by settingsViewModel.themeStyle.collectAsState(initial = "default")
            var showIntro by remember { mutableStateOf(false) }
            
            // Check if intro should be shown
            val hasSeenIntro by settingsViewModel.hasSeenIntro.collectAsState(initial = false)
            LaunchedEffect(hasSeenIntro) {
                showIntro = !hasSeenIntro
            }
            
            // Load rewarded ad on app start
            LaunchedEffect(Unit) {
                rewardedAdHelper.loadRewardedAd(
                    activity = this@MainActivity,
                    onAdLoaded = {},
                    onAdFailedToLoad = {}
                )
            }
            
            PrettyJSONTheme(themePreference = themePreference, themeStyle = themeStyle) {
                val scope = rememberCoroutineScope()
                if (showIntro) {
                    com.prettyjson.android.ui.screens.IntroScreen(
                        onComplete = {
                            scope.launch {
                                settingsViewModel.setHasSeenIntro(true)
                            }
                            showIntro = false
                        }
                    )
                } else {
                    NavGraph(
                        navController = navController,
                        onSupportUs = {
                            rewardedAdHelper.showRewardedAd(
                                activity = this@MainActivity,
                                onUserEarnedReward = {
                                    // Show thank you message
                                },
                                onAdDismissed = {},
                                onAdFailedToShow = { error ->
                                    // Show error message
                                }
                            )
                        },
                        onShowRewardedAd = { onRewardEarned, onAdFailed ->
                            rewardedAdHelper.showRewardedAd(
                                activity = this@MainActivity,
                                onUserEarnedReward = onRewardEarned,
                                onAdDismissed = {
                                    // User dismissed ad without completing - don't proceed
                                    onAdFailed("Please watch the ad to export. Export cancelled.")
                                },
                                onAdFailedToShow = { error ->
                                    // If ad fails, try to reload and show a message
                                    onAdFailed("Ad failed to load. Please try again or check your connection.")
                                    // Reload ad for next time
                                    scope.launch {
                                        rewardedAdHelper.loadRewardedAd(
                                            activity = this@MainActivity,
                                            onAdLoaded = {},
                                            onAdFailedToLoad = {}
                                        )
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}