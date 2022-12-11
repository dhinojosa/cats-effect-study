package com.xyzcorp.datatypes

import cats._
import cats.implicits._
import cats.effect._
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.FileInputStream

class ResourceSpec extends AnyFunSpec with Matchers {
    describe("Resource allocates and releases a resource") {
        val resource: Resource[IO, FileInputStream] = {
            Resource.make(IO(new FileInputStream("somefile.txt")))(
            fis => IO(fis.close()).handleErrorWith(_ => IO.unit))
        }
        val rz: Resource[IO, Array[Byte]] = for {fis <- resource} yield fis.readAllBytes();
    }
}
