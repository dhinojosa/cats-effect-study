/*
 * Copyright 2020 Daniel Hinojosa
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

name := "cats-effect-study"

version := "1.0-SNAPSHOT"

scalaVersion := "3.3.7"

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-feature",
  "-deprecation"
)

fork := true

libraryDependencies := Seq(
  "org.scalatest" %% "scalatest" % "3.3.0-alpha.2" % "test",
  "org.scalatest" %% "scalatest-funspec" % "3.3.0-alpha.2" % "test",
  "org.typelevel" %% "cats-effect-testing-scalatest" % "1.7.0" % "test",
  ("org.typelevel" %% "cats-effect" % "3.7-4972921").withSources().withJavadoc()
)
