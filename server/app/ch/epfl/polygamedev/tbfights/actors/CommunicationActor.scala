package ch.epfl.polygamedev.tbfights.actors

import akka.actor.{Actor, ActorRef, Props}
import ch.epfl.polygamedev.tbfights.messages._

class CommunicationActor(out: ActorRef) extends Actor with NodeSingletonAddresses {
  out ! Pong("Welcome")

  battleActor ! BattleActor.Join
  def receive = {
    case Ping(msg) =>
      out ! Pong(msg + "!")
    case forwarded: MoveTroop =>
      battleActor ! forwarded
    case forwarded: OutMessage =>
      out ! forwarded
  }
}

object CommunicationActor {
  def props(out: ActorRef) = Props(new CommunicationActor(out))
}