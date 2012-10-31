import sbt._
import Keys._
import com.typesafe.sbt.SbtSite._
import SiteKeys._
import com.typesafe.sbt.SbtGhPages.ghpages
import com.typesafe.sbt.SbtGit.git

object PluginDef extends Build {

  val actorsMigration = (Project("scala-actors-migration", file(".")) settings(
    organization := "org.scala-lang",
    name := "scala-actors-migration",
    version <<= scalaVersion.identity,
    scalaVersion := "2.10.0-RC1",
    resolvers += "junit interface repo" at "https://repository.jboss.org/nexus/content/repositories/scala-tools-releases",
    resolvers += "Sonatype Snapshots repo" at "https://oss.sonatype.org/content/repositories/snapshots/",
    libraryDependencies ++= scalaVersion apply dependencies
  ) settings(publishSettings:_*) settings(websiteSettings:_*))

  def publishSettings: Seq[Setting[_]] = Seq(
    // If we want on maven central, we need to be in maven style.
    publishMavenStyle := true,
    publishArtifact in Test := false,
    // The Nexus repo we're publishing to.
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots") 
      else                             Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    // Maven central cannot allow other repos.  We're ok here because the artifacts we
    // we use externally are *optional* dependencies.
    pomIncludeRepository := { x => false },
    // Maven central wants some extra metadata to keep things 'clean'.
    pomExtra := (
      <licenses>
        <license>
          <name>BSD-like</name>
          <url>http://www.scala-lang.org/downloads/license.html
          </url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <url>http://docs.scala-lang.org/overviews/actor-migration</url>
      <scm>
        <url>git@github.com:scala/actors-migration.git</url>
        <connection>scm:git:git@github.com:scala/actors-migration.git</connection>
      </scm>
      <developers>
        <developer>
          <id>vjovanov</id>
          <name>Vojin Jovanovic</name>
          <url>http://vjovanov.com</url>
        </developer>
      </developers>)
  )

  def websiteSettings: Seq[Setting[_]] = site.settings ++ ghpages.settings ++ Seq(
    git.remoteRepo := "git@github.com:vjovanov/actors-migration.git",
    siteMappings <++= (baseDirectory, target, streams) map { (dir, out, s) => 
      val jekyllSrc = dir / "src" / "jekyll"
      val jekyllOutput = out / "jekyll"
      // Run Jekyll
      sbt.Process(Seq("jekyll", jekyllOutput.getAbsolutePath), Some(jekyllSrc)).!;
      // Figure out what was generated.
      (jekyllOutput ** ("*.html" | "*.png" | "*.js" | "*.css" | "CNAME") x relativeTo(jekyllOutput))
    }
  )

  def dependencies(sv: String) = Seq(
    "junit" % "junit" % "4.5" % "test",
    "com.novocode" % "junit-interface" % "0.7" % "test->default",
    "org.scala-lang" % "scala-actors" % sv
  )

  
}
