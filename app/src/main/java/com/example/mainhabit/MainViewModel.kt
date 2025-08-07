package com.example.mainhabit

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mainhabit.ui.theme.primary_Light_Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import androidx.core.content.edit

class MainViewModel: ViewModel(){


    private var _isReady = MutableStateFlow(false)
    var isReady = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            delay(2000)
            _isReady.value = true
        }
    }

    private var taskId: String? = null
    private var taskToUpdate: TaskDetailWithChecklist? = null
    private var notificationToUpdate: NotificationEntity? = null
    private var color: Color = primary_Light_Color
    private var viewingImageUri: Uri? = null

    private var taskDao: TaskDao? = null



    fun setTaskDao(td: TaskDao){
        Log.d("View Model","Setting the task Dao")
        taskDao = td
    }
    fun getTaskDao(): TaskDao?{
        return taskDao
    }

    fun setTaskId(id:String){
        taskId = id
    }

    fun getTaskId():String?{
        return taskId
    }

    fun setTaskToUpdate(taskDetailWithChecklist: TaskDetailWithChecklist?){
        taskToUpdate = taskDetailWithChecklist

    }
    fun getTaskToUpdate():TaskDetailWithChecklist?{
        return taskToUpdate
    }

    fun setNotificationToUpdate(notificationEntity: NotificationEntity?){
        notificationToUpdate = notificationEntity
    }
    fun getNotificationToUpdate():NotificationEntity?{
        return notificationToUpdate
    }

    fun setColor(colour:Color){
        color = colour
    }
    fun getColor():Color{
        return color
    }

    fun setViewingImageUri(uri: Uri){
        viewingImageUri = uri
    }
    fun getViewingImageUri():Uri?{
        return viewingImageUri
    }


    fun isTodayTask(frequency: String , currentLocalDate: LocalDate = LocalDate.now(),taskDao: TaskDao,taskId: String):Boolean{
        return when(frequency.take(2)){
            "EV" -> true
//            form = "SW1,2,3,4,5,6,7"
            "SW" -> {
                val daysInWeek = listOf("MO","TU","WE","TH","FR","SA","SU")
                val daysList = frequency.substring(2).split(",").map{index -> daysInWeek[index.toInt()-1]}
                daysList.contains(currentLocalDate.dayOfWeek.toString().take(2))
            }
//            form = "SM1,2,3,4,5,6,7" represent specific days in month
            "SM" -> {
                frequency.substring(2).split(",").contains(currentLocalDate.dayOfMonth.toString())
            }
//            "NW5_0_12" represent 5 of day per week in which 0 is completed on the 12th week of the year
            "NW" -> {
                val numbers = frequency.substring(2).split("_").map{it.toInt()}
                val weekFields = WeekFields.of(Locale.getDefault())
                val weekOfYear = LocalDate.now().get(weekFields.weekOfWeekBasedYear())
                if(numbers[2] == weekOfYear)
                  (numbers[0] > numbers[1])
                else {
                    viewModelScope.launch(Dispatchers.IO) {
                        taskDao.updateFrequency(taskId,0, weekOfYear)
                    }
                    true
                }
            }
            "NM" -> {
                val numbers = frequency.substring(2).split("_").map { it.toInt() }
                if(numbers[2] == LocalDate.now().monthValue)
                    (numbers[0] > numbers[1])
                else {
                    viewModelScope.launch(Dispatchers.IO) {
                        taskDao.updateFrequency(taskId,0, LocalDate.now().monthValue)
                    }
                    true
                }
            }
            else -> false
        }
    }

    fun loadDateWithTask(date: LocalDate,taskDao: TaskDao){
        val currentDayId = date.toString()
        Log.d("loading data",currentDayId)
        viewModelScope.launch(Dispatchers.IO){
            insertAbsentDayEntityTillDate(taskDao = taskDao)
            val dayEntity = taskDao.getDayFlow(date.toString()).firstOrNull()
            val tasksWithFrequency = taskDao.getTasksWithFrequency().first()
            if (dayEntity == null) {
                Log.d("LoadTaskForTheDate","Loading day its task")
                val newDay = DayEntity(currentDayId, date)
                taskDao.insertDay(newDay)
                tasksWithFrequency.forEach {
                    Log.d("Selecting today tasks", it.frequency)
                    sendAchievementUnlockedNotification(it.taskId,taskDao)
                    if(isTodayTask(it.frequency,date,taskDao,it.taskId)){
                        taskDao.insertTodayTask(TodayTasks(taskId = it.taskId, dayId = currentDayId, isCompleted = false))
                        Log.d("LoadTaskForTheDate",it.toString())
                    }
                }
                Log.d("LoadTaskForTheDate","Loaded day its task")

            } else{
                val alreadyListedTaskIds = taskDao.getTodayTasks(date.toString()).first().map{it.taskId}
                val unlistedTask = tasksWithFrequency.filter{!alreadyListedTaskIds.contains(it.taskId)}
                unlistedTask.forEach {
                    if(isTodayTask(it.frequency,date,taskDao,it.taskId)){
                        taskDao.insertTodayTask(TodayTasks(taskId = it.taskId, dayId = currentDayId))
                    }
                }
            }
            Log.d("LoadTaskForTheDate",dayEntity.toString())
        }
    }

    suspend fun getListOfDays(taskDao: TaskDao):List<DayEntity>{
        val dayList =viewModelScope.async(Dispatchers.IO){
            val daysTillToday = taskDao.getAllDaysTillToday().first()
            val afterToday = generateDateAheadToday(10)
            return@async daysTillToday + afterToday
        }
        return dayList.await()
    }


    suspend fun insertAbsentDayEntityTillDate(date: LocalDate = LocalDate.now(),taskDao: TaskDao){
        val lastDate = taskDao.getLastDate()
        if (lastDate == null) return
        if(lastDate.plusDays(1).isEqual(date)) return
        var addingDate = lastDate.plusDays(1)
        while(addingDate.isBefore(date)){
            val newDay = DayEntity(addingDate.toString(),addingDate)
            taskDao.insertDay(newDay)
            addingDate = addingDate.plusDays(1)
        }
    }


    fun updateProgress(taskDao: TaskDao,dayId:String){
        viewModelScope.launch(Dispatchers.IO){
            taskDao.getTodayTasks(dayId).collect{ list ->
                val completed = list.count{ it.isCompleted }
                val sizeOfTaskList = list.size
                if(sizeOfTaskList == 0){
                    val progress = 0f
                    taskDao.setDayProgress(dayId,progress)
                }else{
                    val progress = completed.toFloat()/sizeOfTaskList
                    taskDao.setDayProgress(dayId,progress)
                }

            }
        }
    }
