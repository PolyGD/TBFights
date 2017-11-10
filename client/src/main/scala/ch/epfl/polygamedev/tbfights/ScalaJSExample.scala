package ch.epfl.polygamedev.tbfights

import ch.epfl.polygamedev.tbfights.messages.{Ping, Pong}
import ch.epfl.polygamedev.tbfights.shared.SharedMessages
import com.definitelyscala.phaser._
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLInputElement}

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
      var troops: Seq[Sprite] = Seq.empty

      override def create(game: Game): Unit = {
        map = game.add.tilemap("badMap")
        map.addTilesetImage("placeholder")
        map.addTilesetImage("grass")

        val layer1 = map.createLayer("Tile Layer 1")
        val layer2 = map.createLayer("Tile Layer 2")

        layer1.resizeWorld()

        def addHumanAtTile(x: Int, y: Int) = {
          // head starts at the tile above
          game.add.sprite(32 * x, 32 * (y - 1), "human1")
        }

        troops :+= addHumanAtTile(1, 2)
        troops :+= addHumanAtTile(2, 4)
      }

      override def update(game: Game): Unit = {

      }
    }
    game.state.add("battle", battleState)
    game.state.start("battle", clearWorld = true, clearCache = true)
  }

}
