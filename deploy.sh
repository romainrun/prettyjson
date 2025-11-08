#!/bin/bash

# Quick deployment script for PrettyJSON
# Builds SIGNED RELEASE AAB (Android App Bundle) ONLY and deploys to DeployGate
# 
# IMPORTANT: This script ONLY builds signed release versions
# - Only signed AAB is built and deployed (no APK, no debug builds)
# - AAB is the required format for Google Play Store submission
# - AAB signature is verified before deployment
# - Release builds are ALWAYS signed (configured in build.gradle.kts)

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 PrettyJSON Deployment Script${NC}"
echo ""

# Load .env file if it exists
if [ -f .env ]; then
    source .env
    echo -e "${GREEN}✅ Loaded credentials from .env file${NC}"
fi

# Check if environment variables are set
if [ -z "$DEPLOYGATE_API_TOKEN" ]; then
    echo -e "${RED}❌ DEPLOYGATE_API_TOKEN not set${NC}"
    echo "Please create a .env file with your credentials"
    echo "Or set it: export DEPLOYGATE_API_TOKEN='your_token'"
    exit 1
fi

# Set default organization if not provided
if [ -z "$DEPLOYGATE_ORGANIZATION" ]; then
    export DEPLOYGATE_ORGANIZATION="sportx-test3"
    echo -e "${YELLOW}ℹ️  Using default organization: sportx-test3${NC}"
fi

echo -e "${GREEN}✅ Environment variables configured${NC}"
echo ""

# Ask for deployment message
if [ -z "$1" ]; then
    echo -e "${YELLOW}Enter deployment message (or press Enter for default):${NC}"
    read -r message
    if [ -z "$message" ]; then
        message="Deployed from local machine - $(date +'%Y-%m-%d %H:%M')"
    fi
else
    message="$1"
fi

echo ""
echo -e "${GREEN}📦 Starting deployment...${NC}"
echo "Message: $message"
echo ""

# Build and deploy using Fastlane
# Try bundle exec first, fall back to direct fastlane if bundle fails
if command -v bundle &> /dev/null && [ -f Gemfile ]; then
    if bundle exec fastlane android deploy message:"$message" release_note:"Deployed from local machine" 2>&1; then
        echo -e "${GREEN}✅ Deployment successful!${NC}"
    else
        echo -e "${YELLOW}⚠️  Bundle exec failed, trying direct fastlane...${NC}"
        fastlane android deploy message:"$message" release_note:"Deployed from local machine"
    fi
else
    fastlane android deploy message:"$message" release_note:"Deployed from local machine"
fi

echo ""
echo -e "${GREEN}✅ Deployment complete!${NC}"
echo ""
echo "Check your DeployGate dashboard for the new build."

