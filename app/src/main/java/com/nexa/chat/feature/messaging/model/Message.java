package com.nexa.chat.feature.messaging.model;
import java.util.Objects;
/** Immutable PROPOSED domain value; no JSON mapping. Server identity composes via ServerMessage. */
public final class Message {
 private final String clientMessageId, conversationId, senderId, content, attachmentId, replyToId;
 private final String createdAt, editedAt, revokedAt;
 private final MessageType type;
 public Message(String client, String conversation, String sender, MessageType type, String text) {
  this(client, conversation, sender, type, text, null, null, null, null, null);
 }
 public Message(String client, String conversation, String sender, MessageType type, String text,
                String attachment, String reply, String created, String edited, String revoked) {
  clientMessageId=requireId(client); conversationId=requireId(conversation); senderId=requireId(sender);
  this.type=Objects.requireNonNull(type); content=Objects.requireNonNull(text);
  attachmentId=attachment==null?null:requireId(attachment); replyToId=reply==null?null:requireId(reply);
  createdAt=created; editedAt=edited; revokedAt=revoked;
 }
 static String requireId(String v) { if(v==null||v.trim().isEmpty()) throw new IllegalArgumentException("Blank identifier"); return v; }
 public String getClientMessageId(){return clientMessageId;}
 public String getConversationId(){return conversationId;}
 public String getSenderId(){return senderId;}
 public MessageType getType(){return type;}
 public String getContent(){return content;}
 public String getText(){return content;}
 public String getAttachmentId(){return attachmentId;}
 public String getReplyToId(){return replyToId;}
 public String getCreatedAt(){return createdAt;}
 public String getEditedAt(){return editedAt;}
 public String getRevokedAt(){return revokedAt;}
 public boolean samePayload(Message m) {
  return clientMessageId.equals(m.clientMessageId)&&conversationId.equals(m.conversationId)
   &&senderId.equals(m.senderId)&&type==m.type&&content.equals(m.content)
   &&Objects.equals(attachmentId,m.attachmentId)&&Objects.equals(replyToId,m.replyToId)
   &&Objects.equals(createdAt,m.createdAt)&&Objects.equals(editedAt,m.editedAt)&&Objects.equals(revokedAt,m.revokedAt);
 }
}
