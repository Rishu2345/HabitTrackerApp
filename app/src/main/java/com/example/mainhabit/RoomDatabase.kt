package com.example.mainhabit

import android.content.Context
import android.net.Uri
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Junction
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

data class DayWithTasks(
    @Embedded val day: DayEntity?,

    // all the TodayTasks for this day
    @Relation(
        parentColumn = "dayId",
        entityColumn = "dayId"
    )
    val todayTasks: List<TodayTasks>,

    // then get all the TaskEntity via the junction
    @Relation(
        parentColumn = "dayId",
        entity       = TaskEntity::class,
        entityColumn = "taskId",
        associateBy  = Junction(
            value = TodayTasks::class,
            parentColumn = "dayId",
            entityColumn = "taskId"
        )
    )
    val tasks: List<TaskEntity>
)


data class TaskWithDetails(
    @Embedded val task: TaskEntity,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val checklistItems: List<ChecklistEntity>,

    @Relation(
        parentColumn = "taskId",
        entityColumn = "taskId"
    )
    val notifications: List<NotificationEntity>
)


@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: DayEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodayTask(todayTasks: TodayTasks)


    @Transaction
    @Query("Select * from days where dayId = :dayId")
    fun getDayWithTasks(dayId: String):Flow<DayWithTasks>

    @Query("Select date from days Order By date desc Limit 1")
    suspend fun getLastDate(): LocalDate?


    @Query("Select * from today_tasks where dayId = :dayId")
    fun getTodayTasks(dayId: String):Flow<List<TodayTasks>>

    @Query("SELECT * FROM days where date <= :date order by date")
    fun getAllDaysTillToday(date: LocalDate = LocalDate.now()): Flow<List<DayEntity>>
    @Query("UPDATE days SET progress = :progress WHERE dayId = :dayId")
    suspend fun setDayProgress(dayId: String, progress: Float)

    @Query("UPDATE tasks SET streak =  streak + 1 WHERE taskId = :taskId")
    suspend fun increaseDayStreak(taskId: String)

    @Query("UPDATE tasks SET streak = 0 WHERE taskId = :taskId")
    suspend fun resetDayStreak(taskId: String)

    @Query("UPDATE tasks SET maxStreak = max(maxStreak, streak) WHERE taskId = :taskId")
    suspend fun updateMaxStreak(taskId: String)

    @Query("UPDATE tasks SET streak =  streak - 1 WHERE taskId = :taskId")
    suspend fun decreaseDayStreak(taskId: String)


    @Query("Select isCompleted from today_tasks where taskId = :taskId and dayId = :dayId")
    fun isTaskCompleted(taskId: String,dayId: String):Flow<Boolean?>

    @Query("""UPDATE tasks
        SET frequency = 
        substr(frequency, 1, instr(frequency, '_') - 1)
        || '_' || :newCompleteCount 
        || '_' || :newWeekOrMonthNumber
        WHERE taskId = :taskId""")
    suspend fun updateFrequency(taskId: String,newCompleteCount:Int,newWeekOrMonthNumber:Int)

    @Query("SELECT taskId, frequency,streak FROM tasks")
    fun getTasksWithFrequency(): Flow<List<TaskWithFrequency>>

    @Query("UPDATE tasks SET frequency = 'Deleted' WHERE taskId = :taskId")
    suspend fun setFrequencyToDeleted(taskId: String)

    @Query("Delete from today_tasks where taskId = :taskId")
    suspend fun deleteTodayTask(taskId: String)

    @Query("Update checklist_items set isCompleted = 1 where taskId = :taskId")
    suspend fun completeAllChecklist(taskId: String)

    @Query("Update checklist_items set isCompleted = 0 where taskId = :taskId")
    suspend fun incompleteAllChecklist(taskId: String)

    @Query("Select maxStreak from tasks where taskId = :taskId")
    suspend fun getMaxStreak(taskId: String):Int

    //Unused



    @Query("SELECT * FROM days WHERE dayId = :dayId")
    fun getDayFlow(dayId: String): Flow<DayEntity?>



    @Query("SELECT taskName, icon, color, streak FROM tasks WHERE taskId = :taskId")
    fun getTaskDetails(taskId: String): Flow<TaskDetails>

    @Query("UPDATE today_tasks SET isCompleted = :isCompleted WHERE taskId = :taskId and dayId = :dayId")
    suspend fun setTaskCompleted(dayId: String,taskId: String, isCompleted: Boolean)


    @Query("""
  SELECT 
    t.taskId            AS taskId,
    t.taskName          AS taskName,
    t.icon              AS icon,
    t.color             AS color,
    t.streak            AS streak,
    tt.isCompleted      AS isCompleted,
    d.date              AS date
  FROM today_tasks tt
    INNER JOIN tasks t  ON tt.taskId = t.taskId
    INNER JOIN days d   ON tt.dayId = d.dayId
  WHERE tt.dayId = :dayId
    """)
    fun getTasksForDayFlow(dayId: String): Flow<List<TaskDisplayData>>




    @Query("Select Count(*) from today_tasks where dayId = :dayId and isCompleted = 1")
    fun completedTask(dayId: String):Flow<Int>



    @Query("""Select t.color, t.icon,t.taskName, n.taskId,n.date,n.id,n.imageUri,n.msg,n.notesTitle,n.type
                      from tasks as t
                      inner join notifications as n
                      on t.taskId = n.taskId
                      Order by n.date desc""")
    fun getAllNotification():Flow<List<NotificationWithNIC>>

    @Query("Select * from notifications where taskId = :taskId Order by date desc")
    fun getNotificationsForTask(taskId:String):Flow<List<NotificationEntity>>

    @Insert
    suspend fun insertNotification(notificationEntity: NotificationEntity)

    @Query("Update notifications set date = :date , msg = :msg ,notesTitle = :notesTitle  where id = :notificationId")
    suspend fun updateNotesNotification(notificationId:Long,date:LocalDate,msg:String,notesTitle:String?)


    @Query("Select * from tasks where taskId = :taskId")
    fun getTaskEntity(taskId: String):Flow<TaskEntity>

    @Query("Delete from notifications where id = :notificationId")
    suspend fun deleteNotification(notificationId: Long)

    @Query("""Select d.date 
        from days as d
        inner join today_tasks as tt 
        on d.dayId = tt.dayId
        where tt.isCompleted = 1 and tt.taskId = :taskId
        """)
    fun dateOfCompletion(taskId: String):Flow<List<LocalDate>>


    @Query("""
    SELECT t.taskId, t.taskName, t.description, t.color, t.icon, t.frequency, 
           t.streak, t.maxStreak, t.isReminder, t.time, t.daysInWeek, 
           CASE WHEN tt.isCompleted IS NULL THEN 0 ELSE tt.isCompleted END as isCompleted
            FROM tasks AS t
            LEFT JOIN today_tasks AS tt ON t.taskId = tt.taskId AND tt.dayId = :dayId AND t.taskId = :taskId
    """)
    fun getFullTaskDetailsFlow(dayId: String,taskId: String): Flow<fullTaskDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChecklistOption(checklistEntity: ChecklistEntity)

    @Query("Delete from checklist_items where id = :checklistId")
    suspend fun deleteChecklist(checklistId: String)


    @Query("Select * from checklist_items where taskId = :taskId")
    fun getChecklistItems(taskId:String):Flow<List<ChecklistEntity>>


    @Transaction
    @Query("Select * from tasks where taskId = :taskId")
    fun getTaskWithDetails(taskId: String):Flow<TaskWithDetails>



    @Query("Update checklist_items Set isCompleted = :isCompleted where id = :checklistId")
    suspend fun  setChecklistCompleted(checklistId:String,isCompleted: Boolean)


    @Query("Select taskId,taskName,daysInWeek,time from tasks where isReminder = 1")
    suspend fun getAllHabitReminders():List<HabitReminder>

    @Query("Select id,task,daysInWeek,time from checklist_items where isReminder = 1")
    suspend fun getAllCheckListReminder():List<ChecklistReminder>

    @Query("SELECT COUNT(*) FROM today_tasks WHERE dayId = :dayId and taskId = :taskId")
    suspend fun isTaskIsAlreadyInTheList(dayId:String,taskId: String): Int



}



@Database(entities = [DayEntity::class,
    TaskEntity::class,
    TodayTasks::class,
    ChecklistEntity::class,
    NotificationEntity::class],
    version = 3)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_tracker_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}



//Converters for room database
class Converters {
    @TypeConverter fun fromLocalDate(d: LocalDate) = d.toEpochDay()
    @TypeConverter fun toLocalDate(v: Long) = LocalDate.ofEpochDay(v)


    @TypeConverter fun fromLocalTime(t: LocalTime) = t.toString()
    @TypeConverter fun toLocalTime(s: String) = LocalTime.parse(s)

    @TypeConverter fun fromUri(u: Uri?) = u?.toString()
    @TypeConverter fun toUri(s: String?) = s?.let(Uri::parse)

    @TypeConverter fun fromIntList(list: List<Int>) = list.joinToString(",")
    @TypeConverter fun toIntList(data: String) = data.split(",").mapNotNull { it.toIntOrNull() }
}