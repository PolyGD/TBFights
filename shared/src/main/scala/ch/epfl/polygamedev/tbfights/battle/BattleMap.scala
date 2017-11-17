package ch.epfl.polygamedev.tbfights.battle

case class Size(width: Int, height: Int)

case class BattleMap(size: Size) {
  def isInBounds(position: Position): Boolean = {
    import position._
    import size._
    x >= 0 && y >= 0 && x < width && y < height
  }
}
