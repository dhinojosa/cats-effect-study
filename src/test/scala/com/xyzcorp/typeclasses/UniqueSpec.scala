package com.xyzcorp.typeclasses

import cats.effect.IO
import cats.effect.kernel.Unique
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funspec.{AnyFunSpec, AsyncFunSpec}
import org.scalatest.matchers.should.Matchers

class UniqueSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  describe("Unique[F] provides a unique number") {
    it(
        """has a unique method that returns the unique number.
          |  A unique token just seems to be an empty object with unique
          |  hash code and identity""".stripMargin) {
      val tokenIO = for {
        _ <- IO.println("Retrieving a unique number")
        token1 <- Unique[IO].unique
        _ <- IO.println(token1)
      } yield token1
      tokenIO.asserting(t => t shouldBe a[Unique.Token])
    }
  }
}
