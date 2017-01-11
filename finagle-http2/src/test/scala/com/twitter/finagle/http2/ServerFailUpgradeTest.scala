package com.twitter.finagle.http2

import com.twitter.finagle
import com.twitter.finagle.http.{AbstractHttp1EndToEndTest, Request, Response}
import com.twitter.finagle.Service
import com.twitter.util.Future
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
 * This is really a HTTP/1.x test suite because the client only speaks HTTP/1.x
 */
@RunWith(classOf[JUnitRunner])
class ServerFailUpgradeTest extends AbstractHttp1EndToEndTest {
  def implName: String = "http/1.1 client, http/2 server"
  def clientImpl(): finagle.Http.Client =
    finagle.Http.client

  def serverImpl(): finagle.Http.Server =
    finagle.Http.server.configuredParams(Http2)

  private[this] lazy val unsupported: Set[Feature] = Set(
    TooLargePayloads,  // flaky because of https://github.com/netty/netty/issues/5982
    TooLongStream      // flaky because of https://github.com/netty/netty/issues/5982
  )

  def featureImplemented(feature: Feature): Boolean = !unsupported.contains(feature)

  test("Upgrade counters are not incremented") {
    val client = nonStreamingConnect(Service.mk { req: Request =>
      Future.value(Response())
    })

    await(client(Request("/")))

    assert(!statsRecv.counters.contains(Seq("server", "upgrade", "success")))
    assert(!statsRecv.counters.contains(Seq("client", "upgrade", "success")))

    await(client.close())
  }
}
