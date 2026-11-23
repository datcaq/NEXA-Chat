package com.nexa.chat.feature.messaging.ui;
import android.content.Context;
import android.widget.*;
import com.nexa.chat.feature.messaging.ChatNavigator;
import com.nexa.chat.feature.messaging.model.*;
import com.nexa.chat.feature.messaging.repository.*;
import java.util.*;
/** Unattached demo View. Host supplies lifecycle, labels/resources and navigator. */
public final class ChatPanel extends LinearLayout {
 public ChatPanel(Context context){super(context);setOrientation(VERTICAL);}
 public void showLoading(){removeAllViews();addView(new ProgressBar(getContext()));}
 public void showError(String label,Runnable retry){removeAllViews();Button b=new Button(getContext());b.setText(label);b.setOnClickListener(v->retry.run());addView(b);}
 public void showConversations(List<Conversation> rows,String empty,ChatNavigator navigator){
  removeAllViews();LinearLayout list=scroll();
  if(rows.isEmpty())label(list,empty);
  for(Conversation c:rows){Button b=new Button(getContext());b.setText(c.getTitle());b.setOnClickListener(v->navigator.openConversation(c.getId()));list.addView(b);}
 }
 /** No dependency on FakeConversationRepository. Detached results are ignored. */
 public void loadConversations(ConversationRepository repository,String empty,String error,ChatNavigator navigator){
  showLoading();repository.listConversations().whenComplete((rows,failure)->post(()->{
   if(!isAttachedToWindow())return;
   if(failure!=null)showError(error,()->loadConversations(repository,empty,error,navigator));
   else showConversations(rows,empty,navigator);
  }));
 }
 public void showChat(String title,List<ServerMessage> messages,List<OutboxEntry> outbox,
                      String draft,String sendLabel,java.util.function.Consumer<String> send){
  removeAllViews();label(this,title);LinearLayout list=scroll();
  Set<String> confirmed=new HashSet<>();
  for(ServerMessage m:messages){confirmed.add(m.getMessage().getClientMessageId());label(list,m.getMessage().getText());}
  for(OutboxEntry e:outbox)if(!confirmed.contains(e.getMessage().getClientMessageId()))label(list,e.getMessage().getText()+" ["+e.getState()+"]");
  EditText input=new EditText(getContext());input.setText(draft);addView(input);
  Button button=new Button(getContext());button.setText(sendLabel);button.setOnClickListener(v->{String text=input.getText().toString();if(!text.trim().isEmpty())send.accept(text);});addView(button);
 }
 private LinearLayout scroll(){ScrollView s=new ScrollView(getContext());LinearLayout list=new LinearLayout(getContext());list.setOrientation(VERTICAL);s.addView(list);addView(s,new LayoutParams(LayoutParams.MATCH_PARENT,0,1));return list;}
 private void label(LinearLayout parent,String text){TextView v=new TextView(getContext());v.setText(text);parent.addView(v);}
}
