package com.nexa.chat.feature.messaging;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.graphics.Rect;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.Window;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.EditText;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.view.WindowCallbackWrapper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.GeneralLocation;
import androidx.test.espresso.util.TreeIterables;

import com.nexa.chat.MainActivity;
import com.nexa.chat.feature.messaging.ui.MessagingController;
import com.nexa.chat.feature.messaging.ui.MessagingExecution;
import com.nexa.chat.feature.messaging.ui.MessagingView;
import com.nexa.chat.feature.messaging.ui.MessagingRouter;

import org.hamcrest.Matcher;
import org.junit.runners.model.MultipleFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/** Independent test-owned surface. No Session/Home/composition-root or manifest integration. */
final class MessagingTestHost implements AutoCloseable {
    static final String LIST_TITLE = "Tin nhắn · FAKE DEMO";
    static final String COMPOSER_HINT = "Nhập tin nhắn";
    static final String BACK = "‹ Danh sách";
    static final String SEND = "Gửi";

    private MessagingExecution execution;
    private MessagingExecution.AccountLease lease;
    private MessagingController controller;
    private ActivityScenario<MainActivity> scenario;
    private MessagingView surface;
    private SafeAreaHost insetHost;
    MessagingTestFixtures fixtures;

    // Bounded failure/input evidence only; no direct navigation comparison probes.
    private MainActivity diagnosticActivity;
    private View diagnosticBack;
    private MessagingRouter.Subscription diagnosticRoutes;
    private Window.Callback originalWindowCallback;
    private boolean appliedSelected;
    private boolean appliedResumed;
    private boolean cleanupVerified;

    static MessagingTestHost launch() throws Exception {
        requireTestThread();
        return launch(MessagingTestFixtures.create());
    }

    static MessagingTestHost launch(MessagingTestFixtures fixtures) throws Exception {
        requireTestThread();
        MessagingTestHost host = new MessagingTestHost();
        try {
            host.fixtures = fixtures;
            host.execution = new MessagingExecution();
            host.lease = host.execution.acquire(fixtures.user, fixtures.epoch);
            host.controller = new MessagingController(fixtures.user,
                    fixtures.epoch, fixtures.client, host.lease);
            host.scenario = ActivityScenario.launch(MainActivity.class);
            host.scenario.onActivity(host::mount);
            return host;
        } catch (Exception | Error failure) {
            try {
                host.close();
            } catch (Exception | Error cleanupFailure) {
                failure.addSuppressed(cleanupFailure);
            }
            throw failure;
        }
    }

