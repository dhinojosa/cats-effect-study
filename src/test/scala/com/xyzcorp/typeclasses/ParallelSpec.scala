package com.xyzcorp.typeclasses

import cats.{Applicative, Parallel}
import cats.effect.IO
import cats.effect.IO.Par
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funspec.AsyncFunSpec
import org.scalatest.matchers.should.Matchers

class ParallelSpec extends AsyncFunSpec with AsyncIOSpec with Matchers{
   describe("Parallel type class is a Spec that perform parallelism") {
     it("can use the Par to perform parallel") {
         val parallel = Parallel.apply[IO]
         val par1: Par[String] = parallel.parallel(IO(s"S1"))
         val par2: Par[String] = parallel.parallel(IO(s"S2"))
         val combine = Applicative[Par]
             .map2(par1, par2)((a, b) => s"From $a and $b comes S3")
         val sequential = parallel.sequential(combine)
         sequential.asserting(s => s should be ("From S1 and S2 comes S3"))
     }
   }
}
