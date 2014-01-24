// core build/testing/publishing -- does not need any sbt plugins

name                 := "scala-actors-migration"

organization         := "org.scala-lang"

licenses             := Seq("BSD 3-clause" -> url("http://opensource.org/licenses/BSD-3-Clause"))

homepage             := Some(url("http://scala.github.com/actors-migration"))

organizationHomepage := Some(url("http://www.scala-lang.org"))

scmInfo              := Some(ScmInfo(url("https://github.com/scala/actors-migration.git"),"git://github.com/scala/actors-migration.git"))

// on release, set version using sbt commands -- for nightlies, default should be -SNAPSHOT
version              := "1.0.0-SNAPSHOT"

scalaVersion         := "2.11.0-M8"

libraryDependencies  += "org.scala-lang" % "scala-actors"      % scalaVersion.value

// for continuations library
libraryDependencies  += "org.scala-lang" % "scala-library-all" % scalaVersion.value

// testing
libraryDependencies  += "junit"          % "junit"             % "4.10" % "test"

libraryDependencies  += "com.novocode"   % "junit-interface"   % "0.10" % "test"

parallelExecution in Test := true

// for integration testing against scala snapshots
resolvers += Resolver.sonatypeRepo("snapshots")

// so we don't have to wait for maven central synch
resolvers += Resolver.sonatypeRepo("releases")


// publishing
credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// If we want on maven central, we need to be in maven style.
publishMavenStyle := true

publishArtifact in Test := false

// The Nexus repo we're publishing to.
publishTo := Some(
  if (version.value.trim.endsWith("SNAPSHOT")) Resolver.sonatypeRepo("snapshots")
  else Opts.resolver.sonatypeStaging
)

// Maven central cannot allow other repos.  We're ok here because the artifacts we
// we use externally are *optional* dependencies.
pomIncludeRepository := { _ => false }

pomExtra := (
  <developers>
    <developer>
      <id>vjovanov</id>
      <name>Vojin Jovanovic</name>
      <url>http://www.vjovanov.com</url>
    </developer>
  </developers>
)
