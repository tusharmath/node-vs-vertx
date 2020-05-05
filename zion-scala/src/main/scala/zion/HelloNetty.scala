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

  class NettyHandler extends SimpleChannelInboundHandler[AnyRef] {
    override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
      println("FLUSH")
      ctx.flush
    }

    def channelRead__1(ctx: ChannelHandlerContext): Unit = {
      ctx.write(
        Unpooled.copiedBuffer(HelloResponse.ok(helloNetty), CharsetUtil.UTF_8)
      )
    }
    def channelRead__2(
        ctx: ChannelHandlerContext
    ): Unit = {

      val buf = Unpooled.copiedBuffer(
        helloNetty,
        CharsetUtil.UTF_8
      )
      val response = new DefaultFullHttpResponse(
        HttpVersion.HTTP_1_1,
        HttpResponseStatus.OK,
        buf
      )
      response.headers.set(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes)
      response.headers.set(HttpHeaders.Names.CONNECTION, "keep-alive")
      ctx.write(response)
    }

    def channelRead__3(
        ctx: ChannelHandlerContext
    ): Unit = {

      val buf = Unpooled.copiedBuffer(
        helloNetty,
        CharsetUtil.UTF_8
      )
      val response = new DefaultFullHttpResponse(
        HttpVersion.HTTP_1_1,
        HttpResponseStatus.OK
      )
      response.headers.set(HttpHeaders.Names.CONTENT_LENGTH, buf.readableBytes)
      response.headers.set(HttpHeaders.Names.CONNECTION, "keep-alive")

      println(response.toString)
      ctx.writeAndFlush(
        Unpooled.copiedBuffer(List("HTTP/1.1 200 OK").mkString("\r\n\r\n"), CharsetUtil.UTF_8)
      )

//      println("helloNetty")
//      ctx.write(
//        Unpooled
//          .copiedBuffer("\r\n\r\n" + helloNetty + "\r\n", CharsetUtil.UTF_8)
//      )
    }

    override def channelRead0(
        ctx: ChannelHandlerContext,
        msg: scala.AnyRef
    ): Unit = {
      channelRead__3(ctx)
    }
  }

  class NettyServer {
    val value = new ChannelInitializer[SocketChannel] {
      def initChannel(socketChannel: SocketChannel) = {
        val pipeline = socketChannel.pipeline
//        pipeline.addLast(new HttpServerCodec())
        pipeline.addLast(new NettyHandler())
      }
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
