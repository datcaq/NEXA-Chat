package com.nexa.chat.feature.messaging.model;
import java.util.Objects;
/** PROPOSED domain metadata, no JSON mapping. */
public final class Conversation {
 private final String id,title,avatarUrl,createdAt,updatedAt;
 private final ConversationType type;
 public Conversation(String id,ConversationType type){this(id,type,id,null,null,null);}
 public Conversation(String id,ConversationType type,String title,String avatarUrl,String createdAt,String updatedAt){
 this.id=Message.requireId(id);this.type=Objects.requireNonNull(type);this.title=Objects.requireNonNull(title);
 this.avatarUrl=avatarUrl;this.createdAt=createdAt;this.updatedAt=updatedAt;}
 public String getId(){return id;} public ConversationType getType(){return type;}
 public String getTitle(){return title;} public String getAvatarUrl(){return avatarUrl;}
 public String getCreatedAt(){return createdAt;} public String getUpdatedAt(){return updatedAt;}
}
