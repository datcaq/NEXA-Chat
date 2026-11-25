-- Initial shared schema: TV1 owns user provisioning; TV2 reuses conversations.
CREATE TABLE users (id uuid PRIMARY KEY);
CREATE TABLE conversations (
  id uuid PRIMARY KEY, type varchar(16) NOT NULL CHECK(type IN ('DIRECT','GROUP')),
  title varchar(120) NOT NULL, avatar_url text,
  created_at timestamptz NOT NULL, updated_at timestamptz NOT NULL
);
CREATE TABLE conversation_members (
  id uuid PRIMARY KEY, conversation_id uuid NOT NULL REFERENCES conversations(id),
  user_id uuid NOT NULL REFERENCES users(id), role varchar(16) NOT NULL CHECK(role IN ('OWNER','ADMIN','MEMBER')),
  status varchar(16) NOT NULL CHECK(status IN ('ACTIVE','LEFT','REMOVED')),
  joined_at timestamptz NOT NULL, left_at timestamptz, last_read_message_id uuid,
  UNIQUE(conversation_id,user_id)
);
CREATE UNIQUE INDEX one_active_owner ON conversation_members(conversation_id) WHERE role='OWNER' AND status='ACTIVE';
CREATE INDEX member_groups ON conversation_members(user_id,status,conversation_id);
CREATE TABLE group_invitations (
  id uuid PRIMARY KEY, conversation_id uuid NOT NULL REFERENCES conversations(id),
  inviter_user_id uuid NOT NULL REFERENCES users(id), invitee_user_id uuid NOT NULL REFERENCES users(id),
  status varchar(16) NOT NULL CHECK(status IN ('PENDING','ACCEPTED','REJECTED','CANCELLED')),
  created_at timestamptz NOT NULL, responded_at timestamptz,
  CHECK(inviter_user_id <> invitee_user_id),
  CHECK((status='PENDING' AND responded_at IS NULL) OR (status<>'PENDING' AND responded_at IS NOT NULL))
);
CREATE UNIQUE INDEX unique_pending_invitation ON group_invitations(conversation_id,invitee_user_id) WHERE status='PENDING';
CREATE INDEX incoming_invitations ON group_invitations(invitee_user_id,status,created_at,id);
CREATE INDEX group_invitation_pages ON group_invitations(conversation_id,created_at,id);
CREATE TABLE group_creation_keys (
  id uuid PRIMARY KEY, user_id uuid NOT NULL REFERENCES users(id), key varchar(128) NOT NULL,
  request_hash varchar(64) NOT NULL, conversation_id uuid REFERENCES conversations(id),
  UNIQUE(user_id,key)
);
-- Shared transactional per-user write budget; suitable for multiple backend instances.
CREATE TABLE group_write_budgets (user_id uuid PRIMARY KEY REFERENCES users(id), window_start timestamptz NOT NULL, operations integer NOT NULL);
