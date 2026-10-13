package app

import io.circe.Codec

final case class Todo(id: Long, title: String, done: Boolean) derives Codec.AsObject

final case class NewTodo(title: String) derives Codec.AsObject
