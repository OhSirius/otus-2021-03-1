package me.chuwy.otusfp.home_work.middleware

import cats.{ApplicativeError}
import cats.data.{Kleisli, OptionT}
import cats.mtl.Handle
import me.chuwy.otusfp.home_work.logging.Logger
import me.chuwy.otusfp.home_work.model.{AppError}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.implicits._

object ExceptionMiddleware {
  def handleError[F[_] : ApplicativeError[*[_], Throwable]: Logger](routes: HttpRoutes[F])(implicit dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl._
    Kleisli { req =>
      OptionT {
        routes.run(req).value
        .recoverWith({
          case e: Throwable => Logger[F].error(e) *> InternalServerError().map(Option(_))
        })
      }
    }
  }
}
