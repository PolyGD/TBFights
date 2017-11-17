package ch.epfl.polygamedev.tbfights.battle

sealed trait Troop {
  def name: String
  def resourceName = name
}

case object HumanFlamethrower extends Troop {
  def name = "human1"
}
