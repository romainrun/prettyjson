# Local Build & Deploy - Quick Start

## ✅ Setup Complete!

You're set up to build and deploy from your local machine.

## 🚀 Quick Deploy

### Option 1: Using the Script (Easiest)

```bash
./deploy.sh "Your deployment message"
```

### Option 2: Using Fastlane Directly

```bash
# Simple deploy
fastlane android deploy

# With message
fastlane android deploy message:"Version 1.1.0"

# With release notes
fastlane android deploy \
  message:"Production Release" \
  release_note:"New features added"
```

### Option 3: Manual Build + Deploy

```bash
# Step 1: Build
./gradlew assembleRelease

# Step 2: Deploy
fastlane android deploy message:"Your message"
```

## ⚙️ Configuration

### Set Environment Variables

Create `.env` file:

```bash
DEPLOYGATE_API_TOKEN=your_token_here
DEPLOYGATE_USER_NAME=your_username_here
```

Or export in terminal:

```bash
export DEPLOYGATE_API_TOKEN="your_token"
export DEPLOYGATE_USER_NAME="your_username"
```

Get your token from: https://deploygate.com/settings/api_tokens

## 📋 Available Commands

- `fastlane android build` - Build release APK only
- `fastlane android deploy` - Build + Deploy release APK
- `fastlane android deploy_debug` - Build + Deploy debug APK
- `fastlane android clean` - Clean build artifacts
- `./gradlew assembleRelease` - Build release APK (Gradle)
- `./gradlew assembleDebug` - Build debug APK (Gradle)

## 📚 Documentation

- **Quick Start**: [LOCAL_DEPLOYMENT.md](LOCAL_DEPLOYMENT.md)
- **Full Guide**: [DEPLOYMENT.md](DEPLOYMENT.md)
- **Fastlane Details**: [fastlane/README.md](fastlane/README.md)

## 🔧 Troubleshooting

If deployment fails, check:
1. Environment variables are set (`echo $DEPLOYGATE_API_TOKEN`)
2. Fastlane plugin installed (`fastlane add_plugin deploygate`)
3. APK was built successfully (`ls app/build/outputs/apk/release/`)

