package ch.epfl.polygamedev.tbfights

import ch.epfl.polygamedev.tbfights.battle._
import ch.epfl.polygamedev.tbfights.messages._
import ch.epfl.polygamedev.tbfights.shared.SharedMessages
import com.definitelyscala.phaser._
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLInputElement}

import scala.scalajs.js

object ScalaJSExample {

  def main(args: Array[String]): Unit = {
    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    val connector = Connector()
    connector.listen {
      case Pong(msg) => println(s"Got ponged:$msg")
    }

    val msgBox = dom.document.getElementById("msg").asInstanceOf[HTMLInputElement]
    dom.document.getElementById("send").asInstanceOf[HTMLButtonElement].onclick = {
      _ => connector ! Ping(msgBox.value)
    }
    val game = new Game(500, 500, Phaser.CANVAS, "gameArea")
    val battleState = new State {
      override def preload(game: Game): Unit = {
        val TILED_JSON = 1
        game.load.tilemap("badMap", "versionedAssets/maps/badmap.json", null, TILED_JSON)
        game.load.image("placeholder", "versionedAssets/images/our-art/placeholder.png")
        game.load.image("grass", "versionedAssets/images/our-art/tiles/grass.png")
        game.load.image("human1", "versionedAssets/images/our-art/units/human1/human1.png")
      }

      var map: Tilemap = _
      var battleStateOpt: Option[BattleState] = None
      var initialized = false
      var troops: Map[TroopId, Sprite] = Map.empty
      var seletectedTroop: Option[TroopId] = None

      override def create(game: Game): Unit = {
        map = game.add.tilemap("badMap")
        map.addTilesetImage("placeholder")
        map.addTilesetImage("grass")

        val layer1 = map.createLayer("Tile Layer 1")
        val layer2 = map.createLayer("Tile Layer 2")
        layer1.inputEnabled = true
        layer1.events.onInputDown.add(mapClicked _, layer1, 0)

        layer1.resizeWorld()
        initialized = true
        placeTroops()
      }

      connector.listen {
        case BattleStarted(initialState) =>
          battleStateOpt = Some(initialState)
          if (initialized) {
            placeTroops()
          }
        case TroopMoved(troop, from, to, newState) =>
          val predictedState = battleStateOpt.flatMap(_.withMove(troop, from, to))
          battleStateOpt = Some(newState)
          if (battleStateOpt == predictedState) {
            println("Didn't expect this state, repositioning all troops")
            println(s"predicted:$predictedState")
            println(s"fromServer:$newState")
            placeTroops()
          } else {
            animateMove(troop, from, to)
            println("Move successful")
          }
        case _:BadTroopMove =>
          println("Move failed")
      }

      def placeTroops(): Unit = {
        troops.valuesIterator.foreach {
          sprite =>
            sprite.destroy()
        }
        battleStateOpt.foreach {
          battleState =>
            troops = battleState.troops.map {
              case (Position(x, y), TroopState(id, troop)) =>
                // head starts at the tile above
                val sprite = game.add.sprite(32 * x, 32 * (y - 1), troop.resourceName)
                sprite.inputEnabled = true
                sprite.events.onInputDown.add(troopClicked _, sprite, 0, id)
                id -> sprite
            }
        }
      }

      def troopClicked(sprite: Sprite, self: Sprite, troop: TroopId): Unit = {
        seletectedTroop = if (seletectedTroop.contains(troop)) {
          println("None selected")
          None
        } else {
          println(s"Selected: $troop")
          Some(troop)
        }
      }

      def mapClicked(mapLayer: TilemapLayer, self: TilemapLayer): Unit = {
        val rawX = game.input.activePointer.x
        val rawY = game.input.activePointer.y
        println(s"Map clicked at $rawX,$rawY")
        val x = (rawX / 32).toInt
        val y = (rawY / 32).toInt
        val target = Position(x, y)
        println(s"Estimated square at $x,$y")
        seletectedTroop.foreach {
          troop =>
            println(s"attempting to move $troop to $x,$y")

            battleStateOpt.foreach {
              battleState =>
                // TODO do not use Option.get
                val troopPosition = battleState.troopPosition(troop).get
                connector ! MoveTroop(troop, troopPosition, target)
                seletectedTroop = None
                println("Troop deselected")
            }
        }
      }

      def animateMove(troopId: TroopId, from: Position, to: Position) = {
        //TODO animate
        //TODO do not use map.apply
        val sprite = troops(troopId)
        sprite.x = to.x * 32
        sprite.y = (to.y - 1) * 32
      }

      override def update(game: Game): Unit = {

      }
    }
    game.state.add("battle", battleState)
    game.state.start("battle", clearWorld = true, clearCache = true)
  }

}
