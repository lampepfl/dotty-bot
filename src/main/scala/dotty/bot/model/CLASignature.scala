package dotty.bot.model

import upickle.default.{Reader, macroR}

case class CLASignature(
  user: String,
  signed: Boolean
)

object CLASignature {
  implicit def reader: Reader[CLASignature] = macroR
}
