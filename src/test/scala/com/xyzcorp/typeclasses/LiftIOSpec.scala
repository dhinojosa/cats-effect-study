package com.xyzcorp.typeclasses

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

class LiftIOSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {

  describe("LiftIO") {
     IO(3).asserting(_ should be (3))
  }
}
