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

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.kernel.Monoid
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.language.postfixOps

class IOSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  describe("IO Monad") {
    it("""captures an IO Effect, something
         |  that happens to the outside world and does so lazily""".stripMargin) {
      import cats.effect.IO
      def sayHelloWorldTwice(s: String): IO[Unit] = {
        IO[Unit](println(s)).flatMap(_ => IO[Unit](println(s)))
      }
      val container: IO[Unit] = sayHelloWorldTwice("Hello World")
      println("Nothing has happened yet!")
      container.asserting(u => u should be (()))
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
      printHelloWorldTwice.asserting(u => u should be (()))
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
      container.asserting(u => u should be (()))
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

      io.asserting(u => u should be (()))
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
      val readLineFake = IO(3)
      interact(readLineFake).asserting(_ should be("You will be 33 years old"))
    }
  }

  describe("IO.async") {
    it("""describes asynchronous processing, it also applies
         |  the laws of async monad""".stripMargin) {

      info("it takes a function of either to unit")

      def processElseWhere(i: Int): IO[Int] =
        IO.async_ { cb =>
          if (i > 0) cb(Left(new Throwable("Not right")))
          else cb(Right(3))
          ()
        }

      val value1 = for { i <- processElseWhere(-2) } yield (i + 5)
      value1.asserting(i => i should be (8))
    }
  }

  describe("IO utilities") {
    it("can be debugged?") {
        val compose = for {
            a <- IO("Awesome").debug("S1")
            b <- IO("Cool").debug("S2")
        } yield (Monoid[String].combine(a, b))
        compose.asserting(s => s should be ("AwesomeCool"))
    }

//    it("""IO can be composed with flatMap""") {
//      import scala.concurrent.duration._
//      val app = IO.apply("Right on")
//      val fork1: IO[Assertion] = app.map(s => assert(s == "Right on"))
//      val fork2: IO[Unit] = app.map(s => println(s))
//
//      val value1: IO[Assertion] = for {
//        x <- fork1
//        _ <- fork2
//      } yield x
//      val result = value1.unsafeRunTimed(3 seconds).getOrElse("None")
//      result
//    }
//
//    it("has a timer") {
//      val result = for {
//        _ <- IO.shift(ec)
//        x <- IO("Cool")
//        y <- IO("World")
//      } yield x + y
//      result.unsafeRunAsyncAndForget()
//    }

    it("has operators *> which does one then the other") {
      val io = IO("Foo") *> IO("Bar")
      io.asserting(s => s should be("Bar"))
    }

    it("has operators &> which does one then the other in parallel") {
      val result =
        IO(Thread.currentThread().getName) &>
          IO(Thread.currentThread().getName)
      val value1 = result.delayBy(10 seconds)
      value1.assertNoException
    }
  }
}
