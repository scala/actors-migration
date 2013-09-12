# Releasing Actors Migration to Maven Central

Releasing Actors Migration artifacts to Maven Central is fairly straightforward.

The sbt build is already configured to publish to Sonatype repository which synchronizes
to Maven Central. In order to publish a release you need to run `publish-signed` sbt
command.

The `publish-signed` command is provided by [sbt-pgp](http://www.scala-sbt.org/sbt-pgp/) plugin.
Follow instructions of the plugin on how to install it on your machine.

Note that you need credentials (stored in `$HOME/.ivy2/.credentials`) to publish to Sonatype.
Check sbt's [documentation](http://www.scala-sbt.org/release/docs/Community/Using-Sonatype.html) for details.

Once artifacts are built and deployed to Sonatype staging repository you need to log into
Sonatype website and "close" staged repository. Once that's done your artifacts should be
synchronized with Maven Central within a few hours.
