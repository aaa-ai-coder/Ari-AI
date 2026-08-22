package com.inspiredandroid.kai.data

import kotlinx.serialization.serializer
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Both pending task lists produced by [TaskStore.getPendingTasksPartitioned]. */
data class PendingTaskPartition(
    val scheduled: List<ScheduledTask>,
    val heartbeatAdditions: List<ScheduledTask>,
)

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
class TaskStore(appSettings: AppSettings) {

    private val tasks = SettingsJsonList(
        read = appSettings::getScheduledTasksJson,
        write = appSettings::setScheduledTasksJson,
        itemSerializer = serializer<ScheduledTask>(),
        label = "TaskStore",
        // Tasks persisted before the `trigger` field existed decode with the default (TIME).
        // Upgrade rows that carry a cron expression to CRON so the scheduler can distinguish
        // time/cron from heartbeat additions. Returning a non-null list persists the upgrade the
        // first time we see it, so every subsequent load is a no-op.
        migrate = { decoded ->
            val upgraded = decoded.map { task ->
                if (task.trigger == TaskTrigger.TIME && task.cron != null) task.copy(trigger = TaskTrigger.CRON) else task
            }
            upgraded.takeIf { it != decoded }
        },
    )

    suspend fun addTask(
        description: String,
        prompt: String,
        scheduledAtEpochMs: Long,
        cron: String? = null,
        trigger: TaskTrigger = if (cron != null) TaskTrigger.CRON else TaskTrigger.TIME,
    ): ScheduledTask {
        val now = Clock.System.now()
        val effectiveScheduledAt = when (trigger) {
            TaskTrigger.HEARTBEAT -> 0L

            // heartbeat tasks are not time-gated
            TaskTrigger.CRON -> if (scheduledAtEpochMs == 0L) {
                try {
                    CronExpression(cron!!).nextAfter(now)?.toEpochMilliseconds() ?: now.toEpochMilliseconds()
                } catch (_: Exception) {
                    now.toEpochMilliseconds()
                }
            } else {
                scheduledAtEpochMs
            }

            TaskTrigger.TIME -> scheduledAtEpochMs
        }
        val task = ScheduledTask(
            id = Uuid.random().toString(),
            description = description,
            prompt = prompt,
            scheduledAtEpochMs = effectiveScheduledAt,
            createdAtEpochMs = now.toEpochMilliseconds(),
            cron = cron,
            trigger = trigger,
        )
        tasks.update { it + task }
        return task
    }

    fun getAllTasks(): List<ScheduledTask> = tasks.get()

    /**
     * Both pending scheduled tasks and heartbeat additions from a single load. Hot-path
     * callers (chat system prompt, heartbeat prompt) need both lists per invocation;
     * combining avoids re-parsing the tasks JSON twice.
     */
    fun getPendingTasksPartitioned(): PendingTaskPartition {
        val (additions, scheduled) = tasks.get()
            .filter { it.status == TaskStatus.PENDING }
            .partition { it.trigger == TaskTrigger.HEARTBEAT }
        return PendingTaskPartition(scheduled = scheduled, heartbeatAdditions = additions)
    }

    suspend fun updateTask(task: ScheduledTask): ScheduledTask {
        tasks.update { current ->
            if (current.none { it.id == task.id }) current else current.map { if (it.id == task.id) task else it }
        }
        return task
    }

    suspend fun removeTask(id: String): Boolean {
        var removed = false
        tasks.update { current ->
            removed = current.any { it.id == id }
            if (removed) current.filterNot { it.id == id } else current
        }
        return removed
    }

    fun getDueTasks(): List<ScheduledTask> {
        val now = Clock.System.now().toEpochMilliseconds()
        return tasks.get().filter {
            it.trigger != TaskTrigger.HEARTBEAT &&
                it.scheduledAtEpochMs <= now &&
                it.status == TaskStatus.PENDING
        }
    }

    suspend fun addPresetAutomation(preset: AutomationPreset): ScheduledTask {
        return addTask(
            description = preset.title,
            prompt = preset.prompt,
            scheduledAtEpochMs = 0L,
            cron = preset.cron,
            trigger = TaskTrigger.CRON,
        )
    }

    companion object {
        val PRESET_AUTOMATIONS = listOf(
            AutomationPreset(
                title = "Daily Morning AI Briefing",
                description = "Summarizes today's agenda, priorities, and key reminders at 8:00 AM daily.",
                prompt = "Provide a concise morning briefing summarizing priorities, calendar reminders, and key topics.",
                cron = "0 8 * * *",
            ),
            AutomationPreset(
                title = "Smart Memory Consolidation",
                description = "Reviews recent conversations and consolidates knowledge into long-term memory every midnight.",
                prompt = "Review recent conversation context and consolidate important facts and developer preferences into memory.",
                cron = "0 0 * * *",
            ),
            AutomationPreset(
                title = "Codebase & Git Health Sentinel",
                description = "Scans workspace projects for uncommitted changes and code quality every 6 hours.",
                prompt = "Check active project workspaces for uncommitted git changes, repository status, and syntax health.",
                cron = "0 */6 * * *",
            ),
        )
    }
}

data class AutomationPreset(
    val title: String,
    val description: String,
    val prompt: String,
    val cron: String,
)
