package com.nexa.chat.feature.messaging.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import com.nexa.chat.feature.messaging.ChatNavigator;
import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * TV2 entry point: host mounts this View and supplies one repository per session.
 * No Activity/Manifest dependency. Call refresh after mounting; retain draft map in host
 * for configuration changes. This View never changes global navigation.
 */
public final class MessagingView extends LinearLayout {
 private final ConversationRepository repository;
 private final String user;
 private final ChatNavigator navigator;
 private final Map<String,String> drafts;
 private String conversation, title;
 private int generation;
 private final MessagingLogic logic;
 private final Set<String> sendingConversations=new HashSet<>();
 public MessagingView(Context context,ConversationRepository repository,String user,
                      ChatNavigator navigator,Map<String,String> drafts){
  super(context);setOrientation(VERTICAL);
  this.repository=Objects.requireNonNull(repository);this.user=Objects.requireNonNull(user);
  this.navigator=Objects.requireNonNull(navigator);this.drafts=Objects.requireNonNull(drafts);this.logic=new MessagingLogic(drafts);
 }
 public void refresh(){if(conversation==null)showList();else showConversation(conversation,title);}
 private void button(LinearLayout parent,String text,Runnable click){
  Button b=new Button(getContext());b.setText(text);b.setOnClickListener(v->click.run());parent.addView(b);
 }
 private void label(LinearLayout parent,String text){TextView v=new TextView(getContext());v.setText(text);parent.addView(v);}
 private LinearLayout list(){
  ScrollView scroll=new ScrollView(getContext());LinearLayout rows=new LinearLayout(getContext());
  rows.setOrientation(VERTICAL);scroll.addView(rows);addView(scroll,new LayoutParams(LayoutParams.MATCH_PARENT,0,1));return rows;
 }
 private void deliver(int version,Runnable action){post(()->{if(version==generation&&isAttachedToWindow())action.run();});}
 private String error(Throwable e){while(e.getCause()!=null)e=e.getCause();return e instanceof ChatFailure?((ChatFailure)e).getCode().name():"Không tải được dữ liệu";}
 public void showList(){
  conversation=null;int version=++generation;removeAllViews();label(this,"Tin nhắn · FAKE DEMO");
  EditText peer=new EditText(getContext());peer.setHint("Peer user ID");addView(peer);
  button(this,"Mở chat 1-1",()->openDirect(peer.getText().toString().trim()));
  LinearLayout rows=list();label(rows,"Đang tải…");
  repository.listConversations().whenComplete((items,e)->deliver(version,()->{
   rows.removeAllViews();
   if(e!=null){label(rows,error(e));button(rows,"Thử lại",this::showList);return;}
   if(items.isEmpty())label(rows,"Chưa có hội thoại");
   for(Conversation c:items)button(rows,c.getTitle(),()->{
    navigator.openConversation(c.getId());showConversation(c.getId(),c.getTitle());
   });
  }));
 }
 public void openDirect(String peer){
  if(peer.isEmpty())return;
  int version=++generation;removeAllViews();label(this,"Đang mở hội thoại…");
  repository.openDirect(peer).whenComplete((c,e)->deliver(version,()->{
   if(e!=null){label(this,error(e));button(this,"Quay lại",this::showList);}
   else{navigator.openConversation(c.getId());showConversation(c.getId(),c.getTitle());}
  }));
 }
 public void showConversation(String id,String name){
  conversation=id;title=name;int version=++generation;removeAllViews();
  button(this,"‹ Danh sách",()->{navigator.back();showList();});label(this,name);
  LinearLayout rows=list();label(rows,"Đang tải…");
  EditText input=new EditText(getContext());input.setHint("Nhập tin nhắn");input.setText(drafts.getOrDefault(id,""));addView(input);
  input.addTextChangedListener(new TextWatcher(){
   public void beforeTextChanged(CharSequence s,int start,int count,int after){}
   public void onTextChanged(CharSequence s,int start,int before,int count){logic.edit(id,s.toString());}
   public void afterTextChanged(Editable s){}
  });
  button(this,"Gửi",()->{
   String text=input.getText().toString();if(text.trim().isEmpty()||sendingConversations.contains(id))return;
   Message m=new Message(UUID.randomUUID().toString(),id,user,MessageType.TEXT,text);
   send(m,false);
  });
  MessagingLogic.load(repository,id).whenComplete((entries,e)->deliver(version,()->{
   rows.removeAllViews();
   if(e!=null){label(rows,error(e));button(rows,"Tải lại",this::refresh);return;}
   for(OutboxEntry entry:entries){
    Message m=entry.getMessage();label(rows,m.getText()+" · "+entry.getState());
    if((entry.getState()==OutboxState.PENDING||entry.getState()==OutboxState.FAILED)&&logic.canRetry(m))
     button(rows,"Thử gửi lại",()->send(m,true));
   }
  }));
 }
 private void send(Message m,boolean retry){
  if(!logic.begin(m,retry))return;
  sendingConversations.add(m.getConversationId());
  int version=generation;long revision=logic.revision(m.getConversationId());
  CompletionStage<OutboxEntry> request;
  try{request=repository.send(m);}catch(RuntimeException e){
   CompletableFuture<OutboxEntry> failed=new CompletableFuture<>();failed.completeExceptionally(e);request=failed;
  }
  request.whenComplete((entry,e)->{
   logic.finish(m,revision,entry,e);
   post(()->{
    sendingConversations.remove(m.getConversationId());
    if(version!=generation||!isAttachedToWindow())return;
    showConversation(m.getConversationId(),title);
    if(e!=null)label(this,error(e));
   });
  });
 }
 @Override protected void onDetachedFromWindow(){generation++;super.onDetachedFromWindow();}
}
