package app.kafka

/** A message in the `userId:message` wire format. */
final case class UserMessage(userId: String, message: String):
  def encode: String = s"$userId:$message"

object UserMessage:

  /** Parses `userId:message`. Splits on the first ':' so the message itself may contain colons. */
  def parse(raw: String): Either[String, UserMessage] =
    raw.indexOf(':') match
      case -1 => Left(s"expected 'userId:message', got '$raw'")
      case i =>
        val userId  = raw.substring(0, i).trim
        val message = raw.substring(i + 1).trim
        if userId.isEmpty then Left(s"empty userId in '$raw'")
        else Right(UserMessage(userId, message))
