package ch.epfl.polygamedev.tbfights.battle

sealed trait BattleUnit {
  def name: String
  def resourceName = name
}

case object HumanFlamethrower extends BattleUnit {
  def name = "human1"
}
