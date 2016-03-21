package controllers

import com.google.inject.Inject
import org.joda.time.DateTime
import org.maproulette.session.SessionManager
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.Promise
import scala.util.{Failure, Success}

/**
  * All the authentication actions go in this class
  *
  * @author cuthbertm
  */
class AuthController @Inject() (val messagesApi: MessagesApi,
                                sessionManager:SessionManager) extends Controller with I18nSupport {

  import scala.concurrent.ExecutionContext.Implicits.global

  /**
    * An action to call to authenticate a user using OAuth 1.0a against the OAuth OSM Provider
    *
    * @return Redirects back to the index page containing a valid session
    */
  def authenticate() = Action.async { implicit request =>
    val p = Promise[Result]
    request.getQueryString("oauth_verifier").map { verifier =>
      sessionManager.retrieveUser(verifier) onComplete {
        case Success(user) =>
          // We received the authorized tokens in the OAuth object - store it before we proceed
          p success Redirect(routes.Application.index())
            .withSession(SessionManager.KEY_TOKEN -> user.osmProfile.requestToken.token,
              SessionManager.KEY_SECRET -> user.osmProfile.requestToken.secret,
              SessionManager.KEY_USER_ID -> user.id.toString,
              SessionManager.KEY_OSM_ID -> user.osmProfile.id.toString,
              SessionManager.KEY_USER_TICK -> DateTime.now().getMillis.toString
            )
        case Failure(e) => p failure e
      }
    }.getOrElse(
      sessionManager.retrieveRequestToken(routes.AuthController.authenticate().absoluteURL()) match {
        case Right(t) => {
          // We received the unauthorized tokens in the OAuth object - store it before we proceed
          p success Redirect(sessionManager.redirectUrl(t.token)).withSession("token" -> t.token, "secret" -> t.secret)
        }
        case Left(e) => p failure e
      })
    p.future
  }

  /**
    * Signs out the user, creating essentially a blank new session and redirects user to the index page
    *
    * @return The index html page
    */
  def signOut() = Action { implicit request =>
    Redirect(routes.Application.index()).withNewSession
  }

  /**
    * Generates a new API key for the user. A user can then use the API key to make API calls directly against
    * the server. Only the current API key for the user will work on any authenticated API calls, any previous
    * keys are immediately discarded once a new one is created.
    *
    * @return Will return NoContent if cannot create the key (which most likely means that no user was
    *         found, or will return the api key as plain text.
    */
  def generateAPIKey() = Action.async { implicit request =>
    sessionManager.userAwareRequest { implicit user =>
      user match {
        case Some(u) => u.generateAPIKey.apiKey match {
          case Some(api) => Ok(api)
          case None => NoContent
        }
        case None => NoContent
      }
    }
  }
}
