# GitHub Setup Verification

## ✅ GitHub Secrets Configuration

Your GitHub repository should have these secrets configured:

### Required Secrets

1. **`DEPLOYGATE_API_TOKEN`**
   - Value: Your DeployGate API token
   - Get it from: https://deploygate.com/settings/api_tokens

2. **`DEPLOYGATE_USER_NAME`**
   - Value: Your DeployGate username
   - This is the username you use to log into DeployGate

### How to Verify

1. Go to your GitHub repository: https://github.com/romainrun/prettyjson
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Verify both secrets are present:
   - ✅ `DEPLOYGATE_API_TOKEN`
   - ✅ `DEPLOYGATE_USER_NAME`

## 🚀 How It Works

### Automatic Deployment

When you push to the `main` branch, GitHub Actions will:

1. ✅ Checkout your code
2. ✅ Set up Java 17 and Ruby 3.0
3. ✅ Cache Gradle dependencies
4. ✅ Install Fastlane and DeployGate plugin
5. ✅ Build release APK using Gradle
6. ✅ Deploy APK to DeployGate automatically
7. ✅ Upload APK as artifact for download

### Manual Trigger

You can also trigger deployment manually:

1. Go to **Actions** tab in GitHub
2. Select **Build and Deploy to DeployGate** workflow
3. Click **Run workflow**
4. Select branch (usually `main`)
5. Click **Run workflow** button

## 📋 Workflow Triggers

The workflow runs on:
- ✅ Push to `main` branch (automatic)
- ✅ Manual trigger via workflow_dispatch (Actions tab)

## 📦 Workflow Output

After deployment:
- ✅ APK will be uploaded to DeployGate
- ✅ APK artifact will be available for download in GitHub Actions
- ✅ Build logs will show deployment status

## 🧪 Testing the Setup

### Test Locally First

Before pushing, test locally:

```bash
# Set environment variables
export DEPLOYGATE_API_TOKEN="your_token"
export DEPLOYGATE_USER_NAME="your_username"

# Test build
fastlane android build

# Test deploy
fastlane android deploy message:"Test deployment"
```

### Test GitHub Actions

1. Push a commit to `main` branch:
   ```bash
   git add .
   git commit -m "Test GitHub Actions deployment"
   git push origin main
   ```

2. Check Actions tab:
   - Go to https://github.com/romainrun/prettyjson/actions
   - Watch the workflow run
   - Check logs if deployment fails

## 🔍 Troubleshooting

### If Deployment Fails

1. **Check GitHub Actions logs:**
   - Go to Actions tab
   - Click on failed workflow
   - Check error messages in logs

2. **Verify Secrets:**
   - Ensure secrets are correctly named (case-sensitive)
   - Verify API token is valid
   - Verify username matches DeployGate account

3. **Common Issues:**

   **Error: Plugin not found**
   ```bash
   # Add to workflow: continue-on-error: true (already added)
   # Or manually install in workflow
   ```

   **Error: APK not found**
   ```bash
   # Check if build succeeded
   # Verify APK path in Fastfile
   ```

   **Error: Authentication failed**
   ```bash
   # Verify API token in GitHub Secrets
   # Test token manually at DeployGate
   ```

## ✅ Ready to Deploy!

Your setup is complete! Next steps:

1. **Commit and push your changes:**
   ```bash
   git add .
   git commit -m "Add Fastlane and GitHub Actions setup"
   git push origin main
   ```

2. **Monitor the deployment:**
   - Go to Actions tab
   - Watch the workflow run
   - Check DeployGate for new build

3. **Verify deployment:**
   - Check DeployGate dashboard
   - Confirm new build appears
   - Test download link

## 📝 Notes

- First deployment may take longer (downloading dependencies)
- Subsequent deployments will be faster (cached dependencies)
- APK artifacts are kept for 7 days in GitHub Actions
- Build logs are saved for troubleshooting

## 🎉 Success!

Once configured, every push to `main` will automatically:
- ✅ Build your app
- ✅ Deploy to DeployGate
- ✅ Make it available for testing

No manual steps needed! 🚀

