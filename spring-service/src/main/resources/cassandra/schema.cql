-- Keyspace definition for local/dev Cassandra
CREATE KEYSPACE IF NOT EXISTS textsecure
WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': 1
};

-- Accounts stored by internal UUID
CREATE TABLE IF NOT EXISTS textsecure.accounts_by_id (
  account_id uuid,
  phone_number text,
  discoverable_by_phone boolean,
  created_at timestamp,
  PRIMARY KEY ((account_id))
);

-- Accounts indexed by phone number for fast lookup
CREATE TABLE IF NOT EXISTS textsecure.accounts_by_phone (
  phone_number text,
  account_id uuid,
  discoverable_by_phone boolean,
  created_at timestamp,
  PRIMARY KEY ((phone_number))
);

-- Basic subscriptions table (can be extended later)
CREATE TABLE IF NOT EXISTS textsecure.subscriptions (
  account_id uuid,
  level int,
  status text,
  started_at timestamp,
  expires_at timestamp,
  PRIMARY KEY ((account_id))
);

-- Messages per recipient, optimized for "recent messages for user"
CREATE TABLE IF NOT EXISTS textsecure.messages_by_recipient (
  recipient_id uuid,
  message_timestamp timestamp,
  message_id uuid,
  sender_id uuid,
  payload blob,
  status text,
  PRIMARY KEY ((recipient_id), message_timestamp, message_id)
) WITH CLUSTERING ORDER BY (message_timestamp DESC);


