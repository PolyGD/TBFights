package ch.epfl.polygamedev.tbfights.battle

sealed trait Troop {
  def name: String
  def resourceName = name
}

object Troop {
  def fromName(name: String): Option[Troop] = name match {
    case HumanFlamethrower.name => Some(HumanFlamethrower)
    case KnightBot.name => Some(KnightBot)
    case _ => None
  }
}

case object HumanFlamethrower extends Troop {
  val name = "human1"
}

case object KnightBot extends Troop {
  val name = "knightbot"
}
