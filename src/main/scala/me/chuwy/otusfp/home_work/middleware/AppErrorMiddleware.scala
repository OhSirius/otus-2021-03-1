package me.chuwy.otusfp.home_work.middleware

import cats.Applicative
import cats.data.{Kleisli, OptionT}
import cats.mtl.Handle
import me.chuwy.otusfp.home_work.api.{CounterApi, StreamApi}
import me.chuwy.otusfp.home_work.logging.Logger
import me.chuwy.otusfp.home_work.model.{AppError, CounterError, StreamError}
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import cats.mtl.implicits._
import cats.implicits._

object AppErrorMiddleware {
  def handleError[F[_] : Logger : Applicative : Handle[*[_], AppError] : Http4sDsl](routes: HttpRoutes[F]): HttpRoutes[F] =
    Kleisli { req =>
      OptionT {
        routes.run(req).value.handleWith[AppError]({
          case counterErr: CounterError => CounterApi.handleError[F](counterErr).map(Option(_))
          case streamErr: StreamError => StreamApi.handleError[F](streamErr).map(Option(_))
        })
      }
    }
}
