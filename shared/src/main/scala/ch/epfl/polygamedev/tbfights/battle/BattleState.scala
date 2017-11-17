package ch.epfl.polygamedev.tbfights.battle

case class Position(x: Int, y: Int)
case class TroopState(id: TroopId, troop: Troop)

case class BattleState(map: BattleMap, troops: Map[Position, TroopState]) {
  def troopPosition(id: TroopId): Option[Position] = troops.collectFirst {
    case (position, TroopState(`id`, _)) => position
  }
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
  def fill(map: BattleMap, placements: (Position, Troop)*) = BattleState(
    map,
    placements.zipWithIndex.map {
      case ((position, troop), index) =>
        position -> TroopState(index, troop)
    }.toMap
  )

  val example1: BattleState = fill(
    BattleMap(Size(30, 30)),
    Position(1, 2) -> HumanFlamethrower,
    Position(2, 4) -> HumanFlamethrower
  )
}