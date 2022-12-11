package com.xyzcorp.datatypes

import cats._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{Clock, IO, SyncIO}
import cats.implicits._
import org.scalatest._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should
import org.scalatest.matchers.should._

class ClockSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  it("can be used for an F[_] which is either IO, Sync, or Async") {
    def timeItem[F[_], A](x: A)(implicit ck: Clock[F], monad: Monad[F]) = {
      for {
        x1 <- ck.monotonic
        y2 <- monad.pure(x)
        z3 <- ck.monotonic
      } yield (y2, z3 - x1)
    }

    val result = timeItem[IO, Int](30)
    result.asserting(t => t._1 should be(30))
    result.asserting(t => t._2.length should be > 200L)
  }


}
