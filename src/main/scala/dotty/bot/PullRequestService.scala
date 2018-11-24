package dotty.bot

import dotty.bot.model.CLASignature
import dotty.bot.model.Github._
import requests.Session
import upickle.default.read

trait PullRequestService {

  /** An authenticated GitHub session */
  def ghSession: Session

  private def claUrl(userName: String) =
    s"https://www.lightbend.com/contribute/cla/scala/check/$userName"

  /** Return a pull request's commits.
   *
   *  @param repo   the repository
   *  @param number pull request number
   */
  private def commits(repo: Repository, number: Int): Seq[Commit] = {
    val url = s"https://api.github.com/repos/${repo.full_name}/pulls/$number/commits"
    val response = ghSession.get(url)
    read[Seq[Commit]](response.text)
  }

  /** Create a comment on an issue or pull request.
   *
   *  @param repo    the repository
   *  @param issueNb issue or pull request number
   *  @param body    comment's body
   */
  private def postComment(repo: Repository, issueNb: Int, body: String): Unit = {
    val url = s"https://api.github.com/repos/${repo.full_name}/issues/$issueNb/comments"
    ghSession.post(url, data = ujson.write(Map("body" -> body)))
  }

  /** Check the CLA and set the commit status accordingly
   *
   *  @param repo   the repository the commit belongs to
   *  @param commit the commit itself
   */
  private def checkCLA(repo: Repository, commit: Commit): Unit = {
    val url = claUrl(commit.author.login)
    val response = requests.get(url)
    val signature = read[CLASignature](response.text)
    def status(state: String, description: String) = Map(
      "state"       -> state,
      "target_url"  -> url,
      "description" -> description,
      "context"     -> "CLA"
    )

    ghSession.post(
      s"https://api.github.com/repos/${repo.full_name}/statuses/${commit.sha}",
      data = ujson.write(
        if (signature.signed)
          status("success", "User signed CLA")
        else
          status("failure", "User needs to sign cla: https://www.lightbend.com/contribute/cla/scala")
      )
    )
  }

  final def checkPullRequest(event: PullRequestEvent): cask.Response = {
    def lastCommit =
      commits(event.repository, event.pull_request.number).last

    event.action match {
      case "opened" =>
        checkCLA(event.repository, lastCommit)
        Ok("New PR checked")
      case "synchronize" =>
        checkCLA(event.repository, lastCommit)
        Ok("Updated PR checked")
      case action =>
        BadRequest(s"Unhandled action: $action")
    }
  }

  final def checkIssueComment(event: IssueCommentEvent): cask.Response = {
    def content = event.comment.body

    if (event.action != "created" || !content.contains("@dotty-bot"))
      Ok("Nothing to do here, move along!")
    else if (content.toLowerCase.contains("check cla")) {
      val repo = event.repository
      val lastCommit = commits(repo, event.issue.number).last
      checkCLA(lastCommit, repo)
      Ok("CLA checked")
    }
    else {
      ???
    }
  }
}
