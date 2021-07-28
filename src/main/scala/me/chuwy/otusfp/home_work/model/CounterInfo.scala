package me.chuwy.otusfp.home_work.model
import cats.effect.Concurrent
import io.circe.Codec
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

case class CounterInfo(value:Int) {
  def increment() = this.copy(value = this.value + 1)
}

object  CounterInfo{
  implicit val codec: Codec.AsObject[CounterInfo] = deriveCodec //derivation.deriveCodec
  implicit def counterEntityEncoder[F[_]: Concurrent]: EntityEncoder[F, CounterInfo] = jsonEncoderOf[F, CounterInfo]
}
