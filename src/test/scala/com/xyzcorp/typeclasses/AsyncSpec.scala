package com.xyzcorp.typeclasses

import java.util.concurrent.Executors
import cats.effect.{Async, IO}
import org.scalatest.funspec.{AnyFunSpec, AsyncFunSpec}
import org.scalatest.matchers.should.Matchers
import cats._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

class AsyncSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  describe("Async extends Sync and LiftIO") {
    it("is generally used as a closure where the callback closes around the IO") {
      val apiCall = Future.successful("I come from the Future!")
      val ioa: IO[String] = {
        Async[IO].async_ { cb =>
          import scala.util.{Failure, Success}
          apiCall.onComplete {
            case Success(value) => cb(Right(value))
            case Failure(error) => cb(Left(error))
          }
        }
      }
      ioa.asserting(s => s should be("I come from the Future!"))
    }
  }
}
