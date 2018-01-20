package ch.epfl.polygamedev.tbfights.battle

trait Player {
  def name: String
}
object Player {
  def fromName(name: String): Option[Player] = name match {
    case Red.name => Some(Red)
    case Blue.name => Some(Blue)
    case _ => None
  }
}

case object Red extends Player {
  val name: String = "red"
}
case object Blue extends Player {
  val name: String = "blue"
}