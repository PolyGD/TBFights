package ch.epfl.polygamedev.tbfights.battle

case class Position(x: Int, y: Int)
case class TroopId(id: Int)
case class TroopState(id: TroopId, troop: Troop)

case class BattleState(map: BattleMap, troops: Map[Position, TroopState]) {
  def withMove(from: Position, to: Position): Option[BattleState] = if (!map.isInBounds(from) || !map.isInBounds(to)) {
    // one of the positions out of bounds
    None
  } else if (troops.isDefinedAt(to)) {
    // there is already something at the target location
    None
  } else {
    val oldUnits = this.troops
    // do not move if there is nothing to move
    oldUnits.get(from).map {
      toBeMoved =>
        copy(troops = oldUnits - from + (to -> toBeMoved))
    }
  }
}

object BattleState {
  val example1: BattleState = BattleState(
    map = BattleMap(Size(30, 30)),
    troops = Map(
      Position(1, 2) -> TroopState(TroopId(1), HumanFlamethrower),
      Position(2, 4) -> TroopState(TroopId(2), HumanFlamethrower)
    )
  )
}