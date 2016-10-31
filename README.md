PlayN
=====

PlayN is a cross-platform Java game development library written in [Java] that targets HTML5
browsers (via [GWT]), desktop JVMs, [Android] and [iOS] devices.

Information on PlayN can be found in in this README and on the [PlayN project website].

PlayN developers, contributors, and users regularly communicate on the [PlayN Google Group].


= Trying current builds

You can try the sample test app from here:
- android - https://dl.bintray.com/raisercostin/maven/io/playn/tests/playn-tests-android/2.0.0/:playn-tests-android-2.0.0.apk
- ios - TODO


Building and Running
--------------------

It is not necessary to build PlayN to use it. PlayN is shipped to [Maven Central] from where it
will be automatically downloaded by projects that use it. However, you still may find it useful to
build a local copy of PlayN in case you need to modify it when debugging your own game, to add new
features, or to fix bugs you find in the PlayN code.

- Building PlayN locally is very easy using [Maven]:

```
cd playn
mvn clean install 
```

This will install the latest snapshot version of PlayN into your local Maven repository. You can
then modify the `playn.version` property of your game to reference that snapshot version and your
game will use your local copy of PlayN instead of a version downloaded from [Maven Central].

Instructions for building and running the PlayN sample games can be found in the [Documentation].
- To install without executing or deploying anything on devices
	```mvn clean install -DskipTests -DskipExec -Pall```
- To deploy artifacts to bintray
	```
	cd playn
	mvn versions:set
	mvn deploy -Prelease -Pall -DskipTests -DskipExec
	```

- To release
	```
	cd playn
	mvn release:prepare release:perform -DskipTests=true -Prelease -Darguments="-DskipTests=true -Prelease"
	```

- Useful for maven
     - bugvm plugin goals: ```mvn bugvm:```
	- bugvm plugin description: ```mvn help:describe -Dcmd=bugvm:archive``` 
	- bugvm plugin detailed description: ```mvn help:describe -Dcmd=bugvm:archive -Ddetail``` 

- Useful for bugvm
	- create *.ipa archive for distribution ```mvn bugvm:archive```

- There are several profiles:
  - all - includes all modules including android and ios that require special dev environment: osx, codex, android ide


Licensing
---------

Unless otherwise stated, all source files are licensed under the Apache License, Version 2.0:

```
Copyright 2011-2015 The PlayN Authors

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF
ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
```

[Android]: http://www.android.com/
[Documentation]: http://playn.github.io/docs/
[GWT]: http://code.google.com/webtoolkit/
[Java]: http://www.java.com/
[Maven Central]: http://search.maven.org/
[Maven]: http://maven.apache.org/
[PlayN Google Group]: http://groups.google.com/group/playn
[PlayN project website]: http://playn.github.io/
[iOS]: https://developer.apple.com/devcenter/ios/index.action
