import sbt._
import Keys._
import com.typesafe.sbt.SbtSite.site
import com.typesafe.sbt.SbtSite.SiteKeys._
import com.typesafe.sbt.site.JekyllSupport.Jekyll
import com.typesafe.sbt.SbtGhPages.ghpages
import com.typesafe.sbt.SbtGit.git
import de.johoop.jacoco4sbt._
import JacocoPlugin._

object MigraitonDef extends Build {

  val actorsMigration = (Project("scala-actors-migration", file(".")) settings (
    organization := "org.scala-lang",
    name := "scala-actors-migration",
    version := "1.0.0",
    scalaVersion := "2.11.0-M4",
    scalaBinaryVersion <<= scalaVersion,
    parallelExecution in Test := true,
    resolvers += "junit interface repo" at "https://repository.jboss.org/nexus/content/repositories/scala-tools-releases",
    resolvers += "Sonatype Snapshots repo" at "https://oss.sonatype.org/content/repositories/snapshots/",
    resolvers += "Sonatype Release" at "https://oss.sonatype.org/content/repositories/releases",
    libraryDependencies <++= scalaVersion apply dependencies) settings (publishSettings: _*) settings (websiteSettings: _*))

  def publishSettings: Seq[Setting[_]] = Seq(
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    // If we want on maven central, we need to be in maven style.
    publishMavenStyle := true,
    publishArtifact in Test := false,
    // The Nexus repo we're publishing to.
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    // Maven central cannot allow other repos.  We're ok here because the artifacts we
    // we use externally are *optional* dependencies.
    pomIncludeRepository := { _ => false },
    // Maven central wants some extra metadata to keep things 'clean'.
    pomExtra := (
      <licenses>
        <license>
          <name>BSD-like</name>
          <url>
            http://www.scala-lang.org/downloads/license.html
          </url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <url>http://scala.github.com/actors-migration</url>
      <scm>
        <url>git@github.com:scala/actors-migration.git</url>
        <connection>scm:git:git@github.com:scala/actors-migration.git</connection>
      </scm>
      <developers>
        <developer>
          <id>vjovanov</id>
          <name>Vojin Jovanovic</name>
          <url>http://www.vjovanov.com</url>
        </developer>
      </developers>))

  def websiteSettings: Seq[Setting[_]] = (
    site.settings ++
    ghpages.settings ++
    site.includeScaladoc() ++
    site.jekyllSupport() ++
    Seq(
      git.remoteRepo := "git@github.com:scala/actors-migration.git",
      includeFilter in Jekyll := ("*.html" | "*.png" | "*.js" | "*.css" | "CNAME"),
      // the migration guide goes to scaladoc
      excludeFilter in Jekyll := ("actors-migration-guide.html")
    )
  )

  def dependencies(sv: String) = Seq(
    "org.scala-lang" % "scala-actors" % sv,
    "junit" % "junit" % "4.10" % "test",
    "com.novocode" % "junit-interface" % "0.10-M2" % "test->default")

}
