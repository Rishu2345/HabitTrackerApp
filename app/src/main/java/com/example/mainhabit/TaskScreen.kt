package com.example.mainhabit

import android.Manifest
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowOverflow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.mainhabit.ui.theme.customColorList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun TaskScreen(viewModel: MainViewModel ,taskDao: TaskDao,navController: NavHostController){
    Log.d("TaskScreen","Started")
    val context = LocalContext.current
    val taskDetailWithChecklist = viewModel.getTaskToUpdate()
    val scope = rememberCoroutineScope()
    var taskName by remember { mutableStateOf("") }
    var description by remember{ mutableStateOf("") }
    var currColor by remember{ mutableStateOf(customColorList.random()) }
    var colorClicked  by remember{ mutableStateOf(false) }
    var iconClicked by remember{ mutableStateOf(false) }
    var currIcon by remember{ mutableIntStateOf(iconList.random()) }
    var pickedTime by remember{mutableStateOf(LocalTime.NOON)}
    var isReminder by remember{mutableStateOf(false)}
    var dayList by remember{mutableStateOf(listOf(1,2,3,4,5,6,7))}
    var taskId by remember{ mutableStateOf("") }
    val checklistList = remember{ mutableStateListOf<ChecklistEntity>() }
    val deletedChecklistId = remember { mutableStateListOf<String>() }
    val frequencyOption = remember{ mutableStateListOf("Everyday", "Specific days of the week","Specific days in of the months","Some days per period") }
    var selectedFrequencyOption by remember{ mutableStateOf(frequencyOption[0]) }
    val dayMap = remember {
        mutableStateMapOf(
            "Mo" to false,
            "Tu" to false,
            "We" to false,
            "Th" to false,
            "Fr" to false,
            "Sa" to false,
            "Su" to false
        )
    }
    val dayOrder = listOf("Mo","Tu","We","Th","Fr","Sa","Su")
    val monthDayMap = remember {
        val days = YearMonth.now().lengthOfMonth()
        mutableStateMapOf<Int, Boolean>().apply {
            for (day in 1..days) {
                this[day] = false
            }
        }
    }


    val options = remember{ listOf("week", "month") }
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(options[0]) }
    var noOfDays by remember { mutableStateOf("") }
    var isToggle by remember { mutableStateOf(false) }
    var openChecklistPopUp by remember { mutableStateOf(false) }

    var isCheckListMenuVisible by remember { mutableStateOf(false) }
    val checklistBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var changingCheckListEntity by remember { mutableStateOf<ChecklistEntity?>(null) }



    val checklistBoxHeight by animateDpAsState(
        targetValue = if(isToggle) (checklistList.size * 40 + 145).dp else 90.dp,
        animationSpec = tween(300)
    )


    val animateSize by animateDpAsState(
        targetValue = if(colorClicked) 60.dp else 0.dp,
        animationSpec = tween(300)
    )
    val animateIconListSize by animateDpAsState(
        targetValue = if(iconClicked) 130.dp else 0.dp,
        animationSpec = tween(400)
    )

    val animateFreqBoxSize by animateDpAsState(
        targetValue = when(selectedFrequencyOption){
            "Specific days of the week" ->{230.dp}
            "Specific days in of the months" ->{230.dp}
            "Some days per period"->{230.dp}
            else -> {180.dp}

        },
        animationSpec = tween(300)
    )

    //if task is editing store the old data
    LaunchedEffect(taskDetailWithChecklist) {
        taskDetailWithChecklist?.let{
            taskId = it.task.taskId
            taskName = it.task.taskName
            description = it.task.description
            currColor = Color(it.task.color)
            currIcon = iconList[it.task.icon]
            pickedTime = it.task.time
            isReminder = it.task.isReminder
            dayList = it.task.daysInWeek
            checklistList.addAll(it.checklistList)
            isToggle = it.checklistList.isNotEmpty()
            when(it.task.frequency.take(2))
            {
                "EV" ->{
                    selectedFrequencyOption ="Everyday"
                }
                "SW" -> {
                    selectedFrequencyOption = "Specific days of the week"
                    val days = listOf("Mo","Tu","We","Th","Fr","Sa","Su")
                    val daysList = it.task.frequency.substring(2).split(",").map{day -> days[day.toInt() - 1]}
                    daysList.forEach { day ->
                        dayMap[day] = true
                    }
                }
                "SM" -> {
                    selectedFrequencyOption ="Specific days in of the months"
                    val daysList = it.task.frequency.substring(2).split(",").map{day -> day.toInt()}
                    daysList.forEach { day ->
                        monthDayMap[day] = true
                    }

                }
                "NW" -> {
                    selectedFrequencyOption = "Some days per period"
                    noOfDays = it.task.frequency.substring(2).split("_")[0]
                    selectedText = options[0]
                }
                "NM" -> {
                    selectedFrequencyOption = "Some days per period"
                    noOfDays = it.task.frequency.substring(2).split("_")[0]
                    selectedText = options[1]
                }
                else -> ""
            }

        }
    }

    LaunchedEffect(Unit) {
        taskDetailWithChecklist?.let{
            it.checklistList.forEach {it ->
                cancelHabitAlarms(context,it.id,it.daysInWeek)
            }
        }
    }


    //pop screen that is used to add the check list
    if(openChecklistPopUp){
            PopScreenToAddCheckList(checklistEntity=changingCheckListEntity,
                currColor   = currColor,
                viewModel = viewModel,
                taskId = taskId,
                onClick = {openChecklistPopUp = false},
                checklistToAdd = { cl -> checklistList.add(cl) }
            )
        }

    //checklist Option Bottom Sheet
    BottomSheet(
            isVisible = isCheckListMenuVisible,
            imageSheetState = checklistBottomSheetState,
            onDismissRequest = {isCheckListMenuVisible = false},
            firstButtonIcon = R.drawable.edit,
            secondButtonIcon = R.drawable.delete,
            firstButtonTitle = "Edit",
            secondButtonTitle = "Delete",
            onFirstClick = {
                changingCheckListEntity?.let{
                    openChecklistPopUp = true
                }
                changingCheckListEntity = null
                isCheckListMenuVisible = false
            },
            onSecondClick = {
                changingCheckListEntity?.let{
                    if(taskDetailWithChecklist?.checklistList?.contains(it) == true ) deletedChecklistId.add(it.id)
                    checklistList.remove(changingCheckListEntity)
                }
                changingCheckListEntity = null
                isCheckListMenuVisible = false
            },
        )

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ granted ->
        if (granted) {
            scope.launch {
                val habits = taskDao.getAllHabitReminders()
                scheduleReminders(context, habits)
                scheduleChecklistReminders(context,taskDao.getAllCheckListReminder())
            }
        } else {
            Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }

    }

    LaunchedEffect(colorClicked) {
        if(iconClicked){
            iconClicked = false
            delay(320)
        }
    }
    LaunchedEffect(iconClicked) {
        if(colorClicked){
            colorClicked = false
            delay(400)
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFECECEC))
        .safeContentPadding()
        .verticalScroll(rememberScrollState())
    ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.08f)
            .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(imageVector = Icons.Default.ArrowBack,
                contentDescription = "",
                modifier = Modifier
                    .size(25.dp)
                    .clickable { navController.popBackStack() }
            )

            Text(text = "Add Habit",
                fontSize = 24.sp
            )

            Row(modifier = Modifier
                .clickable {
                    if(taskName.isNotEmpty()){
                        val taskEntity = TaskEntity(
                                taskName = taskName,
                                description = description,
                                color = currColor.toArgb(),
                                icon = iconList.indexOf(currIcon),
                                frequency = viewModel.formatFrequency(selectedFrequencyOption,dayMap,monthDayMap,noOfDays,selectedText =="week"),
                                taskId = taskDetailWithChecklist?.task?.taskId
                                    ?: (taskName + LocalDate.now()
                                        .toString() + LocalTime.now().nano.toString()),
                                isReminder = isReminder,
                                daysInWeek = dayList,
                                time = pickedTime
                            )
                        Log.d("frequeny",taskEntity.frequency)
                        scope.launch {
                            taskDao.insertTask(taskEntity)
                            checklistList.forEach {
                                taskDao.insertChecklistOption(it.copy(taskId = taskEntity.taskId))
                            }
                            if(checklistList.any{ !it.isCompleted }){taskDao.setTaskCompleted(LocalDate.now().toString(),taskEntity.taskId,false)}
                            deletedChecklistId.forEach {
                                taskDao.deleteChecklist(it)
                            }

                            if(taskDetailWithChecklist?.task?.isReminder == true){
                                if(taskDetailWithChecklist.task.daysInWeek != dayList ||
                                    taskDetailWithChecklist.task.time != pickedTime){
                                    cancelHabitAlarms(context,taskDetailWithChecklist.task.taskId,taskDetailWithChecklist.task.daysInWeek)
                                    scheduleReminders(context,taskDao.getAllHabitReminders())
                                }
                                if(!isReminder){
                                    cancelHabitAlarms(context,taskDetailWithChecklist.task.taskId,taskDetailWithChecklist.task.daysInWeek)
                                }
                                scheduleChecklistReminders(context,taskDao.getAllCheckListReminder())
                            }else{
                                if(isReminder) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        scheduleReminders(context, taskDao.getAllHabitReminders())
                                        scheduleChecklistReminders(context, taskDao.getAllCheckListReminder())
                                    }
                                }else if(checklistList.isNotEmpty()){
                                    scheduleChecklistReminders(context,taskDao.getAllCheckListReminder())
                                }
                            }
                            if(taskDetailWithChecklist == null){
                                Log.d("TaskScreen","Saving Notification")
                                taskDao.insertNotification(
                                    NotificationEntity(
                                        taskId =taskEntity.taskId,
                                        type = MassageType.START,
                                        msg = "Task Started",
                                        date = LocalDate.now(),
                                        imageUri = null,
                                        notesTitle = null
                                    )
                                )
                                Log.d("TaskScreen","Notification saved")
                            }
                            Log.d("TaskScreen Freq",taskEntity.frequency)
                            if(viewModel.isTodayTask(taskEntity.frequency, LocalDate.now(),taskDao,taskEntity.taskId)){
                                Log.d("TaskScreen","saving in today task")
                                val isInList = taskDao.isTaskIsAlreadyInTheList(dayId = LocalDate.now().toString(), taskId =taskEntity.taskId)
                                Log.d("TaskScreen",isInList.toString())
                                Log.d("Ids" ,taskEntity.taskId)
                                if(isInList == 0){
                                    taskDao.insertTodayTask(
                                        TodayTasks(
                                            dayId = LocalDate.now().toString(),
                                            taskId =taskEntity.taskId
                                        )
                                    )
                                }
                                Log.d("TaskScreen","Saved in today task")
                            }
                        }
                        navController.popBackStack()
                    }
                }
            ){
                Text(text = "Save ",
                    fontSize = 18.sp
                )
                Icon(imageVector = Icons.Default.Check,
                    contentDescription = "Save Button",
                    modifier = Modifier
                )
            }
        }

        OutlinedTextField(value = taskName,
            onValueChange = {taskName = it},
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedPlaceholderColor = Color.Gray,
                unfocusedPlaceholderColor = Color.Gray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            placeholder = { Text("Enter Habit Title",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            ) },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            textStyle = TextStyle(
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        )

        OutlinedTextField(value = description,
            onValueChange = {description = it},
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(10.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(10.dp)),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedPlaceholderColor = Color.Gray,
                unfocusedPlaceholderColor = Color.Gray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            placeholder = { Text("Enter Description (optional)",
                fontSize = 15.sp,
                letterSpacing = 1.5.sp
            ) }
        )

        Row(modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(10.dp)

        ){

            Row(modifier = Modifier
                .fillMaxWidth(0.5f)
                .clickable { colorClicked = !colorClicked },
                verticalAlignment = Alignment.CenterVertically
            ){
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(currColor)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.size(20.dp,10.dp))
                Text(text = "Color")
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .clickable { iconClicked = !iconClicked },
                verticalAlignment = Alignment.CenterVertically){
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    Icon(
                        painter = painterResource(currIcon),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center),
                        tint = Color.Black
                    )

                }
                Spacer(modifier = Modifier.size(20.dp,10.dp))
                Text(text = "Icon")
            }
        }
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(animateSize)
            .padding(10.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White)
            .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ){
            for(color in customColorList){
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(color)
                        .clickable {
                            currColor = color
                            colorClicked = false

                        }
                )
                Spacer(modifier = Modifier
                    .fillMaxHeight()
                    .width(10.dp))
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(animateIconListSize)
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White),
            maxItemsInEachRow = 9,
            maxLines = 5,
            overflow = FlowRowOverflow.Clip
        ) {
            repeat(iconList.size) { index ->
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            currIcon = iconList[index]
                            iconClicked = false
                        }
                        .padding(horizontal = 5.dp)
                ) {
                    Icon(
                        painter = painterResource(iconList[index]),
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center),
                        tint = Color.Black
                    )
                }
            }
        }

        Column(modifier = Modifier
            .fillMaxWidth()
            .height(animateFreqBoxSize)
            .padding(horizontal = 10.dp)
            .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
            .clip(RoundedCornerShape(15.dp))
            .background(Color.White))
        {

            Text(text = "Frequency",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp, 10.dp, 10.dp),
                fontWeight = FontWeight.SemiBold,
            )

            frequencyOption.forEach{ option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, 2.dp, 2.dp, 5.dp)
                        .height(25.dp)
                ) {
                    RadioButton(
                        selected = (selectedFrequencyOption == option),
                        onClick = { selectedFrequencyOption = option },
                        colors = RadioButtonDefaults.colors(selectedColor = currColor)
                    )
                    Text(
                        text = option,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                }
                when(option){
                    "Specific days of the week"  ->{
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .height(if (option == selectedFrequencyOption) 50.dp else 0.dp)
                            .padding(horizontal = 10.dp)
                            .clipToBounds(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ){
                            dayOrder.forEach { key ->
                                Box(modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (dayMap[key] == true) currColor else Color(0x6A4D4B4B),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = (option == selectedFrequencyOption)) {
                                        dayMap[key] = !dayMap[key]!!
                                    }
                                ){
                                    Text(text = key,
                                        modifier = Modifier
                                            .align(Alignment.Center),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    "Specific days in of the months" ->{
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .height(if (option == selectedFrequencyOption) 50.dp else 0.dp)
                            .padding(horizontal = 10.dp)
                            .clipToBounds()
                            .horizontalScroll(rememberScrollState()),
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            monthDayMap.forEach { (date,isSelected) ->
                                Box(modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isSelected) currColor else Color(0x6A4D4B4B),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable(enabled = option == selectedFrequencyOption) {
                                        monthDayMap[date] = !isSelected
                                    }
                                ){
                                    Text(text = date.toString(),
                                        modifier = Modifier
                                            .align(Alignment.Center),
                                        color = Color.White
                                    )

                                }
                                Spacer(modifier = Modifier.size(10.dp,40.dp))
                            }
                        }
                    }
                    "Some days per period"->{
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .height(if (option == selectedFrequencyOption) 50.dp else 0.dp)
                            .padding(horizontal = 10.dp)
                            .clipToBounds(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ){
                            TextField(value = noOfDays,
                                onValueChange = {noOfDays = it},
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(50.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = currColor
                                ),
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                enabled = (option == selectedFrequencyOption)
                            )
                            Text(text = "days per",
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                            )
                            ExposedDropdownMenuBox(
                                expanded = expanded,
                                onExpandedChange = { expanded = !expanded }
                            ) {
                                TextField(
                                    value = selectedText,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor() // Required for alignment
                                        .fillMaxWidth(0.6f)
                                        .height(50.dp)
                                        .padding(0.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedIndicatorColor = currColor
                                    ),
                                    enabled = (option == selectedFrequencyOption)
                                )

                                ExposedDropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false }
                                ) {
                                    options.forEach { item ->
                                        DropdownMenuItem(
                                            text = { Text(item) },
                                            onClick = {
                                                selectedText = item
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {}

                }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(checklistBoxHeight)
                .padding(horizontal = 10.dp, vertical = 10.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
                .clip(RoundedCornerShape(15.dp))
                .background(Color.White)
                .padding(horizontal = 15.dp),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment  = Alignment.CenterHorizontally
        )
        {

            Row(modifier = Modifier
                .fillMaxWidth()
                .size(70.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                Text(
                    text = "Checklist",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Switch(
                    checked = isToggle,
                    onCheckedChange = { isToggle = it },
                    modifier = Modifier
                        .scale(0.9f),
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = currColor.copy(0.3f),
                        checkedThumbColor = currColor,
                        checkedBorderColor = currColor.copy(0.3f),
                        uncheckedThumbColor = Color.Black,
                        uncheckedTrackColor = Color.White,
                        uncheckedBorderColor = Color.Black
                    )
                )
            }
            checklistList.forEach { cl ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 5.dp)
                    .height(30.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(text = cl.task,
                        fontSize = 15.sp
                    )

                    Icon(imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterVertically)
                            .graphicsLayer()
                            .clickable {
                                changingCheckListEntity = cl
                                isCheckListMenuVisible = true
                                scope.launch {
                                    checklistBottomSheetState.show()
                                }
                            }
                    )
                }

            }
            Text(text = "Add Checklist +",
                modifier = Modifier
                    .clickable {
                        openChecklistPopUp = true
                    }
                    .padding(bottom = 10.dp),
                fontSize = 17.sp,
                color = currColor,
            )
        }
        SetReminderBox(
            viewModel = viewModel,
            modifier = Modifier,
            color = currColor,
            time = taskDetailWithChecklist?.task?.time ?: LocalTime.NOON,
            isReminder = taskDetailWithChecklist?.task?.isReminder ?: false,
            list = taskDetailWithChecklist?.task?.daysInWeek ?: dayList
        ){
            time,reminder,list ->
            pickedTime = time
            isReminder = reminder
            dayList = list
        }
    }
}


@Composable
fun PopScreenToAddCheckList(checklistEntity: ChecklistEntity? = null,currColor: Color,viewModel: MainViewModel,taskId: String,onClick:() -> Unit,checklistToAdd:(ChecklistEntity)->Unit){
    var checklistTitle by remember { mutableStateOf("") }
    var isCLReminder by remember { mutableStateOf(false) }
    var checklistPickedTime by remember { mutableStateOf(LocalTime.now()) }
    var daysOfWeek by remember { mutableStateOf(listOf(1,2,3,4,5,6,7)) }
    LaunchedEffect(checklistEntity) {
        checklistEntity?.let{
            checklistTitle = it.task
            isCLReminder = it.isReminder
            checklistPickedTime = it.time
            daysOfWeek = it.daysInWeek
        }
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .background(Color.Black.copy(0.8f))
        .clickable(onClick = onClick)
        .zIndex(1f)){
        Column(modifier = Modifier
            .fillMaxWidth(0.8f)
            .background(Color(0xFFECECEC), RoundedCornerShape(15.dp))
            .padding(vertical = 15.dp)
            .clip(RoundedCornerShape(15.dp))
            .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Add Checklist",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
            )
            OutlinedTextField(value = checklistTitle,
                onValueChange = {checklistTitle = it},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .border(1.dp, Color.Black, RoundedCornerShape(10.dp)),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                placeholder = { Text("Enter Checklist Title",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                ) },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )

            )

            SetReminderBox(
                viewModel = viewModel, modifier = Modifier, color = currColor,time = checklistPickedTime,
                isReminder = isCLReminder,list = daysOfWeek
            ){ time, reminder, list ->
                checklistPickedTime = time
                isCLReminder = reminder
                daysOfWeek = list
            }

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
                horizontalArrangement = Arrangement.End
            ){
                TextButton(onClick = onClick,
                    modifier = Modifier) {
                    Text(text = "Cancel",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                            checklistToAdd(
                                ChecklistEntity(
                                    id = checklistEntity?.id ?: (checklistTitle + LocalTime.now().nano.toString()),
                                    taskId = taskId,
                                    isReminder = isCLReminder,
                                    isCompleted = false,
                                    task = checklistTitle,
                                    time = checklistPickedTime,
                                    daysInWeek = daysOfWeek
                                )
                            )
                            onClick()
                    },
                    modifier = Modifier,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currColor,
                        contentColor = Color.Black
                    )
                ){
                    Text(text = "Add",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

}
