import sbt._
import Keys._

object ForPlayBuild extends Build {
  val buildSettings = Defaults.defaultSettings ++ Seq(
    organization    := "com.googlecode.forplay",
    version         := "1.0-SNAPSHOT",
    crossPaths      := false,
    javacOptions    ++= Seq("-Xlint", "-Xlint:-serial"),
    fork in Compile := true,
    resolvers       += "Local Maven Repository" at Path.userHome.asURL + "/.m2/repository"
  )

  val gwtVer = "2.3.0"
  val gwtUser = "com.google.gwt" % "gwt-user" % gwtVer
  val gwtDev = "com.google.gwt" % "gwt-dev" % gwtVer
  val gwtDeps = Seq(gwtUser, gwtDev)

  val testDeps = Seq(
    "junit" % "junit" % "4.+" % "test",
 	  "com.novocode" % "junit-interface" % "0.7" % "test->default"
  )

  //
  // Core projects

  lazy val core = Project(
    "core", file("core"), settings = buildSettings ++ Seq(
      name := "playn-core",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
      libraryDependencies ++= testDeps ++ Seq(
        "com.samskivert" % "pythagoras" % "1.1-SNAPSHOT"
      )
    )
  )

  lazy val gwtbox2d = Project(
    "gwtbox2d", file("gwtbox2d"), settings = buildSettings ++ Seq(
      name := "playn-gwtbox2d",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      libraryDependencies ++= gwtDeps
    )
  ) dependsOn(core)

  lazy val webgl = Project(
    "webgl", file("webgl"), settings = buildSettings ++ Seq(
      name := "playn-webgl",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      libraryDependencies ++= gwtDeps
    )
  )

  lazy val html = Project(
    "html", file("html"), settings = buildSettings ++ Seq(
      name := "playn-html",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
      libraryDependencies ++= gwtDeps ++ testDeps ++ Seq(
        "javax.validation" % "validation-api" % "1.0.0.GA", // TODO: sources also
        "allen_sauer" % "gwt-log" % "1.0.r613",
        "allen_sauer" % "gwt-voices" % "1.0.r421"
      )
    )
  ) dependsOn(core, webgl)

  lazy val flash = Project(
    "flash", file("flash"), settings = buildSettings ++ Seq(
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
    "java", file("java"), settings = buildSettings ++ Seq(
      name := "playn-java",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
      libraryDependencies ++= testDeps ++ Seq(
        "jlayer" % "jlayer" % "1.0.1" // TODO: optional
      )
    )
  ) dependsOn(core)

  lazy val android = Project(
    "android", file("android"), settings = buildSettings ++ Seq(
      name := "playn-android",
      unmanagedSourceDirectories in Compile <+= baseDirectory / "src",
      unmanagedSourceDirectories in Test <+= baseDirectory / "tests",
      libraryDependencies ++= testDeps ++ Seq(
        "com.google.android" % "android" % "2.3.3"
      )
    )
  ) dependsOn(core, java)

  lazy val forplay = Project("forplay", file(".")) aggregate(
    core, gwtbox2d, webgl, java, html, flash, android)

  //
  // Sample projects

  def sampleProject (id :String) = Project(
    id + "-sample", file("sample/" + id), settings = buildSettings ++ Seq(
      name := ("playn-" + id + "-sample"),
      unmanagedSourceDirectories in Compile <+= baseDirectory / "core/src"
    )
  ) dependsOn(core)

  lazy val cute = sampleProject("cute")
  lazy val hello = sampleProject("hello")
  lazy val noise = sampleProject("noise")
  lazy val peas = sampleProject("peas")
  lazy val sprites = sampleProject("sprites")
  lazy val text = sampleProject("text")

  lazy val samples = Project("samples", file(".")) aggregate(
    cute, hello, noise, peas, sprites, text)
}