    private void mount(MainActivity activity) {
        assertEquals(Looper.getMainLooper(), Looper.myLooper());
        ViewGroup content = activity.findViewById(android.R.id.content);
        content.removeAllViews();
        // Leave the Activity content's original padding/window configuration untouched.
        insetHost = new SafeAreaHost(activity);
        surface = new MessagingView(activity, controller, controller.getRouter());
        insetHost.addView(surface, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.addView(insetHost, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        surface.setHostState(true, true); // Explicit selected/resumed, separate from attachment.
        appliedSelected = true;
        appliedResumed = true;
        diagnosticActivity = activity;
        originalWindowCallback = activity.getWindow().getCallback();
        activity.getWindow().setCallback(new WindowCallbackWrapper(originalWindowCallback) {
            @Override public boolean dispatchTouchEvent(MotionEvent event) {
                Log.i("MessagingBackDiag", "WINDOW_TOUCH action=" + event.getActionMasked()
                        + " raw=" + event.getRawX() + "," + event.getRawY());
                boolean handled = super.dispatchTouchEvent(event);
                Log.i("MessagingBackDiag", "WINDOW_TOUCH_RESULT handled=" + handled);
                return handled;
            }
        });
        diagnosticRoutes = controller.getRouter().observe(activity::runOnUiThread,
                () -> diagnosticSnapshot("ROUTE_OBSERVER"));
        ViewCompat.requestApplyInsets(content);
    }

    // Lifecycle operations retain the account owner; all View/Activity access stays on main.
    MessagingController account() { return controller; }
    MessagingExecution runtime() { return execution; }
    MessagingExecution.AccountLease accountLease() { return lease; }
    Lifecycle.State scenarioState() { return scenario.getState(); }
    boolean cleanupVerified() { return cleanupVerified; }

    void inspectSurface(java.util.function.Consumer<MessagingView> assertion) {
        scenario.onActivity(activity -> assertion.accept(surface));
    }

    void setHostState(boolean selected, boolean resumed) {
        scenario.onActivity(activity -> {
            appliedSelected = selected;
            appliedResumed = resumed;
            surface.setHostState(selected, resumed);
        });
    }

    private void unmount() {
        if (diagnosticRoutes != null) {
            diagnosticRoutes.close();
            diagnosticRoutes = null;
        }
        if (diagnosticActivity != null && originalWindowCallback != null) {
            diagnosticActivity.getWindow().setCallback(originalWindowCallback);
        }
        if (diagnosticBack != null) diagnosticBack.setOnTouchListener(null);
        diagnosticBack = null;
        if (surface != null) {
            surface.setHostState(false, false);
            ViewGroup parent = (ViewGroup) surface.getParent();
            if (parent != null) parent.removeView(surface);
            assertFalse("Old surface detached", surface.isAttachedToWindow());
        }
        if (insetHost != null) {
            ViewGroup parent = (ViewGroup) insetHost.getParent();
            if (parent != null) parent.removeView(insetHost);
        }
        surface = null;
        insetHost = null;
        appliedSelected = false;
        appliedResumed = false;
    }

    void detachSurface() {
        requireTestThread();
        scenario.onActivity(activity -> unmount());
    }

    void remountSurface() {
        requireTestThread();
        scenario.onActivity(activity -> {
            unmount();
            mount(activity);
        });
    }

    void recreateActivity() {
        requireTestThread();
        detachSurface();
        scenario.recreate();
        scenario.onActivity(this::mount);
    }

    void invalidateAccount() { controller.invalidate(fixtures.epoch); }

    void closeScenario() {
        requireTestThread();
        if (scenario.getState() != Lifecycle.State.DESTROYED) {
            detachSurface();
            scenario.close();
        }
    }

    void recordComposer(String label) {
        scenario.onActivity(activity -> {
            EditText editor = null;
            for (View child : TreeIterables.breadthFirstViewTraversal(surface)) {
                if (child instanceof EditText
                        && COMPOSER_HINT.equals(String.valueOf(((EditText) child).getHint()))) {
                    editor = (EditText) child;
                    break;
                }
            }
            recordComposerState(label, editor);
        });
    }

    private void recordComposerState(String label, EditText editor) {
        MessagingRouter.Route route = controller.getRouter().currentRoute();
        String draft = controller.draft(fixtures.conversation.getId());
        String base = label + " uptime=" + SystemClock.uptimeMillis()
                + " main=" + (Looper.myLooper() == Looper.getMainLooper())
                + " scopeActive=" + controller.isActive()
                + " route=" + route.getScreen() + " routeRevision=" + route.getRevision()
                + " selected=" + appliedSelected + " resumed=" + appliedResumed
                + " draft=" + quoted(draft)
                + " draftRevision=" + controller.draftRevision(fixtures.conversation.getId());
        if (editor == null) {
            Log.i("MessagingComposerDiag", base + " composer=ABSENT");
            return;
        }
        InputMethodManager ime = (InputMethodManager) editor.getContext()
                .getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(editor.getRootView());
        int[] screen = new int[2];
        editor.getLocationOnScreen(screen);
        Rect visible = new Rect();
        editor.getGlobalVisibleRect(visible);
        float[] center = GeneralLocation.CENTER.calculateCoordinates(editor);
        Rect windowFrame = new Rect();
        editor.getWindowVisibleDisplayFrame(windowFrame);
        Log.i("MessagingComposerDiag", base + " identity=" + System.identityHashCode(editor)
                + " text=" + quoted(editor.getText())
                + " selection=" + editor.getSelectionStart() + "," + editor.getSelectionEnd()
                + " composing=" + BaseInputConnection.getComposingSpanStart(editor.getText())
                + "," + BaseInputConnection.getComposingSpanEnd(editor.getText())
                + " attached=" + editor.isAttachedToWindow() + " shown=" + editor.isShown()
                + " focused=" + editor.hasFocus() + " windowFocus=" + editor.hasWindowFocus()
                + " screen=" + screen[0] + "," + screen[1] + " size="
                + editor.getWidth() + "," + editor.getHeight() + " visible=" + visible
                + " defaultTypeTextTap=" + center[0] + "," + center[1]
                + " windowFrame=" + windowFrame + " layoutPending=" + editor.isLayoutRequested()
                + " textEditor=" + editor.onCheckIsTextEditor()
                + " inputType=" + editor.getInputType() + " imeOptions=" + editor.getImeOptions()
                + " imeActiveForEditor=" + (ime != null && ime.isActive(editor))
                + " imeAcceptingText=" + (ime != null && ime.isAcceptingText())
                + " imeVisible=" + (insets != null && insets.isVisible(WindowInsetsCompat.Type.ime())));
    }

    private static String quoted(CharSequence text) {
        return "\"" + String.valueOf(text).replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    /** Verify the library's unmodified default click target, then perform the real Espresso action. */
    ViewAction checkedBackClick(ViewAction click) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return click.getConstraints(); }
            @Override public String getDescription() { return "real Back click inside inset-safe content"; }
            @Override public void perform(UiController ui, View view) {
                assertTrue("Inset-aware layout completed", insetHost.isLayoutReady());
                diagnosticBack = view;
                float[] target = GeneralLocation.VISIBLE_CENTER.calculateCoordinates(view);
                Rect safeContent = insetHost.safeContentOnScreen();
                Log.i("MessagingBackDiag", "REAL_BACK_CLICK_TARGET x=" + target[0]
                        + " y=" + target[1] + " safeContent=" + safeContent);
                assertTrue("Default Espresso click target must be inside unobstructed content: "
                                + safeContent,
                        safeContent.contains((int) Math.floor(target[0]), (int) Math.floor(target[1])));
                boolean[] delivered = new boolean[2];
                view.setOnTouchListener((selected, event) -> {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) delivered[0] = true;
                    if (event.getActionMasked() == MotionEvent.ACTION_UP) delivered[1] = true;
                    Log.i("MessagingBackDiag", "REAL_BACK_TOUCH identity="
                            + System.identityHashCode(selected) + " action=" + event.getActionMasked()
                            + " raw=" + event.getRawX() + "," + event.getRawY());
                    return false;
                });
                diagnosticSnapshot("REAL_BACK_BEFORE");
                try {
                    click.perform(ui, view);
                } finally {
                    ui.loopMainThreadUntilIdle();
                    diagnosticSnapshot("REAL_BACK_AFTER");
                }
                assertTrue("Back received actual touch down/up", delivered[0] && delivered[1]);
            }
        };
    }

    void recordRestoredList() {
        scenario.onActivity(activity -> diagnosticSnapshot("REAL_BACK_LIST_RESTORED"));
    }

    private void diagnosticSnapshot(String label) {
        if (diagnosticActivity == null || surface == null) return;
        MessagingRouter.Route route = controller.getRouter().currentRoute();
        View content = diagnosticActivity.findViewById(android.R.id.content);
        Rect visibleFrame = new Rect();
        surface.getWindowVisibleDisplayFrame(visibleFrame);
        Log.i("MessagingBackDiag", label + " ROUTE screen=" + route.getScreen()
                + " revision=" + route.getRevision() + " id=" + route.getConversationId()
                + " main=" + (Looper.myLooper() == Looper.getMainLooper())
                + " controllerActive=" + controller.isActive()
                + " routerActive=" + controller.getRouter().isActive()
                + " attached=" + surface.isAttachedToWindow()
                + " windowFocus=" + surface.hasWindowFocus()
                + " lifecycle=" + diagnosticActivity.getLifecycle().getCurrentState()
                + " appliedHostSelected=" + appliedSelected + " appliedHostResumed=" + appliedResumed
                + " contentPadding=" + content.getPaddingLeft() + "," + content.getPaddingTop()
                + "," + content.getPaddingRight() + "," + content.getPaddingBottom()
                + " hostPadding=" + insetHost.getPaddingLeft() + "," + insetHost.getPaddingTop()
                + "," + insetHost.getPaddingRight() + "," + insetHost.getPaddingBottom()
                + " insetLayoutReady=" + insetHost.isLayoutReady()
                + " safeContent=" + insetHost.safeContentOnScreen()
                + " visibleWindow=" + visibleFrame);
        WindowInsetsCompat insets = ViewCompat.getRootWindowInsets(surface.getRootView());
        if (insets != null) {
            Log.i("MessagingBackDiag", label + " ROOT_INSETS status="
                    + insets.getInsets(WindowInsetsCompat.Type.statusBars())
                    + " statusIgnoringVisibility="
                    + insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars())
                    + " navigation=" + insets.getInsets(WindowInsetsCompat.Type.navigationBars())
                    + " gestures=" + insets.getInsets(WindowInsetsCompat.Type.systemGestures())
                    + " ime=" + insets.getInsets(WindowInsetsCompat.Type.ime())
                    + " statusVisible=" + insets.isVisible(WindowInsetsCompat.Type.statusBars()));
        }
        if (diagnosticBack != null) {
            logGeometry(label + " SELECTED_BACK", diagnosticBack);
            int[] location = new int[2];
            diagnosticBack.getLocationOnScreen(location);
            int x = location[0] + diagnosticBack.getWidth() / 2;
            int y = location[1] + diagnosticBack.getHeight() / 2;
            for (View candidate : TreeIterables.breadthFirstViewTraversal(surface.getRootView())) {
                Rect rect = new Rect();
                if (candidate.getGlobalVisibleRect(rect) && rect.contains(x, y)) {
                    logGeometry(label + " APP_VIEW_AT_BACK_CENTER", candidate);
                }
            }
        }
        Log.i("MessagingBackDiag", label + " TEXT_HIERARCHY " + visibleHierarchy(surface.getRootView()));
    }

    private static void logGeometry(String label, View view) {
        int[] screen = new int[2];
        view.getLocationOnScreen(screen);
        Rect globalVisible = new Rect();
        boolean hasVisibleRect = view.getGlobalVisibleRect(globalVisible);
        String text = view instanceof TextView ? ((TextView) view).getText().toString() : "";
        Log.i("MessagingBackDiag", label + " class=" + view.getClass().getName()
                + " identity=" + System.identityHashCode(view) + " text=" + text
                + " screenRect=" + new Rect(screen[0], screen[1],
                screen[0] + view.getWidth(), screen[1] + view.getHeight())
                + " globalVisible=" + globalVisible + " hasVisibleRect=" + hasVisibleRect
                + " clickable=" + view.isClickable() + " enabled=" + view.isEnabled()
                + " shown=" + view.isShown() + " attached=" + view.isAttachedToWindow()
                + " focused=" + view.isFocused() + " z=" + view.getZ()
                + " layoutPending=" + view.isLayoutRequested());
    }

    static void awaitDisplayed(Matcher<View> matcher) {
        awaitUi("one visible matching view: " + matcher,
                root -> insetLayoutReady(root) && !hasPendingLayout(root)
                        && matchingViews(root, allOf(matcher, isDisplayed())) == 1);
    }

    static void awaitUi(String description, Predicate<View> condition) {
        onView(isRoot()).perform(new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "await " + description + " (10s limit)"; }
            @Override public void perform(UiController ui, View root) {
                long deadline = SystemClock.uptimeMillis() + TimeUnit.SECONDS.toMillis(10);
                do {
                    ui.loopMainThreadUntilIdle();
                    if (condition.test(root)) return;
                    // Condition-driven frame polling pumps rendering/callbacks; never Thread.sleep.
                    ui.loopMainThreadForAtLeast(16);
                } while (SystemClock.uptimeMillis() < deadline);
                throw new AssertionError("Timed out awaiting " + description
                        + "; hierarchy: " + visibleHierarchy(root));
            }
        });
    }

    static void assertVisibleCount(Matcher<View> matcher, int expected) {
        onView(isRoot()).check((root, missing) -> {
            if (missing != null) throw missing;
            assertEquals("Visible match count for " + matcher, expected,
                    matchingViews(root, allOf(matcher, isDisplayed())));
        });
    }

    // A repository callback can add a matching row before the next traversal positions the controls.
    // Do not let Espresso tap coordinates from that previous layout.
    private static boolean hasPendingLayout(View root) {
        for (View view : TreeIterables.breadthFirstViewTraversal(root)) {
            if (view.isShown() && view.isLayoutRequested()) return true;
        }
        return false;
    }

    private static String visibleHierarchy(View root) {
        StringBuilder result = new StringBuilder();
        for (View view : TreeIterables.breadthFirstViewTraversal(root)) {
            if (view instanceof android.widget.TextView) {
                result.append('[').append(((android.widget.TextView) view).getText())
                        .append(" displayed=").append(isDisplayed().matches(view))
                        .append(" bounds=").append(view.getLeft()).append(',').append(view.getTop())
                        .append(',').append(view.getRight()).append(',').append(view.getBottom())
                        .append(" layoutPending=").append(view.isLayoutRequested()).append(']');
            }
        }
        return result.toString();
    }

    /**
     * Test-owned container: child dispatch can be consumed by AppCompat ancestors, so use the
     * unconsumed decor insets. Calculate only the unsafe overlap of this container's screen bounds;
     * an ancestor that already moved/shrank the container into the safe area adds no padding here.
     */
    private static final class SafeAreaHost extends FrameLayout
            implements ViewTreeObserver.OnPreDrawListener {
        private final View decor;
        private final int initialLeft, initialTop, initialRight, initialBottom;
        private final Rect safeWindow = new Rect();
        private boolean ready;

        SafeAreaHost(MainActivity activity) {
            super(activity);
            decor = activity.getWindow().getDecorView();
            initialLeft = getPaddingLeft();
            initialTop = getPaddingTop();
            initialRight = getPaddingRight();
            initialBottom = getPaddingBottom();
            ViewCompat.setOnApplyWindowInsetsListener(this, (view, dispatched) -> {
                updateSafePadding();
                return dispatched;
            });
        }

        @Override protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            getViewTreeObserver().addOnPreDrawListener(this);
            ViewCompat.requestApplyInsets(this);
        }

        @Override protected void onDetachedFromWindow() {
            ready = false;
            if (getViewTreeObserver().isAlive()) {
                getViewTreeObserver().removeOnPreDrawListener(this);
            }
            super.onDetachedFromWindow();
        }

        @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            updateSafePadding();
        }

        @Override public boolean onPreDraw() {
            updateSafePadding();
            return true;
        }

        private void updateSafePadding() {
            WindowInsetsCompat root = ViewCompat.getRootWindowInsets(decor);
            if (!isAttachedToWindow() || root == null || getWidth() == 0 || getHeight() == 0
                    || decor.getWidth() == 0 || decor.getHeight() == 0) {
                ready = false;
                return;
            }
            Insets edges = root.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
                            | WindowInsetsCompat.Type.ime());
            int[] window = new int[2];
            int[] container = new int[2];
            decor.getLocationOnScreen(window);
            getLocationOnScreen(container);
            safeWindow.set(window[0] + edges.left, window[1] + edges.top,
                    window[0] + decor.getWidth() - edges.right,
                    window[1] + decor.getHeight() - edges.bottom);
            int left = initialLeft + Math.max(0, safeWindow.left - container[0]);
            int top = initialTop + Math.max(0, safeWindow.top - container[1]);
            int right = initialRight
                    + Math.max(0, container[0] + getWidth() - safeWindow.right);
            int bottom = initialBottom
                    + Math.max(0, container[1] + getHeight() - safeWindow.bottom);
            if (getPaddingLeft() != left || getPaddingTop() != top
                    || getPaddingRight() != right || getPaddingBottom() != bottom) {
                ready = false;
                setPadding(left, top, right, bottom);
                return;
            }
            ready = isLaidOut() && !isLayoutRequested();
        }

        boolean isLayoutReady() {
            return ready && isAttachedToWindow() && !isLayoutRequested();
        }

        Rect safeContentOnScreen() {
            int[] position = new int[2];
            getLocationOnScreen(position);
            Rect inner = new Rect(position[0] + getPaddingLeft(), position[1] + getPaddingTop(),
                    position[0] + getWidth() - getPaddingRight(),
                    position[1] + getHeight() - getPaddingBottom());
            if (!inner.intersect(safeWindow)) inner.setEmpty();
            return inner;
        }
    }

    private static boolean insetLayoutReady(View root) {
        for (View view : TreeIterables.breadthFirstViewTraversal(root)) {
            if (view instanceof SafeAreaHost) return ((SafeAreaHost) view).isLayoutReady();
        }
        return false;
    }

    private static int matchingViews(View root, Matcher<View> matcher) {
        int count = 0;
        for (View view : TreeIterables.breadthFirstViewTraversal(root)) {
            if (matcher.matches(view)) count++;
        }
        return count;
    }

    @Override public void close() throws Exception {
        requireTestThread();
        if (cleanupVerified) return;
        List<Throwable> failures = new ArrayList<>();
        if (controller != null) {
            Log.i("MessagingUiSmoke", "Teardown route=" + controller.getRouter().currentRoute().getScreen());
        }
        // Try every cleanup step even if mounting, a UI assertion, or another cleanup step failed.
        try {
            if (scenario != null && scenario.getState() != Lifecycle.State.DESTROYED) {
                scenario.onActivity(activity -> {
                    if (surface != null) {
                        diagnosticSnapshot("CLEANUP_BEFORE");
                        if (diagnosticRoutes != null) diagnosticRoutes.close();
                        activity.getWindow().setCallback(originalWindowCallback);
                        if (diagnosticBack != null) diagnosticBack.setOnTouchListener(null);
                        appliedSelected = false;
                        appliedResumed = false;
                        surface.setHostState(false, false);
                        ViewGroup parent = (ViewGroup) surface.getParent();
                        if (parent != null) parent.removeView(surface);
                        ViewGroup hostParent = (ViewGroup) insetHost.getParent();
                        if (hostParent != null) hostParent.removeView(insetHost);
                        assertFalse("Surface detached", surface.isAttachedToWindow());
                        surface = null;
                    }
                });
            }
        } catch (Throwable failure) {
            failures.add(failure);
        }
        try {
            if (controller != null) controller.invalidate(fixtures.epoch);
        } catch (Throwable failure) {
            failures.add(failure);
        }
        try {
            // Release held fake sources after invalidation, including assertion-failure paths.
            if (fixtures != null) fixtures.releasePending();
        } catch (Throwable failure) {
            failures.add(failure);
        }
        try {
            if (execution != null) execution.close();
        } catch (Throwable failure) {
            failures.add(failure);
        }
        try {
            if (scenario != null) scenario.close();
        } catch (Throwable failure) {
            failures.add(failure);
        }
        try {
            if (execution != null) {
                assertTrue("Runtime must physically terminate off main",
                        execution.awaitTermination(5, TimeUnit.SECONDS));
                assertEquals("No live or retiring leases", 0, execution.liveLeases());
            }
            if (controller != null) {
                assertFalse("Account invalidated", controller.isActive());
                assertEquals("Account state cleared", "",
                        controller.draft(fixtures.conversation.getId()));
            }
            if (lease != null) {
                assertEquals("No callers", 0, lease.callerReservations());
                assertEquals("No operations", 0, lease.operationReservations());
                assertEquals("No sources", 0, lease.sourceStages());
                assertEquals("No settlements", 0, lease.settlementMailboxSize());
            }
            Log.i("MessagingUiSmoke", "Cleanup verified: inactive account; zero leases/callers/"
                    + "operations/sources/settlements; workers terminated");
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            failures.add(failure);
        } catch (Throwable failure) {
            failures.add(failure);
        }
        MultipleFailureException.assertEmpty(failures);
        cleanupVerified = true;
    }

    private static void requireTestThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new IllegalStateException("Fixture setup/cleanup must not block the main thread");
        }
    }
}
