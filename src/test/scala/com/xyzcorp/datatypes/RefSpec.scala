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
import cats.effect.std.Console
import cats.effect.testing.scalatest.AsyncIOSpec
import cats.kernel.Monoid
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration._
import scala.language.postfixOps
import cats.effect.IO

class RefSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
    describe("Ref") {
        it("""is a memory/state holder. It is unlike the State Monad""".stripMargin) {
            val result = for {
                a <- IO.ref(3)
                b <- IO.ref(10)
                aPrime <- a.get
                bPrime <- b.get
            } yield aPrime + bPrime
            result.asserting(i => i should be(13))
        }
        it("""is can be mutated""".stripMargin) {
            val result = for {
                a <- IO.ref(3)
                b <- IO.ref(10)
                o <- a.tryModify(i => (i * 3, "Operation 1 complete"))
                aPrime <- a.get
                bPrime <- b.get

                x <- IO.fromOption(o)(new Throwable("cannot convert from Option"))
            } yield aPrime + bPrime
            result.asserting(i => i should be(19))
        }
    }
}
