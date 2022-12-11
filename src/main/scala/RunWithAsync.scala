import cats.effect._
object RunWithAsync extends IOApp {
  def putStrLn(value: String): IO[Unit] = IO(println(value))

  def run(args: List[String]): IO[ExitCode] = {
    for {
      s <- IO("Do it").start
      v <- IO(s"$s ${Thread.currentThread().getName}")
      _ <- s.join
      _ <- putStrLn(v)
    } yield ExitCode.Success
  }
}
