package dotty.bot

import dotty.bot.PullRequestService._
import dotty.bot.models.Drone
import dotty.bot.models.Github._
import io.circe.generic.auto._
import io.circe.parser.decode
import org.http4s.client.Client
import org.http4s.client.blaze._
import org.junit.Assert._
import org.junit.{Ignore, Test}

import scalaz.concurrent.Task

class PullRequestServiceTests extends PullRequestService {
  val githubUser         = sys.env("GITHUB_USER")
  val githubToken        = sys.env("GITHUB_TOKEN")
  val droneToken         = sys.env("DRONE_TOKEN")
  val githubClientId     = sys.env("GITHUB_CLIENT_ID")
  val githubClientSecret = sys.env("GITHUB_CLIENT_SECRET")

  private def withClient[A](f: Client => Task[A]): A = {
    val httpClient = SimpleHttp1Client()
    try f(httpClient).run
    finally httpClient.shutdownNow()
  }

  def getResource(r: String): String =
    Option(getClass.getResourceAsStream(r)).map(scala.io.Source.fromInputStream)
      .map(_.mkString)
      .getOrElse(throw new Exception(s"resource not found: $r"))

  @Test def canUnmarshalIssueJson = {
    val json = getResource("/test-pr.json")
    val issue: Issue = decode[Issue](json) match {
      case Right(is: Issue) => is
      case Left(ex) => throw ex
    }

    assertTrue("missing pull request", issue.pull_request.isDefined)
  }

  @Test def canUnmarshalIssueComment = {
    val json = getResource("/test-mention.json")
    val issueComment: IssueComment = decode[IssueComment](json) match {
      case Right(is: IssueComment) => is
      case Left(ex) => throw ex
    }

    assertEquals(
      s"incorrect body: ${issueComment.comment.body}",
      "@dotty-bot: could you recheck this please?",
      issueComment.comment.body
    )
  }

  @Test def canGetAllCommitsFromPR = {
    val issueNbr = 1941 // has 2 commits: https://github.com/lampepfl/dotty/pull/1941/commits
    val List(c1, c2) = withClient(getCommits(issueNbr)(_))

    assertEquals(
      "Represent untyped operators as Ident instead of Name",
      c1.commit.message.takeWhile(_ != '\n')
    )

    assertEquals(
      "Better positions for infix term operations.",
      c2.commit.message.takeWhile(_ != '\n')
    )
  }

  @Test def canGetMoreThan100Commits = {
    val issueNbr = 1840 // has >100 commits: https://github.com/lampepfl/dotty/pull/1840/commits
    val numberOfCommits = withClient(getCommits(issueNbr)(_)).length

    assertTrue(
      s"PR 1840, should have a number of commits greater than 100, but was: $numberOfCommits",
      numberOfCommits > 100
    )
  }

  @Test def canGetComments = {
    val comments: List[Comment] = withClient(getComments(2136, _))
    assertTrue(
      "Could not find Martin's comment on PR 2136",
      comments.exists(_.user.login.contains("odersky"))
    )
  }

  @Test def canCheckCLA = {
    val validUserCommit = Commit("sha-here", Author(Some("felixmulder")), Author(Some("felixmulder")), CommitInfo(""))
    val statuses: List[CommitStatus] = withClient(checkCLA(validUserCommit :: Nil)(_))

    assertEquals(
      s"wrong number of valid statuses: got ${statuses.length}, expected 1",
       1, statuses.length
    )
  }

  @Test def canSetStatus = {
    val sha = "fa64b4b613fe5e78a5b4185b4aeda89e2f1446ff"
    val status = Invalid("smarter", Commit(sha, Author(Some("smarter")), Author(Some("smarter")), CommitInfo("")))

    val statuses: List[StatusResponse] = withClient(sendStatuses(status :: Nil, _))

    assertEquals(
      s"assumed one status response would be returned, got: ${statuses.length}",
      1, statuses.length
    )

    assertEquals(
      s"status set had wrong state, expected 'failure', got: ${statuses.head.state}",
      "failure", statuses.head.state
    )
  }

  @Test def canGetStatus = {
    val sha = "fa64b4b613fe5e78a5b4185b4aeda89e2f1446ff"
    val commit = Commit(sha, Author(None), Author(None), CommitInfo(""))
    val status = withClient(getStatus(commit, _)).head

    assertEquals(sha, status.sha)
  }

  @Test def canPostReview = {
    val invalidUsers = "felixmulder" :: "smarter" :: Nil
    val commit = Commit("", Author(Some("smarter")), Author(Some("smarter")), CommitInfo("Added stuff"))
    val resBody = withClient(sendInitialComment(2281, invalidUsers, commit :: Nil, false)(_))

    assertTrue(
      s"Body of review was not as expected:\n$resBody",
      resBody.contains("We want to keep history") &&
      resBody.contains("Could you folks please sign the CLA?") &&
      resBody.contains("Have an awesome day!")
    )
  }

  @Test def canStartAndStopBuild = {
    val build = withClient(Drone.startBuild(1871, droneToken)(_))
    assertTrue(build.status == "pending" || build.status == "building")
    val killed = withClient(Drone.stopBuild(build.number, droneToken)(_))
    assertTrue(s"Couldn't kill build ${build.number}", killed)
  }

  // FIXME Trying to restart a build that does not exists
  @Ignore def canUnderstandWhenToRestartBuild = {
    val json = getResource("/test-mention.json")
    val issueComment: IssueComment = decode[IssueComment](json) match {
      case Right(is: IssueComment) => is
      case Left(ex) => throw ex
    }

    assertEquals(200, respondToComment(issueComment).run.status.code)
  }

  @Test def canTellUserWhenNotUnderstanding = {
    val json = getResource("/test-mention-no-understandy.json")
    val issueComment: IssueComment = decode[IssueComment](json) match {
      case Right(is: IssueComment) => is
      case Left(ex) => throw ex
    }

    assertEquals(200, respondToComment(issueComment).run.status.code)
  }

  @Test def canGetContributors = {
    val contributors = withClient(getContributors(_))
    assertTrue(contributors.contains("felixmulder"))
  }
}
