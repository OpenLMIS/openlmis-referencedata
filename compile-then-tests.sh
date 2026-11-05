docker run --platform=linux/amd64 \
  -v "$(pwd)":/app \
  -v "$(pwd)/.gradle:/root/.gradle" \
  -w /app --network=host \
  amazoncorretto:8u342-alpine3.16 \
  sh -c "./gradlew clean compileJava --no-daemon --no-build-cache 2>&1 | awk '
/^Errors occurred while build effective model/,/^Note: Some input files use unchecked or unsafe operations\.$/ { next }
{ print }
' && ./gradlew test -x compileJava --warning-mode=none --no-daemon --no-build-cache"