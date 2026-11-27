package com.nexa.chat.feature.messaging.repository;
import com.nexa.chat.feature.messaging.model.*;
import java.util.*;
import java.util.concurrent.*;
/** In-memory fake client AND server; scoped to one user, never production authentication. */
public final class FakeConversationRepository implements ConversationRepository {
 public enum Failure { NONE, UNAUTHENTICATED, FORBIDDEN, CONFLICT, OFFLINE, TIMEOUT }
 private final String user;
 private final Map<String,Conversation> conversations=new LinkedHashMap<>(), peers=new HashMap<>();
 private final Map<String,OutboxEntry> outbox=new LinkedHashMap<>();
 private final Map<String,List<ServerMessage>> history=new HashMap<>();
 private final Map<String,ServerMessage> persisted=new HashMap<>();
 private final Map<String,Long> read=new HashMap<>();
 private Failure failure=Failure.NONE; private boolean loseAck;
 public FakeConversationRepository(String user){if(user==null||user.trim().isEmpty())throw new IllegalArgumentException("user");this.user=user;}
 public synchronized void setFailure(Failure value){failure=Objects.requireNonNull(value);}
 public synchronized void loseNextAcknowledgement(){loseAck=true;}
 private <T> CompletionStage<T> error(ChatFailure.Code code){
  CompletableFuture<T> f=new CompletableFuture<>();f.completeExceptionally(new ChatFailure(code));return f;
 }
 private ChatFailure.Code failureCode(){return ChatFailure.Code.valueOf(failure.name());}
 public synchronized CompletionStage<List<Conversation>> listConversations(){
  if(failure!=Failure.NONE)return error(failureCode());
  return CompletableFuture.completedFuture(Collections.unmodifiableList(new ArrayList<>(conversations.values())));
 }
 public synchronized CompletionStage<Conversation> openDirect(String peer){
  if(failure!=Failure.NONE)return error(failureCode());
  if(peer==null||peer.trim().isEmpty()||peer.equals(user))return error(ChatFailure.Code.INVALID_ARGUMENT);
  Conversation c=peers.get(peer);
  if(c==null){String now=java.time.Instant.now().toString();
   c=new Conversation(UUID.randomUUID().toString(),ConversationType.DIRECT,peer,null,now,now);
   peers.put(peer,c);conversations.put(c.getId(),c);history.put(c.getId(),new ArrayList<>());read.put(c.getId(),0L);
  }return CompletableFuture.completedFuture(c);
 }
 public synchronized CompletionStage<List<ServerMessage>> listMessages(String id){return loadMessages(id,null,100).thenApply(MessagePage::getItems);}
 /** Compatibility helper for existing tests. UI uses the interface loadMessages. */
 public synchronized CompletionStage<List<ServerMessage>> page(String id,long before,int limit){
  return loadMessages(id,before==Long.MAX_VALUE?null:before,limit).thenApply(MessagePage::getItems);
 }
 public synchronized CompletionStage<MessagePage> loadMessages(String id,Long before,int limit){
  if(failure!=Failure.NONE)return error(failureCode());
  List<ServerMessage> rows=history.get(id);
  if(rows==null||limit<1||limit>100)return error(ChatFailure.Code.INVALID_ARGUMENT);
  if(before!=null&&(before<=0||rows.stream().noneMatch(m->m.getServerSequence()==before)))return error(ChatFailure.Code.INVALID_ARGUMENT);
  List<ServerMessage> eligible=new ArrayList<>();
  for(ServerMessage m:rows)if(before==null||m.getServerSequence()<before)eligible.add(m);
  int start=Math.max(0,eligible.size()-limit);
  List<ServerMessage> items=new ArrayList<>(eligible.subList(start,eligible.size()));
  Long next=start>0?items.get(0).getServerSequence():null;
  return CompletableFuture.completedFuture(new MessagePage(items,next));
 }
 public synchronized CompletionStage<List<OutboxEntry>> listOutbox(String id){
  if(!history.containsKey(id))return error(ChatFailure.Code.INVALID_ARGUMENT);
  List<OutboxEntry> rows=new ArrayList<>();
  for(OutboxEntry e:outbox.values())if(e.getMessage().getConversationId().equals(id))rows.add(e);
  return CompletableFuture.completedFuture(Collections.unmodifiableList(rows));
 }
 public synchronized CompletionStage<OutboxEntry> send(Message m){
  Objects.requireNonNull(m);
  if(!user.equals(m.getSenderId())||!history.containsKey(m.getConversationId()))return error(ChatFailure.Code.FORBIDDEN);
  OutboxEntry e=outbox.get(m.getClientMessageId());
  if(e!=null&&!e.getMessage().samePayload(m))return error(ChatFailure.Code.CONFLICT);
  if(failure==Failure.UNAUTHENTICATED||failure==Failure.FORBIDDEN||failure==Failure.CONFLICT)return error(failureCode());
  if(e!=null&&e.getState()==OutboxState.SENT)return CompletableFuture.completedFuture(e);
  if(e==null)e=OutboxEntry.draft(m).transitionTo(OutboxState.PENDING);
  if(e.getState()==OutboxState.FAILED)e=e.retry();
  outbox.put(m.getClientMessageId(),e);
  if(failure==Failure.OFFLINE)return CompletableFuture.completedFuture(e);
  e=e.transitionTo(OutboxState.SENDING);
  if(failure==Failure.TIMEOUT){outbox.put(m.getClientMessageId(),e.onTimeout());return error(ChatFailure.Code.TIMEOUT);}
  ServerMessage server=persisted.get(m.getClientMessageId());
  if(server==null){
   List<ServerMessage> rows=history.get(m.getConversationId());
   server=new ServerMessage(m,new ServerAcknowledgement(m.getClientMessageId(),UUID.randomUUID().toString(),rows.size()+1L),MessageStatus.NONE);
   rows.add(server);persisted.put(m.getClientMessageId(),server);
  }
  if(loseAck){loseAck=false;outbox.put(m.getClientMessageId(),e.onTimeout());return error(ChatFailure.Code.TIMEOUT);}
  e=e.acknowledge(server.getAcknowledgement());outbox.put(m.getClientMessageId(),e);
  return CompletableFuture.completedFuture(e);
 }
 /** Deliver only ACK evidence actually held by the fake server. Safe before/after retry. */
 public synchronized OutboxEntry deliverLateAcknowledgement(String clientId){
  ServerMessage server=persisted.get(clientId);OutboxEntry e=outbox.get(clientId);
  if(server==null||e==null)throw new ChatFailure(ChatFailure.Code.INVALID_ARGUMENT);
  e=e.acknowledge(server.getAcknowledgement());outbox.put(clientId,e);return e;
 }
 public synchronized CompletionStage<Void> markRead(String id,ServerMessage through){
  if(failure!=Failure.NONE)return error(failureCode());
  List<ServerMessage> rows=history.get(id);
  if(rows==null||through==null)return error(ChatFailure.Code.INVALID_ARGUMENT);
  boolean found=rows.stream().anyMatch(m->m.getServerSequence()==through.getServerSequence()
    &&m.getAcknowledgement().getServerMessageId().equals(through.getAcknowledgement().getServerMessageId())
    &&m.getMessage().samePayload(through.getMessage()));
  if(!found)return error(ChatFailure.Code.INVALID_ARGUMENT);
  read.put(id,Math.max(read.get(id),through.getServerSequence()));return CompletableFuture.completedFuture(null);
 }
 public synchronized long getReadSequence(String id){if(!read.containsKey(id))throw new ChatFailure(ChatFailure.Code.INVALID_ARGUMENT);return read.get(id);}
}
