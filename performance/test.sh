#!/usr/bin/env sh

docker run --rm -v $(pwd):/bzt-configs \
  -v $(pwd)/../build/performance-artifacts:/tmp/artifacts \
  undera/taurus \
  -o modules.jmeter.properties.base-uri='https://test.openlmis.org' \
  -o reporting.2.dump-xml=/tmp/artifacts/stats.xml \
  config.yml \
  tests/*.yml
