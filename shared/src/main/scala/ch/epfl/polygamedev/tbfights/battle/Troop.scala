package ch.epfl.polygamedev.tbfights.battle

sealed trait Troop {
  def name: String
  def resourceName = name
}

object Troop {
  def fromName(name: String): Option[Troop] = name match {
    case "human1" => Some(HumanFlamethrower)
    case _ => None
  }
}

case object HumanFlamethrower extends Troop {
  def name = "human1"
}
