package zion
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels._

object MHello {
  def httpResponse(socket: SocketChannel)(content: String) = {
    val delim = "\r\n"

    val bytes = List(
      "HTTP/1.1 200 OK",
      "Content-Type: text/html",
      "Connection: keep-alive",
      s"Content-Length: ${content.length()}",
      delim + content
    ).mkString(delim).getBytes()

    socket.write(ByteBuffer.wrap(bytes))
  }

  def main(args: Array[String]): Unit = {
    // Selector: multiplexor of SelectableChannel objects
    val selector = Selector.open // selector is open here

    // ServerSocketChannel: selectable channel for stream-oriented listening sockets
    val server  = ServerSocketChannel.open
    val address = new InetSocketAddress("localhost", 8080)

    // Binds the channel's socket to a local address and configures the socket to listen for connections
    server.bind(address)

    // Adjusts this channel's blocking mode.
    server.configureBlocking(false)
    val ops      = server.validOps
    val selectKy = server.register(selector, ops, null)
    // Infinite loop..
    // Keep server running
    while (true) {
      log(
        "i'm a server and i'm waiting for new connection and buffer select..."
      )
      // Selects a set of keys whose corresponding channels are ready for I/O operations
      selector.select
      // token representing the registration of a SelectableChannel with a Selector
      val crunchifyKeys     = selector.selectedKeys
      val crunchifyIterator = crunchifyKeys.iterator
      while (crunchifyIterator.hasNext) {
        val myKey = crunchifyIterator.next
        // Tests whether this key's channel is ready to accept a new socket connection
        if (myKey.isAcceptable) {
          val socketChannel = server.accept

          // Adjusts this channel's blocking mode to false
          socketChannel.configureBlocking(false)
          // Operation-set bit for read operations
          socketChannel.register(selector, SelectionKey.OP_READ)
          log("Connection Accepted: " + socketChannel.getLocalAddress + "\n")
          // Tests whether this key's channel is ready for reading

          httpResponse(socketChannel)("Hello World\n")
          //          crunchifyClient.close()
        } else if (myKey.isReadable) {
          val socketChannel =
            myKey.channel.asInstanceOf[SocketChannel]
          val crunchifyBuffer = ByteBuffer.allocate(256)
          socketChannel.read(crunchifyBuffer)
          val result = new String(crunchifyBuffer.array).trim
          log("Message received: " + result)

//          if (result == "Crunchify") {
//            socketChannel.close()
//            log(
//              "\nIt's time to close connection as we got last company name 'Crunchify'"
//            )
//            log(
//              "\nServer will keep running. Try running client again to establish new connection"
//            )
//          }

        }

        crunchifyIterator.remove()
      }
    }
  }

  private def log(str: String): Unit = {
//     System.out.println(str)
  }

}
