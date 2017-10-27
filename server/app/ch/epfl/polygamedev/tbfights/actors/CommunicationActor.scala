package ch.epfl.polygamedev.tbfights.actors

import akka.actor.{Actor, ActorRef, Props}
import ch.epfl.polygamedev.tbfights.messages.{Ping, Pong}

class CommunicationActor(out: ActorRef) extends Actor {
  out ! Pong("Welcome")
  def receive = {
    case Ping(msg) =>
      out ! Pong(msg+"!")
  }
}

object CommunicationActor {
  def props(out: ActorRef) = Props(new CommunicationActor(out))
}