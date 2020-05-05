package zion

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.Unpooled
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket._
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelInitializer, SimpleChannelInboundHandler, _}
import io.netty.handler.codec.http._
import io.netty.util.CharsetUtil

/**
  * Handles a server-side channel.
  */
object HelloNetty extends App {
  def helloNetty = "Hello Netty!\n"

  class NettyHandler extends SimpleChannelInboundHandler[Nothing] {
    override def channelReadComplete(ctx: ChannelHandlerContext): Unit =
      ctx.flush
    override def channelRead0(
        ctx: ChannelHandlerContext,
        msg: Nothing
    ): Unit = {
      val buf = Unpooled.copiedBuffer(helloNetty, CharsetUtil.UTF_8)
      val response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf)

      response.headers.set(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes)
      response.headers.set(HttpHeaders.Names.CONNECTION, "keep-alive")

      ctx.write(response)
    }
  }

  class NettyServer {
    val value: ChannelInitializer[SocketChannel] =
      (socketChannel: SocketChannel) => {
        val pipeline = socketChannel.pipeline
        pipeline.addLast(new HttpServerCodec())
        pipeline.addLast(new NettyHandler())
      }

    def run(): Unit = {
      val eventLoopGroup = new NioEventLoopGroup()
      try {
        val serverBootstrap = new ServerBootstrap

        serverBootstrap
          .group(eventLoopGroup)
          .channel(classOf[NioServerSocketChannel])
          .childHandler(value)
        val channel = serverBootstrap.bind(7070).sync.channel
        channel.closeFuture.sync()
      } finally {
        eventLoopGroup.shutdownGracefully()
      }
    }
  }

  new NettyServer().run()
}
