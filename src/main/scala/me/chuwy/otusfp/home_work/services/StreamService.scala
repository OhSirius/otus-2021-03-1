package me.chuwy.otusfp.home_work.services

import cats.Applicative
import cats.effect._
import fs2.Stream
import cats.implicits._
import cats.mtl.{Raise}
import me.chuwy.otusfp.home_work.model.{ChunkVar, StreamError, TimeVar, TotalVar}

object StreamService {

  def run[F[_] : Temporal : Raise[*[_], StreamError]](chunk: ChunkVar, total: TotalVar, time: TimeVar) = for {
    _ <- validate(chunk, total)
    stream = Stream.eval(Unique[F].unique.map(_.hashCode().toByte))
      .repeatN(total.value)
      .chunkN(chunk.value)
      .metered(time.value)
  } yield stream

  private def validate[F[_]](chunk: ChunkVar, total: TotalVar)(implicit RE: Raise[F, StreamError], A: Applicative[F]): F[Unit] = if (chunk.value < total.value) A.unit else RE.raise(StreamError.BadValues(chunk, total))
}
