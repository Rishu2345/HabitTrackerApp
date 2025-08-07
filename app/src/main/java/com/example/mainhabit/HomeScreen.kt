package com.example.mainhabit

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mainhabit.ui.theme.primary_Dark_Color
import java.time.LocalDate

@Composable
fun HomeScreen(viewModel: MainViewModel,navController: NavHostController,taskDao: TaskDao){
    Log.d("Home Screen","Started")
    var date by remember { mutableStateOf(LocalDate.now()) }
    Log.d("Home Screen",date.toString())
    val todayTaskList = taskDao.getDayWithTasks(date.toString()).collectAsState(DayWithTasks(DayEntity(date.toString(),date = date), emptyList(), emptyList()))
    Log.d("Home Screen",todayTaskList.toString())
    var taskList by remember{mutableStateOf(todayTaskList)}
    val scope = rememberCoroutineScope()
    var daysList by remember { mutableStateOf<List<DayEntity>>(emptyList()) }
    var daysListOfDayBeforeToday = taskDao.getAllDaysTillToday().collectAsState(emptyList())
    if(daysListOfDayBeforeToday.value.isNotEmpty()){
        daysList = daysListOfDayBeforeToday.value + viewModel.generateDateAheadToday(10)
    }
    val listState = rememberLazyListState()
    LaunchedEffect(Unit) {
//       daysList  = viewModel.getListOfDays(taskDao)
    }


    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFECECEC))
        .safeContentPadding()
        ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.08f)
            .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(text = if(viewModel.isToday(date)) "Today, " else {""} + viewModel.formatDateToDayWithSuffix(date),
                modifier = Modifier,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(modifier = Modifier
                .width(80.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {},
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
        LazyRow(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.15f)
            .padding(2.dp, 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            state = listState
        ){
            items(daysList.size){ index ->
                DayBox(daysList[index]){
                    viewModel.loadDateWithTask(it,taskDao)
                    date = it
                }
            }
        }
        LazyColumn(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(5.dp, 5.dp)
        ){
            items(taskList.value?.tasks?.size ?: 0 ){ index ->
                Log.d("The task List item ",taskList.toString())
                HabitBox(taskList.value.tasks[index], isTaskCompleted =taskList.value.todayTasks.filter{
                    it.taskId == taskList.value.tasks[index].taskId }[0].isCompleted
                    ,taskDao,viewModel,navController,date = date)
                Spacer(modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp))
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
                tint = primary_Dark_Color,
                modifier = Modifier
                    .clickable {}
                    .size(30.dp)
            )
            Icon(painter = painterResource(R.drawable.add_box),
                contentDescription = "insertScreen",
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
                tint = Color.Gray,
                modifier = Modifier
                    .clickable {
                        navController.navigate(Achievement)
                    }
                    .size(30.dp)
            )
        }

    }
}