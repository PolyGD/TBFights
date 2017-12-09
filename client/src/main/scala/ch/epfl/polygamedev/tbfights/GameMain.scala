package ch.epfl.polygamedev.tbfights

import ch.epfl.polygamedev.tbfights.battle._
import ch.epfl.polygamedev.tbfights.messages._
import com.definitelyscala.phaser._
import scala.scalajs.js

object GameMain {

  def main(args: Array[String]): Unit = {

    val connector = Connector()
    connector.listen {
      case Pong(msg) => println(s"Got ponged:$msg")
    }

    val game = new Game(30*32, 30*32 + 50, Phaser.CANVAS, "gameArea")
    val battleState = new State {
      override def preload(game: Game): Unit = {
        val TILED_JSON = 1
        game.load.tilemap("map1", "versionedAssets/maps/map1.json", null, TILED_JSON)
        game.load.image("grassAndWater", "versionedAssets/images/our-art/tiles/grassAndWater.png")
        game.load.image("markers", "versionedAssets/images/our-art/tiles/markers.png")
        game.load.image("human1", "versionedAssets/images/our-art/units/human1/human1.png")
        game.load.image("knightbot", "versionedAssets/images/our-art/units/knightbot/knightbot.png")
        game.load.image("end-turn-btn", "versionedAssets/images/external-art/dabuttonfactory/button_end-turn-green.png")
      }

      var map: Tilemap = _
      var battleStateOpt: Option[BattleState] = None
      var initialized = false
      var troops: Map[TroopId, Sprite] = Map.empty
      var selectedTroop: Option[TroopId] = None
      var endTurnButton: Button = _
      var statusText: Text = _
      var markerLayer: TilemapLayer = _

      override def create(game: Game): Unit = {
        map = game.add.tilemap("map1")
        map.addTilesetImage("grassAndWater")
        map.addTilesetImage("markers")

        val layer1 = map.createLayer("Ground")
        layer1.inputEnabled = true
        layer1.events.onInputDown.add(mapClicked _, layer1, 0)

        layer1.resizeWorld()

        markerLayer = map.createBlankLayer("Markers", 30, 30, 32, 32)

        endTurnButton = game.add.button(game.width - 100, 30 * 32, "end-turn-btn", endTurn _, endTurnButton)
        endTurnButton.x = game.width - endTurnButton.width

        val textStyle = js.Dynamic.literal("font" -> "12px Arial").asInstanceOf[PhaserTextStyle]
        textStyle.font = "Arial"
        textStyle.fontSize = 36
        textStyle.fill = "#ffffff"
        statusText = game.add.text(0, 30*32, "", textStyle)
        statusText.visible = true

        initialized = true
        resetBoard()
      }

      connector.listen {
        case BattleStarted(initialState) =>
          battleStateOpt = Some(initialState)
          if (initialized) {
            resetBoard()
          }
        case TroopMoved(troop, from, to, newState) =>
          val predictedState = battleStateOpt.flatMap(_.withMove(troop, from, to))
          battleStateOpt = Some(newState)
          if (battleStateOpt != predictedState) {
            println("Didn't expect this state, repositioning all troops")
            println(s"predicted:$predictedState")
            println(s"fromServer:$newState")
            resetBoard()
          } else {
            animateMove(troop, from, to)
            println("Move successful")
          }
        case _:BadTroopMove =>
          println("Move failed")
        case TurnEnded(newState) =>
          val predictedState = battleStateOpt.map(_.withEndTurn)
          battleStateOpt = Some(newState)
          selectTroop(None)
          println("SelectedTroop cleared")
          if (battleStateOpt != predictedState) {
            println("Didn't expect this state, repositioning all troops")
            println(s"predicted:$predictedState")
            println(s"fromServer:$newState")
            resetBoard()
          } else {
            println("Turn changed")
            updateStatusText()
          }
      }

      def resetBoard(): Unit = {
        placeTroops()
        updateStatusText()
        selectTroop(None)
      }

      private def updateStatusText():  Unit = {
        battleStateOpt.fold {
          statusText.text = ""
        } {
          battleState =>
            val text = s"${battleState.currentTurn.name}'s turn"
            statusText.text = text
            statusText.fill = battleState.currentTurn match {
              case Red => "#ff0000"
              case Blue => "#19dbf2"
              case _ => "#ffffff"
            }
        }
      }

      def placeTroops(): Unit = {
        troops.valuesIterator.foreach {
          sprite =>
            sprite.destroy()
        }
        battleStateOpt.foreach {
          battleState =>
            troops = battleState.troops.map {
              case (Position(x, y), TroopState(id, troop, owner)) =>
                // head starts at the tile above
                val sprite = game.add.sprite(32 * x, 32 * (y - 1), troop.resourceName)
                val marker = idleTroopMarker(owner)
                map.putTile(marker, x, y, markerLayer)
                sprite.inputEnabled = true
                sprite.events.onInputDown.add(troopClicked _, sprite, 0, id)
                id -> sprite
            }
        }
      }

      private def idleTroopMarker(owner: Player) = {
        owner match {
          case Blue => 1 + 5 * 1 + 2 //blue corners (1,2)
          case Red => 1 + 5 * 0 + 2 //red corners (0,2)
        }
      }

      private def selectedTroopMarker(owner: Player) = {
        owner match {
          case Blue => 1 + 5 * 1 + 3 //blue solid tile (1,3)
          case Red => 1 + 5 * 0 + 3 //red  solid tile (0,3)
        }
      }

      def endTurn(button: Button, self: Button): Unit = {
        println("End Turn clicked")
        battleStateOpt.foreach {
          battleState =>
            println(s"EndTurn(${battleState.currentTurn})")
            connector ! EndTurn(battleState.currentTurn)
        }
      }

      def troopClicked(sprite: Sprite, self: Sprite, troop: TroopId): Unit = {
        if (selectedTroop.contains(troop)) {
          println("None selected")
          selectTroop(None)
        } else {
          if (battleStateOpt.exists(_.isMovableThisTurn(troop))) {
            println(s"Selected: $troop")

            selectTroop(Some(troop))
          } else {
            println(s"Cannot move this turn: $troop")
            selectTroop(None)
          }
        }
      }

      def selectTroop(troopIdOpt: Option[TroopId]) = {
        selectedTroop.foreach {
          troop =>
            for {
              battleState <- battleStateOpt
              pos@Position(x, y) <- battleState.troopPosition(troop)
              troop <- battleState.troops.get(pos)
            } map.putTile(idleTroopMarker(troop.owner), x, y, markerLayer)
        }

        troopIdOpt.foreach {
          troop =>
            for {
              battleState <- battleStateOpt
              pos@Position(x, y) <- battleState.troopPosition(troop)
              troop <- battleState.troops.get(pos)
            } map.putTile(selectedTroopMarker(troop.owner), x, y, markerLayer)
        }
        selectedTroop = troopIdOpt
      }

      def mapClicked(mapLayer: TilemapLayer, self: TilemapLayer): Unit = {
        val rawX = game.input.activePointer.x
        val rawY = game.input.activePointer.y
        println(s"Map clicked at $rawX,$rawY")
        val x = (rawX / 32).toInt
        val y = (rawY / 32).toInt
        val target = Position(x, y)
        println(s"Estimated square at $x,$y")
        selectedTroop.foreach {
          troop =>
            println(s"attempting to move $troop to $x,$y")

            battleStateOpt.foreach {
              battleState =>
                // TODO do not use Option.get
                val troopPosition = battleState.troopPosition(troop).get
                connector ! MoveTroop(troop, troopPosition, target)
                selectTroop(None)
                println("Troop deselected")
            }
        }
      }

      def animateMove(troopId: TroopId, from: Position, to: Position) = {
        //TODO animate
        //TODO do not use map.apply
        val sprite = troops(troopId)

        val tween = game.add.tween(sprite).to(js.Dynamic.literal("x" -> to.x * 32,"y" -> (to.y - 1) * 32),1000,"Quart.easeOut",true,0,0,false)
        tween.onComplete.add( (t: Tween,self: Tween)=>game.tweens.remove(t),tween,0)

        map.putTile(null, from.x, from.y, markerLayer)

        for {
          battleState <- battleStateOpt
          pos@Position(x, y) <- battleState.troopPosition(troopId)
          troop <- battleState.troops.get(pos)
        } map.putTile(idleTroopMarker(troop.owner), to.x, to.y, markerLayer)

      }

      override def update(game: Game): Unit = {

      }
    }
    game.state.add("battle", battleState)
    game.state.start("battle", clearWorld = true, clearCache = true)
  }

}
