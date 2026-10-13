# infra

Local infrastructure for http4s-ts-app: a single Kafka broker (KRaft mode, no ZooKeeper).

## Start / stop

```bash
docker compose -f infra/docker-compose.yml up -d     # start
docker compose -f infra/docker-compose.yml ps        # wait for "healthy"
docker compose -f infra/docker-compose.yml down      # stop (keeps data)
docker compose -f infra/docker-compose.yml down -v   # stop and wipe data
```

Bootstrap server: `localhost:9092` from your machine, `kafka:29092` from other containers.

## Try it

```bash
# Create a topic
docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 \
  --create --if-not-exists --topic user-messages

# Produce messages in "userId:message" format (one per line, Ctrl+D to finish)
docker exec -i kafka /opt/kafka/bin/kafka-console-producer.sh \
  --bootstrap-server localhost:9092 --topic user-messages <<'MSGS'
42:hello from user 42
7:hi there
42:second message
MSGS

# Read them back
docker exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic user-messages --from-beginning --timeout-ms 5000
```
