package org.wikimedia.wikipedia.test.concurrency;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import org.wikimedia.wikipedia.concurrency.ExceptionHandlingAsyncTask;
import org.wikimedia.wikipedia.test.TestDummyActivity;

import java.util.concurrent.*;

public class AsyncTaskTest extends ActivityUnitTestCase<TestDummyActivity> {
    public static final int TASK_COMPLETION_TIMEOUT = 1000;
    private Executor executor;

    public AsyncTaskTest() {
        super(TestDummyActivity.class);
    }

    private Executor getDefaultExecutor() {
        if (executor == null) {
            executor = new ScheduledThreadPoolExecutor(1);
        }
        return executor;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        startActivity(new Intent(), null, null);
    }

    public void testFinishHandling() throws Exception {
        final CountDownLatch onFinishLatch = new CountDownLatch(1);
        final Integer returned = 42;
        new ExceptionHandlingAsyncTask<Integer>(getDefaultExecutor()) {
            @Override
            public void onFinish(Integer result) {
                assertEquals(returned, result);
                onFinishLatch.countDown();
            }

            @Override
            public void onCatch(Throwable caught) {
                assertTrue("Exception called despite success", false);
            }

            @Override
            public Integer performTask() throws Throwable {
                return returned;
            }
        }.execute();
        assertTrue(onFinishLatch.await(TASK_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    public void testExceptionHandling() throws Exception {
        final CountDownLatch exceptionLatch = new CountDownLatch(1);
        final Throwable thrown = new Exception();
        new ExceptionHandlingAsyncTask<Void>(getDefaultExecutor()) {
            @Override
            public void onFinish(Void result) {
                assertTrue("onFinish called despite exception", false);
            }

            @Override
            public void onCatch(Throwable caught) {
                assertSame(caught, thrown);
                exceptionLatch.countDown();
            }

            @Override
            public Void performTask() throws Throwable {
                throw thrown;
            }
        }.execute();
        assertTrue(exceptionLatch.await(TASK_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    public void testAppropriateThreadFinish() throws Throwable {
        final CountDownLatch completionLatch = new CountDownLatch(1);
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Thread callingThread = Thread.currentThread();
                new ExceptionHandlingAsyncTask<Thread>(getDefaultExecutor()) {
                    @Override
                    public void onBeforeExecute() {
                        assertSame(callingThread, Thread.currentThread());
                    }

                    @Override
                    public void onFinish(Thread result) {
                        assertNotSame(result, Thread.currentThread());
                        assertSame(Thread.currentThread(), callingThread);
                        completionLatch.countDown();
                    }

                    @Override
                    public Thread performTask() throws Throwable {
                        assertNotSame(callingThread, Thread.currentThread());
                        return Thread.currentThread();
                    }
                }.execute();
            }
        });
        assertTrue(completionLatch.await(TASK_COMPLETION_TIMEOUT, TimeUnit.MILLISECONDS));
    }
}