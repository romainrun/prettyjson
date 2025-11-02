package re.weare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import re.weare.app.ui.components.RewardedAdHelper
import re.weare.app.ui.navigation.NavGraph
import re.weare.app.ui.theme.PrettyJSONTheme
import re.weare.app.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val rewardedAdHelper = RewardedAdHelper()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize AdMob
        com.google.android.gms.ads.MobileAds.initialize(this) {}
        
        setContent {
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
                    re.weare.app.ui.screens.IntroScreen(
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
                        }
                    )
                }
            }
        }
    }
}