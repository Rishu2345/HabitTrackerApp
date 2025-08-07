package com.example.mainhabit

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.collections.component1
import kotlin.collections.component2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(viewModel: MainViewModel,navController: NavHostController,taskDao: TaskDao){
    val taskId = viewModel.getTaskId() ?: run {
        Log.d("Task Screen", "TaskId was null")
        navController.navigate(Home)
        return
    }
    Log.d("TaskDetailScreen",taskId)
    val insets = WindowInsets.systemBars
    val topPadding = insets.asPaddingValues().calculateTopPadding()
    val context = LocalContext.current
    val deviceHeight = LocalConfiguration.current.screenHeightDp
    val totalPadding = remember { topPadding + 40.dp }
    val totalPaddingInPx = with(LocalDensity.current){(totalPadding - 1.dp).roundToPx().toFloat()}
    val scope = rememberCoroutineScope()
    val imageSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showImageSheet by remember { mutableStateOf(false) }
    var currentOffset by remember { mutableStateOf(0f) }
    var secondColumnOffset by remember { mutableStateOf(0f) }
    val extendHeight = with(LocalDensity.current){secondColumnOffset.toDp()}
    val decreaseInPadding = with(LocalDensity.current){currentOffset.toDp()}
    val maxTopColumnHeightPx = with(LocalDensity.current) { (deviceHeight / 2f).dp.toPx() + topPadding.toPx() }
    val whiteOverlay by remember(decreaseInPadding, extendHeight, maxTopColumnHeightPx) {
        mutableStateOf(
            1f - (maxTopColumnHeightPx + (secondColumnOffset + currentOffset)) / maxTopColumnHeightPx
        )
    }
    var notificationId by remember{ mutableStateOf<Long?>(null) }
    var imageUri by remember{mutableStateOf<Uri?>(null)}


    var isImageNotificationBottomSheetVisible by remember { mutableStateOf(false) }
    var isNotesNotificationBottomSheetVisible by remember { mutableStateOf(false) }
    var isAchievementBottomSheetVisible by remember { mutableStateOf(false) }
    var isCalenderBottomSheetVisible by remember { mutableStateOf(false) }
    var isCheckListBottomSheetVisible by remember { mutableStateOf(false) }
    var isTaskBottomSheetVisible by remember { mutableStateOf(false) }

    val imageNotificationOptionSheet = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val notesNotificationOption = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val achievementBottomSheet = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val calenderBottomSheet = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val checkListBottomSheet = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val taskBottomSheet = rememberModalBottomSheetState(skipPartiallyExpanded = false)



    //fetching task details
    val taskWithDetail by taskDao.getTaskWithDetails(taskId).collectAsState(
        initial = TaskWithDetails(
            task = TaskEntity(
                taskId = "",
                taskName = "",
                description = "",
                color = Color(0xFF8E97FD).toArgb(),
                icon = iconList.indexOf(R.drawable.badminton),
                frequency = "",
                streak = 0,
                maxStreak = 0,
                isReminder = false,
                time = LocalTime.NOON,
                daysInWeek = listOf()
            ),
            checklistItems = emptyList(),
            notifications = emptyList()
        )
    )
    val isTaskCompleted = taskDao.isTaskCompleted(taskId,LocalDate.now().toString()).collectAsState(initial = false).value ?: false

    val listOfIncompletedChecklist = taskWithDetail.checklistItems.count { !it.isCompleted }

    LaunchedEffect(listOfIncompletedChecklist) {
        if(listOfIncompletedChecklist == 0 && taskWithDetail.checklistItems.isNotEmpty()){
            taskDao.setTaskCompleted(LocalDate.now().toString(),taskId,true)
        }
    }


    val dateList by taskDao.dateOfCompletion(taskWithDetail.task.taskId).collectAsState(initial = emptyList())
    val unlockedAchievements by remember{
        mutableStateOf(
            viewModel.achievementLevel(taskWithDetail.task.maxStreak)?.let {
                (0..it).toList()
            } ?: emptyList()
        )
    }

    //fetching notification
    Log.d("TaskDetailScreen",taskWithDetail.notifications.toString())
    //Image Picker
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ){uri : Uri? ->
        uri?.let {
            // take persistable permission
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            // save into Room
            scope.launch {
                taskDao.insertNotification(
                    NotificationEntity(
                        taskId = taskId,
                        type = MassageType.IMAGE,
                        msg = "",
                        date = LocalDate.now(),
                        imageUri = it,
                        notesTitle = null
                    )
                )
            }
        }
    }

    val tempImageUri = remember{
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "temp_${LocalTime.now().nano}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        }
        context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
    }

    val takePhoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ){success :Boolean ->
        if(success){
            scope.launch {
                taskDao.insertNotification(
                    NotificationEntity(
                        taskId = taskId,
                        type = MassageType.IMAGE,
                        msg = "",
                        date = LocalDate.now(),
                        imageUri = tempImageUri,
                        notesTitle = null
                    )
                )
            }
        }

    }


    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            tempImageUri?.let{
                takePhoto.launch(it)
            }
        } else {
            Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    val permLauncher2 = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickImage.launch(arrayOf("image/*"))
        } else {
            Toast.makeText(context, "Permission Denied!", Toast.LENGTH_SHORT).show()
        }
    }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }



    //Image Notification Options Bottom Sheet
    BottomSheet(
        isVisible = isImageNotificationBottomSheetVisible,
        onDismissRequest ={ isImageNotificationBottomSheetVisible = false },
        imageSheetState = imageNotificationOptionSheet,
        firstButtonTitle = "View",
        secondButtonTitle = "Delete",
        firstButtonIcon = R.drawable.photo,
        secondButtonIcon = R.drawable.delete,
        onFirstClick = {
            isImageNotificationBottomSheetVisible = false
            imageUri?.let {
                viewModel.setViewingImageUri(it)
            }
            navController.navigate(imageScreen)
        },
        onSecondClick = {
            scope.launch {
                notificationId?.let{ id->
                    taskDao.deleteNotification(id)
                }
            }
            isImageNotificationBottomSheetVisible = false
        }

    )

    //Notes Notification Options Bottom Sheet
    BottomSheet(
        isVisible = isNotesNotificationBottomSheetVisible,
        onDismissRequest ={ isNotesNotificationBottomSheetVisible = false },
        imageSheetState = notesNotificationOption,
        firstButtonTitle = "Edit",
        secondButtonTitle = "Delete",
        firstButtonIcon = R.drawable.edit,
        secondButtonIcon = R.drawable.delete,
        onFirstClick = {
            isNotesNotificationBottomSheetVisible = false
            viewModel.setNotificationToUpdate(taskWithDetail.notifications.first{it.id == notificationId})
            viewModel.setColor(Color(taskWithDetail.task.color))
            navController.navigate(NotesScreen)
        },
        onSecondClick = {
            scope.launch {
                notificationId?.let{ id->
                    taskDao.deleteNotification(id)
                }
            }
            isNotesNotificationBottomSheetVisible = false
        }
    )

    //Achievement Bottom Sheet
    if(isAchievementBottomSheetVisible){
        ModalBottomSheet(
            onDismissRequest = { isAchievementBottomSheetVisible = false },
            sheetState = achievementBottomSheet,
            modifier = Modifier
                .fillMaxWidth(),
            dragHandle = { Icon(painterResource(R.drawable.horizontal_rule), null) },
            containerColor =  Color.White,
            scrimColor = Color.Black.copy(0.2f)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Text(
                    text = "Achievements",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Take Photo
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    maxItemsInEachRow = 4,
                    maxLines = 2
                ) {
                    achievementList.forEach { image ->
                        val unlocked = remember{unlockedAchievements.map{achievementList[it]}}
                        Box(modifier = Modifier.
                            size(90.dp)
                        )
                        {
                            Image(painter = painterResource(image),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(100.dp)
                                    .align(Alignment.Center)
                                    .drawWithContent {
                                        drawContent()

                                        drawRect(
                                            color = Color.White.copy(
                                                alpha = if (unlocked.contains(
                                                        image
                                                    )
                                                ) 0.0f else 0.9f
                                            ),
                                            size = size
                                        )
                                    }
                                    .graphicsLayer(
                                        scaleX = if (unlocked.contains(image)) 1.2f else 1f,
                                        scaleY = if (unlocked.contains(image)) 1.2f else 1f
                                    )
                            )

                            if(!unlocked.contains(image)){
                                Icon(imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                )
                            }

                        }

                    }
                }
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp))

            }
        }
    }

    //Calender Bottom Sheet
    if(isCalenderBottomSheetVisible){
        ModalBottomSheet(
            onDismissRequest = { isCalenderBottomSheetVisible = false },
            sheetState = calenderBottomSheet,
            modifier = Modifier
                .fillMaxWidth(),
            dragHandle = { Icon(painterResource(R.drawable.horizontal_rule), null) },
            containerColor =  Color.White,
            scrimColor = Color.Black.copy(0.2f)
        ){
            CustomCalender(
                markedDates = dateList,
                color = Color(taskWithDetail.task.color)
            )
            Spacer(modifier = Modifier
                .fillMaxWidth()
                .height(60.dp))
        }
    }

    //Image Bottom Sheet
    if(showImageSheet){
        ModalBottomSheet(
            onDismissRequest = { showImageSheet = false },
            modifier = Modifier
                .fillMaxWidth(),
            sheetState = imageSheetState,
            dragHandle = { Icon(painterResource(R.drawable.horizontal_rule), null) },
            containerColor =  Color(0xFFF5F5F5),
            scrimColor = Color.Black.copy(0.2f)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Text(
                    text = "Options",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Take Photo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .clickable {
                            permLauncher.launch(Manifest.permission.CAMERA)
                            showImageSheet = false
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.photo_camera),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Take Photo", fontWeight = FontWeight.Medium)
                }

                // From Gallery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .clickable {
                            permLauncher2.launch(permission)
                            showImageSheet = false
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.photo_library),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("From Gallery", fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp))

            }
        }
    }

    //CheckList Bottom Sheet
    if(isCheckListBottomSheetVisible){
        ModalBottomSheet(
            onDismissRequest = {isCheckListBottomSheetVisible = false},
            sheetState =  checkListBottomSheet,
            modifier = Modifier
                .fillMaxWidth(),
            dragHandle = { Icon(painterResource(R.drawable.horizontal_rule), null) },
            containerColor =  Color.White,
            scrimColor = Color.Black.copy(0.2f)
        ){
            Column(modifier = Modifier
                .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Text(
                    text = "Check List",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                for(cl in taskWithDetail.checklistItems){
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    )
                    {
                        Box(modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(0.4f), CircleShape)
                        ){
                            Text(text = (taskWithDetail.checklistItems.indexOf(cl) + 1).toString(),
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = cl.task,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .fillMaxWidth(0.7f),
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Color(taskWithDetail.task.color).copy(if (cl.isCompleted) 1f else 0.4f),
                                CircleShape
                            )
                            .clickable {
                                scope.launch {
                                    taskDao.setChecklistCompleted(cl.id, !cl.isCompleted)
                                    if(cl.isCompleted){
                                        taskDao.setTaskCompleted(LocalDate.now().toString(),taskId,false)
                                    }
                                }
                            }

                        ){
                            Icon(imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.Center)
                            )
                        }

                    }
                }
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp))

            }
        }
    }

    //Task Option Bottom Sheet
    BottomSheet(
        isVisible = isTaskBottomSheetVisible,
        onDismissRequest ={ isTaskBottomSheetVisible = false },
        imageSheetState = taskBottomSheet,
        firstButtonTitle = "Edit",
        secondButtonTitle = "Delete",
        firstButtonIcon = R.drawable.edit,
        secondButtonIcon = R.drawable.delete,
        onFirstClick = {
            isTaskBottomSheetVisible = false
            viewModel.setTaskToUpdate(
                TaskDetailWithChecklist(
                    task = taskWithDetail.task,
                    checklistList = taskWithDetail.checklistItems)
            )
            navController.navigate(InsertScreen)
        },
        onSecondClick = {
            isTaskBottomSheetVisible = false
            navController.popBackStack()
            scope.launch(Dispatchers.IO) {
                taskDao.setFrequencyToDeleted(taskId)
                taskWithDetail.checklistItems.forEach {
                    taskDao.deleteChecklist(it.id)
                    cancelHabitAlarms(context,it.id,it.daysInWeek)
                }
                cancelHabitAlarms(context,taskId,taskWithDetail.task.daysInWeek)
                taskDao.deleteTodayTask(taskId)
            }
        }


    )

    //Nested Scroll Connection
    val nestedScrollConnection = remember{
        object: NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y

                // 1) Try moving the first panel
                val desiredFirst = (currentOffset + delta).coerceIn(-totalPaddingInPx, 0f)
                val consumedByFirst = desiredFirst - currentOffset
                if (consumedByFirst != 0f) {
                    currentOffset = desiredFirst
                    return Offset(0f, consumedByFirst)
                }

                // 2) If first is “locked,” move the second
                val desiredSecond = (secondColumnOffset + delta).coerceIn(-800f, 0f)
                val consumedBySecond = desiredSecond - secondColumnOffset
                if (consumedBySecond != 0f) {
                    secondColumnOffset = desiredSecond
                    return Offset(0f, consumedBySecond)
                }

                // 3) Neither moved, so don’t consume anything
                return Offset.Zero
            }

        }
    }



    Column(modifier = Modifier
        .fillMaxSize()
        .zIndex(1f)
        .safeDrawingPadding())
    {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Box(modifier = Modifier
                .clip(CircleShape)
                .background(Color.LightGray.copy(0.4f), CircleShape)
                .size(30.dp)
                .clickable {
                    navController.popBackStack()
                }
            ){
                Icon(imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center),
                    tint = Color.Black
                )
            }

            Box(modifier = Modifier
                .clip(CircleShape)
                .background(Color.LightGray.copy(0.4f), CircleShape)
                .size(30.dp)
                .clickable {
                    scope.launch {
                        taskBottomSheet.show()
                        isTaskBottomSheetVisible = true
                    }
                }
            ){
                Icon(imageVector = Icons.Default.Create,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center),
                    tint = Color.Black
                )
            }
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .nestedScroll(nestedScrollConnection)
        .graphicsLayer {
            clip = false
        }
        .background(Color(0xFFF8F5F5)))
    {
        if((maxTopColumnHeightPx + secondColumnOffset + currentOffset) <= 400f){
            Box(modifier = Modifier
                .fillMaxWidth()
                .height((deviceHeight / 2f).dp + topPadding + (decreaseInPadding + extendHeight))
            ){
                Text(
                    text = taskWithDetail.task.taskName,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(horizontal = 60.dp)
                        .align(Alignment.BottomStart),
                    textAlign = TextAlign.Left,
                    overflow = TextOverflow.Ellipsis
                )
            }

        }
        else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((deviceHeight / 2f).dp + topPadding + (decreaseInPadding + extendHeight))
                    .background(Color(taskWithDetail.task.color).copy(0.2f))
                    .drawWithContent {
                        drawContent()

                        drawRect(
                            color = Color.White.copy(alpha = whiteOverlay),
                            size = size
                        )
                    }
                    .padding(top = if (totalPadding >= decreaseInPadding) totalPadding + decreaseInPadding else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                Box(
                    modifier = Modifier

                        .size(60.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(taskWithDetail.task.color), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        painter = painterResource(iconList[taskWithDetail.task.icon]),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(40.dp),
                        tint = Color.White
                    )
                }

                Text(
                    text = taskWithDetail.task.taskName,
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(0.6f),
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isTaskCompleted) "Completed" else "Not Completed",
                    fontSize = 12.sp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (isTaskCompleted) Color(taskWithDetail.task.color) else Color(
                                taskWithDetail.task.color
                            ).copy(0.5f)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 25.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Row(
                        modifier = Modifier
                            .background(
                                Color(taskWithDetail.task.color).copy(0.3f),
                                RoundedCornerShape(5.dp)
                            )
                            .clip(RoundedCornerShape(5.dp))
                            .padding(horizontal = 5.dp)
                            .height(30.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier
                                .size(18.dp)
                        )
                        Text(
                            text = viewModel.transformRepetitionString(taskWithDetail.task.frequency),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .padding(5.dp)
                                .widthIn(max = 100.dp),
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        modifier = Modifier
                            .width(100.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(taskWithDetail.task.color).copy(0.3f))
                                .height(30.dp)
                                .width(40.dp)
                                .clickable {
                                    scope.launch {
                                        achievementBottomSheet.show()
                                        isAchievementBottomSheetVisible = true
                                    }
                                }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.trophy),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.Center)

                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(taskWithDetail.task.color).copy(0.3f))
                                .height(30.dp)
                                .width(40.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.callender),
                                contentDescription = null,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .clickable {
                                        scope.launch {
                                            calenderBottomSheet.show()
                                            isCalenderBottomSheetVisible = true
                                        }
                                    }

                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(0.3f), RoundedCornerShape(20.dp))
                            .border(2.dp, Color.LightGray, RoundedCornerShape(20.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = taskWithDetail.task.streak.toString(),
                            fontSize = 22.sp,
                            modifier = Modifier
                        )

                        Text(
                            text = "Current Streak",
                            fontSize = 12.sp,
                            modifier = Modifier,
                            color = Color.Gray
                        )

                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.67f)
                            .height(60.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(0.3f), RoundedCornerShape(20.dp))
                            .border(2.dp, Color.LightGray, RoundedCornerShape(20.dp)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(
                            text = taskWithDetail.task.maxStreak.toString(),
                            fontSize = 22.sp,
                            modifier = Modifier
                        )

                        Text(
                            text = "Best Streak",
                            fontSize = 12.sp,
                            modifier = Modifier,
                            color = Color.Gray
                        )

                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                        .height(70.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (taskWithDetail.checklistItems.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(40.dp)
                                .padding(0.dp, 0.dp, 15.dp, 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(0.dp, 0.dp, 5.dp, 0.dp)
                                    .fillMaxWidth(0.7f)
                                    .height(40.dp)
                                    .background(
                                        Color(taskWithDetail.task.color).copy(if (isTaskCompleted) 1f else 0.5f),
                                        RoundedCornerShape(15.dp, 0.dp, 0.dp, 15.dp)
                                    )
                                    .clickable {

                                        scope.launch {
                                            val uncompletedChecklist =
                                                taskWithDetail.checklistItems.filter { !it.isCompleted }
                                            if (uncompletedChecklist.isNotEmpty()) {
                                                taskDao.setChecklistCompleted(
                                                    uncompletedChecklist[0].id,
                                                    !uncompletedChecklist[0].isCompleted
                                                )
                                                if (uncompletedChecklist.size == 1) {
                                                    taskDao.setTaskCompleted(
                                                        LocalDate.now().toString(),
                                                        taskWithDetail.task.taskId,
                                                        true
                                                    )
                                                    viewModel.updateProgress(
                                                        taskDao,
                                                        LocalDate.now().toString()
                                                    )
                                                    if(taskWithDetail.task.frequency.startsWith("N")){
                                                        val numbers = taskWithDetail.task.frequency.substring(2).split("_").map{it.toInt()}
                                                        val weekFields = WeekFields.of(Locale.getDefault())
                                                        val weekOfYear = LocalDate.now().get(weekFields.weekOfWeekBasedYear())
                                                        if(taskWithDetail.task.frequency[1] == 'W'){
                                                            taskDao.updateFrequency(taskWithDetail.task.taskId,numbers[1]+1,weekOfYear)
                                                        }else{
                                                            taskDao.updateFrequency(taskWithDetail.task.taskId,numbers[1]+1, LocalDate.now().monthValue)
                                                        }
                                                    }
                                                }
                                            } else {
                                                scope.launch {
                                                    imageSheetState.show()
                                                    isCheckListBottomSheetVisible = true
                                                }
                                            }
                                        }

                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.Center)){
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isTaskCompleted) Color.White else Color.Black,
                                        modifier = Modifier
                                            .padding(horizontal = 5.dp)
                                    )
                                    Text(
                                        text = if(isTaskCompleted) "Done"
                                        else
                                            "${taskWithDetail.checklistItems.count { it.isCompleted }}/${taskWithDetail.checklistItems.size}",
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isTaskCompleted) Color.White else Color.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .background(
                                        Color(taskWithDetail.task.color).copy(if (isTaskCompleted) 1f else 0.5f),
                                        RoundedCornerShape(0.dp, 15.dp, 15.dp, 0.dp)
                                    )
                                    .clickable {
                                        scope.launch {
                                            checkListBottomSheet.show()
                                            isCheckListBottomSheetVisible = true
                                        }
                                    }

                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isTaskCompleted) Color.White else Color.Black,
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.4f)
                                .height(40.dp)
                                .padding(0.dp, 0.dp, 15.dp, 0.dp)
                                .background(
                                    Color(taskWithDetail.task.color).copy(if (isTaskCompleted) 1f else 0.5f),
                                    RoundedCornerShape(15.dp)
                                )
                                .clickable {
                                    scope.launch {
                                        taskDao.setTaskCompleted(
                                            LocalDate.now().toString(),
                                            taskWithDetail.task.taskId,
                                            !isTaskCompleted
                                        )
                                        viewModel.updateProgress(
                                            taskDao,
                                            LocalDate.now().toString()
                                        )
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.Center)){
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isTaskCompleted) Color.White else Color.Black,
                                    modifier = Modifier
                                        .padding(horizontal = 5.dp)
                                )
                                Text(
                                    text = "Done",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isTaskCompleted) Color.White else Color.Black,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.67f)
                            .height(40.dp)
                            .padding(0.dp, 0.dp, 15.dp, 0.dp)
                            .background(
                                Color(taskWithDetail.task.color).copy(0.5f),
                                RoundedCornerShape(15.dp)
                            )
                            .clickable {
                                viewModel.setColor(Color(taskWithDetail.task.color))
                                viewModel.setNotificationToUpdate(null)
                                navController.navigate(NotesScreen)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add_notes),
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                            )
                            Text(
                                text = "Add Note",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .background(
                                Color(taskWithDetail.task.color).copy(0.5f),
                                RoundedCornerShape(15.dp)
                            )
                            .clickable {
                                scope.launch {
                                    imageSheetState.show()
                                    showImageSheet = true
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.Center)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.add_a_photo),
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.93f),
            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 10.dp)
        ) {
            taskWithDetail.notifications.reversed()
                .groupBy { it.date.month }
                .forEach { (month, items) ->
                    stickyHeader {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .background(Color(0xFFF8F5F5)),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(text = "$month ${LocalDate.now().year}",
                                fontWeight = FontWeight.SemiBold
                            )

                        }
                    }
                    items(items,key = { it.id }){ notification ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(height = 45.dp, width = 51.5.dp)
                                    .clip(CircleShape)
                                    .padding(start = 6.5.dp)
                                    .background(Color(taskWithDetail.task.color), CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(iconList[taskWithDetail.task.icon]),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                )
                            }
                            Spacer(
                                modifier = Modifier
                                    .width(10.dp)
                            )
                            NotificationBox(notificationEntity = notification,
                                color = Color(taskWithDetail.task.color),
                                taskName = taskWithDetail.task.taskName,
                                onOptionClick = {
                                    if (MassageType.IMAGE == notification.type) {
                                            notificationId = notification.id
                                            imageUri = notification.imageUri
                                            isImageNotificationBottomSheetVisible = true
                                        scope.launch {
                                            imageNotificationOptionSheet.show()
                                        }
                                    } else {
                                            notificationId = notification.id
                                            isNotesNotificationBottomSheetVisible = true
                                        scope.launch {
                                            notesNotificationOption.show()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(isVisible:Boolean = false ,
                onDismissRequest :() ->Unit ,
                imageSheetState: SheetState,
                firstButtonIcon:Int ,
                secondButtonIcon: Int,
                firstButtonTitle:String,
                secondButtonTitle:String ,
                onFirstClick :() ->Unit ,
                onSecondClick :() ->Unit
)
{
    if(isVisible){
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = Modifier
                .fillMaxWidth(),
            sheetState =  imageSheetState,
            dragHandle = { Icon(painterResource(R.drawable.horizontal_rule), null) },
            containerColor =  Color(0xFFF5F5F5),
            scrimColor = Color.Black.copy(0.2f)
        ) {
            Column(modifier = Modifier
                .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(
                    text = "Options",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Take Photo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp)
                        .clickable { onFirstClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(firstButtonIcon),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(firstButtonTitle, fontWeight = FontWeight.Medium)
                }

                // From Gallery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp)
                        .clickable { onSecondClick() },
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Icon(
                        painter = painterResource(secondButtonIcon),
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(secondButtonTitle, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp))

            }
        }
    }
}


