# Fastlane Configuration for PrettyJSON

This directory contains Fastlane configuration for building and deploying the PrettyJSON Android app to DeployGate.

## Setup

### 1. Install Fastlane

```bash
# Using bundler (recommended)
bundle install

# Or using gem
gem install fastlane
```

### 2. Install DeployGate Plugin

```bash
fastlane add_plugin deploygate
```

### 3. Configure Environment Variables

Create a `.env` file in the project root (or set environment variables):

```bash
export DEPLOYGATE_API_TOKEN="your_api_token_here"
export DEPLOYGATE_USER_NAME="your_deploygate_username"
```

Or add them to your shell profile (`~/.zshrc` or `~/.bashrc`).

### 4. Get DeployGate API Token

1. Go to https://deploygate.com/
2. Log in to your account
3. Navigate to Settings → API Tokens
4. Generate a new API token
5. Copy the token and add it to your environment variables

## Available Lanes

### Build Lanes

- `fastlane android build` - Build release APK
- `fastlane android build_debug` - Build debug APK
- `fastlane android clean` - Clean build artifacts

### Deploy Lanes

- `fastlane android deploy` - Build and deploy release APK to DeployGate
- `fastlane android deploy_debug` - Build and deploy debug APK to DeployGate
- `fastlane android deploy_with_version` - Increment version and deploy
- `fastlane android release` - Full pipeline: clean, build, and deploy

### Development Lanes

- `fastlane android test` - Run tests
- `fastlane android ci` - Clean, test, build, and deploy (CI/CD pipeline)

## Usage Examples

### Basic Deploy

```bash
fastlane android deploy
```

### Deploy with Custom Message

```bash
fastlane android deploy message:"New features added"
```

### Deploy with Distribution Key and Release Note

```bash
fastlane android deploy \
  message:"Version 1.1.0" \
  distribution_key:"your_distribution_key" \
  release_note:"Added full screen mode and improved tree view"
```

### Deploy Debug Build

```bash
fastlane android deploy_debug message:"Debug build for testing"
```

### Full Release Pipeline

```bash
fastlane android release \
  message:"Production Release v1.1.0" \
  release_note:"Major update with new features"
```

### CI/CD Pipeline

```bash
fastlane android ci \
  message:"Automated CI build" \
  release_note:"Build from commit $(git rev-parse --short HEAD)"
```

## Options

All deploy lanes support the following options:

- `message`: Deployment message (string)
- `release_note`: Release notes (string)
- `distribution_key`: Distribution key for specific distribution (string, optional)
- `disable_notify`: Disable email notification (boolean, default: false)
- `user_name`: DeployGate username (overrides env variable)

## Files

- `Fastfile` - Main Fastlane configuration with all lanes
- `Appfile` - App configuration (package name, app ID)
- `Pluginfile` - Fastlane plugins (DeployGate)
- `.env` - Environment variables (not committed to git)

## Troubleshooting

### Plugin Installation Issues

If you encounter issues installing the DeployGate plugin:

```bash
# Update Fastlane
gem update fastlane

# Install plugin manually
gem install fastlane-plugin-deploygate
```

### Authentication Issues

Make sure your `DEPLOYGATE_API_TOKEN` is correctly set:

```bash
echo $DEPLOYGATE_API_TOKEN
```

If empty, add it to your environment variables.

### Build Issues

If builds fail, try cleaning first:

```bash
fastlane android clean
fastlane android build
```

## Git Integration

The repository is configured at:
- Remote: `https://github.com/romainrun/prettyjson.git`
- Branch: `main`

To push changes:

```bash
git add .
git commit -m "Your commit message"
git push origin main
```

## CI/CD Integration

For GitHub Actions or other CI/CD platforms, set the environment variables in your CI configuration:

```yaml
env:
  DEPLOYGATE_API_TOKEN: ${{ secrets.DEPLOYGATE_API_TOKEN }}
  DEPLOYGATE_USER_NAME: ${{ secrets.DEPLOYGATE_USER_NAME }}
```

Then run:

```bash
fastlane android ci
```

