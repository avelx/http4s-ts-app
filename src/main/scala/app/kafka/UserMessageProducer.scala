package app.kafka

import scala.concurrent.duration.*
import cats.effect.{IO, IOApp}
import cats.effect.std.Random
import cats.syntax.all.*
import fs2.Stream
import fs2.kafka.*

object UserMessageProducer:

  /** Sends each message to `config.topic`, keyed by userId so a user's messages stay ordered. */
  def pipe(config: KafkaConfig): fs2.Pipe[IO, UserMessage, ProducerResult[String, String]] =
    val settings = ProducerSettings[IO, String, String]
      .withBootstrapServers(config.bootstrapServers)
      .withAcks(Acks.All)

    messages =>
      messages
        .map(m => ProducerRecords.one(ProducerRecord(config.topic, m.userId, m.encode)))
        .through(KafkaProducer.pipe(settings))

  /** Endless stream of sample messages from a handful of users, one every `every`. */
  def sample(every: FiniteDuration): Stream[IO, UserMessage] =
    val words = Vector("hello", "ping", "how are you?", "order placed", "logged in", "logged out")
    Stream.eval(Random.scalaUtilRandom[IO]).flatMap { rnd =>
      Stream
        .awakeEvery[IO](every)
        .zipWithIndex
        .evalMap { case (_, n) =>
          for
            user <- rnd.betweenInt(1, 6)
            word <- rnd.elementOf(words)
          yield UserMessage(s"user-$user", s"#$n $word")
        }
    }

/** sbt "runMain app.kafka.ProducerApp [count]"  — sends `count` sample messages (default 10). */
object ProducerApp extends IOApp:
  import cats.effect.ExitCode

  def run(args: List[String]): IO[ExitCode] =
    val count = args.headOption.flatMap(_.toLongOption).getOrElse(10L)
    for
      config <- KafkaConfig.fromEnv
      _      <- IO.println(s"Producing $count messages to '${config.topic}' at ${config.bootstrapServers}")
      _ <- UserMessageProducer
        .sample(every = 500.millis)
        .take(count)
        .through(UserMessageProducer.pipe(config))
        .evalMap( x => x.traverse { case (record, meta) =>
          IO.println(s"sent  ${record.value}  -> partition ${meta.partition()} offset ${meta.offset()}")
        })
        .compile
        .drain
    yield ExitCode.Success
