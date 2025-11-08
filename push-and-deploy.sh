#!/bin/bash

# Git push wrapper that automatically deploys after successful push
# Usage: ./push-and-deploy.sh [git push arguments]

set -e

echo "🚀 Git Push & Deploy Script"
echo ""

# Perform git push with all arguments passed to this script
echo "📤 Pushing to remote repository..."
if git push "$@"; then
    echo ""
    echo "✅ Git push successful!"
    echo ""
    
    # Source .env if it exists
    if [ -f .env ]; then
        source .env
    fi
    
    # Check if deployment script exists
    if [ -f deploy.sh ]; then
        echo "🚀 Starting automatic deployment..."
        
        # Get commit message for deployment
        COMMIT_MSG=$(git log -1 --pretty=%B)
        COMMIT_HASH=$(git rev-parse --short HEAD)
        
        DEPLOY_MSG="Auto-deploy: ${COMMIT_HASH} - ${COMMIT_MSG}"
        
        # Run deployment
        ./deploy.sh "$DEPLOY_MSG"
        
        if [ $? -eq 0 ]; then
            echo ""
            echo "✅ Deployment successful!"
        else
            echo ""
            echo "❌ Deployment failed. Check logs above."
            exit 1
        fi
    else
        echo "⚠️  deploy.sh not found. Skipping deployment."
        echo "   You can deploy manually with: ./deploy.sh \"Your message\""
    fi
else
    echo ""
    echo "❌ Git push failed. Deployment skipped."
    exit 1
fi

