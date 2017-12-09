package dotty.bot

import org.http4s.server.blaze._
import org.http4s.util.ProcessApp

import scalaz.concurrent.Task
import scalaz.stream.Process

object Main extends ProcessApp with PullRequestService {

  val githubUser         = sys.env("GITHUB_USER")
  val githubToken        = sys.env("GITHUB_TOKEN")
  val githubClientId     = sys.env("GITHUB_CLIENT_ID")
  val githubClientSecret = sys.env("GITHUB_CLIENT_SECRET")
  val droneToken         = sys.env("DRONE_TOKEN")
  val port               = sys.env("PORT").toInt

  /** Services mounted to the server */
  final def services = prService

  override def process(args: List[String]): Process[Task, Nothing] = {
    BlazeBuilder
      .bindHttp(port, "0.0.0.0")
      .mountService(services, "/api")
      .serve
  }
}
