package zion

object HelloResponse {
  def ok(content: String): String = {
    val delim = "\r\n"
    List(
      "HTTP/1.1 200 OK",
      "Connection: keep-alive",
      s"Content-Length: ${content.length()}",
      delim + content
    ).mkString(delim)
  }
}
