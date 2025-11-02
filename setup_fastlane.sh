#!/bin/bash

# Setup script for Fastlane and DeployGate integration
# Run this script to set up Fastlane for the first time

set -e

echo "🚀 Setting up Fastlane for PrettyJSON..."
echo ""

# Check if Fastlane is installed
if ! command -v fastlane &> /dev/null; then
    echo "❌ Fastlane is not installed."
    echo "📦 Installing Fastlane..."
    
    if command -v bundler &> /dev/null; then
        bundle install
    else
        echo "⚠️  Please install Fastlane manually:"
        echo "   gem install fastlane"
        exit 1
    fi
else
    echo "✅ Fastlane is installed"
fi

echo ""

# Install DeployGate plugin
echo "📦 Installing DeployGate plugin..."
fastlane add_plugin deploygate || {
    echo "⚠️  Failed to install plugin automatically. Installing manually..."
    gem install fastlane-plugin-deploygate
}

echo ""

# Create .env file if it doesn't exist
if [ ! -f .env ]; then
    echo "📝 Creating .env file from .env.example..."
    cp .env.example .env
    echo "⚠️  Please edit .env file and add your DeployGate credentials:"
    echo "   - DEPLOYGATE_API_TOKEN"
    echo "   - DEPLOYGATE_USER_NAME"
    echo ""
else
    echo "✅ .env file already exists"
fi

echo ""
echo "✅ Setup complete!"
echo ""
echo "📋 Next steps:"
echo "1. Edit .env file and add your DeployGate credentials"
echo "2. Get your API token from: https://deploygate.com/settings/api_tokens"
echo "3. Test the setup: fastlane android build"
echo "4. Deploy to DeployGate: fastlane android deploy"
echo ""

