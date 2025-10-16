package com.xyzcorp.datatypes

import cats.effect._
import cats.effect.std.Env
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.implicits._
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

class EnvSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  describe("Env retrieves the environment variables") {
    it("should read Java_HOME since everything has a JAVA_HOME in the JVM") {
      val io = for {
        javaHome <- Env[IO].get("JAVA_HOME")
        _ <- IO.println(javaHome)
      } yield javaHome
      io.asserting(o => o shouldBe a [Some[_]])
    }
  }
}
