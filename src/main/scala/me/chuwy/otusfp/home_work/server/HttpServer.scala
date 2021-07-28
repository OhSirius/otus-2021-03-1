package me.chuwy.otusfp.home_work.server

import cats.effect.std.Semaphore
import cats.effect.{Async, Concurrent}
import cats.mtl.Handle
import me.chuwy.otusfp.home_work.api.{CounterApi, StreamApi}
import me.chuwy.otusfp.home_work.config.AppConfig
import me.chuwy.otusfp.home_work.logging.Logger
import me.chuwy.otusfp.home_work.middleware.{AppErrorMiddleware, ExceptionMiddleware}
import me.chuwy.otusfp.home_work.model.{AppError}
import me.chuwy.otusfp.home_work.repositories.CounterRep
import org.http4s.HttpApp
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import scala.concurrent.ExecutionContext


object HttpServer {

  def getBuilder[F[_] : Async : Logger : CounterRep : Concurrent : Handle[*[_], AppError]](cpuPool: ExecutionContext, config: AppConfig, s: Semaphore[F]) = {
    implicit val dsl = Http4sDsl.apply[F]
    val counterApi = CounterApi.routes(s, config.counter.limit.value)
    val streamApi = StreamApi.routes

    val httpApp: HttpApp[F] = Router(
      "/" -> ExceptionMiddleware.handleError(AppErrorMiddleware.handleError(counterApi)),
      "/" -> ExceptionMiddleware.handleError(AppErrorMiddleware.handleError(streamApi))
    ).orNotFound

    BlazeServerBuilder[F](cpuPool)
      .bindLocal(config.server.port)
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
  }

}


