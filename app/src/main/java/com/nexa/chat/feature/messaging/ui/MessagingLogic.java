package com.nexa.chat.feature.messaging.ui;
import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import java.util.*;
import java.util.concurrent.*;
/** TV2 UI-only logic. No wire or repository contract changes. */
public final class MessagingLogic {
 private final Map<String,String> drafts;
 private final Map<String,Long> revisions=new HashMap<>();
 private final Set<String> inFlight=new HashSet<>();
 private final Map<String,Boolean> retry=new HashMap<>();
 public MessagingLogic(Map<String,String> drafts){this.drafts=drafts;}
 public synchronized void edit(String id,String text){drafts.put(id,text);revisions.put(id,revision(id)+1);}
 public synchronized String draft(String id){return drafts.getOrDefault(id,"");}
 public synchronized long revision(String id){return revisions.getOrDefault(id,0L);}
 private String key(Message m){return m.getConversationId()+"/"+m.getSenderId()+"/"+m.getClientMessageId();}
 public synchronized boolean begin(Message m,boolean isRetry){
  String k=key(m);if(inFlight.contains(k)||(isRetry&&!Boolean.TRUE.equals(retry.get(k))))return false;
  inFlight.add(k);return true;
 }
 public synchronized boolean busy(Message m){return inFlight.contains(key(m));}
 public synchronized boolean canRetry(Message m){return !busy(m)&&Boolean.TRUE.equals(retry.get(key(m)));}
 public synchronized void finish(Message m,long revision,OutboxEntry entry,Throwable failure){
  String k=key(m);inFlight.remove(k);
  Throwable cause=failure;while(cause!=null&&cause.getCause()!=null)cause=cause.getCause();
  boolean eligible=cause instanceof ChatFailure&&((ChatFailure)cause).isRetryable();
  if(failure==null&&entry!=null&&entry.getState()==OutboxState.PENDING)eligible=true;
  retry.put(k,eligible);
  // Keep rejected draft untouched. Clear only accepted snapshot without subsequent edits.
  if(failure==null&&entry!=null&&revision(m.getConversationId())==revision
    &&draft(m.getConversationId()).equals(m.getText()))edit(m.getConversationId(),"");
 }
 /** Controller invalidation only; callers must hold their account gate. */
 synchronized void clear(){drafts.clear();revisions.clear();inFlight.clear();retry.clear();}
 public static CompletionStage<List<OutboxEntry>> load(ConversationRepository repository,String id){
  return repository.listMessages(id).thenCombine(repository.listOutbox(id),(history,outbox)->merge(id,history,outbox));
 }
 public static List<OutboxEntry> merge(String id,List<ServerMessage> history,List<OutboxEntry> outbox){
  Map<String,OutboxEntry> rows=new LinkedHashMap<>();
  for(OutboxEntry e:outbox){Message m=e.getMessage();if(!id.equals(m.getConversationId()))throw new IllegalArgumentException("Conversation mismatch");
   rows.put(m.getSenderId()+"/"+m.getClientMessageId(),e);}
  Set<String> serverIds=new HashSet<>();
  List<ServerMessage> sorted=new ArrayList<>(history);sorted.sort(Comparator.comparingLong(ServerMessage::getServerSequence));
  List<OutboxEntry> result=new ArrayList<>();
  for(ServerMessage s:sorted){Message m=s.getMessage();if(!id.equals(m.getConversationId()))throw new IllegalArgumentException("Conversation mismatch");
   String key=m.getSenderId()+"/"+m.getClientMessageId();
   OutboxEntry old=rows.remove(key);
   if(old!=null&&!old.getMessage().samePayload(m))throw new IllegalArgumentException("Conflicting logical identity");
   if(!serverIds.add(s.getAcknowledgement().getServerMessageId()))continue;
   OutboxEntry confirmed=OutboxEntry.draft(m).transitionTo(OutboxState.PENDING).acknowledge(s.getAcknowledgement());
   result.add(confirmed);
  }
  result.addAll(rows.values());return Collections.unmodifiableList(result);
 }
}
