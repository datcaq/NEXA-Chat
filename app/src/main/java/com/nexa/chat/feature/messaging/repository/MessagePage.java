package com.nexa.chat.feature.messaging.repository;
import com.nexa.chat.feature.messaging.model.ServerMessage;
import java.util.*;
/** PROPOSED local page; null nextCursor means end. */
public final class MessagePage {
 private final List<ServerMessage> items;private final Long nextCursor;
 public MessagePage(List<ServerMessage> items,Long nextCursor){this.items=Collections.unmodifiableList(new ArrayList<>(items));this.nextCursor=nextCursor;}
 public List<ServerMessage> getItems(){return items;}public Long getNextCursor(){return nextCursor;}
}
