package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.Future
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfig
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.Controller
import slick.driver.JdbcProfile
import java.sql.Timestamp
import java.util.Calendar
import javax.inject.Inject
import scala.util.Try
import scala.util.Success
import scala.util.Failure
import play.Logger
import dal.OperazioniSerieTemporali

@Singleton
class HomeController extends Controller  {

  def swagger = Action {
    request =>
      Ok(views.html.swagger())
  }

  def meteo = Action {
    request =>
      Ok(views.html.esempioMeteo())
  }
  def azioni = Action {
    request =>
      Ok(views.html.esempioAzioni())
  }

}

