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

@Api(value = "/azioni", description = "semplcie api per recuperare il prezzo dell'azione a cui siamo interessati")
@Singleton
class ApiAzioni @Inject() (conf: play.api.Configuration, serieTemporali: OperazioniSerieTemporali, ws: WSClient, system: ActorSystem) extends Controller {

  val ApiAzioniUrl = "http://query.yahooapis.com/v1/public/yql"
  def yql(quote: String) = s"select * from yahoo.finance.quotes where symbol ='${quote}'"
  val env = "store://datatables.org/alltableswithkeys"

  import scala.concurrent.duration._

  val port = Play

  system.scheduler.schedule(10 seconds, 120 seconds) {
    Logger.info("Inizio lettura azioni")

    val port = conf.getInt("http.port").getOrElse(9000)
    ws.url(s"http://0.0.0.0:${port}/azioni/AAPL").post("")
    ws.url(s"http://0.0.0.0:${port}/azioni/MSFT").post("")
    Logger.info("fine lettura azioni")
  }

  @ApiOperation(value = "un api wrapper per popolare il valore attuale di un'azione. Chiama Yahoo query.yahooapis.com per recuperare il valore dell'azione",
    notes = "Ritorna un punto serietemporale",
    response = classOf[models.RigaSerieTemporale],
    httpMethod = "POST")
  @ApiResponses(Array(new ApiResponse(code = 500, message = "Opps")))
  def aggiungiAzione(@ApiParam(name = "nome", value = "esempi sono AAPL o MSFT", required = true, defaultValue = "AAPL") nome: String) =
    Action.async(BodyParsers.parse.empty) { implicit request =>

      val futureResponse: Future[WSResponse] = for {
        yahooResponse <- ws.url(ApiAzioniUrl)
          .withQueryString("q" -> yql(nome))
          .withQueryString("env" -> env)
          .withQueryString("format" -> "json")
          .get()
        timeSeriesResponse <- {
          Logger.info(s"${nome} ${yahooResponse.body}")
          val dt = (yahooResponse.json \ "query" \ "created").as[String]
          val lastTradePriceOnly = (yahooResponse.json \ "query" \ "results" \ "quote" \ "LastTradePriceOnly").as[String]
          val store = Json.toJson(models.ValoreEtichettaSerieTemporale("" + dt, "" + lastTradePriceOnly))

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



