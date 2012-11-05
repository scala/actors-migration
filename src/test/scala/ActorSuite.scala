package scala.actors.migration

trait ActorSuite {

  @org.junit.Before
  final def beforeClass(): Unit = {
    // prevents a deadlock that happens in the scala library
    synchronized { 
      val x = scala.actors.Actor.State.New
      if (x == scala.actors.Actor.State.Runnable) {
        throw new RuntimeException("Just in case jvm is smart.")
      }
    }
  }
}