package ch.epfl.polygamedev.tbfights.controllers

import javax.inject._

import akka.actor.ActorSystem
import akka.stream.Materializer
import ch.epfl.polygamedev.tbfights.actors.CommunicationActor
import ch.epfl.polygamedev.tbfights.messages.{InMessage, OutMessage, Ping, Pong}
import ch.epfl.polygamedev.tbfights.shared.SharedMessages
import play.api.libs.json._
import play.api.libs.streams.ActorFlow
import play.api.mvc.WebSocket.MessageFlowTransformer
import play.api.mvc._

@Singleton
class Application @Inject()(cc: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index(SharedMessages.itWorks))
  }

  implicit val pingFormat = Json.format[Ping]
  implicit val inMessageFormat = Json.format[InMessage]
  implicit val pongFormat = Json.format[Pong]
  implicit val outMessageFormat = Json.format[OutMessage]

  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[InMessage, OutMessage]

  def socket = WebSocket.accept[InMessage, OutMessage] {
    _ =>
      ActorFlow.actorRef { out =>
        CommunicationActor.props(out)
      }
  }

}
