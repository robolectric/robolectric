package org.robolectric.util;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.util.TaskManager.Listener;

@RunWith(JUnit4.class)
public class TaskManagerImplTest {

  private TaskManager taskManager;

  @Before
  public void setUp() {
    taskManager = new TaskManagerImpl();
  }

  @Test
  public void size_empty() {
    assertThat(taskManager.size()).isEqualTo(0);
  }

  @Test
  public void getNextScheduledTime_empty() {
    assertThat(taskManager.getScheduledTimeOfNextTask()).isEqualTo(-1);
  }

  @Test
  public void runNextTask_empty() {
    assertThat(taskManager.runNextTask()).isEqualTo(-1);
  }

  @Test
  public void remove_empty() {
    taskManager.remove(() -> {
    });
  }

  @Test
  public void postAndRunNextTask() {
    final AtomicBoolean wasRun = new AtomicBoolean(false);
    taskManager.post(() -> wasRun.set(true), 0);
    assertThat(wasRun.get()).isFalse();
    assertThat(taskManager.size()).isEqualTo(1);
    assertThat(taskManager.runNextTask()).isEqualTo(0);
    assertThat(wasRun.get()).isTrue();
  }

  @Test
  public void post_withListener() {
    Listener mockListener = mock(Listener.class);
    taskManager.addListener(mockListener);
    taskManager.post(() -> {
    }, 0);
    verify(mockListener, times(1)).newTaskPosted();
  }

  @Test
  public void post_withMultipleListeners() {
    Listener mockListener1 = mock(Listener.class);
    Listener mockListener2 = mock(Listener.class);
    taskManager.addListener(mockListener1);
    taskManager.addListener(mockListener2);
    taskManager.post(() -> {
    }, 0);
    verify(mockListener1, times(1)).newTaskPosted();
    verify(mockListener2, times(1)).newTaskPosted();
  }

  @Test
  public void post_sorting() {
    taskManager.post(() -> {}, 100);
    taskManager.post(() -> {}, 50);

    assertThat(taskManager.runNextTask()).isEqualTo(50);
    assertThat(taskManager.runNextTask()).isEqualTo(100);
    assertThat(taskManager.runNextTask()).isEqualTo(-1);
  }

  @Test
  public void post_AtFront() {
    final AtomicBoolean wasRun = new AtomicBoolean(false);
    taskManager.post(() -> {wasRun.set(true);}, 0);
    taskManager.postAtFrontOfQueue(() -> {});

    assertThat(taskManager.runNextTask()).isEqualTo(0);
    assertThat(wasRun.get()).isFalse();
    assertThat(taskManager.runNextTask()).isEqualTo(0);
    assertThat(wasRun.get()).isTrue();
    assertThat(taskManager.runNextTask()).isEqualTo(-1);
  }
}