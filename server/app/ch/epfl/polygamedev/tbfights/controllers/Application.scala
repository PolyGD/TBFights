package ch.epfl.polygamedev.tbfights.controllers

import javax.inject._

import ch.epfl.polygamedev.tbfights.shared.SharedMessages
import play.api.mvc._

@Singleton
class Application @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index = Action {
    Ok(views.html.index(SharedMessages.itWorks))
  }

}
