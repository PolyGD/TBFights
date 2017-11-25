package ch.epfl.polygamedev.tbfights.actors

import akka.actor.{Actor, ActorRef, Props}
import ch.epfl.polygamedev.tbfights.battle.BattleState
import ch.epfl.polygamedev.tbfights.messages._

class CommunicationActor(out: ActorRef) extends Actor {
  out ! Pong("Welcome")

  var battleState =  BattleState.example1

  out ! BattleStarted(battleState)

  def receive = {
    case Ping(msg) =>
      out ! Pong(msg + "!")
    case MoveTroop(troopId, from, to) =>
      val result = battleState.withMove(troopId, from, to)
      result.fold {
        out ! BadTroopMove(troopId, from, to)
      } {
        newState =>
          battleState = newState
          out ! TroopMoved(troopId, from, to, newState)
      }
  }
}

object CommunicationActor {
  def props(out: ActorRef) = Props(new CommunicationActor(out))
}