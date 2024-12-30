package com.xyzcorp.datatypes

import cats.effect.IO
import cats.effect.std.{Dispatcher, Random}
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import java.util

class DispatcherSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  describe("""Dispatcher is a fiber based Supervisor for evaluating
             |  across an impure boundary and that boundary is reactive
             |  producing potentially many values and for every value it
             |  must be done as an impure callback""".stripMargin) {
    pending

    val javaQueue: java.util.Queue[String] = new util.LinkedList[String]()

    it("has a sequential") {
      val dispatcher = Dispatcher.sequential[IO]
      val result = dispatcher.use { d =>
        IO.delay(d.unsafeRunAndForget(IO(javaQueue.add("hello"))))
      }
      result.asserting(u => javaQueue.size() should be(1))
    }
  }
}
