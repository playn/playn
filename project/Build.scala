import java.io.File
import sbt._
import Keys._
import net.thunderklaus.GwtPlugin._

object PlayNBuild extends samskivert.MavenBuild {

  // our source and tests are in non-standard places
  val srcDirSettings = seq(
    javaSource in Compile <<= baseDirectory / "src",
    javaSource in Test <<= baseDirectory / "tests"
  )
  // avoid publishing our test projects to Ivy
  val testSettings = seq(
    publishLocal := false,
    publish := false
  )

  def excludePath (path :String) :FileFilter = new FileFilter {
    def accept (f :File) :Boolean = f.getAbsolutePath.contains(path)
  }

  override val globalSettings = Seq(
    crossPaths   := false,
    scalaVersion := "2.9.2",
    javacOptions ++= Seq("-Xlint", "-Xlint:-serial", "-source", "1.6", "-target", "1.6"),
    javaOptions ++= Seq("-ea"),
    fork in Compile := true,
    autoScalaLibrary := false, // no scala-library dependency
    publishArtifact in (Compile, packageDoc) := false, // no scaladocs; it fails
    resolvers    += "Forplay Legacy" at "http://forplay.googlecode.com/svn/mavenrepo",
    // no parallel test execution to avoid confusions
    parallelExecution in Test := false
  )

  override def moduleSettings (name :String, pom :pomutil.POM) = name match {
    case "core" => srcDirSettings ++ seq(
      unmanagedBase <<= baseDirectory { base => base / "disabled" },
      // tests depends on resource files mixed into source directory, yay!
      unmanagedResourceDirectories in Test <+= baseDirectory / "tests",
      libraryDependencies ++= Seq(
        "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
    case "java" | "android" => srcDirSettings ++ seq(
      libraryDependencies ++= Seq(
        "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
    case "jbox2d" => srcDirSettings ++ seq(
      // exclude GWT generator and supersource code
      excludeFilter in unmanagedSources ~= {
        _ || "PoolingStackGenerator.java" || excludePath("org/jbox2d/emul")
      }
    )
    case "webgl" | "flash" | "ios" => srcDirSettings
    case "html" => srcDirSettings ++ seq(
      // exclude GWT supersource code
      excludeFilter in unmanagedSources ~= { _ || excludePath("playn/super") }
    )
    case "tests-assets" => testSettings
    case "tests-core" => testSettings
    case "tests-java" => LWJGLPlugin.lwjglSettings ++ testSettings ++ seq(
      LWJGLPlugin.lwjgl.version := pom.getAttr("lwjgl.version").get
    )
    case "tests-html" => gwtSettings ++ testSettings ++ seq(
      gwtVersion := pom.getAttr("gwt.version").get,
      javaOptions in Gwt ++= Seq("-mx512M"), // give GWT mo' memory
      libraryDependencies ++= Seq(
        "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",
        "com.novocode" % "junit-interface" % "0.7" % "test->default"
      )
    )
    case _ => Nil
  }

  override protected def projects (builder :samskivert.ProjectBuilder) =
    super.projects(builder) ++ Seq(builder("tests-assets"), builder("tests-core"),
                                   builder("tests-java"), builder("tests-html"))
}
