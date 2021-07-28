package me.chuwy.otusfp.home_work.api
import cats.{Applicative}
import cats.effect.{Temporal}
import cats.effect.std.Semaphore
import cats.implicits._
import cats.mtl.Handle
import me.chuwy.otusfp.home_work.logging.Logger
import me.chuwy.otusfp.home_work.model.CounterError.CounterLimited
import me.chuwy.otusfp.home_work.model.{AppError, CounterError}
import me.chuwy.otusfp.home_work.repositories.CounterRep
import me.chuwy.otusfp.home_work.services.CounterService
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes

object CounterApi {
  def routes[F[_] : Temporal : Logger : CounterRep : Handle[*[_], AppError]](s: Semaphore[F], limit: Int)(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "counter" => CounterService.procesAndGet(s, limit).flatMap(r => Ok(r))
    }
  }

  def handleError[F[_] : Applicative : Logger](error: CounterError)(implicit dsl: Http4sDsl[F]) = {
    import dsl._
    error match {
      case CounterLimited(limit) => Logger[F].warn(s"Счетчик не удалось обновить, достигнут лимит: $limit") *> Forbidden(s"Достигнут лимит $limit")
    }
  }
}
