package com.xyzcorp.datatypes

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{FiberIO, IO}
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

class FiberSpec extends AsyncFunSpec with AsyncIOSpec with Matchers  {
  describe("Fibers are virtual threads that don't take an entire OS Thread when blocking") {
    it(
        """can be create by the start method, here we will use a join, but
          |  this is not optimal since this would cause a race issue""".stripMargin) {
        val fiber1: IO[FiberIO[Int]] = IO(10).start
        val fiber2: IO[FiberIO[Int]] = IO(10).start
        val composed = for {
            fiberIO1 <- fiber1
            fiberIO2 <- fiber2
            outcome1 <- fiberIO1.join.onError(_ => fiberIO1.cancel)
            outcome2 <- fiberIO2.join.onError(_ => fiberIO2.cancel)
        } yield (outcome1, outcome2)
        info("Note that Outcome is not monadic")
        composed.asserting(o => o._1.isSuccess should be (true))
        composed.asserting(o => o._2.isSuccess should be (true))
    }
  }
}
