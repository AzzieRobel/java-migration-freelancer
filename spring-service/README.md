## Spring Service – VM Setup & Deployment

Simple, step‑by‑step guide to run the new Spring Boot backend (with Cassandra, Redis, RabbitMQ and gRPC) on a Linux VM.

---

### 1. Requirements on the VM

- **OS**: Ubuntu 22.04 (or similar Linux)
- **Installed packages**:
  - Git
  - OpenJDK **17+**
  - Docker
  - `curl`

Example (Ubuntu):

```bash
sudo apt update
sudo apt install -y git openjdk-17-jdk docker.io curl
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"   # log out/in to take effect
```

---

### 2. Clone the repository

```bash
cd ~
git clone <your-repo-url> 2026_java
cd 2026_java/origin
```

Replace `<your-repo-url>` with the real Git URL.

---

### 3. Start infrastructure (Cassandra, Redis, RabbitMQ)

All three run via Docker.

```bash
docker run --name cassandra -p 9042:9042 -d cassandra:4.1
docker run --name redis     -p 6379:6379 -d redis:7
docker run --name rabbitmq  -p 5672:5672 -p 15672:15672 -d rabbitmq:3-management
```

Check they are running:

```bash
docker ps
# You should see containers named cassandra, redis, rabbitmq, all with STATUS "Up"
```

---

### 4. Initialize Cassandra schema

Open a CQL shell inside the Cassandra container:

```bash
docker exec -it cassandra cqlsh
```

In the `cqlsh>` prompt, paste:

```sql
CREATE KEYSPACE IF NOT EXISTS textsecure
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};

CREATE TABLE IF NOT EXISTS textsecure.accounts_by_id (
  account_id uuid,
  phone_number text,
  discoverable_by_phone boolean,
  created_at timestamp,
  PRIMARY KEY ((account_id))
);

CREATE TABLE IF NOT EXISTS textsecure.accounts_by_phone (
  phone_number text,
  account_id uuid,
  discoverable_by_phone boolean,
  created_at timestamp,
  PRIMARY KEY ((phone_number))
);

CREATE TABLE IF NOT EXISTS textsecure.subscriptions (
  account_id uuid,
  level int,
  status text,
  started_at timestamp,
  expires_at timestamp,
  PRIMARY KEY ((account_id))
);

CREATE TABLE IF NOT EXISTS textsecure.messages_by_recipient (
  recipient_id uuid,
  message_timestamp timestamp,
  message_id uuid,
  sender_id uuid,
  payload blob,
  status text,
  PRIMARY KEY ((recipient_id), message_timestamp, message_id)
) WITH CLUSTERING ORDER BY (message_timestamp DESC);
```

Type `exit;` to leave `cqlsh` when done.

---

### 5. Build the Spring service

From the project root:

```bash
cd ~/2026_java/origin
chmod +x mvnw
./mvnw -pl spring-service test
```

This will:

- Download Maven + dependencies (first run may take a while).
- Generate gRPC classes from `messaging.proto`.
- Compile the `spring-service` module.

You should see `BUILD SUCCESS`.

---

### 6. Run the Spring service

Start the Spring Boot app:

```bash
cd ~/2026_java/origin
./mvnw -pl spring-service spring-boot:run
```

- HTTP REST port: **9080**
- gRPC port: **9090** (configurable via `grpc.messaging.port` in `application.yml`)

Keep this process running (in its own terminal).

---

### 7. Quick REST API checks

Open a **second** terminal on the VM.

#### 7.1 Health check

```bash
curl http://localhost:9080/api/health
```

You should get a small JSON with `status` and `timestamp`.

#### 7.2 Create an account

```bash
curl -X POST "http://localhost:9080/api/accounts" \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"+15550001111","discoverableByPhoneNumber":true}'
```

Note the returned `id` and `phoneNumber`.

#### 7.3 Lookup account by id / phone

```bash
ACCOUNT_ID=<paste-id-from-previous-response>

curl "http://localhost:9080/api/accounts/$ACCOUNT_ID"
curl "http://localhost:9080/api/accounts?phone=+15550001111"
```

#### 7.4 Send and read a message

Create a second account, then:

```bash
SENDER=<first-account-id>
RECIPIENT=<second-account-id>

curl -X POST "http://localhost:9080/api/messages" \
  -H "Content-Type: application/json" \
  -d "{\"senderId\":\"$SENDER\",\"recipientId\":\"$RECIPIENT\",\"body\":\"hello from Spring\"}"

curl "http://localhost:9080/api/messages/$RECIPIENT?limit=50"
```

#### 7.5 Rate limiting (Redis)

```bash
for i in {1..5}; do
  curl -s -o /dev/null -w "%{http_code}\n" \
    -X POST "http://localhost:9080/api/messages" \
    -H "Content-Type: application/json" \
    -d "{\"senderId\":\"$SENDER\",\"recipientId\":\"$RECIPIENT\",\"body\":\"test\"}"
done

curl -s -o /dev/null -w "%{http_code}\n" \
  -X POST "http://localhost:9080/api/messages" \
  -H "Content-Type: application/json" \
  -d "{\"senderId\":\"$SENDER\",\"recipientId\":\"$RECIPIENT\",\"body\":\"over limit\"}"
```

The last call should return `429`.

---

### 8. RabbitMQ verification (message events)

1. Open RabbitMQ management UI in a browser (from your host or VM):

   - URL: `http://<vm-host-or-ip>:15672`
   - Username: `guest`
   - Password: `guest`

2. Go to:

   - **Queues** → `spring.message.events.queue`
   - Click **Get messages** to see events for each message sent via `/api/messages`.

---

### 9. gRPC endpoint (for Elixir / Phoenix)

- gRPC service definition: `spring-service/src/main/proto/messaging.proto`
- Service: `textsecure.messaging.v1.MessagingService`
  - `SendMessage`
  - `ListMessages`
- Server:
  - Runs inside the Spring app on port **9090** by default.

Clients (e.g., Elixir/Phoenix) should:

1. Generate gRPC client code from `messaging.proto`.
2. Connect to `localhost:9090` (or the configured `grpc.messaging.port`).
3. Call `SendMessage` / `ListMessages` with UUID strings that match the REST account IDs.

---

### 10. Stopping services

To stop the Spring app:

```bash
# In the terminal running spring-boot:run
Ctrl+C
```

To stop Docker services:

```bash
docker stop cassandra redis rabbitmq
```

To remove them completely (optional):

```bash
docker rm cassandra redis rabbitmq
```

