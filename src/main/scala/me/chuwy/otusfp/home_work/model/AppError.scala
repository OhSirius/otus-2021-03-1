package me.chuwy.otusfp.home_work.model

sealed trait AppError

sealed trait CounterError extends AppError
object CounterError {
  case class CounterLimited(limit: Int) extends CounterError
}

sealed trait StreamError extends AppError
object StreamError {
  case class BadValues(chunkVar: ChunkVar, totalVar: TotalVar) extends StreamError
}
