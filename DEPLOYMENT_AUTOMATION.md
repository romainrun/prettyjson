# 🚀 Automatic Deployment Guide

After each commit and push, you can automatically deploy to DeployGate using the provided scripts.

## ✅ Quick Start

### Option 1: Use the Push & Deploy Script (Recommended)

Instead of `git push`, use:

```bash
./push-and-deploy.sh
```

This script will:
1. ✅ Push your commits to the remote repository
2. ✅ Automatically deploy to DeployGate after successful push
3. ✅ Use your commit message for the deployment

**Example:**
```bash
git add .
git commit -m "Add new feature"
./push-and-deploy.sh
```

### Option 2: Manual Deployment

If you want to push without deploying:

```bash
git push
```

Then deploy manually:

```bash
./deploy.sh "Your deployment message"
```

## 🔧 Setup

1. **Ensure credentials are configured:**
   - `.env` file should contain:
     ```bash
     DEPLOYGATE_API_TOKEN=your_token_here
     DEPLOYGATE_USER_NAME=your_username
     ```

2. **Make scripts executable (already done):**
   ```bash
   chmod +x push-and-deploy.sh
   chmod +x deploy.sh
   ```

## 📋 How It Works

### `push-and-deploy.sh`
- Wraps `git push` command
- Passes all arguments to git push (e.g., `./push-and-deploy.sh origin main`)
- After successful push, automatically runs `deploy.sh`
- Uses commit message for deployment message

### `deploy.sh`
- Builds the release APK
- Uploads to DeployGate using credentials from `.env`
- Provides deployment status

## 🎯 Workflow

**Normal workflow:**
```bash
# 1. Make changes
# 2. Stage changes
git add .

# 3. Commit
git commit -m "Your commit message"

# 4. Push and deploy automatically
./push-and-deploy.sh
```

**Branch-specific pushes:**
```bash
# Push specific branch
./push-and-deploy.sh origin feature-branch

# Push with tags
./push-and-deploy.sh --tags
```

## ⚙️ Configuration

### DeployGate Credentials
Stored in `.env` file (not committed to git):
```
DEPLOYGATE_API_TOKEN=deploygate_xgrp_YSr01zerbAnOjpTqRbFWDG2h2aykRq_083vwP
DEPLOYGATE_USER_NAME=app-sportsbook
```

### Deployment Message Format
The deployment message is automatically generated:
```
Auto-deploy: {commit_hash} - {commit_message}
```

## 🐛 Troubleshooting

### Deployment fails
1. Check `.env` file exists and has correct credentials
2. Verify you have network connectivity
3. Check DeployGate API token is valid
4. Review error messages in terminal

### Git push fails
- The script will exit and skip deployment
- Fix git issues first, then retry

### Script not executable
```bash
chmod +x push-and-deploy.sh
chmod +x deploy.sh
```

## 📝 Notes

- **Git hooks**: Git doesn't support native `post-push` hooks. The wrapper script is the recommended approach.
- **Branch filtering**: By default, deployment runs for all branches. Modify `push-and-deploy.sh` to filter specific branches.
- **Manual control**: You can always push without deploying using `git push`, then deploy manually later.

## 🎉 You're All Set!

Now every time you run `./push-and-deploy.sh`, your app will automatically build and deploy to DeployGate! 🚀

