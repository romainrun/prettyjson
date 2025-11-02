# Deployment Guide for PrettyJSON

This guide explains how to build and deploy the PrettyJSON Android app to DeployGate using Fastlane.

## Prerequisites

1. **Ruby and Bundler** installed
2. **Fastlane** installed
3. **DeployGate account** with API access
4. **Android SDK** configured

## Initial Setup

### 1. Install Dependencies

```bash
# Install Ruby gems (Bundler)
bundle install

# Or install Fastlane directly
gem install fastlane
```

### 2. Install DeployGate Plugin

```bash
fastlane add_plugin deploygate
```

Or run the setup script:

```bash
./setup_fastlane.sh
```

### 3. Configure DeployGate Credentials

1. **Get your API Token:**
   - Go to https://deploygate.com/
   - Log in to your account
   - Navigate to **Settings** → **API Tokens**
   - Click **Generate New Token**
   - Copy the token

2. **Set Environment Variables:**

   Create a `.env` file in the project root:
   
   ```bash
   DEPLOYGATE_API_TOKEN=your_api_token_here
   DEPLOYGATE_USER_NAME=your_deploygate_username
   ```
   
   Or export them in your shell:
   
   ```bash
   export DEPLOYGATE_API_TOKEN="your_api_token_here"
   export DEPLOYGATE_USER_NAME="your_deploygate_username"
   ```

## Available Fastlane Lanes

### Build Commands

```bash
# Build release APK
fastlane android build

# Build debug APK
fastlane android build_debug

# Clean build artifacts
fastlane android clean
```

### Deploy Commands

```bash
# Build and deploy release APK
fastlane android deploy

# Build and deploy debug APK
fastlane android deploy_debug

# Deploy with custom message
fastlane android deploy message:"Version 1.1.0 - New features"

# Deploy with release notes
fastlane android deploy \
  message:"Production Release" \
  release_note:"Added full screen mode and improved tree view"

# Deploy with distribution key
fastlane android deploy \
  distribution_key:"your_distribution_key" \
  message:"Release for beta testers"

# Full release pipeline
fastlane android release \
  message:"Production Release v1.1.0" \
  release_note:"Major update with new features"
```

### CI/CD Commands

```bash
# Full CI pipeline: clean, test, build, deploy
fastlane android ci message:"CI Build" release_note:"Automated build"

# Deploy with version bump
fastlane android deploy_with_version \
  message:"Version 1.2.0" \
  release_note:"New version with updates"
```

## Common Workflows

### Daily Development Build

```bash
# Quick debug build and deploy
fastlane android deploy_debug message:"Daily build $(date +%Y-%m-%d)"
```

### Beta Release

```bash
# Build and deploy to beta testers
fastlane android deploy \
  distribution_key:"your_beta_distribution_key" \
  message:"Beta Release v1.1.0" \
  release_note:"New features ready for testing"
```

### Production Release

```bash
# Full production release
fastlane android release \
  message:"Production Release v1.1.0" \
  release_note:"Major update with new features" \
  disable_notify:false
```

### CI/CD Integration

For GitHub Actions, add this to `.github/workflows/deploy.yml`:

```yaml
name: Build and Deploy

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Ruby
        uses: ruby/setup-ruby@v1
        with:
          ruby-version: 3.0
          
      - name: Install dependencies
        run: bundle install
        
      - name: Build and Deploy
        env:
          DEPLOYGATE_API_TOKEN: ${{ secrets.DEPLOYGATE_API_TOKEN }}
          DEPLOYGATE_USER_NAME: ${{ secrets.DEPLOYGATE_USER_NAME }}
        run: fastlane android deploy message:"CI Build" release_note:"Automated CI/CD build"
```

## Git Setup

The repository is configured at:
- **Remote**: `https://github.com/romainrun/prettyjson.git`
- **Branch**: `main`

### First Time Setup

```bash
# Initialize git (if not already done)
git init

# Add remote
git remote add origin https://github.com/romainrun/prettyjson.git

# Set main branch
git branch -M main

# Add files
git add .

# Commit
git commit -m "Initial commit with Fastlane setup"

# Push
git push -u origin main
```

### Regular Workflow

```bash
# Make changes
# ... edit files ...

# Commit
git add .
git commit -m "Your commit message"

# Push
git push origin main

# Deploy
fastlane android deploy
```

## Troubleshooting

### Fastlane Not Found

```bash
# Install via Bundler
bundle install

# Or install globally
gem install fastlane
```

### Plugin Installation Issues

```bash
# Update Fastlane first
gem update fastlane

# Install plugin manually
gem install fastlane-plugin-deploygate

# Run plugin installation
fastlane add_plugin deploygate
```

### Build Failures

```bash
# Clean first
fastlane android clean

# Try building manually
./gradlew clean assembleRelease

# Check build logs
cat fastlane/report.xml
```

### Authentication Issues

```bash
# Verify environment variables
echo $DEPLOYGATE_API_TOKEN
echo $DEPLOYGATE_USER_NAME

# Test DeployGate API
curl -X GET "https://deploygate.com/api/users/YOUR_USERNAME/apps?token=YOUR_TOKEN"
```

### APK Not Found

Make sure the build completes successfully:

```bash
# Check if APK exists
ls -la app/build/outputs/apk/release/app-release.apk

# If not, build first
./gradlew assembleRelease
```

## Environment Variables Reference

| Variable | Required | Description |
|----------|----------|-------------|
| `DEPLOYGATE_API_TOKEN` | Yes | Your DeployGate API token |
| `DEPLOYGATE_USER_NAME` | Yes | Your DeployGate username |
| `DEPLOYGATE_DISTRIBUTION_KEY` | No | Distribution key for specific distribution |

## DeployGate Options

When deploying, you can use these options:

- `message`: Deployment message (string)
- `release_note`: Release notes (string)
- `distribution_key`: Target distribution key (string)
- `disable_notify`: Disable email notifications (boolean)

## Support

For issues:
- Fastlane docs: https://docs.fastlane.tools
- DeployGate docs: https://docs.deploygate.com
- Plugin docs: https://github.com/deploygate/fastlane-plugin-deploygate

