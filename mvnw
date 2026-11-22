#!/bin/sh
set -eu

WRAPPER_JAR="$(dirname "$0")/.mvn/wrapper/maven-wrapper.jar"
exec java -Dmaven.multiModuleProjectDirectory="$(cd "$(dirname "$0")" && pwd)" \
  -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"
