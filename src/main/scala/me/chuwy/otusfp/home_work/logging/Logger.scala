package me.chuwy.otusfp.home_work.logging

import cats.Applicative
import cats.effect.std.Console
import cats.Show._

trait Logger[F[_]] {
  def info(message: => String):F[Unit]
  def warn(message: => String): F[Unit]
  def error(ex: => Throwable):F[Unit]
}

object Logger {
  def apply[F[_]](implicit ev: Logger[F]): Logger[F] = ev

  def consoleInterpreter[F[_] : Console] = new Logger[F] {
    override def info(message: => String): F[Unit] = Console[F].println(message)
    override def warn(message: => String): F[Unit] = Console[F].errorln(message)
    override def error(ex: => Throwable): F[Unit] = Console[F].errorln(ex)
  }

  def stubInterpreter[F[_] : Applicative] = new Logger[F] {
    override def info(message: => String): F[Unit] = Applicative[F].unit
    override def warn(message: => String): F[Unit] = Applicative[F].unit
    override def error(ex: => Throwable): F[Unit] = Applicative[F].unit
  }

}
