package com.xyzcorp.concurrency

import cats.effect.concurrent.Deferred
import cats.effect.{ContextShift, IO}
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class DeferredSpec extends FunSpec with Matchers {
  describe("Deferred is analogous to a Promise in regular Scala parlance") {
    it("""has the following structure, where get, gets the value and
         |  complete offers the complete answer""".stripMargin) {
      abstract class Deferred[F[_], A] {
        def get: F[A]
        def complete(a: A): F[Unit]
      }
    }
    it("""requires a context shift established. Notice that Deferred
         |  returns a IO[Deferred[IO, Int]] which I suspect is used for
         |  monadic for comprehensions""".stripMargin) {
      implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
      val deferred: IO[Deferred[IO, Int]] = Deferred[IO, Int]
      val value2: IO[Int] = deferred.flatMap(_.get)
      deferred.flatMap(df => df.complete(40))
      val value1: IO[Int] = deferred.flatMap(df => df.get)
      value1.flatMap(x => IO(println(x)))
      println("Are we able to complete?")
    }
    it("""can be converted into for-comprehension, because they are all
         |  members of IO[_]""".stripMargin) {
      implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
      val io = for {
        _ <- IO(println("Starting Deferred"))
        d <- Deferred[IO, Int]
        _ <- IO(println("Waiting"))
        v <- IO.pure(d.get).start
        _ <- IO(println("Waiting some more"))
        _ <- IO(println("And answer!"))
        _ <- IO(println(s"Answer is: $v"))
      } yield ()

      import scala.concurrent.duration._
      io.unsafeRunTimed(10 seconds)
    }
  }
}
