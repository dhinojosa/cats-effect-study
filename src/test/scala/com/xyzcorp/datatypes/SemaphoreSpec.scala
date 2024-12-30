package com.xyzcorp.datatypes

import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

class SemaphoreSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
    describe(
      """ A semaphore has a non-negative number of permits available.
        | Acquiring a permit decrements the current number of permits
        | and releasing a permit increases the current number of permits.
        | An acquire that occurs when there are no permits available
        | results in fiber blocking until a permit becomes available.
        |""".stripMargin) {


    }
}
