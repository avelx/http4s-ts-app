package app.kafka

import cats.effect.IO

final case class KafkaConfig(bootstrapServers: String, topic: String, groupId: String)

object KafkaConfig:
  /** Defaults match infra/docker-compose.yml; override with environment variables. */
  def fromEnv: IO[KafkaConfig] = IO {
    KafkaConfig(
      bootstrapServers = sys.env.getOrElse("KAFKA_BOOTSTRAP_SERVERS", "localhost:9092"),
      topic            = sys.env.getOrElse("KAFKA_TOPIC", "user-messages"),
      groupId          = sys.env.getOrElse("KAFKA_GROUP_ID", "http4s-ts-app")
    )
  }
