package ch.epfl.polygamedev.tbfights.messages

import play.api.libs.json._

sealed trait InMessage
sealed trait OutMessage

case class Ping(msg: String) extends InMessage
case class Pong(msg: String) extends OutMessage

object JSONProtocol {
  implicit val pingFormat = Json.format[Ping]
  implicit val inMessageFormat = Json.format[InMessage]

  implicit val pongFormat = Json.format[Pong]
  implicit val outMessageFormat = Json.format[OutMessage]
}