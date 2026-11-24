package com.nexa.chat.feature.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public final class AuthRepositoryContractTest {
    private static final String PHONE = "+12025550123";
    private static final String OTHER_PHONE = "+12025550124";
    private static final RegistrationChallenge CHALLENGE = new RegistrationChallenge("challenge-a");
    private static final RegistrationChallenge OTHER_CHALLENGE = new RegistrationChallenge("challenge-b");

    private final FakeAuthRepository repository = new FakeAuthRepository();
    private final RecordingCallback callback = new RecordingCallback();

    @Test
    public void successInvokesOnlyTheChallengeCallbackOnce() {
        FakeAuthRepository.PendingRequest request = repository.startRegistrationChallenge(PHONE, callback);

        assertTrue(request.succeed(CHALLENGE));
        assertFalse(request.succeed(OTHER_CHALLENGE));
        assertFalse(request.fail(AuthFailure.REJECTED));
        assertEquals(1, callback.calls);
        assertSame(CHALLENGE, callback.challenge);
        assertNull(callback.failure);
    }

    @Test
    public void failureInvokesOnlyTheFailureCallbackOnce() {
        for (AuthFailure failure : AuthFailure.values()) {
            RecordingCallback result = new RecordingCallback();
            FakeAuthRepository.PendingRequest request = repository.startRegistrationChallenge(PHONE, result);

            assertTrue(request.fail(failure));
            assertFalse(request.fail(AuthFailure.UNAVAILABLE));
            assertFalse(request.succeed(CHALLENGE));
            assertEquals(1, result.calls);
            assertSame(failure, result.failure);
            assertNull(result.challenge);
        }
    }

    @Test
    public void cancellingBeforeAdmissionSuppressesSuccessAndFailure() {
        FakeAuthRepository.PendingRequest request = repository.startRegistrationChallenge(PHONE, callback);
        request.cancel();

        assertFalse(request.succeed(CHALLENGE));
        assertFalse(request.fail(AuthFailure.UNAVAILABLE));
        assertEquals(0, callback.calls);
    }

    @Test
    public void cancellationIsIdempotentAndIsolatedFromAnotherRequest() {
        FakeAuthRepository.PendingRequest first = repository.startRegistrationChallenge(PHONE, callback);
        RecordingCallback secondResult = new RecordingCallback();
        FakeAuthRepository.PendingRequest second = repository.startRegistrationChallenge(PHONE, secondResult);

        first.cancel();
        first.cancel();

        assertFalse(first.succeed(CHALLENGE));
        assertTrue(second.succeed(OTHER_CHALLENGE));
        assertEquals(0, callback.calls);
        assertEquals(1, secondResult.calls);
        assertSame(OTHER_CHALLENGE, secondResult.challenge);
    }

    @Test
    public void cancellingAfterCompletionDoesNotChangeTheResult() {
        FakeAuthRepository.PendingRequest request = repository.startRegistrationChallenge(PHONE, callback);

        assertTrue(request.succeed(CHALLENGE));
        request.cancel();

        assertFalse(request.fail(AuthFailure.REJECTED));
        assertEquals(1, callback.calls);
        assertSame(CHALLENGE, callback.challenge);
    }

    @Test
    public void cancellationDoesNotWaitForAnAdmittedCallback() throws Exception {
        CountDownLatch enteredCallback = new CountDownLatch(1);
        CountDownLatch releaseCallback = new CountDownLatch(1);
        FakeAuthRepository.PendingRequest request = repository.startRegistrationChallenge(
                PHONE, new RecordingCallback() {
                    @Override
                    public void onChallenge(RegistrationChallenge challenge) {
                        enteredCallback.countDown();
                        await(releaseCallback);
                        super.onChallenge(challenge);
                    }
                });
        FutureTask<Boolean> completion = start(() -> request.succeed(CHALLENGE));

        try {
            await(enteredCallback);
            FutureTask<Void> cancellation = start(() -> {
                request.cancel();
                return null;
            });
            cancellation.get(5, TimeUnit.SECONDS);
            assertFalse(completion.isDone());
            assertFalse(request.succeed(OTHER_CHALLENGE));
        } finally {
            releaseCallback.countDown();
        }
        assertTrue(completion.get(5, TimeUnit.SECONDS));
    }

    @Test
    public void concurrentCancellationAndCompletionAllowAtMostOneTerminalCallback() throws Exception {
        FakeAuthRepository.PendingRequest request = repository.startRegistrationChallenge(PHONE, callback);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch startRace = new CountDownLatch(1);
        FutureTask<Boolean> completion = start(() -> {
            ready.countDown();
            await(startRace);
            return request.succeed(CHALLENGE);
        });
        FutureTask<Void> cancellation = start(() -> {
            ready.countDown();
            await(startRace);
            request.cancel();
            return null;
        });

        try {
            await(ready);
        } finally {
            startRace.countDown();
        }
        cancellation.get(5, TimeUnit.SECONDS);
        boolean admitted = completion.get(5, TimeUnit.SECONDS);
        assertEquals(admitted ? 1 : 0, callback.calls);
        assertFalse(request.fail(AuthFailure.REJECTED));
    }

    @Test
    public void requestsDeliverTheirOwnResultsInCompletionOrder() {
        List<String> deliveries = new ArrayList<>();
        FakeAuthRepository.PendingRequest first = repository.startRegistrationChallenge(
                PHONE, recordingInto(deliveries, "first"));
        FakeAuthRepository.PendingRequest second = repository.startRegistrationChallenge(
                OTHER_PHONE, recordingInto(deliveries, "second"));

        assertTrue(second.succeed(OTHER_CHALLENGE));
        assertTrue(first.succeed(CHALLENGE));

        assertEquals(Arrays.asList("second:challenge-b", "first:challenge-a"), deliveries);
    }

    @Test
    public void callbackMayRunInlineBeforeStartReturns() {
        FakeAuthRepository inlineRepository = new FakeAuthRepository(CHALLENGE);
        AtomicBoolean startReturned = new AtomicBoolean();
        FakeAuthRepository.PendingRequest request = inlineRepository.startRegistrationChallenge(
                PHONE, new RecordingCallback() {
                    @Override
                    public void onChallenge(RegistrationChallenge challenge) {
                        assertFalse(startReturned.get());
                        callback.onChallenge(challenge);
                    }
                });
        startReturned.set(true);

        assertEquals(1, callback.calls);
        assertSame(CHALLENGE, callback.challenge);
        assertFalse(request.fail(AuthFailure.UNAVAILABLE));
    }

    @Test
    public void callbackCanReentrantlyCancelItsRequest() {
        AuthRepository.Request[] handle = new AuthRepository.Request[1];
        FakeAuthRepository.PendingRequest request = repository.startRegistrationChallenge(
                PHONE, new RecordingCallback() {
                    @Override
                    public void onChallenge(RegistrationChallenge challenge) {
                        handle[0].cancel();
                        callback.onChallenge(challenge);
                    }
                });
        handle[0] = request;

        assertTrue(request.succeed(CHALLENGE));
        assertEquals(1, callback.calls);
        assertFalse(request.fail(AuthFailure.REJECTED));
    }

    @Test
    public void invalidOrMissingArgumentsFailSynchronouslyWithoutDelivery() {
        assertThrows(NullPointerException.class, () -> repository.startRegistrationChallenge(null, callback));
        assertThrows(NullPointerException.class, () -> repository.startRegistrationChallenge(PHONE, null));
        for (String number : Arrays.asList("", "2025550123", "+1 202 555 0123", "+12", " " + PHONE)) {
            assertThrows(IllegalArgumentException.class,
                    () -> repository.startRegistrationChallenge(number, callback));
        }
        assertEquals(0, callback.calls);
    }

    @Test
    public void nullCompletionArgumentsDoNotClaimTheRequest() {
        FakeAuthRepository.PendingRequest request = repository.startRegistrationChallenge(PHONE, callback);

        assertThrows(NullPointerException.class, () -> request.succeed(null));
        assertThrows(NullPointerException.class, () -> request.fail(null));

        assertTrue(request.succeed(CHALLENGE));
        assertEquals(1, callback.calls);
    }

    @Test
    public void challengeHandleIsOpaqueAndNeverIncludedInToString() {
        String opaqueHandle = "opaque:handle/with+punctuation";
        RegistrationChallenge challenge = new RegistrationChallenge(opaqueHandle);

        assertEquals(opaqueHandle, challenge.getHandle());
        assertFalse(challenge.toString().contains(opaqueHandle));
        assertThrows(NullPointerException.class, () -> new RegistrationChallenge(null));
        assertThrows(IllegalArgumentException.class, () -> new RegistrationChallenge(""));
    }

    private static RecordingCallback recordingInto(List<String> deliveries, String request) {
        return new RecordingCallback() {
            @Override
            public void onChallenge(RegistrationChallenge challenge) {
                deliveries.add(request + ":" + challenge.getHandle());
            }
        };
    }

    private static <T> FutureTask<T> start(Callable<T> work) {
        FutureTask<T> task = new FutureTask<>(work);
        Thread thread = new Thread(task, "auth-repository-contract-test");
        thread.setDaemon(true);
        thread.start();
        return task;
    }

    private static void await(CountDownLatch latch) {
        try {
            assertTrue("Timed out waiting for test synchronization", latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new AssertionError(interrupted);
        }
    }

    private static class RecordingCallback implements AuthRepository.ChallengeCallback {
        int calls;
        RegistrationChallenge challenge;
        AuthFailure failure;

        @Override
        public void onChallenge(RegistrationChallenge challenge) {
            calls++;
            this.challenge = challenge;
        }

        @Override
        public void onFailure(AuthFailure failure) {
            calls++;
            this.failure = failure;
        }
    }
}
