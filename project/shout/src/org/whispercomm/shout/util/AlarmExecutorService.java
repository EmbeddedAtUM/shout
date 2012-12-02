
package org.whispercomm.shout.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

/**
 * An {@link ScheduledExecutorService}-like class that uses the Android
 * {@link AlarmManager} to ensure the phone wakes up to execute the tasks.
 * 
 * @author David R. Bild
 */
public class AlarmExecutorService {
	private static final String TAG = AlarmExecutorService.class.getSimpleName();

	private static final String ALARM_INTENT_ACTION = "org.whispercomm.shout.util.AlarmManagerExecutorService";
	private static final Uri ALARM_CONTENT_URI = Uri
			.parse("content://org.whispercomm.shout.alarmmanagerexecutorservice/alarm");
	private static final String ALARM_CONTENT_TYPE = "vnd.org.whispercomm.shout/alarm-manager-executor-service";

	private final Context context;
	private final AlarmManager alarmManager;
	private final PowerManager powerManager;

	private final ExecutorService executor;
	private final Receiver receiver;
	private final PowerManager.WakeLock wakeLock;
	private final AlarmCache cache;
	private final SparseArray<TaskImpl> tasks;

	private final String tag;

	private volatile boolean running;

	public interface Task {
		public void cancel();
	}

