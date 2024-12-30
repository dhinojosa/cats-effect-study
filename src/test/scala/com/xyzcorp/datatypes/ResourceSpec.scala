package com.xyzcorp.datatypes

import cats._
import cats.implicits._
import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.funspec.{AnyFunSpec, AsyncFunSpec}
import org.scalatest.matchers.should.Matchers

import java.io.{File, FileInputStream}

class ResourceSpec extends AsyncFunSpec with AsyncIOSpec with Matchers {
  describe("Resource allocates and releases a resource, regardless of failure") {
    val resource: Resource[IO, FileInputStream] = {
      Resource.make(IO(new FileInputStream("somefile.txt")))(fis => IO(fis.close()).handleErrorWith(_ => IO.unit))
    }
    val rz: Resource[IO, Array[Byte]] = for { fis <- resource } yield fis.readAllBytes();
  }
}
