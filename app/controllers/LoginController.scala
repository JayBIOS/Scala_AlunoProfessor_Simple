package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Controller

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

import forms.LoginForm
import models.services.api.UsuarioService
import utils.auth.DefaultEnv

class LoginController @Inject() (
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  usuarioService: UsuarioService,
  authInfoRepository: AuthInfoRepository,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
  configuration: Configuration,
  clock: Clock,
  implicit val webJarAssets: WebJarAssets
) extends Controller with I18nSupport {

  def show = silhouette.UnsecuredAction.async { implicit request =>
    Future.successful(Ok(views.html.autenticacao.login(LoginForm.form, socialProviderRegistry)))
  }

  def enviar = silhouette.UnsecuredAction.async { implicit request =>
    LoginForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.autenticacao.login(form, socialProviderRegistry))),
      data => {
        val credentials = Credentials(data.email, data.senha)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          val result = Redirect(routes.MainController.index())
          usuarioService.retrieve(loginInfo).flatMap {
            case Some(usuario) if !usuario.ativado =>
              Future.successful(Ok(views.html.autenticacao.ativarConta(data.email)))
            case Some(usuario) =>
              val c = configuration.underlying
              silhouette.env.authenticatorService.create(loginInfo).map {
                case authenticator if data.lembrarMe =>
                  authenticator.copy(
                    expirationDateTime = clock.now + c.as[FiniteDuration](
                    "silhouette.authenticator.rememberMe.authenticatorExpiry"
                  ),
                    idleTimeout = c.getAs[FiniteDuration](
                      "silhouette.authenticator.rememberMe.authenticatorIdleTimeout"
                    ),
                    cookieMaxAge = c.getAs[FiniteDuration](
                      "silhouette.authenticator.rememberMe.rememberMe.cookieMaxAge"
                    )
                  )
                case authenticator => authenticator
              }.flatMap { authenticator =>
                silhouette.env.eventBus.publish(LoginEvent(usuario, request))
                silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                  silhouette.env.authenticatorService.embed(v, result)
                }
              }
            case None => Future.failed(new IdentityNotFoundException("Não foi possível encontrar o usuário"))
          }
        }
      }.recover {
        case e: ProviderException =>
          Redirect(routes.LoginController.show()).flashing("error" -> Messages("login.form.invalido"))
      }
    )
  }
}
