package com.xyzcorp.datatypes

import java.util.concurrent.Executors

import org.scalatest.{FunSpec, Matchers}
import cats._
import cats.effect.{ContextShift, Fiber, IO}

import scala.concurrent.ExecutionContext

class FiberSpec extends FunSpec with Matchers {
  describe("Fibers are green thread that don't take an entire OS Thread") {
    it("can be create by the start method") {
      info("we first start with a context shift")
      val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10))
      implicit val contextShift: ContextShift[IO] = IO.contextShift(executionContext)
      type IOFiber[A] = Fiber[IO, A]
      val start: IO[IOFiber[Int]] = IO.pure(34).start
      start.flatMap(x => x.join)
    }
  }
}
