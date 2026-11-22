package com.nexa.chat.feature.groups.data;

import com.nexa.chat.feature.groups.model.GroupModels.*;
import com.nexa.chat.feature.groups.repository.GroupRepository;
import retrofit2.*;

/** Uses TV1's authenticated Retrofit client. No token storage or auth implementation here. */
public final class RemoteGroupRepository implements GroupRepository {
  private final GroupApi api;

  public RemoteGroupRepository(GroupApi api) {
    this.api = api;
  }

  private <T> void run(Call<T> call, Result<T> callback) {
    call.enqueue(
        new Callback<T>() {
          public void onResponse(Call<T> c, Response<T> r) {
            if (r.isSuccessful() && r.body() != null) callback.success(r.body());
            else callback.failure("HTTP_" + r.code());
          }

          public void onFailure(Call<T> c, Throwable t) {
            callback.failure("NETWORK_ERROR");
          }
        });
  }

  public void groups(int p, Result<Page<Summary>> c) {
    run(api.groups(p), c);
  }

  public void create(String k, Create r, Result<Created> c) {
    run(api.create(k, r), c);
  }

  public void details(String g, Result<Summary> c) {
    run(api.details(g), c);
  }

  public void members(String g, int p, Result<Page<Member>> c) {
    run(api.members(g, p), c);
  }

  public void outgoing(String g, int p, Result<Page<Invitation>> c) {
    run(api.outgoing(g, p), c);
  }

  public void invitations(int p, Result<Page<Invitation>> c) {
    run(api.invitations(p), c);
  }

  public void respond(String id, boolean accept, Result<Invitation> c) {
    run(accept ? api.accept(id) : api.reject(id), c);
  }
}
