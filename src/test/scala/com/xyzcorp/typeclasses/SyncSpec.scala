package com.xyzcorp.typeclasses

import cats.effect.{Clock, IO, Sync}
import org.scalatest.{FunSpec, Matchers}

class SyncSpec extends FunSpec with Matchers {
  describe("""A Monad that can suspend the execution of
             |  side effects in the F[_] context.""".stripMargin) {
    val f = Sync[IO]
    val a: IO[Int] = f.delay(14 + 10)
  }
}
