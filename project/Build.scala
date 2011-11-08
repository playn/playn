import sbt._
import Keys._

object PlayNBuild extends Build {
  val commonSettings = Defaults.defaultSettings ++ Seq(
    organization     := "com.googlecode.playn",
    version          := "1.1-SNAPSHOT",
    crossPaths       := false,
    javacOptions     ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
    fork in Compile  := true,
    autoScalaLibrary := false, // no scala-library dependency
    publishArtifact in (Compile, packageDoc) := false, // no scaladocs; it fails
    resolvers        += "Forplay Legacy" at "http://forplay.googlecode.com/svn/mavenrepo"
  )

  val gwtVer = "2.4.0"
  val gwtDeps = Seq(
    "com.google.gwt" % "gwt-user" % gwtVer,
    "com.google.gwt" % "gwt-dev" % gwtVer
  )

  val testDeps = Seq(
    "junit" % "junit" % "4.+" % "test",
 	  "com.novocode" % "junit-interface" % "0.7" % "test->default"
  )

  val locals = new com.samskivert.condep.Depends(
    ("pythagoras", null, "com.samskivert" % "pythagoras" % "1.1")
  )

  //
  // Core projects

  lazy val core = locals.addDeps(Project(
    "core", file("core"), settings = commonSettings ++ Seq(
      name := "playn-core",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
      // adds source files to our jar file (needed by GWT)
      unmanagedResourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedBase <<= baseDirectory { base => base / "disabled" },
      libraryDependencies ++= locals.libDeps ++ testDeps
    )
  ))

  lazy val gwtbox2d = Project(
    "gwtbox2d", file("gwtbox2d"), settings = commonSettings ++ Seq(
      name := "playn-gwtbox2d",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      // adds source files to our jar file (needed by GWT)
      unmanagedResourceDirectories in Compile <+= baseDirectory / "src",
      libraryDependencies ++= gwtDeps
    )
  ) dependsOn(core)

  lazy val webgl = Project(
    "webgl", file("webgl"), settings = commonSettings ++ Seq(
      name := "playn-webgl",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      // adds source files to our jar file (needed by GWT)
      unmanagedResourceDirectories in Compile <+= baseDirectory / "src",
      libraryDependencies ++= gwtDeps
    )
  )

  lazy val html = Project(
    "html", file("html"), settings = commonSettings ++ Seq(
      name := "playn-html",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
      // adds source files to our jar file (needed by GWT)
      unmanagedResourceDirectories in Compile <+= baseDirectory / "src",
      libraryDependencies ++= gwtDeps ++ testDeps ++ Seq(
        "javax.validation" % "validation-api" % "1.0.0.GA", // TODO: sources also
        "com.allen-sauer.gwt.log" % "gwt-log" % "3.1.6",
        "com.allen-sauer.gwt.voices" % "gwt-voices" % "2.1.3"
      )
    )
  ) dependsOn(core, webgl)

  lazy val flash = Project(
    "flash", file("flash"), settings = commonSettings ++ Seq(
      name := "playn-flash",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
      libraryDependencies ++= gwtDeps ++ testDeps ++ Seq(
        "javax.validation" % "validation-api" % "1.0.0.GA", // TODO: sources also
        "com.google.gwt" % "gwt-flash" % "1.0"
      )
    )
  ) dependsOn(core, html)

  lazy val java = Project(
    "java", file("java"), settings = commonSettings ++ Seq(
      name := "playn-java",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
      libraryDependencies ++= testDeps ++ Seq(
        "com.google.guava" % "guava" % "r09",
        "jlayer" % "jlayer" % "1.0.1" // TODO: optional
      )
    )
  ) dependsOn(core)

  lazy val android = Project(
    "android", file("android"), settings = commonSettings ++ Seq(
      name := "playn-android",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
      libraryDependencies ++= testDeps ++ Seq(
        "com.google.android" % "android" % "2.3.3"
      )
    )
  ) dependsOn(core, java)

  lazy val playn = Project("playn", file(".")) aggregate(
    core, gwtbox2d, webgl, java, html, flash, android)

  //
  // Sample projects

  def sampleProject (id :String, extraSettings :Seq[Setting[_]] = Seq()) = Project(
    id + "-sample", file("sample/" + id), settings = commonSettings ++ extraSettings ++ Seq(
      name := ("playn-" + id + "-sample"),
      baseDirectory in run := file("sample/" + id + "/core"),
      unmanagedSourceDirectories in Compile <+= baseDirectory / "core/src"
    )
  ) dependsOn(java)

  // lazy val cute = sampleProject("cute")
  // lazy val hello = sampleProject("hello")
  // lazy val noise = sampleProject("noise")
  // lazy val showcase = sampleProject("showcase", Seq(
  //   libraryDependencies += "com.threerings" % "tripleplay" % "1.0"
  // )) dependsOn(gwtbox2d)

  // lazy val samples = Project("samples", file(".")) aggregate(cute, hello, noise, showcase)
}
