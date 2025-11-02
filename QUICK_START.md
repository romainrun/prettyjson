# Quick Start Guide - Deploy PrettyJSON to DeployGate

## 🚀 One-Time Setup

### 1. Install Fastlane Plugin

```bash
fastlane add_plugin deploygate
```

Or run the setup script:

```bash
./setup_fastlane.sh
```

### 2. Get DeployGate API Token

1. Go to https://deploygate.com/settings/api_tokens
2. Click "Generate New Token"
3. Copy the token

### 3. Configure Credentials

Create `.env` file:

```bash
cp .env.example .env
```

Edit `.env` and add:

```bash
DEPLOYGATE_API_TOKEN=your_token_here
DEPLOYGATE_USER_NAME=your_username_here
```

## 📦 Build & Deploy

### Quick Deploy

```bash
fastlane android deploy
```

### Deploy with Message

```bash
fastlane android deploy message:"Version 1.0"
```

### Deploy with Release Notes

```bash
fastlane android deploy \
  message:"Production Release" \
  release_note:"Added full screen mode and improved UI"
```

### Deploy Debug Build

```bash
fastlane android deploy_debug message:"Debug build"
```

## 🔧 Available Commands

- `fastlane android build` - Build release APK
- `fastlane android deploy` - Build & deploy release
- `fastlane android deploy_debug` - Build & deploy debug
- `fastlane android release` - Full pipeline (clean, build, deploy)
- `fastlane android ci` - CI/CD pipeline (clean, test, build, deploy)

## 📝 Git Setup

```bash
# First time
git init
git remote add origin https://github.com/romainrun/prettyjson.git
git branch -M main

# Regular workflow
git add .
git commit -m "Your message"
git push origin main
```

For detailed information, see [DEPLOYMENT.md](DEPLOYMENT.md)

