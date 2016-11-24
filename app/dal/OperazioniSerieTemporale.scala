package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import models.RigaSerieTemporale
import scala.concurrent.{ Future, ExecutionContext }
import scala.collection.mutable.ArrayBuffer


@Singleton
class OperazioniSerieTemporali @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  
  import dbConfig._
  import driver.api._

  class TabellaSerieTemporali(tag: Tag) extends Table[models.RigaSerieTemporale](tag, "serieTemporale") {
    
    def id = column[Long]("id",O.PrimaryKey,O.AutoInc)
    def nome = column[String]("nome")
    def etichetta = column[String]("etichetta")
    def valore = column[String]("valore")

    override def * = (id,nome,etichetta,valore) <> (models.RigaSerieTemporale.tupled, models.RigaSerieTemporale.unapply _)
  }
  
  private val serieTemporale = TableQuery[TabellaSerieTemporali]
  
  def create(nome: String, etichetta: String , valore: String): Future[RigaSerieTemporale] = db.run {

    (serieTemporale.map(p => (p.nome, p.etichetta,p.valore))
      returning serieTemporale.map(_.id)
     
      into ((entry, id) => { RigaSerieTemporale(id,entry._1, entry._2,entry._3)} )
    ) .+=(nome, etichetta, valore)
  }

  def list(nome:String): Future[Seq[RigaSerieTemporale]] = db.run {
    serieTemporale.filter( _.nome === nome ).result
  }
  
 
  
}
