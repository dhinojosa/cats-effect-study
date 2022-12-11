package com.xyzcorp.datatypes

import cats.effect.IO
import cats.effect.std.Random
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funspec.{AnyFunSpec, AsyncFunSpec}
import org.scalatest.matchers.should.Matchers

class RandomSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  describe("the Random trait") {
    it("contains a few methods that are used for generating random values") {
      trait Random[F[_]] {
        def nextInt: F[Int]
        def shuffleList[A](list: List[A]): F[List[A]]
        // ... and more
      }
      IO.unit.assertNoException
    }
  }

  describe("the way to generate a Random") {
    it("has nextInt which generates the next int") {
      val result: IO[Int] = for {
        random <- Random.scalaUtilRandom[IO]
        a <- random.nextInt
        b <- random.nextInt
      } yield (a + b)
      result.asserting(i => i shouldBe an[Int])
    }

    it("has nextInt which generates between int") {
      val result: IO[Int] = for {
        random <- Random.scalaUtilRandom[IO]
        a <- random.betweenInt(0, 10)
      } yield a
      result.asserting(i => i should ((be >= 0).and(be < 10)))
    }

    it("has gaussian returning a Gaussian normally distributed number") {
      val result: IO[Double] = for {
        random <- Random.scalaUtilRandom[IO]
        gaussian <- random.nextGaussian
      } yield gaussian
      result.asserting(i => i should ((be > -3.0).and(be < 3.0)))
    }
  }
}
