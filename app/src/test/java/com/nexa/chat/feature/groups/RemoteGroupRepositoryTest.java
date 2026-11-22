package com.nexa.chat.feature.groups;

import static org.junit.Assert.*;

import com.nexa.chat.feature.groups.data.*;
import com.nexa.chat.feature.groups.model.GroupModels.*;
import com.nexa.chat.feature.groups.repository.GroupRepository;
import java.util.*;
import java.util.concurrent.*;
import okhttp3.mockwebserver.*;
import org.junit.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RemoteGroupRepositoryTest {
  MockWebServer server;
  RemoteGroupRepository repository;

  @Before
  public void setup() throws Exception {
    server = new MockWebServer();
    server.start();
    repository =
        new RemoteGroupRepository(
            new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .callbackExecutor(Runnable::run)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GroupApi.class));
  }

  @After
  public void close() throws Exception {
    server.shutdown();
  }

  @Test
  public void sendsStableIdempotencyHeaderAndPayload() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(
                "{\"group\":{\"conversation\":{\"id\":\"g1\",\"type\":\"GROUP\",\"title\":\"Study\"},\"memberCount\":1},\"invitations\":[]}"));
    Result<Created> result = new Result<>();
    repository.create("same-key", new Create("Study", List.of("u1")), result);
    assertTrue(result.done.await(5, TimeUnit.SECONDS));
    assertEquals("g1", result.value.group.conversation.id);
    RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);
    assertEquals("/api/v1/groups", request.getPath());
    assertEquals("same-key", request.getHeader("Idempotency-Key"));
    assertTrue(request.getBody().readUtf8().contains("\"inviteeUserIds\":[\"u1\"]"));
  }

  @Test
  public void forbiddenIsFailureNotFakeSuccess() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(403));
    Result<Summary> result = new Result<>();
    repository.details("private", result);
    assertTrue(result.done.await(5, TimeUnit.SECONDS));
    assertNull(result.value);
    assertEquals("HTTP_403", result.error);
  }

  static class Result<T> implements GroupRepository.Result<T> {
    CountDownLatch done = new CountDownLatch(1);
    T value;
    String error;

    public void success(T v) {
      value = v;
      done.countDown();
    }

    public void failure(String e) {
      error = e;
      done.countDown();
    }
  }
}
