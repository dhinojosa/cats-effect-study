package com.xyzcorp.typeclasses

import cats.Eval
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{Clock, IO, Sync, SyncIO}
import org.scalatest.funspec.{AnyFunSpec, AsyncFunSpec}
import org.scalatest.matchers.should.Matchers

class SyncIOSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  describe("""A Monad that can suspend the execution of
             |  side effects in the F[_] context.""".stripMargin) {
    val a: IO[Int] = Sync[IO].delay(14 + 10)
    a.asserting(i => i should be(24))
  }

  describe("""SyncIO is similar to IO, but does not support
             | asynchronous computations.  All these commands
             | of course run on the same thread""".stripMargin) {

    describe("Creation") {
      it("is similar to IO[_] but processes and blocks synchronously") {
        val result = SyncIO.apply {
          Thread.sleep(3000)
          302
        }
        result.asserting(i => i should be(302))
      }

      it("can be converted to an AsyncIO") {
        val result: SyncIO[Int] = SyncIO.apply {
          Thread.sleep(3000)
          302
        }

        val mapped = result.map(_ + 3).to[IO]
        mapped.asserting(i => i should be(305))
      }

      it("can be created via an Eval") {
        val eval = Eval.later {
          "Thunk"
        }
        val syncIO = SyncIO.eval(eval)
        syncIO.asserting(s => s should be("Thunk"))
      }

      it("""can be derived from an Either where the left is a Throwable,
           |  in this case a Right""".stripMargin) {
        val e: Either[Throwable, Int] = Right(40)
        val result = SyncIO.fromEither(e)
        result.asserting(_ should be(40))
      }

      it("""can be derived from an Either where the left is a Throwable,
           |  in this case a Left. Here we will call
           |  with handleErrorWith since there is a exception""".stripMargin) {
        val e: Either[Throwable, Int] = Left(new Throwable("Fail"))
        val result = SyncIO
          .fromEither(e)
          .handleErrorWith(_ => SyncIO.unit)
        result.asserting(_ should be(()))
      }
    }
  }
}
