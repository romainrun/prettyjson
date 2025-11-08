# 🚀 Quick Deploy Guide

Your DeployGate credentials are configured! You can now deploy from your local machine.

## ✅ Setup Complete

- ✅ `.env` file created with your credentials
- ✅ `.env` is in `.gitignore` (won't be committed)
- ✅ Deployment scripts ready

## 🚀 Deploy Now

### Quick Deploy (Easiest)

```bash
./deploy.sh "Your deployment message"
```

Example:
```bash
./deploy.sh "Version 1.1.0 - Added full screen mode"
```

### Using Fastlane Directly

```bash
# Simple deploy
fastlane android deploy

# With custom message
fastlane android deploy message:"Version 1.1.0"

# With release notes
fastlane android deploy \
  message:"Production Release" \
  release_note:"New features added"
```

### Manual Build + Deploy

```bash
# Step 1: Build
./gradlew assembleRelease

# Step 2: Deploy (Fastlane will use .env automatically)
fastlane android deploy message:"Deployment message"
```

## 📋 Your Configuration

- **DeployGate User**: `app-sportsbook`
- **API Token**: Configured in `.env` file
- **Environment**: Local deployment from your machine

## 🔧 First Time Setup

If you haven't installed the DeployGate plugin yet:

```bash
./setup_local_deploy.sh
```

Or manually:

```bash
fastlane add_plugin deploygate
```

## 🧪 Test Deployment

To test the setup:

```bash
# Test build
fastlane android build

# Test deploy
fastlane android deploy message:"Test deployment"
```

## 📝 Notes

- **Environment variables** are loaded from `.env` file automatically
- **No GitHub Actions** needed - everything runs locally
- **Fastlane handles** all the DeployGate API calls
- **Your credentials** are safe in `.env` (not committed to git)

## 🎯 Ready!

Everything is configured. Just run:

```bash
./deploy.sh "Your message"
```

And your APK will be built and deployed to DeployGate! 🚀

