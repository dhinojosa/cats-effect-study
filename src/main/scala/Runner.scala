import cats._
import cats.effect._
import cats.implicits._

import scala.concurrent.duration.FiniteDuration

object Runner extends IOApp.Simple {
  def timeProcess[F[_]: FlatMap, A](f: F[A])(implicit clock: Clock[F]): F[(A, FiniteDuration)] =
    for {
      start <- clock.realTime
      x <- f
      end <- clock.realTime
    } yield (x, end - start)

  def putStrLn(value: String): IO[Unit] = IO(println(value))
  val readLn: IO[String] = IO(scala.io.StdIn.readLine())

  val run =
    for {
      _ <- putStrLn("Enter what you message you want to time")
      t <- timeProcess(readLn)
      _ <- putStrLn("The result is: %s".format(t.toString()))
    } yield ()
}
