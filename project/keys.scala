object VersionKeys {
  import sbt.settingKey

  // To facilitate scripted build of all modules (while we're working on getting dbuild up and running)
  val continuationsVersion = settingKey[String]("Version to use for the scala-continuations-library dependency.")
}
