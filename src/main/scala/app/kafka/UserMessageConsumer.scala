package app.kafka

import scala.concurrent.duration.*

import cats.effect.{IO, IOApp}
import fs2.Stream
import fs2.kafka.*

object UserMessageConsumer:

  /** Raw records parsed as `userId:message`; malformed values are passed on as Left. */
  final case class Received(partition: Int, offset: Long, parsed: Either[String, UserMessage])

  /**
   * Subscribes to `config.topic`, hands every record to `handle`, then commits offsets
   * in batches. Keys and values are Option[String] so null keys/tombstones don't crash
   * the stream.
   */
  def stream(config: KafkaConfig)(handle: Received => IO[Unit]): Stream[IO, Unit] =
    val settings = ConsumerSettings[IO, Option[String], Option[String]]
      .withBootstrapServers(config.bootstrapServers)
      .withGroupId(config.groupId)
      .withAutoOffsetReset(AutoOffsetReset.Earliest)

    KafkaConsumer
      .stream(settings)
      .subscribeTo(config.topic)
      .records
      .evalMap { committable =>
        val r      = committable.record
        val parsed = r.value.toRight("null value").flatMap(UserMessage.parse)
        handle(Received(r.partition, r.offset, parsed)).as(committable.offset)
      }
      .through(commitBatchWithin[IO](100, 5.seconds))

/** sbt "runMain app.kafka.ConsumerApp"  — prints every message until Ctrl+C. */
object ConsumerApp extends IOApp.Simple:

  def run: IO[Unit] =
    KafkaConfig.fromEnv.flatMap { config =>
      IO.println(s"Consuming '${config.topic}' from ${config.bootstrapServers} as group '${config.groupId}'") *>
        UserMessageConsumer
          .stream(config) {
            case UserMessageConsumer.Received(p, o, Right(UserMessage(userId, message))) =>
              IO.println(s"recv  [$p/$o] $userId -> $message")
            case UserMessageConsumer.Received(p, o, Left(error)) =>
              IO.println(s"skip  [$p/$o] malformed: $error")
          }
          .compile
          .drain
    }
