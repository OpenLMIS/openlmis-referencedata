#!/usr/bin/env sh

rm -Rf artifacts/*
docker run --rm -v $(pwd):/bzt-configs -v $(pwd)/artifacts:/tmp/artifacts \
  undera/taurus \
  -o modules.jmeter.properties.base-uri='https://test.openlmis.org' \
  config.yml \
  tests/*.yml
