package dotty.bot.model

import dotty.bot.util.HttpClientAux
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client

import scalaz.concurrent.Task

object Drone {
  import HttpClientAux._

  case class Build(
    number: Int,
    event: String,
    status: String,
    commit: String,
    author: String
  )

  private[this] val baseUrl = "http://dotty-ci.epfl.ch/api"

  private def job(id: Int) =
    s"$baseUrl/repos/lampepfl/dotty/builds/$id"

  private def job(id: Int, subId: Int) =
    s"$baseUrl/repos/lampepfl/dotty/builds/$id/$subId"

  def stopBuild(id: Int, token: String)(implicit client: Client): Task[Boolean] = {
    client.fetch(delete(job(id, 1)).withOauth2(token)) { res =>
      val isSuccessful = res.status.code >= 200 && res.status.code < 400
      Task.now(isSuccessful)
    }
  }

  def startBuild(id: Int, token: String)(implicit client: Client): Task[Build] =
    client.expect(post(job(id)).withOauth2(token))(jsonOf[Build])
}
