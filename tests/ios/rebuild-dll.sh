#!/bin/sh

if [ -z "$IKVM_HOME" ]; then
    IKVM_HOME=../../../ikvm-monotouch
fi
MAVEN_REPO=$HOME/.m2/repository
export MONO_PATH=/Developer/MonoTouch/usr/lib/mono/2.1

cd `dirname $0`

echo "Converting Java bytecode to CLR dll..."
rm -f tests-core.dll
mono $IKVM_HOME/bin/ikvmc.exe -nostdlib -debug -target:library -out:tests-core.dll \
    -r:$MONO_PATH/mscorlib.dll \
    -r:$MONO_PATH/System.dll \
    -r:$MONO_PATH/System.Core.dll \
    -r:$MONO_PATH/System.Data.dll \
    -r:$MONO_PATH/OpenTK.dll \
    -r:$MONO_PATH/monotouch.dll \
    -r:$MONO_PATH/Mono.Data.Sqlite.dll \
    $MAVEN_REPO/com/samskivert/pythagoras/1.1/pythagoras-1.1.jar \
    ../../core/target/playn-core-1.2-SNAPSHOT.jar \
    ../../ios/target/playn-ios-1.2-SNAPSHOT.jar \
    ../core/target/playn-tests-core-1.2-SNAPSHOT.jar
