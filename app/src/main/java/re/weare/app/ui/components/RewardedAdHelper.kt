package re.weare.app.ui.components

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import re.weare.app.data.AdConstants

/**
 * Helper class for managing rewarded ads
 */
class RewardedAdHelper {
    private var rewardedAd: RewardedAd? = null
    
    fun loadRewardedAd(activity: Activity, onAdLoaded: () -> Unit, onAdFailedToLoad: (String) -> Unit) {
        val adRequest = AdRequest.Builder().build()
        
        RewardedAd.load(
            activity,
            AdConstants.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    onAdLoaded()
                }
                
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    onAdFailedToLoad(error.message)
                }
            }
        )
    }
    
    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit,
        onAdFailedToShow: (String) -> Unit
    ) {
        rewardedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                onUserEarnedReward()
                rewardedAd = null
                loadRewardedAd(activity, {}, {}) // Reload ad
            }
        } ?: run {
            onAdFailedToShow("Ad not loaded")
        }
    }
}



