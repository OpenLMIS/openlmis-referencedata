#!/bin/bash

set +x
set -e

function finish {
  docker-compose -f docker-compose.builder.yml down --volumes
}
trap finish EXIT

sudo rm -f .env
cp $ENV_FILE .env

docker-compose -f docker-compose.builder.yml run -e BUILD_NUMBER=$BUILD_NUMBER -e GIT_BRANCH=$GIT_BRANCH builder
docker-compose -f docker-compose.builder.yml build image
docker-compose -f docker-compose.builder.yml down --volumes
docker tag openlmis/referencedata:latest openlmis/referencedata:$STAGING_VERSION
docker push openlmis/referencedata:$STAGING_VERSION

