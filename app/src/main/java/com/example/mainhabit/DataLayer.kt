package com.example.mainhabit

import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime


@Entity(tableName = "days")
data class DayEntity(
    @PrimaryKey val dayId :String,
    val date: LocalDate,
    var isSelected: Boolean = false,
    val progress:Float = 0f,
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val taskId:String,
    val taskName:String ="Task Name",
    val description:String ="",
    val color:Int = Color(0xFF8E97FD).toArgb(),
    val icon:Int,
    val frequency:String,
    val streak:Int = 0,
    val maxStreak:Int = 0,
    val isReminder: Boolean = false,
    val time: LocalTime = LocalTime.NOON,
    val daysInWeek:List<Int> = listOf()
)

@Entity(tableName = "today_tasks",
    foreignKeys = [ForeignKey(
        entity = DayEntity::class,
        parentColumns = ["dayId"],
        childColumns = ["dayId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(entity = TaskEntity::class,
        parentColumns = ["taskId"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("taskId")])
data class TodayTasks(
    @PrimaryKey(autoGenerate = true) val id:Int = 0,
    val dayId:String,
    val taskId:String,
    val isCompleted:Boolean = false
)

@Entity(tableName = "checklist_items",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["taskId"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("taskId")])
data class ChecklistEntity(
    @PrimaryKey val id:String,
    val taskId:String,
    val task:String,
    val isReminder:Boolean,
    val isCompleted: Boolean,
    val time:LocalTime,
    val daysInWeek:List<Int>
)


@Entity(
    tableName = "notifications",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["taskId"],
        childColumns  = ["taskId"],
        onDelete = ForeignKey.NO_ACTION
    )],
    indices = [Index("taskId")]
)
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: String,
    val type: String,
    val msg: String,
    val date: LocalDate,
    val imageUri: Uri? = null,
    val notesTitle: String? = null
)

data class TaskWithFrequency(
    val taskId:String,
    val frequency:String,
    val streak:Int
)

data class TaskDetails(
    val taskName:String,
    val icon:Int,
    val color:Int,
    val streak:Int
)

data class TaskDisplayData(
    val taskId:String,
    val taskName: String,
    val icon: Int,
    val color: Int,
    val streak: Int,
    val isCompleted: Boolean,
    val date: LocalDate
)

data class fullTaskDetails(
    val taskName:String ="Task Name",
    val description:String ="",
    val color:Int =Color.Cyan.toArgb(),
    val icon:Int,
    val frequency:String,
    val streak:Int = 0,
    val maxStreak:Int = 0,
    val isReminder: Boolean = false,
    val time: LocalTime,
    val daysInWeek:List<Int>,
    val taskId:String,
    val isCompleted:Boolean = false
)

data class TaskDetailWithChecklist(
    val task: TaskEntity,
    val checklistList: List<ChecklistEntity>
)

data class NotificationWithNIC(
    val id:Long,
    val taskName:String,
    val taskId: String,
    val type: String,
    val msg: String,
    val date: LocalDate,
    val imageUri: Uri?,
    val notesTitle: String?,
    val icon:Int,
    val color:Int
)

data class HabitReminder(
    val taskId: String,
    val taskName: String,
    val daysInWeek: List<Int>,
    val time : LocalTime
)

data class ChecklistReminder(
    val id: String,
    val task: String,
    val daysInWeek: List<Int>,
    val time : LocalTime
)

object MassageType {
    const val AUTOMATED: String = "Streak"
    const val ACHIEVEMENT: String = "Achieve"
    const val IMAGE: String = "Image"
    const val NOTES: String = "notes"
    const val START:String = "Start"
}

val iconList = listOf(
    R.drawable.badminton,
    R.drawable.bedtime,
    R.drawable.book,
    R.drawable.brush,
    R.drawable.cake,
    R.drawable.callender,
    R.drawable.celebration,
    R.drawable.coding,
    R.drawable.cycling,
    R.drawable.development,
    R.drawable.dining,
    R.drawable.exercise,
    R.drawable.handshake,
    R.drawable.music,
    R.drawable.person_celebrate,
    R.drawable.running,
    R.drawable.self_care,
    R.drawable.self_improvement,
    R.drawable.sports_martial_arts,
    R.drawable.sports_soccer,
    R.drawable.sunny,
    R.drawable.temple,
    R.drawable.trophy,
    R.drawable.water_bottle
)

val achievementList = listOf(
    R.drawable.day1,
    R.drawable.day_7,
    R.drawable.day_14,
    R.drawable.day_30,
    R.drawable.day_50,
    R.drawable.day_100,
    R.drawable.day_200,
    R.drawable.day_365
)





