package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.Future

import models.Usuario

class UsuarioDAOImpl @Inject()(
  protected val dbConfigProvider: DatabaseConfigProvider
  ) extends UsuarioDAO with DAO {
    import driver.api._

    // TODO: Implement
}
