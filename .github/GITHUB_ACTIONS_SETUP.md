# GitHub Actions Setup Guide

## Required GitHub Secrets

These secrets must be configured in your GitHub repository for the workflow to build signed releases.

### How to Add Secrets

1. Go to your repository: https://github.com/romainrun/prettyjson
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add each secret below:

### Required Secrets

1. **`ANDROID_KEYSTORE_PASSWORD`** ✅ (You have this)
   - **Value**: Your Android keystore password
   - **Current value**: `jsonviewer2024`
   - **Purpose**: Used to sign the release APK and AAB
   - **Security**: ⚠️ Keep this secret! Never commit to repository.

2. **`ANDROID_KEY_PASSWORD`** ✅ (You have this)
   - **Value**: Your Android key password
   - **Current value**: `jsonviewer2024`
   - **Purpose**: Used to sign the release APK and AAB
   - **Security**: ⚠️ Keep this secret! Never commit to repository.

3. **`ANDROID_KEYSTORE_BASE64`** (Optional - Recommended for CI/CD)
   - **Value**: Base64-encoded keystore file
   - **How to create**: 
     ```bash
     base64 -i app/json-viewer-release-key.jks | pbcopy
     # Then paste into GitHub secret
     ```
   - **Purpose**: Allows the workflow to extract the keystore file
   - **Alternative**: Place `json-viewer-release-key.jks` on your self-hosted runner at the project root
   - **Security**: ⚠️ Keep this secret! Never commit to repository.

### Optional Secrets (for future DeployGate integration)

3. **`DEPLOYGATE_API_TOKEN`** (optional)
   - **Value**: Your DeployGate API token
   - **Get it from**: https://deploygate.com/settings/api_tokens
   - **Purpose**: For automatic deployment to DeployGate (if added later)

4. **`DEPLOYGATE_USER_NAME`** (optional)
   - **Value**: Your DeployGate username/organization
   - **Purpose**: For automatic deployment to DeployGate (if added later)

## GitHub Variables (Optional)

You can also set repository variables for non-sensitive configuration:

1. Go to **Settings** → **Secrets and variables** → **Actions** → **Variables** tab
2. Click **New repository variable**

### Recommended Variables

- **`JAVA_VERSION`**: `17` (defaults to 17 if not set)
- **`ARTIFACT_RETENTION_DAYS`**: `30` (defaults to 30 if not set)

These are already set as workflow-level `env` variables, but you can override them with repository variables if needed.

## Workflow Configuration

The workflow uses environment variables that can be overridden:

- **`JAVA_VERSION`**: Java version to use (default: `17`)
- **`ARTIFACT_RETENTION_DAYS`**: How long to keep artifacts (default: `30`)
- **`GRADLE_BUILD_FLAGS`**: Gradle build flags (default: `--no-configuration-cache`)

## Testing the Setup

### 1. Verify Secrets are Set

```bash
# Check if secrets are accessible (they won't show values, but you can verify they exist)
gh secret list
```

### 2. Test the Workflow

Create and push a test tag:

```bash
git tag 20251108_1.0.0_DeployGate
git push origin 20251108_1.0.0_DeployGate
```

### 3. Check Workflow Run

1. Go to **Actions** tab in GitHub
2. Find the workflow run for your tag
3. Verify:
   - ✅ Build completes successfully
   - ✅ APK and AAB are built
   - ✅ Artifacts are uploaded
   - ✅ Signing works (check APK/AAB signature)

## Security Best Practices

1. ✅ **Never commit secrets** to the repository
2. ✅ **Use GitHub Secrets** for all sensitive data
3. ✅ **Rotate secrets** periodically
4. ✅ **Use least privilege** - only grant necessary permissions
5. ✅ **Review workflow logs** to ensure secrets aren't exposed

## Troubleshooting

### Build fails with "Keystore password was incorrect"

- Verify `ANDROID_KEYSTORE_PASSWORD` secret is set correctly
- Check that the keystore file `json-viewer-release-key.jks` exists in the repository

### Build fails with "Key password was incorrect"

- Verify `ANDROID_KEY_PASSWORD` secret is set correctly
- Ensure it matches the key password used when creating the keystore

### Artifacts not uploaded

- Check that build completed successfully
- Verify file paths in the workflow match actual output paths
- Check `if-no-files-found: warn` - warnings won't fail the workflow

