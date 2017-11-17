package ch.epfl.polygamedev.tbfights

import ch.epfl.polygamedev.tbfights.battle._
import ch.epfl.polygamedev.tbfights.messages.{Ping, Pong}
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
      var battleState: BattleState = BattleState.example1
      var troops: Map[Position,Sprite] = Map.empty
      var seletectedTroop: Option[TroopId] = None

      override def create(game: Game): Unit = {
        map = game.add.tilemap("badMap")
        map.addTilesetImage("placeholder")
        map.addTilesetImage("grass")

        val layer1 = map.createLayer("Tile Layer 1")
        val layer2 = map.createLayer("Tile Layer 2")

        layer1.resizeWorld()

        troops = battleState.troops.map {
          case (pos@Position(x, y), TroopState(id, troop)) =>
            // head starts at the tile above
            val sprite = game.add.sprite(32 * x, 32 * (y - 1), troop.resourceName)
            sprite.inputEnabled = true
            sprite.events.onInputDown.add(troopClicked _, sprite, 0, id.id)
            pos -> sprite
        }
      }

      def troopClicked(sprite: Sprite, self: Sprite, id: Int): Unit = {
        val troop = TroopId(id)
        seletectedTroop = if (seletectedTroop.contains(troop)) {
          println("None selected")
          None
        } else {
          println(s"Selected: $troop")
          Some(troop)
        }
      }

      override def update(game: Game): Unit = {

      }
    }
    game.state.add("battle", battleState)
    game.state.start("battle", clearWorld = true, clearCache = true)
  }

}
