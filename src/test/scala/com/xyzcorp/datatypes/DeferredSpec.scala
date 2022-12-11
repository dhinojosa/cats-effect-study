package com.xyzcorp.datatypes

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should._

import scala.language.postfixOps

class DeferredSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  describe("Deferred is analogous to a Promise in regular Scala parlance") {
    it("""has the following structure, where get, gets the value and
         |  complete offers the complete answer""".stripMargin) {
      abstract class Deferred[F[_], A] {
        def get: F[A]
        def complete(a: A): F[Unit]
      }
      IO.unit
    }

    it("""can be converted into for-comprehension, because they are all
         |  members of IO[_]""".stripMargin) {
      val io = for {
        _ <- IO.println("Starting Deferred")
        deferred <- Deferred[IO, Int]
        _ <- IO.println("Waiting")
        fiber <- IO.pure(deferred.get).start
        _ <- IO.println("Waiting some more")
        _ <- IO.println("And answer!")
        _ <- deferred.complete(100)
        outcome <- fiber.join.onError(_ => fiber.cancel)
        resolution <- outcome.embed(onCancel = IO(IO(-1)))
        result <- resolution
        _ <- IO.println(s"Answer is: $result")
      } yield result
      io.asserting(v => v should be(100))
    }
  }
}