	public AlarmExecutorService(Context context, ExecutorService executor, String tag) {
		this.context = context;
		this.executor = executor;
		this.tag = tag;
		this.receiver = new Receiver();
		this.cache = new AlarmCache();
		this.tasks = new SparseArray<TaskImpl>();
		this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag);
		this.wakeLock.setReferenceCounted(true);
		this.running = true;
	}

	public void shutdown() {
		running = false;
		cancelAllTasks();
		executor.shutdownNow();
	}

	/**
	 * Executes the task the run as soon as possible.
	 * 
	 * @param command the task to execute
	 * @throws RejectedExecutionException if the executor has been shutdown
	 */
	@SuppressLint("Wakelock")
	public void execute(Runnable command) {
		if (!running)
			throw new RejectedExecutionException("Cannot schedule new command after shutdown.");
		try {
			// Use timeout so the phone won't stay awake forever if code is
			// buggy or the app is destroyed.
			// TODO: All tasks must complete faster than 1 minute or the
			// wakelock will be double-released. Fix this.
			wakeLock.acquire(1000 * 60); // 1 minute
			executor.execute(new Worker(command));
		} catch (RejectedExecutionException e) {
			wakeLock.release();
			throw e;
		} catch (RuntimeException e) {
			wakeLock.release();
			throw e;
		}
	}

	/**
	 * Schedules a one-shot task to run after the given delay.
	 * 
	 * @param command the task to execute
	 * @param delay the time from now until the task is enabled
	 * @param unit the time unit of the delay argument
	 * @return a handle to the task to allow cancellation
	 * @throws RejectedExecutionException if the executor has been shutdown
	 */
	public Task schedule(Runnable command, long delay, TimeUnit unit) {
		return schedule(command, TimeUnit.MILLISECONDS.convert(delay, unit));
	}

	/**
	 * Swallows the RejectedExceptionException if this
	 * {@code AlarmExecutorService} is stopped. With this, locks would be needed
	 * to ensure that all alarmmanager-scheduled tasks are successfully canceled
	 * before shutting down the executor service.
	 * 
	 * @param command
	 */
	private void safeExecute(Runnable command) {
		try {
			execute(command);
		} catch (RejectedExecutionException e) {
			if (!running)
				Log.d(TAG, "Ignoring RejectedExecutionException after shutdown");
			else
				throw e;
		}
	}

	@SuppressLint("Wakelock")
	private void onReceive(Context context, Intent intent) {
		int id = (int) ContentUris.parseId(intent.getData());
		TaskImpl task = tasks.get(id);
		if (task != null)
			safeExecute(task.getCommand());
	}

	private Task schedule(Runnable command, long delay) {
		TaskImpl task = newTask(command);

		long triggerAtMillis = SystemClock.elapsedRealtime() + delay;
		alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, task.getAlarm()
				.getIntent());
		return task;
	}

	private TaskImpl newTask(Runnable command) {
		TaskImpl task = new TaskImpl(cache.get(), command);
		synchronized (tasks) {
			if (tasks.size() == 0)
				registerReceiver();
			tasks.put(task.getId(), task);
		}
		return task;
	}

	private void unschedule(TaskImpl task) {
		/*
		 * It would be nice to use AlarmManager#cancel(Intent) to cancel the
		 * task, but unfortunately there is no way to determine if the task was
		 * canceled. The alarm may have already expired and the
		 * #onReceive(Intent) be scheduled to run. This means there is no safe
		 * way to use cancel (while reusing alarms/pendingintents). If we call
		 * cancel and return the alarm to the cache, the onReceive(Intent) could
		 * still be called based on the old task but then execute the Runnable
		 * for the new task.
		 */
		/*
		 * Instead, we don't cancel the alarm. The task has already recorded
		 * that it was canceled. The alarm will fire at the scheduled time and
		 * the task will clean itself up without actually executing.
		 */
		/*
		 * This scheme is not usable with a periodic alarm, for which cancel
		 * must be called, so this class does not support period alarms.
		 */
		/*
		 * AlarmManager#cancel(Intent) should return a boolean indicating if the
		 * alarm was canceled or if it was already scheduled for execution. Then
		 * the following code would work.
		 */
		// if (alarmManager.cancel(task.getAlarm().getIntent())
		// removeTask(task);
	}

	private void removeTask(TaskImpl task) {
		synchronized (tasks) {
			tasks.remove(task.getId());
			if (tasks.size() == 0)
				unregisterReceiver();
		}
		cache.put(task.getAlarm());
	}

	/**
	 * Called by the shutdown code to cancel all pending alarms. The cancel()
	 * races with the alarm execution, but that's ok. Canceling an already run
	 * task is OK.
	 */
	private void cancelAllTasks() {
		synchronized (tasks) {
			for (int i = 0; i < tasks.size(); i++) {
				TaskImpl task = tasks.valueAt(i);
				alarmManager.cancel(task.getAlarm().getIntent());
				removeTask(task);
			}
		}
	}

	private void registerReceiver() {
		context.registerReceiver(receiver,
				IntentFilter.create(ALARM_INTENT_ACTION + "." + tag, ALARM_CONTENT_TYPE));
	}

	private void unregisterReceiver() {
		context.unregisterReceiver(receiver);
	}

	private class Receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			AlarmExecutorService.this.onReceive(context, intent);
		}

	}

	private class Alarm {
		private final int id;
		private final PendingIntent intent;

		public Alarm(PendingIntent intent, int id) {
			this.id = id;
			this.intent = intent;
		}

		public int getId() {
			return id;
		}

		public PendingIntent getIntent() {
			return intent;
		}
	}

	private class TaskImpl implements Task, Runnable {

		private final Alarm alarm;

		private final Runnable command;

		private boolean scheduled;

		public TaskImpl(Alarm alarm, Runnable command) {
			this.alarm = alarm;
			this.command = command;
			this.scheduled = true;
		}

		public int getId() {
			return alarm.getId();
		}

		public Runnable getCommand() {
			return command;
		}

		public Alarm getAlarm() {
			return alarm;
		}

		@Override
		public void cancel() {
			if (take())
				unschedule(this);
		}

		@Override
		public void run() {
			try {
				if (take())
					command.run();
				removeTask(this);
			} finally {
				wakeLock.release();
			}
		}

		/**
		 * Attempts to take control of this task to either run it or cancel it.
		 * Sets {@link #scheduled} to {@code false} if this task is taken.
		 * 
		 * @return {@code true} if the task is successfully taken and
		 *         {@code false} if someone else took it first.
		 */
		private boolean take() {
			boolean wasScheduled;
			synchronized (this) {
				wasScheduled = scheduled;
				scheduled = false;
			}
			return wasScheduled;
		}
	}

	/**
	 * Wrapper for task execution that ensures the wakelock is released.
	 * 
	 * @author David R. Bild
	 */
	private class Worker implements Runnable {

		public Runnable command;

		public Worker(Runnable command) {
			this.command = command;
		}

		@Override
		public void run() {
			try {
				command.run();
			} finally {
				wakeLock.release();
			}
		}
	}

	/**
	 * Reuse alarms (really, the pending intents) to minimize the number of
	 * lines in the stats shown by 'adb dumpsys alarm'.
	 * 
	 * @author David R. Bild
	 */
	private class AlarmCache {
		private List<Alarm> alarms;

		private int nextId;

		public AlarmCache() {
			this.nextId = 0;
			this.alarms = new ArrayList<Alarm>();
		}

		private synchronized int nextId() {
			return nextId++;
		}

		private Alarm newAlarm() {
			int id = nextId();
			Uri data = ContentUris.withAppendedId(ALARM_CONTENT_URI, id);
			Intent i = new Intent(ALARM_INTENT_ACTION + "." + tag).setDataAndType(data,
					ALARM_CONTENT_TYPE);
			return new Alarm(PendingIntent.getBroadcast(context, 0, i, 0), id);
		}

		public synchronized Alarm get() {
			if (alarms.size() == 0)
				return newAlarm();
			return alarms.remove(alarms.size() - 1);
		}

		public synchronized void put(Alarm intent) {
			alarms.add(intent);
		}
	}

}
