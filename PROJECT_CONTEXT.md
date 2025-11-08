# PrettyJSON Android - Project Context & Continuation Guide

**Last Updated:** November 8, 2024  
**Purpose:** This document provides complete context for continuing development work on the PrettyJSON Android app, ensuring no subject matter is lost when switching between development environments or AI assistants.

---

## 📋 Table of Contents

1. [Project Overview](#project-overview)
2. [Current State](#current-state)
3. [Recent Changes & Work Completed](#recent-changes--work-completed)
4. [Architecture & Key Components](#architecture--key-components)
5. [Deployment & CI/CD](#deployment--cicd)
6. [Pro Plan System](#pro-plan-system)
7. [Known Issues & Pending Tasks](#known-issues--pending-tasks)
8. [Development Workflow](#development-workflow)
9. [Key Files Reference](#key-files-reference)
10. [Next Steps & Recommendations](#next-steps--recommendations)

---

## 🎯 Project Overview

**PrettyJSON** is an Android application for viewing, editing, formatting, and validating JSON data. The app provides a comprehensive JSON editor with features like:

- **JSON Viewer & Editor** - Syntax highlighting, line numbers, error detection
- **Form Editor** - Visual form-based JSON editing (Pro feature)
- **Data Buckets** - Reusable JSON snippets (Pro feature)
- **Export Options** - JSON, TXT, PDF, HTML export
- **QR Code Sharing** - Generate QR codes for JSON data
- **Tree View** - Collapsible JSON tree structure
- **Search & Navigation** - Find text, go to line, bracket matching
- **Auto-fix** - Fix common JSON errors (trailing commas, etc.)

**Package ID:** `com.prettyjson.android`  
**Min SDK:** 24  
**Target SDK:** 35  
**Compile SDK:** 36  
**Version:** 1.0 (version code is timestamp-based)

---

## 📊 Current State

### Build Configuration
- **Build System:** Gradle with Kotlin DSL
- **Signing:** Release builds are **mandatory signed** using `json-viewer-release-key.jks`
- **Output Format:** **AAB only** (Android App Bundle) - APK builds have been removed
- **Version Code:** Generated dynamically from timestamp (ensures unique, incrementing values)
- **Version Name:** 1.0

### CI/CD Status
- ✅ **GitHub Actions** workflows configured
- ✅ **Self-hosted runner** setup for `android-release.yml`
- ✅ **DeployGate** integration via direct API (Fastlane plugin removed)
- ✅ **Tag-based triggers** for automated builds (`*_DeployGate` pattern)
- ✅ **Secrets configured:** `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_PASSWORD`, `DEPLOYGATE_API_TOKEN`, `DEPLOYGATE_USER_NAME`

### Pro Plan Status
- ✅ **Play Billing Library** integrated (version 7.1.1)
- ✅ **One-time purchase** at €1.50 (Product ID: `pro_plan`)
- ✅ **ProManager** handles billing and DataStore state
- ✅ **UpgradeToProScreen** implemented
- ✅ **Feature gating** in place (Form Editor, Data Buckets, exports)
- ✅ **Ad removal** for Pro users

### AdMob Integration
- **App ID:** `ca-app-pub-3137130387262789~9240936516`
- **Banner Ad Unit:** `ca-app-pub-3137130387262789/3843966149`
- **Rewarded Ad Unit:** `ca-app-pub-3137130387262789/9134615995`
- **Status:** Production IDs configured, ads hidden for Pro users

---

## 🔄 Recent Changes & Work Completed

### November 8, 2024 - GitHub Actions & Workflow Updates

1. **Removed Fastlane Dependency from GitHub Actions**
   - Replaced Fastlane plugin with direct DeployGate API calls
   - Fixed `fastlane-plugin-deploygate` not found error (gem doesn't exist on RubyGems)
   - Updated `deploy.yml` to use curl for DeployGate uploads

2. **AAB-Only Builds**
   - Removed APK build steps from both workflows
   - Updated `android-release.yml` to build only AAB
   - Updated artifact uploads to include only AAB files
   - Job renamed to "Build Release AAB"

3. **Git Hook Updates**
   - Modified `.git/hooks/post-commit` to skip local deployment when commit is tagged
   - Tagged releases are handled by GitHub Actions, not local Fastlane

4. **Keystore Management**
   - Updated `build.gradle.kts` to handle keystore in both local and CI/CD environments
   - Added keystore extraction step in GitHub Actions from `ANDROID_KEYSTORE_BASE64` secret
   - Keystore path resolution: checks `app/json-viewer-release-key.jks` (local) then `json-viewer-release-key.jks` (CI/CD)

### Previous Major Updates

- **Pro Plan System** - Complete implementation with Play Billing
- **Quick Wins** - Auto-scroll to error, search navigation, expand/collapse all, JSON path copy, bracket matching, auto-fix trailing commas
- **UI/UX Improvements** - Expandable FAB, haptic feedback, better error messages, loading states
- **Form Tab Pro Gating** - View-only for free users, Pro required for actions
- **System Bar Padding** - Fixed app content being behind system bars
- **Package ID Migration** - Changed from `re.weare.app` to `com.prettyjson.android`

---

## 🏗️ Architecture & Key Components

### Package Structure
```
com.prettyjson.android/
├── data/
│   ├── billing/
│   │   └── ProManager.kt          # Play Billing integration, Pro state management
│   ├── database/                   # Room database entities and DAOs
│   ├── preferences/
│   │   ├── PreferencesManager.kt  # User preferences
│   │   └── PremiumManager.kt      # Legacy premium manager (synced with ProManager)
│   └── repository/                # Data repositories
├── di/
│   └── appModule.kt                # Koin dependency injection
├── ui/
│   ├── components/
│   │   ├── LineNumberTextField.kt  # Custom JSON editor with line numbers
│   │   ├── JsonTreeView.kt         # Collapsible JSON tree
│   │   ├── ExpandableFAB.kt       # Floating action button with expandable menu
│   │   └── BannerAdView.kt        # AdMob banner ads
│   ├── screens/
│   │   ├── MainScreen.kt           # Main JSON editor screen
│   │   ├── SettingsScreen.kt       # Settings and Pro upgrade
│   │   ├── UpgradeToProScreen.kt  # Pro plan purchase screen
│   │   ├── FormEditorScreen.kt    # Visual form editor (Pro)
│   │   └── DataBucketsScreen.kt   # Reusable JSON snippets (Pro)
│   ├── viewmodel/
│   │   ├── MainViewModel.kt         # Main screen logic
│   │   ├── ProViewModel.kt        # Pro plan state
│   │   └── PremiumViewModel.kt     # Legacy premium state
│   └── navigation/
│       └── NavGraph.kt              # Navigation routes
└── util/
    ├── JsonPathGenerator.kt         # Generate JSON paths for tree nodes
    ├── BracketMatcher.kt           # Match brackets for highlighting
    ├── JsonAutoFix.kt              # Auto-fix common JSON errors
    ├── HapticFeedback.kt           # Haptic feedback utilities
    └── ...
```

### Key Technologies
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM with ViewModels
- **Dependency Injection:** Koin
- **Database:** Room
- **Preferences:** DataStore Preferences
- **Billing:** Google Play Billing Library 7.1.1
- **Ads:** AdMob (Banner + Rewarded)
- **JSON Parsing:** Gson, JSONObject/JSONArray
- **QR Codes:** ZXing library

---

## 🚀 Deployment & CI/CD

### GitHub Actions Workflows

#### 1. `android-release.yml` (Tag-triggered)
- **Trigger:** Push tags matching `*_DeployGate` pattern
- **Runner:** `self-hosted`
- **Builds:** Release AAB only
- **Artifacts:** Uploads AAB to GitHub Actions artifacts
- **Tag Format:** `YYYYMMDD_X.Y.Z_DeployGate` (e.g., `20251108_1.0.0_DeployGate`)

**Required Secrets:**
- `ANDROID_KEYSTORE_BASE64` - Base64-encoded keystore file
- `ANDROID_KEYSTORE_PASSWORD` - Keystore password
- `ANDROID_KEY_PASSWORD` - Key password

#### 2. `deploy.yml` (Manual)
- **Trigger:** `workflow_dispatch` (manual)
- **Runner:** `ubuntu-latest`
- **Builds:** Release AAB
- **Deploys:** Uploads to DeployGate via API

**Required Secrets:**
- All signing secrets (same as above)
- `DEPLOYGATE_API_TOKEN` - DeployGate API token
- `DEPLOYGATE_USER_NAME` - DeployGate username

### Local Deployment

**Script:** `deploy.sh`
- Builds signed AAB using Fastlane
- Uploads to DeployGate
- **Note:** Local deployment is skipped when commit is tagged (handled by GitHub Actions)

### Git Hooks

**`.git/hooks/post-commit`**
- Auto-deploys on regular commits
- **Skips deployment** if commit is tagged (lets GitHub Actions handle it)
- Checks for tags with `_DeployGate` suffix

### Build Commands

```bash
# Build AAB locally
./gradlew bundleRelease

# Build with Fastlane (includes DeployGate upload)
fastlane android deploy

# Or use deploy script
./deploy.sh "Deployment message"
```

---

## 💎 Pro Plan System

### Implementation Details

**Product ID:** `pro_plan` (matches Google Play Console)  
**Price:** €1.50 (one-time purchase)  
**State Storage:** DataStore Preferences (`is_pro_user` boolean)

### Pro Features
1. **Ad-free experience** - Removes all banner and rewarded ads
2. **Full Form Editor** - Add/edit/delete fields in Form tab
3. **Data Buckets** - Unlimited reusable JSON snippets
4. **Unlimited exports** - No ads required for exports
5. **Custom themes** - Additional Material3 color palettes (future)
6. **Early Access** - Badge for experimental features (future)

### Free Features
- Core JSON viewing/editing
- Form Editor view-only
- Data Buckets require rewarded ad
- Exports require rewarded ad
- Banner ads visible

### Key Files
- `data/billing/ProManager.kt` - Billing client, purchase handling, state management
- `ui/viewmodel/ProViewModel.kt` - Exposes Pro state to UI
- `ui/screens/UpgradeToProScreen.kt` - Purchase screen
- `data/preferences/PremiumManager.kt` - Legacy manager (synced with ProManager)

### Development Mode
- `KEY_DEV_MODE_PRO` in DataStore for testing
- Toggle in Settings screen to enable/disable Pro features locally
- Bypasses actual billing for debug builds

---

## ⚠️ Known Issues & Pending Tasks

### Known Issues

1. **Line Number Display Issue**
   - **Status:** Partially resolved, may still occur
   - **Description:** Line numbers sometimes show "1 1 1 1" instead of correct numbers
   - **Location:** `LineNumberTextField.kt`
   - **Last Attempt:** Updated line counting logic to use `textForLineCounting.count { it == '\n' } + 1`
   - **Note:** May be related to text wrapping or rendering synchronization

2. **Deprecated API Warnings**
   - `Icons.Filled` deprecated (should use `Icons.Default`)
   - `Divider` composable deprecated
   - **Impact:** Non-critical, warnings only

3. **Unit Test Failures**
   - Some existing tests may fail (`CursorPositionInserterTest`, `JsonBuilderTest`, `TypedValueConverterTest`)
   - **Status:** Not blocking, but should be addressed

### Pending Tasks

1. **Investigate and fix line number rendering** - High priority
2. **Update deprecated APIs** - Medium priority
3. **Fix failing unit tests** - Medium priority
4. **Add more unit tests** - Low priority
5. **Custom theme color picker** - Future feature
6. **Early Access badge system** - Future feature

---

## 🔧 Development Workflow

### Setting Up Development Environment

1. **Clone Repository**
   ```bash
   git clone https://github.com/romainrun/prettyjson.git
   cd prettyjson
   ```

2. **Configure Signing**
   - Place `json-viewer-release-key.jks` in `app/` directory
   - Or set `KEYSTORE_PASSWORD` and `KEY_PASSWORD` environment variables
   - For CI/CD: Use `ANDROID_KEYSTORE_BASE64` secret

3. **Configure AdMob** (if needed)
   - Update `AdConstants.kt` with your AdMob IDs
   - Or use production IDs already configured

4. **Run Locally**
   ```bash
   ./gradlew installDebug
   ```

### Making Changes

1. **Feature Development**
   - Create feature branch: `git checkout -b feature/feature-name`
   - Make changes
   - Test locally
   - Commit: `git commit -m "Description"`
   - Push: `git push origin feature/feature-name`
   - Create PR

2. **Deployment**
   - Merge to `main`
   - Create tag: `git tag YYYYMMDD_X.Y.Z_DeployGate`
   - Push tag: `git push origin YYYYMMDD_X.Y.Z_DeployGate`
   - GitHub Actions will automatically build and upload AAB

### Testing Pro Features Locally

1. Open Settings screen
2. Enable "Development Mode" toggle
3. Pro features will be unlocked without purchase
4. Test all Pro-gated functionality

---

## 📁 Key Files Reference

### Build Configuration
- `app/build.gradle.kts` - Main build configuration, signing, versioning
- `gradle/libs.versions.toml` - Dependency versions
- `app/src/main/AndroidManifest.xml` - App manifest, permissions

### CI/CD
- `.github/workflows/android-release.yml` - Tag-triggered AAB build
- `.github/workflows/deploy.yml` - Manual DeployGate deployment
- `.github/GITHUB_ACTIONS_SETUP.md` - GitHub Actions setup guide
- `deploy.sh` - Local deployment script
- `fastlane/Fastfile` - Fastlane lanes (for local deployment)

### Pro Plan
- `app/src/main/java/com/prettyjson/android/data/billing/ProManager.kt`
- `app/src/main/java/com/prettyjson/android/ui/viewmodel/ProViewModel.kt`
- `app/src/main/java/com/prettyjson/android/ui/screens/UpgradeToProScreen.kt`

### Core UI
- `app/src/main/java/com/prettyjson/android/ui/screens/MainScreen.kt`
- `app/src/main/java/com/prettyjson/android/ui/components/LineNumberTextField.kt`
- `app/src/main/java/com/prettyjson/android/ui/components/JsonTreeView.kt`
- `app/src/main/java/com/prettyjson/android/ui/components/ExpandableFAB.kt`

### Utilities
- `app/src/main/java/com/prettyjson/android/util/JsonPathGenerator.kt`
- `app/src/main/java/com/prettyjson/android/util/BracketMatcher.kt`
- `app/src/main/java/com/prettyjson/android/util/JsonAutoFix.kt`
- `app/src/main/java/com/prettyjson/android/util/HapticFeedback.kt`

### Documentation
- `COMPREHENSIVE_APP_DESCRIPTION.md` - Detailed app description for marketing/AI
- `PLAY_STORE_DESCRIPTIONS.md` - Play Store short and full descriptions
- `PLAY_STORE_CHECKLIST.md` - Play Store submission checklist
- `PROJECT_CONTEXT.md` - This file

---

## 🎯 Next Steps & Recommendations

### Immediate Priorities

1. **Fix Line Number Issue**
   - Investigate `LineNumberTextField.kt` line counting logic
   - Test with text wrapping enabled/disabled
   - Consider using `TextLayoutResult` for accurate line counting

2. **Update Deprecated APIs**
   - Replace `Icons.Filled` with `Icons.Default`
   - Update `Divider` composable usage
   - Run `./gradlew lint` to find all deprecated usages

3. **Test GitHub Actions Workflow**
   - Verify tag-triggered build works on self-hosted runner
   - Check AAB is properly signed
   - Confirm artifact upload works

### Short-term Goals

1. **Play Store Submission**
   - Complete Play Store listing
   - Upload AAB to Play Console
   - Set up closed testing track
   - Submit for review

2. **Pro Plan Testing**
   - Test purchase flow end-to-end
   - Verify restore purchase functionality
   - Test feature gating

3. **Performance Optimization**
   - Profile app performance
   - Optimize large JSON handling
   - Improve rendering performance

### Long-term Goals

1. **Custom Themes**
   - Implement color picker
   - Add theme presets
   - Save user theme preferences

2. **Early Access Features**
   - Implement badge system
   - Add experimental features toggle
   - Create feature flag system

3. **Additional Features**
   - JSON diff viewer
   - JSON schema validation
   - Multiple file support
   - Cloud sync (optional)

---

## 🔐 Security & Secrets

### Keystore Management
- **Local:** `app/json-viewer-release-key.jks` (not in git)
- **CI/CD:** Extracted from `ANDROID_KEYSTORE_BASE64` secret
- **Passwords:** Stored in GitHub Secrets (`ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_PASSWORD`)

### API Keys
- **AdMob:** Configured in `AdConstants.kt` (production IDs)
- **DeployGate:** Stored in GitHub Secrets (`DEPLOYGATE_API_TOKEN`, `DEPLOYGATE_USER_NAME`)

### Important Notes
- **Never commit keystore files** - Already in `.gitignore`
- **Never commit API keys** - Use environment variables or secrets
- **Rotate secrets regularly** - Especially if exposed

---

## 📞 Support & Resources

### Documentation Files
- `COMPREHENSIVE_APP_DESCRIPTION.md` - Full app description (1295 lines)
- `PLAY_STORE_DESCRIPTIONS.md` - Store listing text
- `PLAY_STORE_CHECKLIST.md` - Submission checklist
- `.github/GITHUB_ACTIONS_SETUP.md` - CI/CD setup guide

### External Resources
- **GitHub Repository:** https://github.com/romainrun/prettyjson
- **DeployGate:** https://deploygate.com
- **Google Play Console:** https://play.google.com/console

---

## 📝 Notes for AI Assistants

When continuing work on this project:

1. **Always check this file first** - It contains the latest context
2. **Review recent commits** - `git log --oneline -20` to see what changed
3. **Check GitHub Actions** - See if any workflows are running/failing
4. **Test locally first** - Before making changes, verify current state
5. **Update this file** - When making significant changes, update relevant sections
6. **Follow existing patterns** - Maintain code style and architecture
7. **Consider Pro Plan** - Many features are Pro-gated, test both free and Pro flows
8. **Build only AAB** - APK builds have been removed, don't add them back
9. **Use GitHub Actions for tags** - Don't run local deployment for tagged commits
10. **Check known issues** - Review the "Known Issues" section before starting

---

**Last Updated:** November 8, 2024  
**Maintained By:** Development Team  
**Version:** 1.0

