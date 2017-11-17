package ch.epfl.polygamedev.tbfights.battle

case class Position(x: Int, y: Int)
case class BattleUnitState(unit: BattleUnit)

case class BattleState(map: BattleMap, units: Map[Position, BattleUnitState]) {
  def withMove(from: Position, to: Position): Option[BattleState] = if (!map.isInBounds(from) || !map.isInBounds(to)) {
    // one of the positions out of bounds
    None
  } else if (units.isDefinedAt(to)) {
    // there is already something at the target location
    None
  } else {
    val oldUnits = this.units
    // do not move if there is nothing to move
    oldUnits.get(from).map {
      toBeMoved =>
        copy(units = oldUnits - from + (to -> toBeMoved))
    }
  }
}

object BattleState {
  val example1: BattleState = BattleState(
    map = BattleMap(Size(30, 30)),
    units = Map(
      Position(1, 2) -> BattleUnitState(HumanFlamethrower),
      Position(2, 4) -> BattleUnitState(HumanFlamethrower)
    )
  )
}