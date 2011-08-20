PlayN
=====

Playn is a cross-platform Java game development library written in [Java] that
targets browsers (via [Google Web Toolkit]), [Android] devices and [Flash].

Information on PlayN can be found in in this README and on the PlayN project
website: http://code.google.com/p/playn

PlayN developers, contributors, and users regularly communicate on the PlayN
Google Group: http://groups.google.com/group/playn

Building
--------

PlayN requires Java 6 or higher. It can be built using one of [Ant], [Maven],
or [Eclipse].

### Getting the source

The PlayN source code is available via Git. You will need to install a [Git]
client. Check out the source code as follows:

    git clone https://code.google.com/p/playn/

The subsequent build instructions will refer to the directory in which you
checked out the PlayN source as `playn`.

### Building with Ant

Build and install the PlayN jar files into your local Maven repository like so:

    cd playn
    ant install

### Building with Maven

Build and install the PlayN jar files into your local Maven repository like so:

    cd playn
    mvn install

### Building with Eclipse

To build PlayN with Eclipse, you must first install the [Google Plugin for
Eclipse] and install support for [GWT] and [Android] development. Detailed
instructions on performing these installations are available on the [Google
Plugin for Eclipse] website.

Once that is installed, choose _File_ → _Import_ and select _Maven_ → _Existing
Maven Projects_. Then enter the directory into which you checked out PlayN as
the root directory, and Eclipse will autodetect the various PlayN subprojects.

Running Sample Games
--------------------

PlayN comes with a number of sample games, to demonstrate its capabilities.
These are located in `playn/sample`.

### Running from the command line

To run the samples from the command line, you must use [Ant]. As PlayN is a
cross-platform game development library, there are multiple ways to run the
sample games. Presently, you can run them using the JVM backend, and the HTML5
backend as compiled JavaScript, and the HTML5 backend via the GWT development
mode.

The following instructions are for the `cute` sample, but can be applied to the
other samples by simply substituting the appropriate directory for the desired
sample.

To run via the JVM backend, do the following:

    cd playn/sample/cute
    ant run-java

To compile the code into JavaScript and run in a web browser, do the following:

    cd playn/sample/cute
    ant run-html
    # the Ant output will display a URL to be opened in your browser
    # in cute's case it is: http://localhost:8080/cute/CuteGame.html

To run the code via the GWT development mode, do the following:

    cd playn/sample/cute
    ant run-devmode
    # then click 'Launch Default Browser' in the devmode console
    # you may need to install the GWT devmode plugin at this point

Note that testing in GWT devmode is not recommended. Performance is extremely
poor in GWT devmode due to the way JavaScript and Java communicate. Performance
of the compiled JavaScript is substantially better.

### Running from Eclipse

To run the samples from Eclipse, right click on, for example, the
`playn-cute-core` project in the _Package Explorer_ and select _Run as_ → _Java
Application_. Then double click on `CuteGameJava` in the dialog that popus up.
This will launch the cute sample game using the JVM backend.

To run the HTML versions of the game, one must first manually configure Google
Web Toolkit support on the project (this manual step will hopefully eventually
be eliminated when the GWT Eclipse plugin autodetects GWT support). Right click
on, for example, `playn-cute-html` and select _Properties_. Navigate to
_Google_ → _Web Toolkit_ and click the checkbox labeled _Use Google Web
Toolkit_. Once this has been performed once for a project, it need not be done
again.

Now right click on `playn-cute-html` and select _Run as_ → _Web Application_.
This will run the game using GWT devmode, which you will immediately discover
is incredibly slow. However, it can sometimes be useful for simple debugging.

To compile the game into JavaScript, right click on `playn-cute-html` and
select _Google_ → _GWT Compile_. This will generate the JavaScript version of
the game in the `war` subdirectory. You have to put that somewhere that a web
server can serve it, or use the Ant instructions above for a turn-key way of
testing the HTML version of the game.

Using PlayN
-------------

To use PlayN in your project, you must currently check out the source and build
and install the PlayN artifacts into your local Maven repository. PlayN will
eventually ship stable artifacts to the Maven Central repository, and snapshot
artifacts to the SonaType OSS repository, but that's not set up yet.

Once you have built and installed PlayN (per the directions in the _Building_
section above), you can add a dependency on PlayN to your Maven project build
like so:

    <dependency>
      <groupId>com.googlecode.playn</groupId>
      <artifactId>playn-BACKEND</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>

`BACKEND` should be replaced with `html` for your build that generates an HTML
version of your game, `android` for your build that generates an Android
version of your game, and `core` for your build that you use to test your game
by running it on the JVM.

If you use Ivy or another build system that supports fetching artifacts from
Maven repositories, you can simply translate the above into the syntax used by
your build system.

Licensing
---------

Unless otherwise stated, all source files are licensed under the Apache
License, Version 2.0:

    Copyright 2011 The PlayN Authors

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations
    under the License.

### Additional license info

Code under `gwtbox2d`:

* URL: http://www.jbox2d.org/
* License: zlib
* Description: GWT-friendly adaptation of JBox2D.
* Local modifications: Various modifications to support cross-compilation to
  supported platforms.

Image resources under `sample/(cute|peas)`:

* URL: http://www.lostgarden.com/2007/03/lost-garden-license.html
* License: Creative Commons (CC BY 3.0) -
* http://creativecommons.org/licenses/by/3.0/us/
* Description: Game art resources created by Dan Cook.

Audio resources under `sample/noise`:

* URL: http://www.freesound.org/
* License: Creative Commons (various) - http://creativecommons.org/
* Description: Sample audio resources from the Freesound project.

[Java]: http://www.java.com/
[Google Web Toolkit]: http://code.google.com/webtoolkit/
[GWT]: http://code.google.com/webtoolkit/
[Android]: http://www.android.com/
[Flash]: http://www.adobe.com/products/flash.html
[Ant]: http://ant.apache.org/
[Maven]: http://maven.apache.org/
[Eclipse]: http://www.eclipse.org/
[Git]: http://git-scm.com/
[Google Plugin for Eclipse]: http://code.google.com/eclipse/
