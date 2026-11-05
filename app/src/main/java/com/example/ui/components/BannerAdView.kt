package com.example.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

const val DEFAULT_BANNER_AD_UNIT_ID = "ca-app-pub-4133309600675996/4456667944"

/**
 * AdMob BannerAdView composable using AndroidView.
 * Configured with standard AdSize.BANNER (320x50), lifecycle release, and the user's Banner Ad Unit ID.
 */
@Composable
fun BannerAdView(
    modifier: Modifier = Modifier,
    adUnitId: String = DEFAULT_BANNER_AD_UNIT_ID
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surface)
            .testTag("banner_ad_container"),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .wrapContentHeight()
                .testTag("banner_ad_view"),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("BannerAdView", "AdMob Banner loaded successfully")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w("BannerAdView", "AdMob Banner failed to load: ${error.message} (code: ${error.code})")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            },
            onRelease = { adView ->
                try {
                    adView.destroy()
                } catch (e: Exception) {
                    Log.e("BannerAdView", "Error destroying AdView", e)
                }
            }
        )
    }
}
