#!/bin/bash

set +x
set -e

echo "----- Printing test of env variables for Jenkinsfile -----"
echo "Staging version: $STAGING_VERSION"
echo "Build number: $BUILD_NUMBER"
echo "Git Branch: $GIT_BRANCH"
echo "ENV File: $ENV_FILE"