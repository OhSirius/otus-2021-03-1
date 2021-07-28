package me.chuwy.otusfp.home_work.bootstrap

import cats.implicits._
import cats.effect.{Concurrent, Ref}
import cats.effect.std.{Console, Semaphore}
import me.chuwy.otusfp.home_work.config.AppConfig
import me.chuwy.otusfp.home_work.logging.Logger
import me.chuwy.otusfp.home_work.model.CounterInfo
import me.chuwy.otusfp.home_work.repositories.CounterRep
import scala.concurrent.ExecutionContext.global

import scala.concurrent.ExecutionContext

class Environment[F[_]](rep:CounterRep[F], logger:Logger[F], val sem: Semaphore[F], val ec:ExecutionContext, val config: AppConfig) {
  implicit val repF: CounterRep[F] = rep
  implicit val loggerF: Logger[F] = logger
}

object Environment {
  def initialize[F[_] : Console : Concurrent](config: AppConfig) =
    for {
      ref <- Ref[F].of(CounterInfo(config.counter.init.value))
      sem <- Semaphore[F](1)
      rep = CounterRep.memoryInterpreter(ref)
      logger = Logger.consoleInterpreter
    } yield new Environment[F](rep, logger, sem, global, config)

}
