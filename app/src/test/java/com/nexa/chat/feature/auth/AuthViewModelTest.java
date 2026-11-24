package com.nexa.chat.feature.auth;

import static com.nexa.chat.feature.auth.AuthViewModel.Screen.OTP;
import static com.nexa.chat.feature.auth.AuthViewModel.Screen.REGISTER;
import static com.nexa.chat.feature.auth.AuthViewModel.Screen.SIGN_IN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public final class AuthViewModelTest {
    private static final String PHONE = "+12025550123";
    private static final String SECOND_PHONE = "+12025550124";
    private static final RegistrationChallenge A = new RegistrationChallenge("challenge-a");
    private static final RegistrationChallenge B = new RegistrationChallenge("challenge-b");

    private final Queue<Runnable> ownerQueue = new ConcurrentLinkedQueue<>();
    private final ControlledRepository repository = new ControlledRepository();
    private AuthViewModel model;

    @Before
    public void setUp() {
        Thread owner = Thread.currentThread();
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            @Override
            public void executeOnDiskIO(Runnable command) {
                throw new AssertionError("Unexpected disk work");
            }

            @Override
            public void postToMainThread(Runnable command) {
                ownerQueue.add(command);
            }

            @Override
            public boolean isMainThread() {
                return Thread.currentThread() == owner;
            }
        });
        model = viewModel(ownerQueue::add);
    }

    @After
    public void tearDown() {
        try {
            model.onCleared();
        } finally {
            ArchTaskExecutor.getInstance().setDelegate(null);
        }
    }

    @Test
    public void invalidPhoneDoesNotCallRepository() {
        assertFalse(model.submit(null, null));
        assertFalse(model.submit("+12", null));
        assertFalse(model.submit("2025550123", null));
        assertEquals(0, repository.calls.size());
        assertFalse(state().loading);
    }

    @Test
    public void normalizesPhoneAndBlocksDuplicateWhileLoading() {
        assertTrue(model.submit("(202) 555-0123", "US"));
        assertTrue(model.submit(PHONE, null));

        assertEquals(1, repository.calls.size());
        assertEquals(PHONE, repository.calls.get(0).phone);
        assertTrue(state().loading);
        assertEquals(REGISTER, state().screen);
    }

    @Test
    public void challengeSuccessOnlyOpensCodeEntry() {
        model.submit(PHONE, null);
        repository.calls.get(0).callback.onChallenge(A);

        assertEquals(OTP, state().screen);
        assertSame(A, state().challenge);
        assertFalse(state().loading);
        assertNull(state().failure);
        // The registration challenge cannot trigger verification or another request.
        assertTrue(model.submit(null, null));
        assertEquals(1, repository.calls.size());
        assertEquals(OTP, state().screen);
    }

    @Test
    public void failureEndsLoadingAndKeepsRegistrationRetryable() {
        for (AuthFailure failure : AuthFailure.values()) {
            int count = repository.calls.size();
            model.submit(PHONE, null);
            repository.calls.get(count).callback.onFailure(failure);

            assertEquals(REGISTER, state().screen);
            assertFalse(state().loading);
            assertSame(failure, state().failure);
            assertNull(state().challenge);
        }
    }

    @Test
    public void editingPhoneInvalidatesBeforeCancellingTransport() {
        model.submit(PHONE, null);
        Call first = repository.calls.get(0);
        first.onCancel = () -> first.callback.onChallenge(A);

        model.phoneChanged();

        assertEquals(1, first.cancellations);
        assertEquals(REGISTER, state().screen);
        assertFalse(state().loading);
        assertNull(state().challenge);
        model.submit(SECOND_PHONE, null);
        assertEquals(SECOND_PHONE, repository.calls.get(1).phone);
    }

    @Test
    public void switchingToLoginDropsAlreadyAdmittedCallback() throws Exception {
        model.submit(PHONE, null);
        Call first = repository.calls.get(0);
        onWorker(() -> first.callback.onChallenge(A));

        model.show(SIGN_IN);
        drainOwner();

        assertEquals(1, first.cancellations);
        assertEquals(SIGN_IN, state().screen);
        assertFalse(state().loading);
        assertNull(state().challenge);
    }

    @Test
    public void oldResponsesCannotOverwriteNewLoadingOrChallenge() {
        model.submit(PHONE, null);
        Call first = repository.calls.get(0);
        model.phoneChanged();
        model.submit(SECOND_PHONE, null);
        Call second = repository.calls.get(1);
        model.phoneChanged();
        model.submit(PHONE, null);
        Call current = repository.calls.get(2);

        second.callback.onFailure(AuthFailure.RATE_LIMITED);
        assertTrue(state().loading);
        assertNull(state().failure);
        current.callback.onChallenge(B);
        first.callback.onChallenge(A);

        assertEquals(OTP, state().screen);
        assertSame(B, state().challenge);
        assertFalse(state().loading);
        assertNull(state().failure);
    }

    @Test
    public void inlineSuccessLeavesNoPendingHandle() {
        model.onCleared();
        model = new AuthViewModel(new FakeAuthRepository(A), ownerQueue::add);
        model.show(REGISTER);

        model.submit(PHONE, null);

        assertEquals(OTP, state().screen);
        assertSame(A, state().challenge);
        assertFalse(state().loading);
        assertFalse(model.isRegistrationPending());
    }

    @Test
    public void inlineFailureClearsLoadingAndOldHandle() {
        repository.onStart = call -> call.callback.onFailure(AuthFailure.UNAVAILABLE);

        model.submit(PHONE, null);
        Call first = repository.calls.get(0);

        assertFalse(state().loading);
        assertSame(AuthFailure.UNAVAILABLE, state().failure);
        assertEquals(1, first.cancellations);
        assertFalse(model.isRegistrationPending());
    }

    @Test
    public void returnedHandleCannotReplaceNewerAttempt() {
        repository.onStart = first -> {
            repository.onStart = null;
            model.phoneChanged();
            model.submit(SECOND_PHONE, null);
        };

        model.submit(PHONE, null);
        Call first = repository.calls.get(0);
        Call second = repository.calls.get(1);

        assertEquals(1, first.cancellations);
        assertEquals(0, second.cancellations);
        assertTrue(state().loading);
        model.show(SIGN_IN);
        assertEquals(1, second.cancellations);
    }

    @Test
    public void workerCallbackMutatesStateOnlyOnOwner() throws Exception {
        model.submit(PHONE, null);
        Call call = repository.calls.get(0);
        onWorker(() -> call.callback.onChallenge(A));

        assertTrue(state().loading);
        assertNull(state().challenge);
        drainOwner();

        assertEquals(OTP, state().screen);
        assertSame(A, state().challenge);
    }

    @Test
    public void clearCancelsOnceAndPreventsPendingDeliveryOrFurtherSubmission() throws Exception {
        model.submit(PHONE, null);
        Call call = repository.calls.get(0);
        onWorker(() -> call.callback.onChallenge(A));
        AuthViewModel.State beforeClear = state();

        model.onCleared();
        model.onCleared();
        drainOwner();
        model.submit(SECOND_PHONE, null);

        assertEquals(1, call.cancellations);
        assertEquals(1, repository.calls.size());
        assertSame(beforeClear, state());
    }

    @Test
    public void dispatcherShutdownDoesNotFallBackToWorkerStateMutation() throws Exception {
        model.onCleared();
        model = viewModel(command -> {
            throw new RejectedExecutionException("Owner stopped");
        });
        model.submit(PHONE, null);
        Call call = repository.calls.get(0);
        AuthViewModel.State before = state();

        onWorker(() -> call.callback.onFailure(AuthFailure.UNAVAILABLE));

        assertSame(before, state());
        model.onCleared();
        assertEquals(1, call.cancellations);
    }

    private AuthViewModel viewModel(Executor dispatcher) {
        AuthViewModel result = new AuthViewModel(repository, dispatcher);
        result.show(REGISTER);
        return result;
    }

    private AuthViewModel.State state() {
        return model.state().getValue();
    }

    private void drainOwner() {
        for (Runnable command = ownerQueue.poll(); command != null; command = ownerQueue.poll()) {
            command.run();
        }
    }

    private static void onWorker(Runnable callback) throws Exception {
        FutureTask<Void> task = new FutureTask<>(callback, null);
        Thread worker = new Thread(task, "auth-viewmodel-test-worker");
        worker.setDaemon(true);
        worker.start();
        task.get(5, TimeUnit.SECONDS);
    }

    private static final class ControlledRepository implements AuthRepository {
        final List<Call> calls = new ArrayList<>();
        Consumer<Call> onStart;

        @Override
        public Request startRegistrationChallenge(String phoneE164, ChallengeCallback callback) {
            Call call = new Call(phoneE164, callback);
            calls.add(call);
            if (onStart != null) {
                onStart.accept(call);
            }
            return call;
        }
    }

    private static final class Call implements AuthRepository.Request {
        final String phone;
        final AuthRepository.ChallengeCallback callback;
        Runnable onCancel;
        int cancellations;

        Call(String phone, AuthRepository.ChallengeCallback callback) {
            this.phone = phone;
            this.callback = callback;
        }

        @Override
        public void cancel() {
            cancellations++;
            if (onCancel != null) {
                onCancel.run();
            }
        }
    }
}
