package ch.epfl.polygamedev.tbfights

import ch.epfl.polygamedev.tbfights.messages.{OutMessage, Ping, Pong}
import ch.epfl.polygamedev.tbfights.shared.SharedMessages
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLButtonElement, HTMLInputElement}

object ScalaJSExample {

  def main(args: Array[String]): Unit = {
    dom.document.getElementById("scalajsShoutOut").textContent = SharedMessages.itWorks

    val connector = Connector()
    connector.listen(handleMessage)

    val msgBox = dom.document.getElementById("msg").asInstanceOf[HTMLInputElement]
    dom.document.getElementById("send").asInstanceOf[HTMLButtonElement].onclick = {
      _ => connector ! Ping(msgBox.value)
    }
  }

  private def handleMessage(out: OutMessage): Unit = {
    out match {
      case Pong(msg) => println(s"Got ponged:$msg")
    }
  }
}
