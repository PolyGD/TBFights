package ch.epfl.polygamedev.tbfights

import ch.epfl.polygamedev.tbfights.shared.SharedMessages
import ch.epfl.polygamedev.tbfights.messages.{InMessage, OutMessage, Ping, Pong}
import ch.epfl.polygamedev.tbfights.messages.JSONProtocol._
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLInputElement, MessageEvent, WebSocket}
import play.api.libs.json.{JsError, JsSuccess, Json}

object ScalaJSExample {

  def main(args: Array[String]): Unit = {
    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks


    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"

    val ws = new WebSocket(s"$wsProtocol://${dom.document.location.host}/ws")
    ws.onmessage = {
      event =>
        println(event.data)
        val data = Json.parse(event.data.toString)
        val result = Json.fromJson[OutMessage](data)
        result match {
          case JsSuccess(msg, _) =>
            handleMessage(msg)
          case JsError(errors) =>
            Console.err.println(s"Invalid JSON $data, Errors: $errors")
        }
    }
    ws.onopen = {
      _ =>
        println("websocket open")
    }
    def send(inMessage: InMessage) = ws.send(Json.toJson(inMessage).toString)

    val msgBox = dom.document.getElementById("msg").asInstanceOf[HTMLInputElement]
    dom.document.getElementById("send").asInstanceOf[HTMLButtonElement].onclick = {
      _ => send(Ping(msgBox.value))
    }
  }

  private def handleMessage(out: OutMessage): Unit = {
    out match {
      case Pong(msg) => println(s"Got ponged:$msg")
    }
  }
}
