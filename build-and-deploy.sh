#!/bin/bash

# Complete build and deploy script
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}🚀 Build and Deploy Script${NC}"
echo ""

cd /Users/romain.ricquebourg/Projects/PrettyJSON

# Load environment
if [ -f .env ]; then
    source .env
    echo -e "${GREEN}✅ Credentials loaded${NC}"
else
    echo -e "${RED}❌ .env file not found${NC}"
    exit 1
fi

# Step 1: Build APK (debug for faster builds)
echo -e "${YELLOW}📦 Building debug APK (faster)...${NC}"
./gradlew clean assembleDebug --no-daemon --parallel --build-cache

if [ ! -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo -e "${RED}❌ Build failed - APK not found${NC}"
    exit 1
fi

echo -e "${GREEN}✅ APK built successfully${NC}"
echo ""

# Step 2: Deploy to DeployGate
echo -e "${YELLOW}📤 Deploying to DeployGate...${NC}"
MESSAGE="${1:-Deployed: $(date +'%Y-%m-%d %H:%M:%S')}"

# Generate release notes from latest git commits
echo -e "${YELLOW}📝 Generating release notes from git commits...${NC}"
RELEASE_NOTES=$(git log --pretty=format:"• %s" -10 --no-merges 2>/dev/null | head -10)
# If release notes is too long or empty, provide a fallback
if [ -z "$RELEASE_NOTES" ] || [ $(echo "$RELEASE_NOTES" | wc -c) -lt 10 ]; then
    RELEASE_NOTES="Deployed from local machine - $(date +'%Y-%m-%d %H:%M:%S')"
else
    # Add header and format nicely
    RELEASE_NOTES="Latest changes:

$RELEASE_NOTES"
fi

# Limit release notes length (DeployGate may have limits) - keep first 1500 chars
RELEASE_NOTES=$(echo "$RELEASE_NOTES" | head -c 1500)

echo -e "${GREEN}📋 Latest changes:${NC}"
echo "$RELEASE_NOTES" | head -5
echo ""

APK_FILE="app/build/outputs/apk/debug/app-debug.apk"
ORG_NAME="${DEPLOYGATE_ORGANIZATION:-sportx-test3}"

# Upload using DeployGate API
RESPONSE=$(curl -s -X POST \
    -F "token=$DEPLOYGATE_API_TOKEN" \
    -F "file=@$APK_FILE" \
    -F "message=$MESSAGE" \
    -F "release_note=$RELEASE_NOTES" \
    "https://deploygate.com/api/users/$ORG_NAME/apps")

# Check response
if echo "$RESPONSE" | grep -q '"error":\s*true'; then
    echo -e "${RED}❌ Deployment failed${NC}"
    echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
    exit 1
fi

echo -e "${GREEN}✅ Deployment successful!${NC}"
echo ""
echo "Check your DeployGate dashboard:"
echo "https://deploygate.com/organizations/$ORG_NAME/"
echo ""

