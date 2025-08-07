package com.example.mainhabit

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mainhabit.ui.theme.primary_Dark_Color
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(viewModel: MainViewModel,navController: NavHostController,taskDao: TaskDao){
    Log.d("Achievement","started")
    val scope = rememberCoroutineScope()

    var notificationId by remember { mutableStateOf<Long?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageNotificationBottomSheetVisible by remember { mutableStateOf(false) }
    var isNotesNotificationBottomSheetVisible by remember { mutableStateOf(false) }


    val imageNotificationOptionSheet = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val notesNotificationOption = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val notificationEntityList = taskDao.getAllNotification().collectAsState(emptyList())


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

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFECECEC))
        .safeContentPadding(),
        verticalArrangement = Arrangement.SpaceBetween

        ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.08f)
            .padding(10.dp, 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text = if(viewModel.isToday(LocalDate.now())) "Today, " else {""} + viewModel.formatDateToDayWithSuffix(LocalDate.now()),
                modifier = Modifier,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(modifier = Modifier
                .width(80.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .size(35.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(Color.LightGray.copy(0.7f)),
                    contentPadding = PaddingValues(0.dp),

                    ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = "",
                        tint = Color.Black
                    )
                }
                Button(
                    onClick = {},
                    modifier = Modifier
                        .size(35.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(Color.LightGray.copy(0.7f)),
                    contentPadding = PaddingValues(0.dp),

                    ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "",
                        tint = Color.Black
                    )
                }
            }
        }
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.93f),
            contentPadding = PaddingValues(vertical = 10.dp, horizontal = 10.dp)
        ) {
            notificationEntityList.value
                .groupBy { it.date.month }
                .forEach { (month, items) ->
                    stickyHeader {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .background(Color(0xFFECECEC)),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            Text(text = "$month ${LocalDate.now().year}",
                                fontWeight = FontWeight.SemiBold
                            )

                        }
                    }
                    items(items, key = { it.id }){ notification ->
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp))
                        {
                            Box(
                                modifier = Modifier
                                    .size(height = 35.dp, width = 41.5.dp)
                                    .clip(CircleShape)
                                    .padding(start = 6.5.dp)
                                    .background(Color(notification.color), CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(iconList[notification.icon]),
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
                            NotificationBox(notificationEntity = NotificationEntity(
                                taskId = notification.taskId,
                                type = notification.type,
                                msg = notification.msg,
                                date = notification.date,
                                imageUri = notification.imageUri,
                                notesTitle = notification.notesTitle
                            ),
                                color = Color(notification.color),
                                taskName = notification.taskName,
                                onOptionClick = {
                                    if(MassageType.IMAGE == notification.type){
                                            notificationId = notification.id
                                            imageUri = notification.imageUri
                                            isImageNotificationBottomSheetVisible = true
                                        scope.launch {
                                            imageNotificationOptionSheet.show()
                                        }
                                    }
                                    else{
                                            notificationId = notification.id
                                            isNotesNotificationBottomSheetVisible = true
                                        scope.launch {
                                            notesNotificationOption.show()
                                        }
                                    }
                                })
                        }
                    }
                }
        }
        Row(modifier = Modifier
            .fillMaxSize()
            .padding(0.dp, 10.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(painter = painterResource(R.drawable.dashboard),
                contentDescription = "Dashboard",
                tint = Color.Gray,
                modifier = Modifier
                    .clickable { navController.popBackStack() }
                    .size(30.dp)
            )
            Icon(painter = painterResource(R.drawable.add_box),
                contentDescription = "Dashboard",
                tint = Color.Black,
                modifier = Modifier
                    .clickable {
                        viewModel.setTaskToUpdate(null)
                        navController.navigate(InsertScreen)
                    }
                    .size(30.dp)
                    .graphicsLayer(
                        scaleX = 2f,
                        scaleY = 2f,
                    )
            )
            Icon(painter = painterResource(R.drawable.crown),
                contentDescription = "Dashboard",
                tint = primary_Dark_Color,
                modifier = Modifier
                    .size(30.dp)
            )
        }

    }
}

