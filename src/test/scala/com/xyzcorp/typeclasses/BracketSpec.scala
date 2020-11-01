package com.xyzcorp.typeclasses

import cats.effect.{Bracket, IO}
import org.scalatest.{FunSpec, Matchers}

class BracketSpec extends FunSpec with Matchers {

  describe("""Bracket extends MonadError (see cats core MonadError)
             |  with bracket operation, a generalized abstracted pattern of safe
             |  resource acquisition and release during failures.""".stripMargin) {

    it("""is a trait that has two methods, bracket case and bracket""") {

      import cats.MonadError

      sealed abstract class ExitCase[+E]

      trait Bracket[F[_], E] extends MonadError[F, E] {
        def bracketCase[A, B](acquire: F[A])(use: A => F[B])(
          release: (A, ExitCase[E]) => F[Unit]
        ): F[B]

        // Simpler version, doesn't distinguish b/t exit conditions
        def bracket[A, B](acquire: F[A])(use: A => F[B])(
          release: A => F[Unit]
        ): F[B]
      }
    }

    describe("Bracketing") {
      it("ensures that we have functional error handling") {
        def tryBracket(x: Int, y: Int)(br: Bracket[IO, String]) = {
          br.bracket(IO.apply(x))(i => IO.apply(i + y))(i => IO(println(i)))
        }
      }
    }
  }
}
