# AdMob Ad Units Required for JSON Viewer & Editor

## Overview

Your app uses **2 types of AdMob ad units**:

1. **Banner Ad** - Displayed at the bottom of the main screen
2. **Rewarded Video Ad** - Shown before export actions (PDF, TXT, Share)

## Required Ad Units

### 1. Banner Ad Unit ⚠️ REQUIRED

**Type:** Banner Ad
**Purpose:** Shows a small banner ad at the bottom of the main screen (for free users only - hidden for Pro users)

**Where it's used:**
- `app/src/main/java/com/prettyjson/android/ui/components/BannerAdView.kt`
- Displayed in `MainScreen.kt` at the bottom bar (when user is not premium)

**Ad Unit ID Format:**
```
ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
```

**Current (Test):**
```
ca-app-pub-3940256099942544/6300978111
```

---

### 2. Rewarded Video Ad Unit ⚠️ REQUIRED

**Type:** Rewarded Ad
**Purpose:** Video ads users must watch to unlock export features (PDF, TXT, Share)

**Where it's used:**
- `app/src/main/java/com/prettyjson/android/ui/components/RewardedAdHelper.kt`
- Triggered before:
  - Export as PDF
  - Export as TXT File
  - Share as Text
  - (Free users only - Pro users skip ads)

**Ad Unit ID Format:**
```
ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX
```

**Current (Test):**
```
ca-app-pub-3940256099942544/5224354917
```

---

### 3. AdMob App ID ⚠️ REQUIRED

**Type:** Application ID
**Purpose:** Your AdMob app identifier (one per app)

**Where it's used:**
- `app/src/main/AndroidManifest.xml` (meta-data tag)

**App ID Format:**
```
ca-app-pub-XXXXXXXXXXXXXXXX~XXXXXXXXXX
```

**Current (Test):**
```
ca-app-pub-3940256099942544~3347511713
```

---

## How to Create Ad Units in AdMob

1. **Go to Google AdMob Console:**
   - Visit https://apps.admob.com/
   - Log in with your Google account

2. **Create Your App:**
   - Click "Apps" → "Add app"
   - Select "Android"
   - Enter app name: "JSON Viewer & Editor"
   - Package name: `com.prettyjson.android`
   - Click "Add app"
   - **Save the App ID** - You'll get something like: `ca-app-pub-1234567890123456~1234567890`

3. **Create Banner Ad Unit:**
   - In your app's page, click "Ad units" → "Get started"
   - Select "Banner"
   - Ad unit name: "Main Screen Banner"
   - Click "Create ad unit"
   - **Copy the Ad Unit ID** (format: `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX`)

4. **Create Rewarded Ad Unit:**
   - Still in "Ad units", click "Add ad unit"
   - Select "Rewarded"
   - Ad unit name: "Export Rewarded Video"
   - Click "Create ad unit"
   - **Copy the Ad Unit ID** (format: `ca-app-pub-XXXXXXXXXXXXXXXX/XXXXXXXXXX`)

---

## How to Replace Test IDs with Production IDs

Once you have your production AdMob IDs, replace them in:

### File: `app/src/main/java/com/prettyjson/android/data/AdConstants.kt`

```kotlin
object AdConstants {
    // Replace with your production ad unit IDs
    const val BANNER_AD_UNIT_ID = "ca-app-pub-YOUR-PUBLISHER-ID/YOUR-BANNER-ID"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-YOUR-PUBLISHER-ID/YOUR-REWARDED-ID"
}
```

### File: `app/src/main/AndroidManifest.xml`

```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="ca-app-pub-YOUR-PUBLISHER-ID~YOUR-APP-ID" />
```

---

## Important Notes

### Ad Placement Strategy

1. **Banner Ad:**
   - Shows only for free users (hidden for Pro users)
   - Located at bottom of main screen
   - Non-intrusive, always visible during JSON editing

2. **Rewarded Ad:**
   - Only shown before export actions
   - Users must watch the video to proceed
   - Free users: Required to watch ad
   - Pro users: Ad is skipped automatically

### Monetization Flow

- **Free Users:**
  - Banner ad always visible at bottom
  - Rewarded video required for export actions
  
- **Pro Users ($4.99 one-time purchase):**
  - No banner ads
  - No rewarded ads (export actions work immediately)
  - Ad-free experience

### Testing

- Current setup uses Google's test ad units (safe for development)
- Test ads will show "Test Ad" labels
- Production ads will show real advertisements
- Test your production ad units in a test environment first

---

## Checklist Before Publishing

- [ ] Create AdMob account
- [ ] Create AdMob app and get App ID
- [ ] Create Banner ad unit
- [ ] Create Rewarded ad unit
- [ ] Replace test IDs with production IDs in `AdConstants.kt`
- [ ] Replace test App ID in `AndroidManifest.xml`
- [ ] Test ads in debug build
- [ ] Verify ads don't show for Pro users (premium check works)
- [ ] Submit app to Google Play

---

## Revenue Optimization Tips

1. **Banner Ads:**
   - Always visible = consistent impressions
   - Consider adaptive banner sizes for better CPM

2. **Rewarded Ads:**
   - Higher eCPM than banners
   - Users more engaged (watching video)
   - Only show when user wants to export (high intent)

3. **Pro Upgrade:**
   - Removes all ads
   - $4.99 one-time purchase
   - Users can test app with ads, then upgrade if they like it

---

## Support

If you need help:
- AdMob Help: https://support.google.com/admob
- AdMob Policies: https://support.google.com/admob/answer/6128543



