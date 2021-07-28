package me.chuwy.otusfp.home_work

import cats.effect.kernel.Sync
import pureconfig.ConfigSource

import java.nio.file.Paths
import pureconfig.generic.auto._

package object config {

  type Name = String
  type Email = String
  case class Init(value:Int) extends AnyVal
  case class Limit(value:Int) extends AnyVal
  case class CounterConfig(init: Init, limit: Limit)
  case class ServerConfig(host: String, port: Int)
  case class AppConfig(appName: Name, appEmail: Email, counter: CounterConfig, server: ServerConfig)

  def loadConfig[F[_] : Sync]: F[AppConfig] = Sync[F].delay(ConfigSource.file(Paths.get("src/main/resources/application.conf")).loadOrThrow[AppConfig])
}
