package ch.epfl.polygamedev.tbfights.messages

sealed trait InMessage
sealed trait OutMessage

case class Ping(msg: String) extends InMessage
case class Pong(msg: String) extends OutMessage