package models

import play.api.libs.json._

 case class ValoreEtichettaSerieTemporale(etichetta:String, valore:String)
 
 object ValoreEtichettaSerieTemporale  extends ((String,String) => ValoreEtichettaSerieTemporale) {
  
  implicit val formatoPersona = Json.format[ValoreEtichettaSerieTemporale]
}
