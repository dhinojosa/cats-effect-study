package com.xyzcorp.typeclasses

import cats.effect.{IO, Spawn}
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

class SpawnSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {

  describe("Spawn has multiple ways to spawn a fiber, which is analogous to a Virtual Thread in Java") {
    describe("Concepts") {
      describe("Spawn[F]#start: start and forget, no lifecycle management for the spawned fiber") {
        Spawn[IO].start()
      }
      describe(
        "Spawn[F]#background: ties the lifecycle of the spawned fiber to that of the fiber that invoked background"
      ) {}
    }
    describe("Companions") {}
    describe("Methods") {}
    describe("Extensions") {}
  }

}
