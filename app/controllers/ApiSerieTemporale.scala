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

@Api(value = "/serietemporali", description = "Operazioni per l'utilizzo e la popolazione delle serietemporali")
@Singleton
class ApiSerieTemporale @Inject() (serieTemporali: OperazioniSerieTemporali) extends Controller {

  @ApiOperation(nickname = "prendiDaNome", value = 
      "prende le informaizioni per la serie in base al nome."+ 
      "La lista è ritornata in ordine di inserimento sul database",
    notes = "Ritorna una lista di punti serietemporale",
    responseContainer = "List", response = classOf[models.RigaSerieTemporale],
    httpMethod = "GET")
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Opps")))
  def prendiDaNome(@ApiParam(name = "nome", value = "esempi sono Rovigo, Napoli, AAPL o MSFT" , required = true, defaultValue = "AAPL") nome: String) =
    Action.async {
      serieTemporali.list(nome).map { righeSerieTemporali =>
        Ok(Json.toJson(righeSerieTemporali))
      }
    }

  @ApiOperation(nickname = "mandaDaNome", value = "aggiunge un dato per una serie temporale in base al nome",
    notes = "Ritorna i punti creati per la serietemporale, non vengono fatti controlli sui dati",
    response = classOf[models.RigaSerieTemporale],
    httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "body",
      dataType = "models.ValoreEtichettaSerieTemporale",
      required = true,
      paramType = "body",
      value = "oggetto con la coppia etichetta valore. Per esempio <br>{ \"etichetta\": \"2016-05-14T06:40:35Z\",<br> \"valore\": \"1000\" }"
      )))
  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "Non valido")))
  def mandaDaNome(@ApiParam(name = "nome",  value = "esempi sono Rovigo o Londra oppure examples are London or Darlington or AAPL or MSFT" , required = true, defaultValue = "AAPL") nome: String) = Action.async(BodyParsers.parse.json) { implicit request =>

    val create = request.body.validate[models.ValoreEtichettaSerieTemporale]

    create.fold(
      errors => {
        Future.successful(InternalServerError(JsError.toJson(errors)))
      },
      row => {
        serieTemporali.create(nome, row.etichetta, row.valore).map(created =>
          Ok(Json.toJson(created)))
      })

  }

 
}



