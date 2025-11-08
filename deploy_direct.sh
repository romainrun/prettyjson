#!/bin/bash

# Direct deployment to DeployGate using curl
# This script builds the APK and uploads it directly to DeployGate

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}🚀 Direct DeployGate Deployment${NC}"
echo ""

# Load .env
if [ -f .env ]; then
    source .env
    echo -e "${GREEN}✅ Loaded credentials${NC}"
else
    echo -e "${RED}❌ .env file not found${NC}"
    exit 1
fi

# Check credentials
if [ -z "$DEPLOYGATE_API_TOKEN" ] || [ -z "$DEPLOYGATE_ORGANIZATION" ]; then
    echo -e "${RED}❌ Missing credentials in .env${NC}"
    exit 1
fi

# Build message
MESSAGE="${1:-Deployed from local machine - $(date +'%Y-%m-%d %H:%M:%S')}"

echo -e "${GREEN}📦 Building APK...${NC}"
echo ""

# Build the release APK
./gradlew assembleRelease

if [ ! -f "app/build/outputs/apk/release/app-release.apk" ]; then
    echo -e "${RED}❌ APK build failed${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✅ APK built successfully${NC}"
echo ""

APK_FILE="app/build/outputs/apk/release/app-release.apk"
ORG_NAME="$DEPLOYGATE_ORGANIZATION"

echo -e "${GREEN}📤 Uploading to DeployGate...${NC}"
echo "Organization: $ORG_NAME"
echo "Message: $MESSAGE"
echo ""

# Upload using DeployGate API
RESPONSE=$(curl -s -X POST \
    -F "token=$DEPLOYGATE_API_TOKEN" \
    -F "file=@$APK_FILE" \
    -F "message=$MESSAGE" \
    -F "release_note=Deployed from local machine" \
    "https://deploygate.com/api/users/$ORG_NAME/apps")

echo "$RESPONSE" | grep -q '"error"' && {
    echo -e "${RED}❌ Upload failed${NC}"
    echo "$RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE"
    exit 1
}

echo -e "${GREEN}✅ Deployment successful!${NC}"
echo ""
echo "Check your DeployGate dashboard:"
echo "https://deploygate.com/organizations/$ORG_NAME/"
echo ""

