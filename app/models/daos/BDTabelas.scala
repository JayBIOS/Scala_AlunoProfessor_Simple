package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import slick.driver.PostgresDriver
import slick.lifted.ProvenShape.proveShapeOf

trait BDTabelas {

  // Driver utilizado pelo Slick
  protected val driver: PostgresDriver

  import driver.api._

  case class DBUsuario (
    id: Long,
    papel: String,
    nomeCompleto: String,
    email: String,
    avatarUrl: String
  )

  class Usuarios(tag: Tag) extends Table[DBUsuario](tag, "usuarios") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def papel = column[String]("papel")
    def nomeCompleto = column[String]("nome_completo")
    def email = column[String]("email")
    def avatarUrl = column[String]("avatar_url")
    override def * = (id, papel, nomeCompleto, email, avatarUrl) <> (Usuario.tupled, Usuario.unapply)
  }

  // Querys das tabelas
  val usuarios = TableQuery[Usuarios]

}
