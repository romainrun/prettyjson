# Local Build & Deploy Guide

This guide explains how to build the APK locally and deploy it to DeployGate from your machine.

## Prerequisites

1. **Fastlane installed** (globally or via bundler)
2. **DeployGate account** with API token
3. **Android SDK** configured
4. **Environment variables** set up

## Quick Setup

### 1. Install Fastlane (if not already installed)

```bash
# Using gem
gem install fastlane

# Or using bundler
bundle install
```

### 2. Install DeployGate Plugin

```bash
fastlane add_plugin deploygate
```

### 3. Set Up Environment Variables

Create a `.env` file in the project root:

```bash
DEPLOYGATE_API_TOKEN=your_api_token_here
DEPLOYGATE_USER_NAME=your_deploygate_username
```

Or export them in your terminal:

```bash
export DEPLOYGATE_API_TOKEN="your_token"
export DEPLOYGATE_USER_NAME="your_username"
```

Get your API token from: https://deploygate.com/settings/api_tokens

## Building Locally

### Build Release APK

```bash
# Using Gradle directly
./gradlew assembleRelease

# Or using Fastlane
fastlane android build
```

The APK will be created at:
- `app/build/outputs/apk/release/app-release.apk`

### Build Debug APK

```bash
# Using Gradle
./gradlew assembleDebug

# Or using Fastlane
fastlane android build_debug
```

The APK will be created at:
- `app/build/outputs/apk/debug/app-debug.apk`

## Deploying to DeployGate

### Basic Deploy (Release APK)

```bash
fastlane android deploy
```

This will:
1. Build the release APK
2. Upload it to DeployGate
3. Show you the deployment URL

### Deploy with Custom Message

```bash
fastlane android deploy message:"Version 1.1.0 - New features"
```

### Deploy with Release Notes

```bash
fastlane android deploy \
  message:"Production Release" \
  release_note:"Added full screen mode and improved tree view"
```

### Deploy to Specific Distribution

```bash
fastlane android deploy \
  distribution_key:"your_distribution_key" \
  message:"Beta release for testers"
```

### Deploy Debug Build

```bash
fastlane android deploy_debug message:"Debug build for testing"
```

### Build First, Then Deploy Separately

If you want to build first and then deploy later:

```bash
# Step 1: Build APK
./gradlew assembleRelease

# Step 2: Deploy the built APK (using Fastlane's direct upload)
fastlane android deploy
```

Or if you already have an APK built:

```bash
# Deploy existing APK using Fastlane
fastlane run deploygate \
  api_token:$DEPLOYGATE_API_TOKEN \
  user_name:$DEPLOYGATE_USER_NAME \
  apk:app/build/outputs/apk/release/app-release.apk \
  message:"Deployed from local machine"
```

## Common Workflows

### Daily Build & Deploy

```bash
fastlane android deploy message:"Daily build $(date +%Y-%m-%d)"
```

### Production Release

```bash
fastlane android release \
  message:"Production Release v1.1.0" \
  release_note:"Major update with new features"
```

### Test Deployment

```bash
# Build and deploy debug APK
fastlane android deploy_debug message:"Test build"
```

## Troubleshooting

### Fastlane Not Found

```bash
# Install Fastlane
gem install fastlane

# Or use bundler
bundle install
```

### Environment Variables Not Working

```bash
# Check if variables are set
echo $DEPLOYGATE_API_TOKEN
echo $DEPLOYGATE_USER_NAME

# If empty, add them:
export DEPLOYGATE_API_TOKEN="your_token"
export DEPLOYGATE_USER_NAME="your_username"
```

### Build Fails

```bash
# Clean first
./gradlew clean

# Try building again
./gradlew assembleRelease
```

### DeployGate Upload Fails

1. Verify API token is correct
2. Check username matches your DeployGate account
3. Test token manually: https://deploygate.com/settings/api_tokens

### APK Not Found

Make sure the build completed successfully:

```bash
# Check if APK exists
ls -la app/build/outputs/apk/release/app-release.apk

# If not, build it first
./gradlew assembleRelease
```

## Useful Commands

### Clean Build

```bash
fastlane android clean
fastlane android build
```

### Full Pipeline (Clean, Build, Deploy)

```bash
fastlane android release \
  message:"Full release" \
  release_note:"Clean build and deploy"
```

### Check Build Configuration

```bash
# Check version code and name
./gradlew -q printVersion
```

## Environment Setup Script

You can create a script to set up environment variables:

```bash
#!/bin/bash
# save as setup_env.sh

export DEPLOYGATE_API_TOKEN="your_token_here"
export DEPLOYGATE_USER_NAME="your_username_here"

echo "Environment variables set!"
echo "DEPLOYGATE_USER_NAME: $DEPLOYGATE_USER_NAME"
```

Then source it before deploying:

```bash
source setup_env.sh
fastlane android deploy
```

## Integration with Your Workflow

### Pre-Deploy Checklist

- [ ] Code changes committed
- [ ] Version number updated (if needed)
- [ ] Tests passed (if applicable)
- [ ] Environment variables set
- [ ] Fastlane plugin installed

### Deployment Steps

1. **Build the APK:**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Verify APK exists:**
   ```bash
   ls app/build/outputs/apk/release/app-release.apk
   ```

3. **Deploy to DeployGate:**
   ```bash
   fastlane android deploy message:"Your deployment message"
   ```

4. **Verify in DeployGate:**
   - Check your DeployGate dashboard
   - Confirm new build appears
   - Test download link

## Notes

- **Local builds** are faster than CI/CD for quick iterations
- **You control** exactly when deployments happen
- **No GitHub Actions** required for local deployment
- **Fastlane handles** all the DeployGate API interactions

## Quick Reference

```bash
# Build release APK
./gradlew assembleRelease

# Deploy (builds + uploads)
fastlane android deploy message:"Deployment message"

# Deploy existing APK only
# (requires APK already built)
fastlane android deploy

# Deploy debug build
fastlane android deploy_debug message:"Debug build"
```

For more details, see [DEPLOYMENT.md](DEPLOYMENT.md) or [fastlane/README.md](fastlane/README.md).

