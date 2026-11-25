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
  GroupsAccountScope scope;

  @Before
  public void setup() throws Exception {
    scope =
        new GroupsAccountScope(
            UUID.randomUUID(),
            UUID.randomUUID(),
            () -> GroupsAccountScope.Access.ACTIVE,
            Runnable::run);
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
    repository.bindScope(scope);
  }

  @After
  public void close() throws Exception {
    repository.close();
    scope.close();
    assertTrue(scope.awaitCleanup(5, TimeUnit.SECONDS));
    server.shutdown();
    assertTrue(repository.awaitTermination(5, TimeUnit.SECONDS));
  }

  @Test
  public void productionConstructorCannotDispatchWithoutBoundAccountScope() {
    PendingCall<Summary> call = new PendingCall<>();
    GroupApi api =
        (GroupApi)
            java.lang.reflect.Proxy.newProxyInstance(
                GroupApi.class.getClassLoader(), new Class[] {GroupApi.class}, (p, m, a) -> call);
    RemoteGroupRepository unbound = new RemoteGroupRepository(api);
    assertThrows(RejectedExecutionException.class, () -> unbound.details("group", new Result<>()));
    assertNull(call.callback);
    assertEquals(0, unbound.inFlight());
    unbound.close();
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

  @Test
  public void responseAlreadyQueuedOnUiExecutorIsSuppressedAfterClose() throws Exception {
    BlockingQueue<Runnable> delivery = new LinkedBlockingQueue<>();
    repository =
        new RemoteGroupRepository(
            new Retrofit.Builder()
                .baseUrl(server.url("/"))
                .callbackExecutor(delivery::add)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GroupApi.class));
    repository.bindScope(scope);
    server.enqueue(new MockResponse().setBody("{}"));
    Result<Summary> result = new Result<>();
    repository.details("group", result);
    Runnable queued = delivery.poll(5, TimeUnit.SECONDS);
    assertNotNull(queued);
    repository.close();
    queued.run();
    assertEquals(1, result.done.getCount());
    assertNull(result.value);
    assertNull(result.error);
  }

  @Test
  public void closeCancelsInflightRequestAndRejectsItsLateCallback() throws Exception {
    PendingCall<Summary> call = new PendingCall<>();
    GroupApi api =
        (GroupApi)
            java.lang.reflect.Proxy.newProxyInstance(
                GroupApi.class.getClassLoader(),
                new Class[] {GroupApi.class},
                (proxy, method, args) -> call);
    repository = new RemoteGroupRepository(api);
    repository.bindScope(scope);
    Result<Summary> result = new Result<>();
    repository.details("group", result);
    assertTrue(call.enqueued.await(5, TimeUnit.SECONDS));
    assertNotNull(call.callback);
    repository.close();
    repository.close();
    assertTrue(call.cancelSignal.await(5, TimeUnit.SECONDS));
    assertTrue(call.cancelled);
    call.callback.onResponse(call, retrofit2.Response.success(new Summary()));
    call.callback.onFailure(call, new java.io.IOException("cancelled"));
    assertEquals(1, result.done.getCount());
  }

  @Test
  public void closedRepositoryDoesNotEnqueueNewRequests() {
    PendingCall<Summary> call = new PendingCall<>();
    GroupApi api =
        (GroupApi)
            java.lang.reflect.Proxy.newProxyInstance(
                GroupApi.class.getClassLoader(),
                new Class[] {GroupApi.class},
                (proxy, method, args) -> call);
    repository = new RemoteGroupRepository(api);
    repository.bindScope(scope);
    repository.close();
    assertThrows(
        RejectedExecutionException.class, () -> repository.details("group", new Result<>()));
    assertFalse(call.cancelled);
    assertNull(call.callback);
  }

  static class PendingCall<T> implements retrofit2.Call<T> {
    volatile boolean cancelled;
    final CountDownLatch enqueued = new CountDownLatch(1), cancelSignal = new CountDownLatch(1);
    retrofit2.Callback<T> callback;

    public retrofit2.Response<T> execute() {
      throw new UnsupportedOperationException();
    }

    public void enqueue(retrofit2.Callback<T> callback) {
      this.callback = callback;
      enqueued.countDown();
    }

    public boolean isExecuted() {
      return callback != null;
    }

    public void cancel() {
      cancelled = true;
      cancelSignal.countDown();
    }

    public boolean isCanceled() {
      return cancelled;
    }

    public retrofit2.Call<T> clone() {
      return new PendingCall<>();
    }

    public okhttp3.Request request() {
      return new okhttp3.Request.Builder().url("https://example.invalid/").build();
    }

    public okio.Timeout timeout() {
      return okio.Timeout.NONE;
    }
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
