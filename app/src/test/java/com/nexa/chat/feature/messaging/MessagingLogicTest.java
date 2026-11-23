package com.nexa.chat.feature.messaging;
import com.nexa.chat.feature.messaging.ui.MessagingLogic;
import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;
public class MessagingLogicTest {
 @Test public void callbacksInEitherOrderWaitForBoth(){
  for(boolean historyFirst:new boolean[]{true,false}){
   java.util.concurrent.CompletableFuture<List<ServerMessage>> history=new java.util.concurrent.CompletableFuture<>();
   java.util.concurrent.CompletableFuture<List<OutboxEntry>> outbox=new java.util.concurrent.CompletableFuture<>();
   ConversationRepository repository=(ConversationRepository)java.lang.reflect.Proxy.newProxyInstance(
    ConversationRepository.class.getClassLoader(),new Class<?>[]{ConversationRepository.class},
    (proxy,method,args)->method.getName().equals("listMessages")?history:outbox);
   java.util.concurrent.CompletableFuture<List<OutboxEntry>> result=MessagingLogic.load(repository,"c").toCompletableFuture();
   Message m=message();ServerMessage server=new ServerMessage(m,new ServerAcknowledgement("i","s",1),MessageStatus.NONE);
   if(historyFirst)history.complete(Collections.singletonList(server));
   else outbox.complete(Collections.singletonList(OutboxEntry.draft(m).transitionTo(OutboxState.PENDING)));
   assertFalse(result.isDone());
   if(historyFirst)outbox.complete(Collections.singletonList(OutboxEntry.draft(m).transitionTo(OutboxState.PENDING)));
   else history.complete(Collections.singletonList(server));
   assertEquals(1,result.join().size());assertEquals(OutboxState.SENT,result.join().get(0).getState());
  }
 }
 private Message message(){return new Message("i","c","u",MessageType.TEXT,"hello");}
 @Test public void lateAckMergeHasOneRow(){
  Message m=message();OutboxEntry pending=OutboxEntry.draft(m).transitionTo(OutboxState.PENDING);
  ServerMessage server=new ServerMessage(m,new ServerAcknowledgement("i","s",1),MessageStatus.NONE);
  assertEquals(1,MessagingLogic.merge("c",Arrays.asList(server),Arrays.asList(pending)).size());
  assertEquals(OutboxState.SENT,MessagingLogic.merge("c",Arrays.asList(server),Arrays.asList(pending)).get(0).getState());
 }
 @Test public void rejectedDraftRemains(){
  MessagingLogic l=new MessagingLogic(new HashMap<>());l.edit("c","hello");long rev=l.revision("c");
  l.begin(message(),false);l.finish(message(),rev,null,new ChatFailure(ChatFailure.Code.FORBIDDEN));
  assertEquals("hello",l.draft("c"));assertFalse(l.canRetry(message()));
 }
 @Test public void newDraftSurvivesOldSuccess(){
  MessagingLogic l=new MessagingLogic(new HashMap<>());l.edit("c","hello");long rev=l.revision("c");
  l.begin(message(),false);l.edit("c","new");l.finish(message(),rev,OutboxEntry.draft(message()).transitionTo(OutboxState.PENDING),null);
  assertEquals("new",l.draft("c"));
 }
 @Test public void acceptedSnapshotClears(){
  MessagingLogic l=new MessagingLogic(new HashMap<>());l.edit("c","hello");long rev=l.revision("c");
  l.begin(message(),false);l.finish(message(),rev,OutboxEntry.draft(message()).transitionTo(OutboxState.PENDING),null);
  assertEquals("",l.draft("c"));
 }
 @Test public void retryGateBlocksDuplicateAndTerminalFailures(){
  for(ChatFailure.Code code:ChatFailure.Code.values()){
   MessagingLogic l=new MessagingLogic(new HashMap<>());Message m=message();
   assertFalse(l.begin(m,true));assertTrue(l.begin(m,false));assertFalse(l.begin(m,false));
   l.finish(m,0,null,new ChatFailure(code));
   assertEquals(code==ChatFailure.Code.OFFLINE||code==ChatFailure.Code.TIMEOUT,l.begin(m,true));
  }
 }
 @Test public void unchangedTextWithNewRevisionIsNotCleared(){
  MessagingLogic l=new MessagingLogic(new HashMap<>());l.edit("c","hello");long rev=l.revision("c");
  l.edit("c","");l.edit("c","hello");l.finish(message(),rev,OutboxEntry.draft(message()),null);assertEquals("hello",l.draft("c"));
 }
}
