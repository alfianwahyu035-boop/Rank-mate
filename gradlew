#!/bin/sh
# RankMate bootstrap, converts to standard Gradle wrapper with `./gradlew wrapper`.
DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
exec java -classpath "$DIR/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
