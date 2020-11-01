package com.xyzcorp.datatypes

import cats.effect._
import cats.implicits._
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.ExecutionContext

class ContextShiftSpec extends FunSpec with Matchers {

  describe("""Context Shift is equivalent to Executors in Java
             |  and Execution Context in Scala""".stripMargin) {

    it("is defined by the following trait") {
      info("""Context Shift is not a type class,
             | there is no coherence restriction""".stripMargin)

      import scala.concurrent.ExecutionContext

      trait ContextShift[F[_]] {
        def shift: F[Unit]
        def evalOn[A](ec: ExecutionContext)(f: F[A]): F[A]
      }
    }
  }

  describe("Shift operation is an effect that triggers a logical fork") {
    val cs = IO.contextShift(ExecutionContext.global)
    val a: IO[Unit] = cs.shift
    val value1: IO[Unit] = a *> IO(println("What?"))
  }
}
