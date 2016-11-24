package models

import play.api.libs.json._

case class RigaSerieTemporale(id: Long, nome: String, etichetta:String , valore:String)

object RigaSerieTemporale extends ((Long, String, String,String) => RigaSerieTemporale) {
   implicit val FormatoSerieTemporale = Json.format[RigaSerieTemporale]
}

