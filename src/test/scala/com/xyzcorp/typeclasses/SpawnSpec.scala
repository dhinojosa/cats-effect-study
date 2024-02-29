package com.xyzcorp.typeclasses

import cats.effect.{IO, Spawn}
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

class SpawnSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {

  describe("Spawn") {
    val spawn = Spawn.apply[IO]
    val fiber = spawn.start(IO {
      println(s"Starting ${Thread.currentThread()}"); 60
    })
  }
}
