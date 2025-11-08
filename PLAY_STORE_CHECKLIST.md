# Play Store Submission Checklist

## ✅ What You Have

### 1. **App Configuration** ✅
- ✅ Package ID: `com.prettyjson.android`
- ✅ App Name: "JSON Viewer & Editor"
- ✅ Version Code: 1
- ✅ Version Name: "1.0"
- ✅ Target SDK: 35 (Android 15)
- ✅ Min SDK: 24 (Android 7.0)
- ✅ Signing: Release keystore configured (`json-viewer-release-key.jks`)

### 2. **App Assets** ✅
- ✅ App Icon: Launcher icons in all densities (hdpi, mdpi, xhdpi, xxhdpi, xxxhdpi)
- ✅ Adaptive Icons: Configured for Android 8.0+
- ✅ Round Icon: Available
- ✅ Play Store Icon: `ic_launcher-playstore.png` exists

### 3. **Technical Requirements** ✅
- ✅ Manifest configured correctly
- ✅ Permissions declared properly
- ✅ File Provider configured
- ✅ Backup rules configured
- ✅ Data extraction rules configured

### 4. **Content** ✅
- ✅ App description available (`APP_DESCRIPTION_FOR_MARKETING.md`)
- ✅ Feature list documented
- ✅ Marketing materials prepared

---

## ❌ What You're Missing

### 1. **App Bundle (AAB)** ❌ **CRITICAL**
**Status:** You're currently building APK, but Play Store requires AAB (Android App Bundle)

**Action Required:**
```bash
# Build AAB instead of APK
./gradlew bundleRelease
```

**Location:** `app/build/outputs/bundle/release/app-release.aab`

**Note:** You'll need to update your Fastlane configuration to build AAB for Play Store.

---

### 2. **Privacy Policy URL** ❌ **REQUIRED**
**Status:** Not configured

**Action Required:**
- Create a privacy policy webpage
- Host it online (GitHub Pages, your website, etc.)
- Add the URL in Play Console → App Content → Privacy Policy

