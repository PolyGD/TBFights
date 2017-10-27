package ch.epfl.polygamedev.tbfights

import ch.epfl.polygamedev.tbfights.shared.SharedMessages
import org.scalajs.dom
import org.scalajs.dom.raw.{MessageEvent, WebSocket}

object ScalaJSExample {

  def main(args: Array[String]): Unit = {
    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    val ws = new WebSocket(s"$wsProtocol://${dom.document.location.host}/ws")
    ws.onmessage = {
      event =>
        println(event.data)
    }
    ws.onopen = {
      _ =>
        println("websocket open")
    }
  }
}
