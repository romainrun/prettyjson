package com.prettyjson.android.ui.components

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.prettyjson.android.data.AdConstants

/**
 * Composable banner ad view
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdConstants.BANNER_AD_UNIT_ID
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}









