package dotty.bot.model

import upickle.default.{Reader, macroR, Writer, macroW}

//object Github {
//  case class Issue(
//    action: Option[String], // "opened", "reopened", "closed", "synchronize"
//    number: Int,
//    pull_request: Option[PullRequest]
//  )
//
//  case class PullRequest(url: String, id: Option[Long], commits_url: Option[String])
//
//  case class CommitInfo(message: String)
//
//  case class Commit(
//    sha: String,
//    author: Author,
//    committer: Author,
//    commit: CommitInfo
//  )
//
//  case class Author(
//    login: Option[String]
//  )
//
//  case class Status(
//    state: String,
//    target_url: String,
//    description: String,
//    context: String = "CLA"
//  )
//
//  case class StatusResponse(
//    url: String,
//    id: Long,
//    state: String,
//    context: String,
//    target_url: String
//  ) {
//    def sha: String = url.split('/').last
//  }
//
//  /** A PR review */
//  case class Review(body: String, event: String)
//
//  object Review {
//    def approve(body: String) = Review(body, "APPROVE")
//    def requestChanges(body: String) = Review(body, "REQUEST_CHANGES")
//    def comment(body: String) = Review(body, "COMMENT")
//  }
//
//  case class ReviewResponse(body: String, state: String, id: Long)
//
//  case class IssueComment(action: String, issue: Issue, comment: Comment)
//
//  case class Comment(user: Author, body: String)
//}

/** Models JSON objects from the GitHub v3 REST API.
 *
 *  GitHub documentation at: https://developer.github.com/v3/
 *
 *  This object includes JSON serializer using the µPickle library.
 *
 *  Notes:
 *    - A model fields name must match its JSON object.
 *    - These models are incomplete. New fields should be added as needed.
 */
object Github {

  // https://developer.github.com/v3/users/
  case class User(login: String)
  object User {
    implicit def reader: Reader[User] = macroR
  }

  // https://developer.github.com/v3/repos/
  case class Repository(name: String, full_name: String)
  object Repository {
    implicit def reader: Reader[Repository] = macroR
  }

  // https://developer.github.com/v3/pulls/
  case class PullRequest(
    number: Int,
    user: User
  )
  object PullRequest {
    implicit def reader: Reader[PullRequest] = macroR
  }

  // https://developer.github.com/v3/activity/events/types/#pullrequestevent
  case class PullRequestEvent(
    action: String,
    pull_request: PullRequest,
    repository: Repository
  )
  object PullRequestEvent {
    implicit def reader: Reader[PullRequestEvent] = macroR
  }

  case class Issue(number: Int)
  object Issue {
    implicit def reader: Reader[Issue] = macroR
  }

  // https://developer.github.com/v3/issues/comments/
  case class Comment(
    user: User,
    body: String
  )
  object Comment {
    implicit def reader: Reader[Comment] = macroR
  }

  // https://developer.github.com/v3/activity/events/types/#issuecommentevent
  case class IssueCommentEvent(
    action: String,
    issue: Issue,
    comment: Comment,
    repository: Repository
  )
  object IssueCommentEvent {
    implicit def reader: Reader[IssueCommentEvent] = macroR
  }

  // https://developer.github.com/v3/pulls/#list-commits-on-a-pull-request
  case class Commit(
    sha: String,
    author: User,
    committer: User,
    commit: CommitInfo
  )
  object Commit {
    implicit def reader: Reader[Commit] = macroR
  }

  case class CommitInfo(message: String)
  object CommitInfo {
    implicit def reader: Reader[CommitInfo] = macroR
  }
}
