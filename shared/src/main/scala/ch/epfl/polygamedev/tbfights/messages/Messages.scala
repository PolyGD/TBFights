package ch.epfl.polygamedev.tbfights.messages

import ch.epfl.polygamedev.tbfights.battle._
import play.api.libs.json._

sealed trait InMessage
sealed trait OutMessage

case class Ping(msg: String) extends InMessage
case class Pong(msg: String) extends OutMessage

case class MoveTroop(troop: TroopId, from: Position, to: Position) extends InMessage
case class BattleStarted(initialState: BattleState) extends OutMessage
case class TroopMoved(troop: TroopId, from: Position, to: Position, newState: BattleState) extends OutMessage
case class BadTroopMove(troop: TroopId, from: Position, to: Position) extends OutMessage

case class EndTurn(player: Player) extends InMessage
case class TurnEnded(newState: BattleState) extends OutMessage

object JSONProtocol {
  implicit val pingFormat = Json.format[Ping]
  implicit val pongFormat = Json.format[Pong]

  implicit val troopFormat = new Format[Troop]{
    def writes(o: Troop) = JsString(o.name)

    def reads(json: JsValue) = json match {
      case JsString(name) =>
        Troop.fromName(name).fold[JsResult[Troop]](JsError(s"unknown troop $name"))(JsSuccess(_))
      case _ => JsError()
    }
  }

  implicit val playerFormat = new Format[Player]{
    def writes(o: Player) = JsString(o.name)

    def reads(json: JsValue) = json match {
      case JsString(name) =>
        Player.fromName(name).fold[JsResult[Player]](JsError(s"unknown troop $name"))(JsSuccess(_))
      case _ => JsError()
    }
  }

  implicit val sizeFormat = Json.format[Size]
  implicit val positionFormat = Json.format[Position]
  implicit val troopStateFormat = Json.format[TroopState]
  implicit val battleMapFormat = Json.format[BattleMap]
  implicit val positionsFormat = new Format[Map[Position, TroopState]] {

    case class Item(pos: Position, troopState: TroopState)

    val itemFormat = Json.format[Item]

    def writes(o: Map[Position, TroopState]) = JsArray(o.map {
      case (pos, troopState) => itemFormat.writes(Item(pos, troopState))
    }.toIndexedSeq)

    def reads(json: JsValue) = json match {
      case JsArray(seq) =>
        val parsed = seq.foldRight[JsResult[List[(Position, TroopState)]]](JsSuccess(Nil)) {
          case (head, JsSuccess(tail, _)) =>
            val headItem = itemFormat.reads(head)
            headItem.map {
              item =>
                (item.pos, item.troopState) :: tail
            }
          case (_, error) => error
        }
        parsed.map(_.toMap)
      case _ => JsError()
    }
  }
  implicit val battleStateFormat = Json.format[BattleState]

  implicit val moveTroopFormat = Json.format[MoveTroop]
  implicit val endTurnFormat = Json.format[EndTurn]
  implicit val inMessageFormat = Json.format[InMessage]

  implicit val battleStartedFormat = Json.format[BattleStarted]
  implicit val troopMovedFormat = Json.format[TroopMoved]
  implicit val badTroopMoveFormat = Json.format[BadTroopMove]
  implicit val turnEnded = Json.format[TurnEnded]
  implicit val outMessageFormat = Json.format[OutMessage]
}