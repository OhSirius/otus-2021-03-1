package me.chuwy.otusfp.home_work.client

import cats.effect.std.{Semaphore}
import cats.effect.{Async, Concurrent}
import cats.mtl.Handle
import me.chuwy.otusfp.home_work.api.{CounterApi, StreamApi}
import me.chuwy.otusfp.home_work.bootstrap.Environment
import me.chuwy.otusfp.home_work.config.{Limit}
import me.chuwy.otusfp.home_work.logging.Logger
import me.chuwy.otusfp.home_work.middleware.{AppErrorMiddleware, ExceptionMiddleware}
import me.chuwy.otusfp.home_work.model.{AppError, ChunkVar, CounterInfo, TimeVar, TotalVar}
import me.chuwy.otusfp.home_work.repositories.CounterRep
import org.http4s.{EntityDecoder, Request}
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.{http4sKleisliResponseSyntaxOptionT, http4sLiteralsSyntax}
import org.http4s.server.Router
import org.http4s.client.Client


object HttpClient {

  def createCounterRequest[F[_]] = Request[F]().withUri(uri"http://localhost:9090/counter")

  def createStreamRequest[F[_]](chunk: ChunkVar, total: TotalVar, time: TimeVar) = Request[F]().withUri(uri"http://localhost:9090/slow" / (chunk.value.toString) / (total.value.toString) / (time.value.toSeconds.toString))

  def getClient[F[_] : Async : Handle[*[_], AppError] : EntityDecoder[*[_], CounterInfo]](env: Environment[F]) = {
    import env._
    Client.fromHttpApp[F](getHttpApp[F](env.config.counter.limit, env.sem))
  }

  private def getHttpApp[F[_] : Async : Logger : CounterRep : Concurrent : Handle[*[_], AppError]](limit: Limit, s: Semaphore[F]) = {
    implicit val dsl = Http4sDsl.apply[F]
    val counterApi = CounterApi.routes(s, limit.value)
    val streamApi = StreamApi.routes

    Router(
      "/" -> ExceptionMiddleware.handleError(AppErrorMiddleware.handleError(counterApi)),
      "/" -> ExceptionMiddleware.handleError(AppErrorMiddleware.handleError(streamApi))
    ).orNotFound
  }
}
