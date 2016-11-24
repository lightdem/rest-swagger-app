package controllers

import scala.concurrent.Future
import dal.OperazioniSerieTemporali
import javax.inject.Inject
import javax.inject.Singleton
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsError
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.mvc.BodyParsers
import play.api.mvc.Controller
import scala.collection.mutable.ArrayBuffer
import models.RigaSerieTemporale
import io.swagger.annotations._
import play.api.mvc._
import play.api.libs.ws._
import play.api.libs.concurrent.Promise
import play.api.Play
import akka.actor._
import play.Logger;

@Api(value = "/meteo", description = "semplice api per prendere la temperatura del posto interessato")
@Singleton
class ApiMeteo @Inject() (conf: play.api.Configuration, serieTemporali: OperazioniSerieTemporali, ws: WSClient, system: ActorSystem) extends Controller {

  val ApiMeteoUrl = "http://api.openweathermap.org/data/2.5/weather"
  val APPID = "13c4195b3e9f9898fea40601ab493c7f"

  import scala.concurrent.duration._

  val port = Play
  system.scheduler.schedule(10 seconds, 120 seconds) {
    Logger.info("Inizio lettura pianificata meteo")

    val port = conf.getInt("http.port").getOrElse(9000)
    ws.url(s"http://0.0.0.0:${port}/meteo/Rovigo").post("")
    ws.url(s"http://0.0.0.0:${port}/meteo/Napoli").post("")
    Logger.info("fine lettura pianificata meteo")
  }

  @ApiOperation(value = "un api wrapper per popolare il meteo di una città. Chiama l'API api.openweathermap.org per ottenere il valore",
    notes = "Ritorna un punto serietemporale",
    response = classOf[models.RigaSerieTemporale],
    httpMethod = "POST")
  @ApiResponses(Array(new ApiResponse(code = 500, message = "Opps")))
  def aggiungiMeteo(@ApiParam(name = "nome", value = "esempi sono Rovigo o Napoli", required = true, defaultValue = "Rovigo") nome: String) =
    Action.async(BodyParsers.parse.empty) { implicit request =>

      val futureResponse: Future[WSResponse] = for {
        openWeatherResponse <- ws.url(ApiMeteoUrl)
          .withQueryString("q" -> nome)
          .withQueryString("units" -> "metric")
          .withQueryString("APPID" -> APPID)
          .get()
        timeSeriesResponse <- {
          Logger.info(s"${nome} ${openWeatherResponse.body}")
          val temp = (openWeatherResponse.json \ "main" \ "temp").as[Float]
          val dt = (openWeatherResponse.json \ "dt").as[Long]
          val store = Json.toJson(models.ValoreEtichettaSerieTemporale("" + dt, "" + temp))

          val url = routes.ApiSerieTemporale.mandaDaNome(nome).absoluteURL()

          ws.url(url).post(store)
        }
      } yield timeSeriesResponse

      futureResponse.map {
        r =>
          {
            Ok(r.json).as("application/json")
          }
      }
    }
}



