package me.chuwy.otusfp.home_work.model

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

final case class ChunkVar private (value:Int)
object ChunkVar {
  def unapply(str: String): Option[ChunkVar] = NumericParser.parse(str)(ChunkVar(_))
}

final case class TotalVar private (value:Int)
object TotalVar {
  def unapply(str: String) = NumericParser.parse(str)(TotalVar(_))
}

final case class TimeVar private(value:FiniteDuration)
object TimeVar {
  def unapply(str: String): Option[TimeVar] = NumericParser.parse[Long, TimeVar](str)(v => TimeVar(FiniteDuration(v, TimeUnit.SECONDS)))
}

object NumericParser {
  def parse[A, B](str: String)(ctor: A => B)(implicit N: Numeric[A], O: Ordering[A]): Option[B] = N.parseString(str).filter(O.gt(_, N.zero)).map(ctor(_))
}


