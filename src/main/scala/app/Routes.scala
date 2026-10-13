package app

import cats.effect.IO
import cats.syntax.all.*
import fs2.io.file.Path
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.http4s.server.staticcontent.{fileService, FileService}

object Routes:

  private object NameParam extends OptionalQueryParamDecoderMatcher[String]("name")

  def api(repo: TodoRepo): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" :? NameParam(name) =>
      Ok(Map("message" -> s"Hello, ${name.getOrElse("world")}!"))

    case GET -> Root / "todos" =>
      repo.list.flatMap(Ok(_))

    case req @ POST -> Root / "todos" =>
      req.as[NewTodo].flatMap { nt =>
        if nt.title.trim.isEmpty then BadRequest(Map("error" -> "title must not be empty"))
        else repo.create(nt.title.trim).flatMap(Created(_))
      }

    case POST -> Root / "todos" / LongVar(id) / "toggle" =>
      repo.toggle(id).flatMap(_.fold(NotFound())(Ok(_)))

    case DELETE -> Root / "todos" / LongVar(id) =>
      repo.delete(id).ifM(NoContent(), NotFound())
  }

  /** Serves the compiled TypeScript frontend from ./static */
  val static: HttpRoutes[IO] =
    fileService[IO](FileService.Config(systemPath = Path("static").toString))
