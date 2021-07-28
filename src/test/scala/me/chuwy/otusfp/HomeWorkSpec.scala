package me.chuwy.otusfp

import org.specs2.mutable.Specification
import cats.effect.testing.specs2.CatsEffect
import cats.{Applicative, Monad, MonadError}
import cats.data.{EitherT, State}
import cats.effect.std.Semaphore
import cats.effect.{IO, Ref}
import me.chuwy.otusfp.home_work.bootstrap.Environment
import me.chuwy.otusfp.home_work.client.HttpClient.{createCounterRequest, createStreamRequest, getClient}
import me.chuwy.otusfp.home_work.config.{AppConfig, CounterConfig, Init, Limit, ServerConfig}
import me.chuwy.otusfp.home_work.logging.Logger
import me.chuwy.otusfp.home_work.model.{AppError, ChunkVar, CounterInfo, TimeVar, TotalVar}
import me.chuwy.otusfp.home_work.repositories.CounterRep
import org.http4s.Status.{Forbidden, Ok}
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import scala.concurrent.duration.{DurationInt}
import scala.concurrent.ExecutionContext.global

class HomeWorkSpecSpec extends Specification with CatsEffect {
  type F[A] = EitherT[IO, AppError, A]

  def init =  for {
    config <- Applicative[F].pure(AppConfig("","", CounterConfig(Init(0), Limit(3)), ServerConfig("", 9090)))
    ref <- Ref[F].of(CounterInfo(config.counter.init.value))
    sem <- Semaphore[F](1)
    rep = CounterRep.memoryInterpreter(ref)
    logger = Logger.stubInterpreter[F]
  } yield new Environment[F](rep, logger, sem, global, config)

  "CounterApi" should {
    "ok with counter 2" in {
      (for {
        env <- init
        _   <- getClient[F](env).expect[CounterInfo](createCounterRequest[F])
        res <- getClient[F](env).expect[CounterInfo](createCounterRequest[F])
      } yield res).value.map {
        case Left(_) => ko
        case Right(res) => res.value must beEqualTo(2)
      }
    }
    "forbidden for counter 4 request" in {
      (for {
        env    <- init
        _      <- getClient[F](env).expect[CounterInfo](createCounterRequest[F])
        _      <- getClient[F](env).expect[CounterInfo](createCounterRequest[F])
        _      <- getClient[F](env).expect[CounterInfo](createCounterRequest[F])
        status <- getClient[F](env).status(createCounterRequest[F])
      } yield status).value.map {
        case Left(_) => ko
        case Right(status) => status must beEqualTo(Forbidden)
      }
    }
  }

  "StreamApi" should {
    "ok status for stream" in {
      (for {
        env      <- init
        status   <- getClient[F](env).status(createStreamRequest[F](ChunkVar(2), TotalVar(5), TimeVar(1.seconds)))
      } yield status).value.map {
        case Left(_) => ko
        case Right(status) => status must beEqualTo(Ok)
      }
    }
    "forbidden for chunk more total" in {
      (for {
        env      <- init
        status   <- getClient[F](env).status(createStreamRequest[F](ChunkVar(5), TotalVar(2), TimeVar(1.seconds)))
      } yield status).value.map {
        case Left(_) => ko
        case Right(status) => status must beEqualTo(Forbidden)
      }
    }
  }

}
