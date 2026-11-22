package com.nexa.chat.feature.groups.repository;

import com.nexa.chat.feature.groups.model.GroupModels.*;

public interface GroupRepository {
  interface Result<T> {
    void success(T value);

    void failure(String code);
  }

  void groups(int page, Result<Page<Summary>> callback);

  void create(String key, Create request, Result<Created> callback);

  void details(String group, Result<Summary> callback);

  void members(String group, int page, Result<Page<Member>> callback);

  void outgoing(String group, int page, Result<Page<Invitation>> callback);

  void invitations(int page, Result<Page<Invitation>> callback);

  void respond(String invitation, boolean accept, Result<Invitation> callback);
}