//    val taskProgress = taskDao.completedTask(dayId)
//        .map { completed ->
//            if (sizeOfTaskList == 0) 1f
//            else completed.toFloat() / sizeOfTaskList
//        }
//        .onEach { progress ->
//            taskDao.setDayProgress(dayId, progress)
//        }
//        .launchIn(viewModelScope)


//    suspend fun updateTaskProgress(dayId: String, taskId: Int,sizeOfTaskList:Int){
//        val completedTask = taskDao.completedTask(dayId).collect{ completed ->
//            val progress = if(completed == sizeOfTaskList || sizeOfTaskList == 0)  1f
//            else completed.toFloat()/sizeOfTaskList.toFloat()
//            taskDao.setDayProgress(dayId,progress)
//        }
//
//
//    }

    var currentLocalDate:LocalDate = LocalDate.now()

    val localDate = LocalDate.now()

    fun isToday(date: LocalDate):Boolean{
        return date.isEqual(LocalDate.now())
    }

    fun dayLabelToIndex(label: String) = when(label) {
        "Mo" -> 1
        "Tu" -> 2
        "We" -> 3
        "Th" -> 4
        "Fr" -> 5
        "Sa" -> 6
        "Su" -> 7
        else -> throw IllegalArgumentException("Unknown day: $label")
    }

    //Check this is messed up 
    fun generateDateAheadToday(range:Int = 30):List<DayEntity>{
        val current = LocalDate.now()
        return (1..range).map{
            val cur = current.plusDays(it.toLong())
            DayEntity(dayId =cur.toString(), date = cur)
        }
    }

    fun formatDateToDayWithSuffix(date: LocalDate = LocalDate.now()): String {
        val day = date.dayOfMonth
        val suffix = getDaySuffix(day)
        val month = date.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH))
        return "$day$suffix $month"
    }

    private fun getDaySuffix(day: Int): String {
        return if (day in 11..13) {
            "th"
        } else {
            when (day % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }
    }


    fun transformRepetitionString(rep:String):String{
        return when(rep.take(2)){
            "SW" -> "On" + convertIndexToDate(rep.substring(2))
            "SM" -> "On" + rep.substring(2)
            "NW" -> rep.substring(2,rep.indexOf('_')) + " days per week"
            "NM" -> rep.substring(2,rep.indexOf('_')) + " days per month"
            else  -> "Everyday"
        }
    }

    private fun convertIndexToDate(indices :String ):String{
        val days = listOf("Mo","Tu","We","Th","Fr","Sa","Su")
        return indices.split(",").joinToString(",") { days[it.toInt()] }
    }


    fun achievementLevel(streak:Int):Int?{
        return when(streak){
            in 1 until 7 -> 0
            in 7 until 14 -> 1
            in 14 until 30 -> 2
            in 30 until 50 -> 3
            in 50 until 100 -> 4
            in 100 until 200 -> 5
            in 200 until 365 -> 6
            in 365..Int.MAX_VALUE -> 7
            else -> null
        }
    }


    fun sendAchievementUnlockedNotification(taskId:String, taskDao: TaskDao){
        viewModelScope.launch (Dispatchers.IO){
            val streak = taskDao.getMaxStreak(taskId)
            if(streak in listOf(1,7,14,30,50,100,200,365)){
                taskDao.insertNotification(NotificationEntity(
                    taskId = taskId,
                    type = MassageType.ACHIEVEMENT,
                    msg = "Unlocked $streak day streak",
                    date = LocalDate.now()
                ))
            }
        }
    }


    fun formatFrequency(frequencyOption: String,dayMap:Map<String,Boolean>,monthMap:Map<Int,Boolean>,noOfDays:String,isWeek:Boolean):String{
        val dayList = dayMapToDayList(dayMap)
        val monthList = monthMapToMonthList(monthMap)
        var frequency = ""
        val weekFields = WeekFields.of(Locale.getDefault())
        val weekOfYear = LocalDate.now().get(weekFields.weekOfWeekBasedYear())
        frequency = when(frequencyOption){
            "Some days per period" ->{
                if(isWeek){
                    "NW"+noOfDays+"_0_$weekOfYear"
                } else
                    "NM"+noOfDays+"_0_${LocalDate.now().monthValue}"
            }

            "Specific days in of the months"->{
                "SM$monthList"
            }

            "Specific days of the week"->{
                "SW$dayList"
            }

            else -> "EV"
        }
        return frequency
    }

    fun dayMapToDayList(dayMap:Map<String,Boolean>):String{
        val indexList = listOf("Mo","Tu","We","Th","Fr","Sa","Su")
        return dayMap.keys.filter{ dayMap[it] == true }.map{index -> (indexList.indexOf(index)+1) }.joinToString(",")
    }

    fun monthMapToMonthList(monthMap:Map<Int,Boolean>):String{
        return monthMap.keys.filter{ monthMap[it] == true }.joinToString(",")
    }


    fun resetBrokenStreaks(taskDao: TaskDao){
        viewModelScope.launch(Dispatchers.IO) {
            val today = LocalDate.now()
            val taskWithFrequency = taskDao.getTasksWithFrequency().first()
            taskWithFrequency.forEach {
                if(it.streak == 0)
                    return@forEach
                taskDao.updateMaxStreak(it.taskId)
                when(it.frequency.take(2)){
                    "EV" -> {
                        taskDao.isTaskCompleted(
                            taskId=it.taskId,
                            dayId = today.minusDays(1).toString())
                            .first()?.let { it1 ->
                                if(!it1){
                                    taskDao.resetDayStreak(it.taskId)
                                }
                            }
                    }

                    "SW" ->{
                        val dayList = it.frequency.substring(2).split(",").map{day -> day.toInt()}.sorted()
                        for(day in dayList) {
                            if (today.dayOfWeek.value > day) {
                                taskDao.isTaskCompleted(
                                    taskId = it.taskId,
                                    dayId = today.minusDays((today.dayOfWeek.value - day).toLong()).toString()
                                )
                                    .first()?.let { it1 ->
                                        if (!it1
                                        ) {
                                            taskDao.resetDayStreak(it.taskId)
                                            break
                                        }
                                    }
                            }
                        }
                    }

                    "SM" -> {
                        val dayList = it.frequency.substring(2).split(",").map{day -> day.toInt()}.sorted()
                        for(day in dayList) {
                            if (today.dayOfMonth > day) {
                                taskDao.isTaskCompleted(
                                    taskId = it.taskId,
                                    dayId = today.minusDays((today.dayOfMonth - day).toLong()).toString()
                                )
                                    .first()?.let { it1 ->
                                        if (!it1
                                        ) {
                                            taskDao.resetDayStreak(it.taskId)
                                            break
                                        }
                                    }
                            }
                        }
                    }

                    "NW" -> {
                        val numbers = it.frequency.substring(2).split("_").map{it.toInt()}
                        val weekFields = WeekFields.of(Locale.getDefault())
                        val weekOfYear = today.get(weekFields.weekOfWeekBasedYear())
                        if(numbers[2] == weekOfYear -1){
                            if(numbers[0] > numbers[1]){
                                taskDao.resetDayStreak(it.taskId)
                            }
                        }else{
                            taskDao.resetDayStreak(it.taskId)
                            taskDao.updateFrequency(it.taskId,0, weekOfYear)
                        }
                    }
                    "NM" -> {
                        val numbers = it.frequency.substring(2).split("_").map{it.toInt()}
                        if(numbers[2] == today.monthValue - 1){
                            if(numbers[0] > numbers[1]){
                                taskDao.resetDayStreak(it.taskId)
                            }
                        }else{
                            taskDao.resetDayStreak(it.taskId)
                            taskDao.updateFrequency(it.taskId,0, today.monthValue)
                        }

                    }
                }
            }
        }
    }







}

object PrefsHelper {
    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun hasSeenWelcome(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HAS_SEEN_WELCOME, false)

    fun setSeenWelcome(context: Context) {
        prefs(context).edit {
            putBoolean(KEY_HAS_SEEN_WELCOME, true)
        }
    }
}