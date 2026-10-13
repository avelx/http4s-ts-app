package app

import cats.effect.{IO, IOApp}
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.http4s.server.middleware.Logger

object Main extends IOApp.Simple:

  def run: IO[Unit] =
    for
      port <- IO(sys.env.get("PORT").flatMap(Port.fromString).getOrElse(port"8080"))
      repo <- TodoRepo.make
      httpApp = Router(
        "/api" -> Routes.api(repo),
        "/"    -> Routes.static
      ).orNotFound
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port)
        .withHttpApp(Logger.httpApp(logHeaders = false, logBody = false)(httpApp))
        .build
        .evalTap(server => IO.println(s"Server running at ${server.baseUri}"))
        .useForever
    yield ()
