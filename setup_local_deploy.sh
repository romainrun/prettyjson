#!/bin/bash

# Setup script for local deployment to DeployGate
# This sets up your environment for local builds and deployments

set -e

echo "🚀 Setting up local deployment for PrettyJSON..."
echo ""

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file..."
    cat > .env << EOF
# DeployGate Configuration
DEPLOYGATE_API_TOKEN=deploygate_xgrp_YSr01zerbAnOjpTqRbFWDG2h2aykRq_083vwP
DEPLOYGATE_USER_NAME=app-sportsbook
EOF
    echo "✅ .env file created"
else
    echo "✅ .env file already exists"
fi

# Check if variables are set
source .env 2>/dev/null || true

if [ -z "$DEPLOYGATE_API_TOKEN" ]; then
    echo "⚠️  DEPLOYGATE_API_TOKEN not found in .env"
    echo "Please add it to your .env file"
    exit 1
fi

if [ -z "$DEPLOYGATE_USER_NAME" ]; then
    echo "⚠️  DEPLOYGATE_USER_NAME not found in .env"
    echo "Please add it to your .env file"
    exit 1
fi

echo ""
echo "✅ Environment configured!"
echo "  User: $DEPLOYGATE_USER_NAME"
echo "  Token: ${DEPLOYGATE_API_TOKEN:0:20}..."
echo ""

# Check if Fastlane is installed
if ! command -v fastlane &> /dev/null; then
    echo "❌ Fastlane is not installed"
    echo "📦 Install it with: gem install fastlane"
    exit 1
fi

echo "✅ Fastlane is installed"
echo ""

# Install DeployGate plugin if needed
echo "📦 Installing DeployGate plugin..."
fastlane add_plugin deploygate 2>/dev/null || {
    echo "⚠️  Plugin installation failed, trying manual install..."
    gem install fastlane-plugin-deploygate || {
        echo "❌ Could not install DeployGate plugin"
        exit 1
    }
}

echo ""
echo "✅ Setup complete!"
echo ""
echo "🚀 Ready to deploy! Try:"
echo "   ./deploy.sh 'Your deployment message'"
echo "   or"
echo "   fastlane android deploy message:'Your message'"
echo ""

