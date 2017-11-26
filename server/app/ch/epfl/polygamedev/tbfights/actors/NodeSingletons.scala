package ch.epfl.polygamedev.tbfights.actors

import akka.actor.{ActorContext, ActorSystem}

trait NodeSingletonAddresses {
  def context: ActorContext
  val battleActor = context.actorSelection("/user/the-only-battle")
}

class NodeSingletons(val actorSystem: ActorSystem) {
  // replace with cluster singleton when there is more than one node
  // https://doc.akka.io/docs/akka/2.5.6/scala/cluster-singleton.html
  private lazy val battleActor = actorSystem.actorOf(BattleActor.props, "the-only-battle")
  def all = Seq(battleActor)
}
