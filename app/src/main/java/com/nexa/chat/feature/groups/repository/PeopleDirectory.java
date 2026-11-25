package com.nexa.chat.feature.groups.repository;

import com.nexa.chat.feature.groups.model.GroupModels.*;

/** Adapter supplied by TV1. Return only public identifiers and policy-filtered users. */
public interface PeopleDirectory extends AutoCloseable {
  /** Close this Groups adapter's requests only, not the shared Contacts module. */
  @Override
  void close();

  String currentUserId();

  void friends(int page, GroupRepository.Result<Page<Person>> callback);

  /** TV1 handles country selection / E.164 normalization and search privacy. */
  void searchPhone(String phone, int page, GroupRepository.Result<Page<Person>> callback);
}
