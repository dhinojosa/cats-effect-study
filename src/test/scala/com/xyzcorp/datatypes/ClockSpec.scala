package com.xyzcorp.datatypes

import cats._
import cats.implicits._
import cats.effect.{Clock, Sync}
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration.{MILLISECONDS, TimeUnit}
class ClockSpec extends FunSpec with Matchers {

  implicit val clock: Clock[Option] = new Clock[Option] {
    override def realTime(unit: TimeUnit): Option[Long] = Option(System.currentTimeMillis())

    override def monotonic(unit: TimeUnit): Option[Long] = Option(System.currentTimeMillis())
  }

  it("can be used for an F[_]?") {
    def timeItem[F[_], A](x: A)(implicit ck: Clock[F], monad: Monad[F]) = {
      for {
        x1 <- ck.monotonic(MILLISECONDS)
        y2 <- monad.pure(x)
        z3 <- ck.monotonic(MILLISECONDS)
      } yield (y2, z3 - x1)
    }

    timeItem[Option, Int](30) should be(Some(30, 3))
  }

  it("is a datatype and not a type class to create timestamps") {
    def displayTime[F[_]: Functor](implicit c: Clock[F], s: Sync[F], sh: Show[Long]): F[String] = {
      for (
        i <- c.monotonic(MILLISECONDS);
        _ <- s.delay(0L);
        j <- c.monotonic(MILLISECONDS)
      ) yield sh.show(j - i)
    }
  }
}