**What to Include:**
- Data collection practices
- AdMob usage (you're using AdMob)
- Data storage (Room database, DataStore)
- Permissions usage (Internet, Storage)
- Third-party services (AdMob, Google services)

---

### 3. **Content Rating** ❌ **REQUIRED**
**Status:** Not completed

**Action Required:**
- Go to Play Console → App Content → Content Rating
- Complete the questionnaire
- Get rating certificate (usually "Everyone" for developer tools)

---

### 4. **Store Listing Assets** ❌ **REQUIRED**

#### Screenshots (Required)
- **Phone screenshots:** At least 2, up to 8
  - Minimum: 320px height
  - Maximum: 3840px height
  - Recommended: 1080x1920px (portrait) or 1920x1080px (landscape)
  
**Suggested Screenshots:**
1. Main editor with formatted JSON
2. Form editor interface
3. Dark theme view
4. Settings screen
5. Data Buckets (Pro feature)
6. Export options
7. Search & Replace dialog
8. Help/Guide screen

#### Feature Graphic (Required)
- **Size:** 1024 x 500 pixels
- **Format:** PNG or JPG (24-bit PNG recommended)
- **Content:** App name, tagline, key visual

#### Promotional Graphic (Optional but Recommended)
- **Size:** 180 x 120 pixels
- **Format:** PNG or JPG

---

### 5. **Store Listing Text** ⚠️ **NEEDS REVIEW**

#### Short Description (Required)
- **Max:** 80 characters
- **Current suggestion:** "Professional JSON editor: format, validate, edit, and manage JSON with ease."

#### Full Description (Required)
- **Max:** 4000 characters
- **Status:** You have content in `APP_DESCRIPTION_FOR_MARKETING.md`
- **Action:** Copy and format for Play Store

---

### 6. **App Category** ⚠️ **NEEDS SELECTION**
**Action Required:**
- Select primary category: **Productivity** or **Developer Tools**
- Select secondary category (optional)

---

### 7. **Target Audience** ⚠️ **NEEDS CONFIGURATION**
**Action Required:**
- Set target age group
- Set if app is designed for children (likely "No" for developer tools)

---

### 8. **In-App Purchases Setup** ⚠️ **IF USING**
**Status:** You have Pro plan (€1.50 one-time purchase)

**Action Required:**
- Set up in-app purchase in Play Console
- Configure product ID
- Set pricing
- Test purchase flow

---

### 9. **AdMob Configuration** ⚠️ **NEEDS REVIEW**
**Status:** Using test AdMob IDs

**Action Required:**
- Replace test AdMob App ID with production ID
- Replace test ad unit IDs with production IDs
- Update in `app/src/main/java/com/prettyjson/android/data/AdConstants.kt`
- Update in `AndroidManifest.xml`

**Current Test IDs:**
- App ID: `ca-app-pub-3940256099942544~3347511713` (TEST)
- Banner: Check `AdConstants.kt`
- Rewarded: Check `AdConstants.kt`

---

### 10. **Data Safety Section** ❌ **REQUIRED**
**Action Required:**
- Complete Data Safety form in Play Console
- Declare:
  - Data collection (if any)
  - Data sharing practices
  - Security practices
  - Data deletion policies

---

### 11. **App Access** ⚠️ **IF RESTRICTED**
**Status:** Likely not needed (public app)

**Action:** Only if you want to restrict to specific users (beta testing, etc.)

---

### 12. **Pricing & Distribution** ⚠️ **NEEDS CONFIGURATION**
**Action Required:**
- Set app as Free or Paid
- Select countries for distribution
- Set up pricing (if paid app)

---

## 🔧 Technical Improvements Recommended

### 1. **Enable ProGuard/R8** ⚠️
**Current:** `isMinifyEnabled = false`

**Recommendation:** Enable for release builds to reduce APK size
```kotlin
release {
    isMinifyEnabled = true
    isShrinkResources = true
    // ... existing config
}
```

### 2. **Version Code Management**
**Current:** Version Code 1

**Recommendation:** Use automated versioning or increment before each release

### 3. **Build AAB in Fastlane**
**Action:** Add `bundleRelease` task to Fastfile for Play Store builds

---

## 📋 Pre-Submission Checklist

### Before Uploading:
- [ ] Build AAB (`./gradlew bundleRelease`)
- [ ] Test AAB on multiple devices
- [ ] Verify signing works correctly
- [ ] Test all features (especially Pro purchase)
- [ ] Replace all test AdMob IDs with production
- [ ] Review and update version code/name
- [ ] Enable ProGuard if desired

### In Play Console:
- [ ] Create app listing
- [ ] Upload AAB
- [ ] Add app icon (512x512)
- [ ] Add feature graphic (1024x500)
- [ ] Add screenshots (at least 2)
- [ ] Write short description (80 chars)
- [ ] Write full description (4000 chars)
- [ ] Set app category
- [ ] Complete content rating
- [ ] Add privacy policy URL
- [ ] Complete Data Safety section
- [ ] Set up in-app purchases (if applicable)
- [ ] Configure pricing and distribution
- [ ] Set target audience

### Testing:
- [ ] Internal testing track
- [ ] Closed testing track (optional)
- [ ] Open testing track (recommended before production)

---

## 🚀 Quick Start Guide

### 1. Build AAB for Play Store:
```bash
./gradlew bundleRelease
```

### 2. Create Privacy Policy:
- Use a template or generator
- Host on GitHub Pages, your website, or privacy policy generator
- Include: data collection, AdMob, permissions, storage

### 3. Take Screenshots:
- Use Android Studio's screenshot tool
- Or use device screenshot feature
- Capture key features and screens

### 4. Create Feature Graphic:
- 1024x500 pixels
- Include app name and key visual
- Use design tools (Figma, Canva, etc.)

### 5. Set Up Play Console:
1. Go to https://play.google.com/console
2. Create new app
3. Fill in all required fields
4. Upload AAB
5. Complete all sections

---

## 📝 Notes

- **First Submission:** Can take 1-7 days for review
- **Updates:** Usually reviewed within 1-3 days
- **AAB vs APK:** Play Store only accepts AAB for new apps (since August 2021)
- **Version Code:** Must increment with each release
- **Signing:** Keep your keystore safe! You'll need it for all future updates

---

## 🔗 Helpful Resources

- [Play Console Help](https://support.google.com/googleplay/android-developer)
- [AAB Format Guide](https://developer.android.com/guide/app-bundle)
- [Privacy Policy Generator](https://www.privacypolicygenerator.info/)
- [Play Store Listing Best Practices](https://developer.android.com/distribute/best-practices/launch/store-listing)

---

**Last Updated:** Based on current project state
**Next Steps:** Build AAB, create privacy policy, prepare screenshots

