package me.chuwy.otusfp.home_work.services

import cats.{Applicative}
import cats.effect.{Temporal}
import cats.effect.std.Semaphore
import cats.implicits._
import cats.mtl.Raise
import cats.mtl._
import me.chuwy.otusfp.home_work.extensions.bracket
import me.chuwy.otusfp.home_work.logging.Logger
import me.chuwy.otusfp.home_work.model.{AppError, CounterError}
import me.chuwy.otusfp.home_work.repositories.CounterRep

object CounterService {

  def procesAndGet[F[_] : Temporal : Logger : CounterRep: Handle[*[_], AppError]](s: Semaphore[F], limit: Int) = for {
    _       <- Logger[F].info("Попытка получения счетчика")
    updated <- updateAndGet(s, limit)
    _       <- Logger[F].info(s"Счетчик обновлен: $updated")
  } yield updated

  private def updateAndGet[F[_] : Temporal : Logger : CounterRep: Handle[*[_], AppError]](s: Semaphore[F], limit: Int)(implicit Rep: CounterRep[F] ) =
    bracket(s.acquire)(() => s.release) { _ =>
      for {
        current <- Rep.get()
              _ <- checkLimit(current.value, limit)
        updated <- Rep.updateAndGet()
      } yield updated
    }

  private def checkLimit[F[_]](current: Int, limit: Int)(implicit RE: Raise[F, CounterError], A: Applicative[F]): F[Unit] =
    if (current < limit) A.unit else RE.raise(CounterError.CounterLimited(limit))

}
