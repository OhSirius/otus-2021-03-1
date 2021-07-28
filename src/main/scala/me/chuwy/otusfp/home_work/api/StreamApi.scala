package me.chuwy.otusfp.home_work.api

import cats.effect.Temporal
import cats.effect.std.Semaphore
import cats.mtl.{Handle, Raise}
import me.chuwy.otusfp.home_work.logging.Logger
import me.chuwy.otusfp.home_work.model.{AppError, ChunkVar, CounterError, StreamError, TimeVar, TotalVar}
import me.chuwy.otusfp.home_work.repositories.CounterRep
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats._
import cats.effect.kernel.Concurrent
import cats.implicits._
import me.chuwy.otusfp.home_work.services.StreamService
import cats.effect._
import cats.implicits._
import cats.mtl.implicits._
import me.chuwy.otusfp.home_work.model.StreamError.BadValues

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

object StreamApi {

  def routes[F[_] : Logger : Temporal : Raise[*[_], StreamError]](implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "slow" / ChunkVar(chunk) / TotalVar(total) / TimeVar(time) => StreamService.run(chunk, total, time)
        .flatMap(stream => Ok(stream.map(_.toString).covary[F]))
    }
  }

  def handleError[F[_] : Applicative : Logger](error: StreamError)(implicit dsl: Http4sDsl[F]) = {
    import dsl._
    error match {
      case BadValues(chunk, total) => Logger[F].warn(s"$chunk не должен превышать $total") *> Forbidden(s"$chunk не должен превышать $total")
    }
  }
}
