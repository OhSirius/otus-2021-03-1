package me.chuwy.otusfp.home_work

import cats.data.EitherT
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import me.chuwy.otusfp.home_work.bootstrap.Environment
import me.chuwy.otusfp.home_work.config.{loadConfig}
import me.chuwy.otusfp.home_work.server.HttpServer
import me.chuwy.otusfp.home_work.model.AppError
import cats.effect.std.Console
import me.chuwy.otusfp.home_work.logging.Logger

object Main extends IOApp {
  type F[A] = EitherT[IO, AppError, A]

  override def run(args: List[String]): IO[ExitCode] = {
    (for {
      config <- loadConfig[F]
      env    <- Environment.initialize[F](config)
      _      <- env.loggerF.info(s"Запуск сервера с параметрами $config")
      _      <- start(env) as (ExitCode.Success)
    } yield true).value
    .flatMap({
      case Left(err) => getLogger[IO]().warn(err.toString).as(ExitCode.Error)
      case Right(_) => getLogger[IO]().info("Завершено").as(ExitCode.Success)
    })
    .recoverWith({
        case e: Throwable => getLogger[IO]().error(e).as(ExitCode.Error)
    })
  }

  def start(env: Environment[F]) = {
    import env._
    HttpServer.getBuilder(env.ec, env.config, env.sem)
  }

  private def getLogger[F[_] : Console](): Logger[F] = Logger.consoleInterpreter

}