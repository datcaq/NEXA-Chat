package com.nexa.chat.feature.messaging;
import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import java.util.concurrent.*;
public class MessagingContractTest {
 private final FakeConversationRepository r=new FakeConversationRepository("u");
 private String conversation(){return r.openDirect("p").toCompletableFuture().join().getId();}
 private Message message(String c,String id){return new Message(id,c,"u",MessageType.TEXT,id);}
 private void send(String c,int n){for(int i=1;i<=n;i++)r.send(message(c,""+i)).toCompletableFuture().join();}
 private ChatFailure failure(CompletionStage<?> stage){
  try{stage.toCompletableFuture().join();throw new AssertionError("Expected failure");}
  catch(CompletionException e){return (ChatFailure)e.getCause();}
 }
 @Test public void allMessageTypes(){assertArrayEquals(new String[]{"TEXT","IMAGE","VIDEO","FILE","VOICE"},Arrays.stream(MessageType.values()).map(Enum::name).toArray(String[]::new));}
 @Test public void metadataRoundTrip(){
  Conversation c=new Conversation("c",ConversationType.DIRECT,"title","url","created","updated");
  assertEquals("title",c.getTitle());assertEquals("url",c.getAvatarUrl());assertEquals("created",c.getCreatedAt());assertEquals("updated",c.getUpdatedAt());
  Message m=new Message("id","c","u",MessageType.IMAGE,"text","attachment","reply","created","edited","revoked");
  assertEquals("text",m.getText());assertEquals("attachment",m.getAttachmentId());assertEquals("reply",m.getReplyToId());
  assertEquals("created",m.getCreatedAt());assertEquals("edited",m.getEditedAt());assertEquals("revoked",m.getRevokedAt());
 }
 @Test public void requiredFieldsRejectInvalidValues(){
  assertThrows(IllegalArgumentException.class,()->new Message(" ","c","u",MessageType.TEXT,"x"));
  assertThrows(NullPointerException.class,()->new Message("i","c","u",null,"x"));
  assertThrows(NullPointerException.class,()->new Conversation("c",ConversationType.DIRECT,null,null,null,null));
 }
 @Test public void lostAckRetryAndLateAckDoNotDuplicate(){
  String c=conversation();Message m=message(c,"1");r.loseNextAcknowledgement();
  assertEquals(ChatFailure.Code.TIMEOUT,failure(r.send(m)).getCode());
  assertEquals(1,r.listMessages(c).toCompletableFuture().join().size());
  OutboxEntry retry=r.send(m).toCompletableFuture().join();
  assertEquals(OutboxState.SENT,retry.getState());
  assertSame(retry,r.deliverLateAcknowledgement("1"));
  assertEquals(1,r.listMessages(c).toCompletableFuture().join().size());
 }
 @Test public void lateAckBeforeRetryIsSafe(){
  String c=conversation();Message m=message(c,"1");r.loseNextAcknowledgement();failure(r.send(m));
  OutboxEntry ack=r.deliverLateAcknowledgement("1");
  assertSame(ack,r.send(m).toCompletableFuture().join());
 }
 @Test public void terminalFailuresAreDistinct(){
  String c=conversation();
  for(FakeConversationRepository.Failure f:new FakeConversationRepository.Failure[]{FakeConversationRepository.Failure.UNAUTHENTICATED,FakeConversationRepository.Failure.FORBIDDEN,FakeConversationRepository.Failure.CONFLICT}){
   r.setFailure(f);ChatFailure e=failure(r.send(message(c,"1")));assertEquals(f.name(),e.getCode().name());assertFalse(e.isRetryable());
  }
 }
 @Test public void timeoutCanRetry(){
  String c=conversation();r.setFailure(FakeConversationRepository.Failure.TIMEOUT);
  assertTrue(failure(r.send(message(c,"1"))).isRetryable());
  assertEquals(OutboxState.FAILED,r.listOutbox(c).toCompletableFuture().join().get(0).getState());
  r.setFailure(FakeConversationRepository.Failure.NONE);r.send(message(c,"1")).toCompletableFuture().join();
  assertEquals(1,r.listMessages(c).toCompletableFuture().join().size());
 }
 @Test public void offlinePreservesPending(){
  String c=conversation();r.setFailure(FakeConversationRepository.Failure.OFFLINE);
  assertEquals(OutboxState.PENDING,r.send(message(c,"1")).toCompletableFuture().join().getState());
  assertEquals(ChatFailure.Code.OFFLINE,failure(r.listConversations()).getCode());
 }
 @Test public void readNeverDecreases(){
  String c=conversation();send(c,3);List<ServerMessage> rows=r.listMessages(c).toCompletableFuture().join();
  r.markRead(c,rows.get(2)).toCompletableFuture().join();r.markRead(c,rows.get(0)).toCompletableFuture().join();
  assertEquals(3,r.getReadSequence(c));
 }
 @Test public void paginationAcrossNewInsert(){
  String c=conversation();send(c,5);
  MessagePage first=r.loadMessages(c,null,2).toCompletableFuture().join();assertEquals(4,first.getItems().get(0).getServerSequence());
  r.send(message(c,"6")).toCompletableFuture().join();
  MessagePage second=r.loadMessages(c,first.getNextCursor(),2).toCompletableFuture().join();
  MessagePage last=r.loadMessages(c,second.getNextCursor(),2).toCompletableFuture().join();
  assertEquals(2,second.getItems().get(0).getServerSequence());assertEquals(1,last.getItems().size());assertNull(last.getNextCursor());
  Set<Long> ids=new HashSet<>();for(MessagePage p:Arrays.asList(first,second,last))for(ServerMessage m:p.getItems())assertTrue(ids.add(m.getServerSequence()));
  assertEquals(5,ids.size());
 }
 @Test public void invalidPagination(){
  String c=conversation();send(c,1);
  for(long cursor:new long[]{0,-1,99})assertEquals(ChatFailure.Code.INVALID_ARGUMENT,failure(r.loadMessages(c,cursor,1)).getCode());
  for(int limit:new int[]{0,-1,101})assertEquals(ChatFailure.Code.INVALID_ARGUMENT,failure(r.loadMessages(c,null,limit)).getCode());
 }
 @Test public void emptyPageAndImmutableItems(){
  String c=conversation();MessagePage page=r.loadMessages(c,null,2).toCompletableFuture().join();
  assertTrue(page.getItems().isEmpty());assertNull(page.getNextCursor());
  assertThrows(UnsupportedOperationException.class,()->page.getItems().add(null));
 }
 @Test public void replayChecksAttachments(){
  String c=conversation();r.send(message(c,"1")).toCompletableFuture().join();
  assertEquals(ChatFailure.Code.CONFLICT,failure(r.send(new Message("1",c,"u",MessageType.TEXT,"1","a",null,null,null,null))).getCode());
 }
}
