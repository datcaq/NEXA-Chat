package com.nexa.chat.feature.groups.data;

import com.nexa.chat.feature.groups.model.GroupModels.*;
import retrofit2.Call;
import retrofit2.http.*;

public interface GroupApi {
  @GET("api/v1/groups")
  Call<Page<Summary>> groups(@Query("page") int page);

  @POST("api/v1/groups")
  Call<Created> create(@Header("Idempotency-Key") String key, @Body Create request);

  @GET("api/v1/groups/{id}")
  Call<Summary> details(@Path("id") String id);

  @GET("api/v1/groups/{id}/members")
  Call<Page<Member>> members(@Path("id") String id, @Query("page") int page);

  @GET("api/v1/groups/{id}/invitations")
  Call<Page<Invitation>> outgoing(@Path("id") String id, @Query("page") int page);

  @GET("api/v1/group-invitations")
  Call<Page<Invitation>> invitations(@Query("page") int page);

  @POST("api/v1/group-invitations/{id}/accept")
  Call<Invitation> accept(@Path("id") String id);

  @POST("api/v1/group-invitations/{id}/reject")
  Call<Invitation> reject(@Path("id") String id);
}
