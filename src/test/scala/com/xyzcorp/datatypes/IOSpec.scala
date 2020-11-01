/*
 * Copyright 2019 Daniel Hinojosa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.xyzcorp.datatypes

import java.util.concurrent.Executors

import cats.effect.IO
import org.scalatest.{Assertion, FunSpec, Matchers}

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

class IOSpec extends FunSpec with Matchers {
  describe("IO Monad") {
    it(""" captures an IO Effect, something
        | that happens to the outside world and does so lazily""".stripMargin) {
      import cats.effect.IO
      def sayHelloWorldTwice(s: String): IO[Unit] = {
        IO[Unit](println(s)).flatMap(_ => IO[Unit](println(s)))
      }
      val container: IO[Unit] = sayHelloWorldTwice("Hello World")
      println("No  thing has happened yet!")
      container.unsafeRunSync()
    }

    it("""can be rewritten using a flatMap for better clarity""".stripMargin) {
      import cats.effect.IO
      def printStringTwice(s: String): IO[Unit] = {
        for {
          _ <- IO(println(s))
          _ <- IO(println(s))
        } yield ()
      }
      val printHelloWorldTwice: IO[Unit] = printStringTwice("Hello World")
      printHelloWorldTwice.unsafeRunSync()
    }
    it(
      """can be rewritten now using assignment since the invocation is lazy""".stripMargin
    ) {
      import cats.effect.IO
      def sayHelloWorldTwice(s: String): IO[Unit] = {
        val printString = IO(println(s))
        for {
          _ <- printString
          _ <- printString
        } yield ()
      }
      val container: IO[Unit] = sayHelloWorldTwice("Hello World")
      container.unsafeRunSync()
    }
    it("""can be used to program interaction of events""") {
      import cats.effect.IO

      def putStrLn(value: String) = IO(println(value))
      val readLn = IO(scala.io.StdIn.readLine())

      val io: IO[Unit] = for {
        _ <- putStrLn("What's your name?")
        n <- readLn
        _ <- putStrLn(s"Hello, $n!")
      } yield ()

      io.unsafeRunSync()
    }
  }

  def howOldWillYouBeIn30Years(x: Int): Int = x + 30

  def interact(readLine: IO[Int]): IO[String] = {
    for {
      _ <- IO(println("Hello!"))
      _ <- IO(println("How old are you today?"))
      x <- readLine
    } yield ("You will be " + (x + 30) + " years old")
  }

  describe("Unit testing with the IO Monad") {
    it("""should be able to test as much as possible without leaving as
             |  many lines untested like in Java""".stripMargin) {
      type AssertResult = Either[Throwable, String] => Unit
      val readLineFake = IO(3)
      val f: AssertResult = {
        case Left(t)  => t.getMessage should be("Unable to process")
        case Right(v) => v should be("You will be 33 years old")
      }
      interact(readLineFake).unsafeRunAsync(f)
    }
  }

  describe("IO.async") {
    it("""describes asynchronous processing, it also applies
          |  the laws of async monad""".stripMargin) {

      info("it takes a function of either to unit")

      def processElseWhere(i: Int): IO[Int] =
        IO.async { cb =>
          if (i > 0) cb(Left(new Throwable("Not right")))
          else cb(Right(3))
        }

      val value1 = for { i <- processElseWhere(4) } yield (i + 5)
      value1.unsafeRunAsync {
        case Left(value)  => value shouldBe a[Throwable]
        case Right(value) => value should be(3)
      }
    }
  }

  describe("IO utilities") {
    it("""IO can be composed with flatMap""") {
      import scala.concurrent.duration._
      val app = IO.apply("Right on")
      val fork1: IO[Assertion] = app.map(s => assert(s == "Right on"))
      val fork2: IO[Unit] = app.map(s => println(s))

      val value1: IO[Assertion] = for {
        x <- fork1
        _ <- fork2
      } yield (x)
      val result = value1.unsafeRunTimed(3 seconds).getOrElse("None")
      result
    }

    it("has a timer") {
      val ec =
        ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(3))
      val result = for {
        _ <- IO.shift(ec)
        x <- IO("Cool")
        y <- IO("World")
      } yield x + y
      result.unsafeRunAsyncAndForget()
    }
  }
}
