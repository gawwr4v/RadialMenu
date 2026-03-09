#!/bin/bash
# Run this script to verify local Maven publishing works
# before pushing to CI/CD
#
# Usage: bash scripts/verify-local-publish.sh
#
# What it does:
# 1. Publishes the library to ~/.m2 (local Maven repo)
# 2. Checks that .asc signature files were generated
# 3. Prints the full artifact path for manual inspection

set -e

echo "================================================"
echo " RadialMenu — Local Publish Verification"
echo "================================================"
echo ""

echo "Step 1: Publishing to mavenLocal..."
./gradlew :radialmenu:publishToMavenLocal

echo ""
echo "Step 2: Checking for artifacts..."
ARTIFACT_PATH="$HOME/.m2/repository/io/github/gawwr4v/radialmenu-android/1.0.0"

if [ -d "$ARTIFACT_PATH" ]; then
    echo "✅ Artifact directory found: $ARTIFACT_PATH"
    echo ""
    echo "Files:"
    ls -la "$ARTIFACT_PATH"
    echo ""

    ASC_COUNT=$(ls "$ARTIFACT_PATH"/*.asc 2>/dev/null | wc -l)
    if [ "$ASC_COUNT" -gt 0 ]; then
        echo "✅ GPG signature files (.asc) found: $ASC_COUNT files"
    else
        echo "❌ No .asc signature files found!"
        echo "   This means GPG signing is not configured."
        echo "   Check your ~/.gradle/gradle.properties for:"
        echo "   signing.keyId=XXXXXXXX"
        echo "   signing.password=your_passphrase"
        echo "   signing.secretKeyRingFile=/path/to/secring.gpg"
        exit 1
    fi
else
    echo "❌ Artifact directory not found at: $ARTIFACT_PATH"
    echo "   Publishing may have failed. Run:"
    echo "   ./gradlew :radialmenu:publishToMavenLocal --stacktrace"
    exit 1
fi

echo ""
echo "================================================"
echo " ✅ All checks passed! Safe to publish to CI."
echo "================================================"
