package com.xyzcorp.typeclasses

import java.util.concurrent.Executors

import cats.effect.{Async, IO}
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class AsyncSpec extends FunSpec with Matchers {
  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10))

  describe("Async extends Sync and LiftIO") {
    it("is generally used as a closure where the callback closes around the IO") {
      val apiCall = Future.successful("I come from the Future!")
      val ioa: IO[String] = {
        Async[IO].async { cb =>
          import scala.util.{Failure, Success}
          apiCall.onComplete {
            case Success(value) => cb(Right(value))
            case Failure(error) => cb(Left(error))
          }
        }
      }
      ioa.map(s => s should be ("I come from the Future!")).unsafeRunSync()
    }
  }
}
