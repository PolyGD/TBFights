package ch.epfl.polygamedev.tbfights

import ch.epfl.polygamedev.tbfights.messages.JSONProtocol._
import ch.epfl.polygamedev.tbfights.messages.{InMessage, OutMessage}
import org.scalajs.dom
import org.scalajs.dom.raw.WebSocket
import play.api.libs.json.{JsError, JsSuccess, Json}

trait Connector {
  def send(inMessage: InMessage): Unit
  def ! (inMessage: InMessage): Unit = send(inMessage)
  def listen(f: PartialFunction[OutMessage, Unit]): Unit
}

class WebSocketConnector(url: String) extends Connector {
  private val ws = new WebSocket(url)
  private var listeners: Seq[PartialFunction[OutMessage,Unit]] = Seq.empty

  ws.onmessage = {
    event =>
      val data = Json.parse(event.data.toString)
      val result = Json.fromJson[OutMessage](data)
      result match {
        case JsSuccess(msg, _) =>
          listeners.foreach(_(msg))
        case JsError(errors) =>
          Console.err.println(s"Invalid JSON $data, Errors: $errors")
      }
  }
  ws.onopen = {
    _ =>
      println("websocket open")
  }
  def send(inMessage: InMessage): Unit = ws.send(Json.toJson(inMessage).toString)

  def listen(f: PartialFunction[OutMessage, Unit]): Unit = listeners :+= f
}

object Connector {
  private lazy val default = {
    val wsProtocol = if (dom.document.location.protocol == "https:") "wss" else "ws"
    new WebSocketConnector(s"$wsProtocol://${dom.document.location.host}/ws")
  }
  def apply(): Connector = default
}