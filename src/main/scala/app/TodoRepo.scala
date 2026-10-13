package app

import cats.effect.{IO, Ref}

/** In-memory todo storage backed by a cats-effect Ref. */
final class TodoRepo private (state: Ref[IO, (Long, Map[Long, Todo])]):

  def list: IO[List[Todo]] =
    state.get.map(_._2.values.toList.sortBy(_.id))

  def create(title: String): IO[Todo] =
    state.modify { case (nextId, todos) =>
      val todo = Todo(nextId, title, done = false)
      ((nextId + 1, todos + (nextId -> todo)), todo)
    }

  def toggle(id: Long): IO[Option[Todo]] =
    state.modify { case s @ (nextId, todos) =>
      todos.get(id) match
        case Some(t) =>
          val updated = t.copy(done = !t.done)
          ((nextId, todos + (id -> updated)), Some(updated))
        case None => (s, None)
    }

  def delete(id: Long): IO[Boolean] =
    state.modify { case (nextId, todos) =>
      ((nextId, todos - id), todos.contains(id))
    }

object TodoRepo:
  def make: IO[TodoRepo] = Ref.of[IO, (Long, Map[Long, Todo])]((1L, Map.empty)).map(TodoRepo(_))
