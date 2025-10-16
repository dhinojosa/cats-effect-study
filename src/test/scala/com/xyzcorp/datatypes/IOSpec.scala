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

import cats.Show
import cats.effect._
import cats.effect.kernel.Outcome.Succeeded
import cats.effect.std.Console
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.kernel.Monoid
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.language.postfixOps

class IOSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  describe("Companion Methods") {
    describe("apply") {
      it("takes a thunk of anything and returns an IO[A]") {
        val io = IO.apply("Hello World")

        io.asserting(s => s should be("Hello World"))
      }
    }
    describe("pure") {
      it("""takes a value and returns an IO[A], it should only be
           |  used for things that have been computed""".stripMargin) {
        val io = IO.pure("Hello World")
        io.asserting(s => s should be("Hello World"))
      }
    }
    describe("ref") {
      it("Creates a reference, a mutable variable, that is used within the IO Context") {
        val refIO = IO.ref("Hello World")
        val result = refIO.map(ref => ref.set(s"${ref.get}!"))
        result.asserting(s => s should be("Hello World!"))
      }
    }
    describe("delay") {
      it("takes a thunk of anything and returns an IO[A]. Suspends a synchronous side effect only") {
        val io = IO.delay(println("Hello World"))
        io.asserting(s => s should be(()))
      }
    }
    describe("raiseError") {
      it("takes a throwable and returns an IO[A] with that exception") {
        val io = IO.raiseError(new Throwable("Hello World"))
        io.assertThrowsError((e: Throwable) => e.getMessage should be("Hello World"))
      }
    }
    describe("sleep") {
      it("takes a duration and returns an IO[A] that will sleep for that duration") {
        val io = IO.sleep(10 seconds)
        io.asserting(s => s should be(()))
      }
    }
    describe("never") {
      it("returns an IO[A] that never completes") {
        info(
          """
            |The `IO.never` is essentially a program that represents an effect which never completes.
            |It is used in scenarios where you want to test timeouts, simulate long-running computations, or
            |any blocking operation where termination is controlled externally. This is useful for ensuring
            |your application handles non-terminating effects correctly without causing resource starvation.
            |""".stripMargin
        )
        val io = IO.never[String]
        io.asserting(s => s should be(()))
      }
    }
    describe("async") {
      it("allows you to interoperate with callback-based APIs") {
        val asyncIO = IO.async[Int] { callback =>
          // Simulate callback-based API
          val thread = new Thread(() => {
            Thread.sleep(1000) // Simulate async computation
            callback(Right(42)) // Send the result as Success
          })
          thread.start()
          IO(Option(IO(thread.interrupt())))
        }
        asyncIO.asserting(res => res shouldBe 42)
      }

      it("handles errors in callback-based APIs") {
        val asyncIO = IO.async[Int] { callback =>
          // Simulate callback-based API with error
          val thread = new Thread(() => {
            Thread.sleep(1000) // Simulate async computation
            callback(Left(new RuntimeException("Something went wrong"))) // Send error
          })
          thread.start()
          IO(Option(IO(thread.interrupt())))
        }

        asyncIO.assertThrowsError { (e: RuntimeException) =>
          e.getMessage shouldBe "Something went wrong"
        }
      }
    }
  }

  describe("Instance Methods") {
    describe("debug") {
      it("can be debugged using a debug in each IO") {
        val compose = for {
          a <- IO("Awesome").debug("S1")
          b <- IO("Cool").debug("S2")
        } yield Monoid[String].combine(a, b)
        compose.asserting(s => s should be("AwesomeCool"))
      }
      it("can be debugged using a customized message. This is very useful for debugging threads") {
        implicit val showString: Show[String] = Show.show { str =>
          s"Message: $str on Thread: ${Thread.currentThread().getName}"
        }
        val compose = for {
          a <- IO("Awesome").debug("S1")
          b <- IO("Cool").debug("S2")
        } yield Monoid[String].combine(a, b)
        compose.asserting(s => s should be("AwesomeCool"))
      }
    }
    describe("as") {
      it("replaces the result with another value") {
        IO(100).as("Hello World").asserting(s => s should be("Hello World"))
      }
    }
    describe("start") {
      it("creates your IO into a fiber") {
        info(
          """
            |Fibers in Cats Effect are indeed analogous to the concept of Virtual Threads in Java. Both are lightweight,
            |user-managed threads that do not consume operating system (OS) threads during their lifecycle.
            |They allow for efficient concurrency by enabling cooperative multitasking and thread management without
            |blocking underlying OS resources, making them suitable for high-performance and scalable systems.
            |""".stripMargin
        )

        val io = IO(println("Hello World"))

        // Implicit Show instance for Fiber
        implicit val showFiber: Show[FiberIO[Unit]] = Show.show { fiber =>
          s"Fiber(${fiber.hashCode}) on Thread: ${Thread.currentThread()}"
        }

        val fiber = io.start.debug("F0")
        val result = fiber.map(f => f.join).unsafeRunSync()
        result.asserting(outcome => outcome shouldBe a[Succeeded[IO, _, _]])
      }
    }
    describe("*>") {
      it("has operators *> which does one then the other") {
        val io = IO("Foo") *> IO("Bar")
        io.asserting(s => s should be("Bar"))
      }
    }
    describe("&>") {
      it("has operators &> which does one then the other in parallel") {
        val result =
          IO(Thread.currentThread().getName) &>
            IO(Thread.currentThread().getName)
        val value1 = result.delayBy(10 seconds)
        value1.assertNoException
      }
    }
    describe("!>") {
      it("has operators !> which does one then the other in parallel") {
        val result =
          IO("first") !>
            IO("last")
        result
          .delayBy(10 seconds)
          .asserting(s => s should be("last"))
      }
    }

    describe("foreverM") {
      it("performs an operation IO is executed indefinitely, terminating on cancellation or exception") {
        alert("""Running this line will cause an infinite loop if executed.""".stripMargin)
        val result2 = IO(println("This will print forever!")).foreverM
        val cancelableResult = for {
          fiber <- result2.start // Start the infinite `result` in a fiber
          _ <- IO.sleep(10.seconds) // Let it run briefly for 40 seconds
          _ <- fiber.cancel // Cancel the fiber
        } yield ()

        cancelableResult.asserting(_ => succeed)
      }
    }
  }

  describe("IO Monad") {
    it("""captures an IO Effect, something
         |  that happens to the outside world and does so lazily""".stripMargin) {
      import cats.effect.IO
      def sayHelloWorldTwice(s: String): IO[Unit] = {
        IO[Unit](println(s)).flatMap(_ => IO[Unit](println(s)))
      }

      val container: IO[Unit] = sayHelloWorldTwice("Hello World")
      println("Nothing has happened yet!")
      container.asserting(u => u should be(()))
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
      printHelloWorldTwice.asserting(u => u should be(()))
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
      container.asserting(u => u should be(()))
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

      io.asserting(u => u should be(()))
    }
  }

  def howOldWillYouBeIn30Years(x: Int): Int = x + 30

  def interact(readLine: IO[Int]): IO[String] = {
    for {
      _ <- IO(println("Hello!"))
      _ <- IO(println("How old are you today?"))
      x <- readLine
    } yield "You will be " + (x + 30) + " years old"
  }

  describe("Unit testing with the IO Monad") {
    it("""should be able to test as much as possible without leaving as
         |  many lines untested like in Java""".stripMargin) {
      val readLineFake = IO(3)
      interact(readLineFake).asserting(_ should be("You will be 33 years old"))
    }
  }

  describe("All the console calls can be made with Console in the standard library") {
    it("can bring in a IO[Console]") {
      val console = Console[IO]
      case class Employee(firstName: String, lastName: String)

      val output = for {
        _ <- console.println("Test")
        _ <- console.println("Next, output a common class")
        _ <- console.println(Employee("Bjarne", "Strousoup"))
      } yield ()

      output.asserting(u => u should be(()))
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

      val value1 = for { i <- processElseWhere(-2) } yield i + 5
      value1.asserting(i => i should be(8))
    }
  }

  describe("IO utilities") {

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

  }
}
