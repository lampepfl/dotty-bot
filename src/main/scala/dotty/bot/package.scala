package dotty

import java.net.HttpURLConnection.{HTTP_BAD_REQUEST, HTTP_OK}

package object bot {
  def BadRequest(msg: String) = cask.Response(msg, HTTP_BAD_REQUEST)
  def Ok(msg: String) = cask.Response(msg, HTTP_OK)
}
