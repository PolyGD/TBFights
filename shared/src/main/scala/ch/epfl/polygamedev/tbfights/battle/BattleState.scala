package ch.epfl.polygamedev.tbfights.battle

case class Position(x: Int, y: Int)

case class TroopState(id: TroopId, troop: Troop, owner: Player)

case class BattleState(map: BattleMap,
                       playersInTurnOrder: Seq[Player],
                       troops: Map[Position, TroopState],
                       currentTurn: Player) {
  def troopPosition(id: TroopId): Option[Position] = troops.collectFirst {
    case (position, TroopState(`id`, _, _)) => position
  }

  def withMove(who: TroopId, from: Position, to: Position): Option[BattleState] =
    if (!troopPosition(who).contains(from) || !map.isInBounds(from) || !map.isInBounds(to)) {
      // the troop shold be where we expect
      // one of the positions out of bounds
      None
    } else if (troops.isDefinedAt(to)) {
      // there is already something at the target location
      None
    } else {
      val oldUnits = this.troops
      // do not move if there is nothing to move
      oldUnits.get(from).filter(_.owner == currentTurn).map {
        toBeMoved =>
          copy(troops = oldUnits - from + (to -> toBeMoved))
      }
    }

  def withEndTurn: BattleState = {
    val currentIndex = playersInTurnOrder.indexOf(currentTurn)
    val nextIndex = if(currentIndex >= playersInTurnOrder.size -1) 0 else currentIndex + 1
    val nextTurn = playersInTurnOrder(nextIndex)
    copy(currentTurn = nextTurn)
  }

}

object BattleState {
  def fill(map: BattleMap, placements: ((Position, Troop), Player)*) = BattleState(
    map,
    Seq(Red, Blue),
    placements.zipWithIndex.map {
      case (((position, troop),owner), index) =>
        position -> TroopState(index, troop, owner)
    }.toMap,
    Red
  )

  val example1: BattleState = fill(
    BattleMap(Size(30, 30)),
    Position(4, 2) -> HumanFlamethrower -> Red,
    Position(7, 3) -> HumanFlamethrower -> Red,
    Position(7, 7) -> KnightBot -> Red,

    Position(21, 22) -> HumanFlamethrower -> Blue,
    Position(19, 25) -> HumanFlamethrower -> Blue,
    Position(16, 22) -> KnightBot -> Blue
  )
}