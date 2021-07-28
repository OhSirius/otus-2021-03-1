package me.chuwy.otusfp.home_work.repositories

import cats.effect.{Ref}
import me.chuwy.otusfp.home_work.model.{CounterInfo}

trait CounterRep[F[_]] {
  def get(): F[CounterInfo]
  def updateAndGet(): F[CounterInfo]
}

object CounterRep {
  def apply[F[_]](implicit ev: CounterRep[F]): CounterRep[F] = ev

  def memoryInterpreter[F[_]](ref: Ref[F, CounterInfo]) = new CounterRep[F] {
    override def get(): F[CounterInfo] = ref.get

    override def updateAndGet(): F[CounterInfo] = ref.updateAndGet(_.increment())
  }
}