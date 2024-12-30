package com.xyzcorp.typeclasses

import cats.effect.testing.scalatest.AsyncIOSpec
import cats.effect.{IO, Spawn}
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

class SupervisorSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {

  describe("A supervisor spawns fibers whose lifecycles are bound to that of the supervisor.") {
    describe("Trait") {

    }
    describe("Concepts") {
      describe("Spawn[F]#start: start and forget, no lifecycle management for the spawned fiber") {

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
