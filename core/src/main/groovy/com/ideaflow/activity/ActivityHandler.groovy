package com.ideaflow.activity

import com.ideaflow.controller.IFMController
import org.joda.time.Duration
import org.joda.time.LocalDateTime
import org.joda.time.Period

import java.util.concurrent.atomic.AtomicInteger

class ActivityHandler {

	private static final int SHORTEST_ACTIVITY = 3

	private IFMController controller
	private MessageQueue messageQueue
	private FileActivity activeFileActivity
	private AtomicInteger modificationCount = new AtomicInteger(0)


	private Map<Long, ProcessActivity> activeProcessMap =[:]

	ActivityHandler(IFMController controller, MessageQueue messageQueue) {
		this.controller = controller
		this.messageQueue = messageQueue
	}

	private boolean isSame(String newFilePath) {
		isDifferent(newFilePath) == false
	}

	private boolean isDifferent(String newFilePath) {
		if (activeFileActivity == null) {
			return newFilePath != null
		} else {
			return activeFileActivity.filePath != newFilePath
		}
	}

	private boolean isOverActivityThreshold() {
		activeFileActivity != null && activeFileActivity.durationInSeconds >= SHORTEST_ACTIVITY
	}

	private Long getActiveTaskId() {
		controller.getActiveTask()?.id
	}

	void markIdleTime(Duration idleDuration) {
		Long activeTaskId = activeTaskId
		if (activeTaskId == null) {
			return
		}

		messageQueue.pushIdleActivity(activeTaskId, idleDuration.standardSeconds)
	}

	void markExternalActivity(Duration idleDuration) {
		Long activeTaskId = activeTaskId
		if (activeTaskId == null) {
			return
		}

		if (idleDuration.standardSeconds >= SHORTEST_ACTIVITY) {
			messageQueue.pushExternalActivity(activeTaskId, idleDuration.standardSeconds, null)
			if (activeFileActivity != null) {
				activeFileActivity = createFileActivity(activeTaskId, activeFileActivity.filePath)
			}
		}
	}

	void markProcessStarting(Long taskId, Long processId, String processName, String executionTaskType, boolean isDebug) {
		ProcessActivity processActivity = new ProcessActivity(taskId: taskId, processName: processName, executionTaskType: executionTaskType, isDebug: isDebug, timeStarted: LocalDateTime.now())
		activeProcessMap.put(processId, processActivity)
		//TODO this will leak memory if the processes started are never closed
	}

	void markProcessEnding(Long processId, int exitCode) {
		ProcessActivity processActivity = activeProcessMap.remove(processId)
		if (processActivity && activeTaskId != null) {
			messageQueue.pushExecutionActivity(processActivity.taskId, processActivity.durationInSeconds, processActivity.processName,
					exitCode, processActivity.executionTaskType, processActivity.isDebug)
		}
	}

	void startFileEvent(String filePath) {
		Long activeTaskId = activeTaskId
		if (activeTaskId == null) {
			return
		}

		if (isDifferent(filePath)) {
			if (isOverActivityThreshold()) {
				messageQueue.pushEditorActivity(activeFileActivity.taskId, activeFileActivity.durationInSeconds,
				                                 activeFileActivity.filePath, activeFileActivity.modified)
			}

			activeFileActivity = createFileActivity(activeTaskId, filePath)
		}
	}

	void endFileEvent(String filePath) {
		if ((filePath == null) || isSame(filePath)) {
			startFileEvent(null)
		}
	}

	void fileModified(String filePath) {
		if (activeFileActivity?.filePath == filePath) {
			activeFileActivity.modified = true
		}
		modificationCount.incrementAndGet()
	}

	void pushModificationActivity(Long intervalInSeconds) {
		int modificationCount = modificationCount.getAndSet(0)
		if (modificationCount > 0) {
			messageQueue.pushModificationActivity(activeTaskId, intervalInSeconds, modificationCount)
		}
	}

	private FileActivity createFileActivity(Long taskId, String filePath) {
		filePath == null ? null : new FileActivity(taskId: taskId, filePath: filePath, time: LocalDateTime.now(), modified: false)
	}

	private static class ProcessActivity {
		Long taskId
		LocalDateTime timeStarted
		String processName
		String executionTaskType
		boolean isDebug

		public long getDurationInSeconds() {
			Period.fieldDifference(timeStarted, LocalDateTime.now()).millis / 1000
		}

		public String toString() {
			"ProcessActivity [taskId=${taskId}, processName=${processName}, executionTaskType=${executionTaskType}, " +
					"duration=${durationInSeconds}, isDebug=${isDebug}]"
		}
	}

	private static class FileActivity {
		Long taskId
		LocalDateTime time
		String filePath
		boolean modified

		public long getDurationInSeconds() {

			Period.fieldDifference(time, LocalDateTime.now()).millis / 1000
		}

		public String toString() {
			"FileActivity [taskId=${taskId}, path=${filePath}, modified=${modified}, duration=${durationInSeconds}]"
		}
	}

}
