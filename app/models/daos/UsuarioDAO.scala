package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.concurrent.Execution
import scala.concurrent.Future

import models.Usuario

/**
 * Da acesso ao objeto Usuario.
 */
trait UsuarioDAO {

  def procurar(loginInfo: LoginInfo): Future[Option[Usuario]]

  def procurar(id: Int): Future[Option[Usuario]]

  def salvar(usuario: Usuario): Future[Usuario]
}
