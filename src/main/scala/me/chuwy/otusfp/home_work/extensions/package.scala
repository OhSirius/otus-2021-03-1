package me.chuwy.otusfp.home_work

import cats.effect.MonadCancel
import cats.mtl.{Handle, Raise}
import me.chuwy.otusfp.home_work.model.AppError
import cats.mtl.implicits._
import cats.implicits._

package object extensions {

  def bracket[F[_] : MonadCancel[*[_], Throwable] : Handle[*[_], AppError], A, B](acquire: F[A])(release: () => F[Unit])(use: A => F[B]) = MonadCancel[F].bracket(acquire)(use)(_ => release())
    .handleWith[AppError](er => release() *> Raise[F, AppError].raise(er))


}
