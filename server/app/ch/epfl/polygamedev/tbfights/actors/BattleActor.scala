package ch.epfl.polygamedev.tbfights.actors

import akka.actor.{Actor, ActorRef, Props}
import ch.epfl.polygamedev.tbfights.battle.BattleState
import ch.epfl.polygamedev.tbfights.messages.{BadTroopMove, BattleStarted, MoveTroop, TroopMoved}

class BattleActor extends Actor {
  import BattleActor._
  var subscribers: Seq[ActorRef] = Nil

  var battleState =  BattleState.example1
  def receive = {
    case MoveTroop(troopId, from, to) =>
      val result = battleState.withMove(troopId, from, to)
      result.fold {
        sender ! BadTroopMove(troopId, from, to)
      } {
        newState =>
          battleState = newState
          subscribers.foreach(_ ! TroopMoved(troopId, from, to, newState))
      }
    case Join =>
      subscribers +:= sender
      sender ! BattleStarted(battleState)
  }
}

object BattleActor {
  def props = Props(new BattleActor)

  case object Join

}
