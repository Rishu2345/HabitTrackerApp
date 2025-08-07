package com.example.mainhabit

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.mainhabit.ui.theme.primary_Dark_Color
import com.example.mainhabit.ui.theme.primary_Light_Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun DayBox(dayEntity: DayEntity,clickedDate:(LocalDate) ->Unit){
    Log.d("DayBox","started")
    val animatedProgress by animateFloatAsState(dayEntity.progress)
    val day by remember{mutableStateOf(dayEntity.date.dayOfWeek.name.take(3))}
    val date by remember{ mutableIntStateOf(dayEntity.date.dayOfMonth) }
    Column(modifier = Modifier
        .size(height = 60.dp, width = 50.dp)
        .clickable{
            clickedDate(dayEntity.date)
        },
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally){
        Text(text = day,
            modifier = Modifier,
            color = if(dayEntity.isSelected) Color.Black else Color.Gray,
            fontSize = 12.sp
        )

        Box{
            CircularProgressIndicator(
                progress = animatedProgress,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center),
                trackColor = Color.LightGray,
                color = primary_Dark_Color,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round

            )
            Text(
                modifier = Modifier
                    .align(Alignment.Center),
                text = date.toString(),
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp


            )
        }
    }
}


@Composable
fun HabitBox(
    taskItem: TaskEntity,
    isTaskCompleted: Boolean = false,
    taskDao: TaskDao,
    viewModel: MainViewModel,
    navController: NavController,
    date: LocalDate
){
    Log.d("HabitBox","started")
    var isChanged by remember{mutableStateOf(false)}
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val animatedSize by animateDpAsState(
        targetValue = if(isChanged) 45.dp else 35.dp,
        animationSpec = repeatable(
            iterations = 1,
            animation = tween(
                durationMillis = 300,
            ),
            repeatMode = RepeatMode.Reverse))
    LaunchedEffect(isChanged) {
        if(isChanged)
            delay(600)
        isChanged = false
    }
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(60.dp)
        .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
        .clip(RoundedCornerShape(15.dp))
        .background(Color.White)
        .clickable{
            viewModel.setTaskId(taskItem.taskId)
            navController.navigate(taskScreen)
        }
        .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround

    ){

//        Spacer(modifier = Modifier.size(width = 5.dp, height = 5.dp))
        Box(modifier = Modifier
            .size(40.dp)
            .background(Color.LightGray.copy(0.2f), CircleShape)
            .clip(CircleShape)
        ){
            Icon(painter = painterResource(iconList[taskItem.icon]),
                contentDescription = "",
                tint = Color(taskItem.color),
                modifier = Modifier
                    .fillMaxSize(.7f)
                    .align(Alignment.Center)
            )
        }

        Column(modifier = Modifier
            .fillMaxWidth(0.82f)
            .padding(10.dp, 0.dp)){
            Text(
                text = taskItem.taskName,
                modifier = Modifier,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                letterSpacing = .6.sp,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Streak : ${taskItem.streak} day",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Box(modifier = Modifier
            .size(animatedSize)
            .background(
                Color(taskItem.color).copy(if (isTaskCompleted) 1f else 0.2f),
                CircleShape
            )
            .clip(CircleShape)
            .clickable {
                isChanged = true
                if(!date.isEqual(LocalDate.now())){
                    Toast.makeText(context,"You can only Complete the today's tasks ",Toast.LENGTH_SHORT).show()
                }else {
                    scope.launch(Dispatchers.IO) {
                        taskDao.setTaskCompleted(date.toString(), taskItem.taskId, !isTaskCompleted)

                        if (!isTaskCompleted) {
                            taskDao.increaseDayStreak(taskItem.taskId)
                            taskDao.completeAllChecklist(taskItem.taskId)
                            if(taskItem.frequency.startsWith("N")){
                                val numbers = taskItem.frequency.substring(2).split("_").map{it.toInt()}
                                val weekFields = WeekFields.of(Locale.getDefault())
                                val weekOfYear = LocalDate.now().get(weekFields.weekOfWeekBasedYear())
                                if(taskItem.frequency[1] == 'W'){
                                    taskDao.updateFrequency(taskItem.taskId,numbers[1]+1,weekOfYear)
                                }else{
                                    taskDao.updateFrequency(taskItem.taskId,numbers[1]+1, LocalDate.now().monthValue)
                                }
                            }
                        } else {
                            if (taskItem.streak > 0) taskDao.decreaseDayStreak(taskItem.taskId)
                            taskDao.incompleteAllChecklist(taskItem.taskId)
                        }
                        viewModel.updateProgress(taskDao, date.toString())
                    }
                }
            }
        ){
            Icon(imageVector = Icons.Default.Check,
                contentDescription = "",
                modifier = Modifier
                    .fillMaxSize(.6f)
                    .align(Alignment.Center)

            )
        }


    }

}


@Composable
fun NotificationBox(notificationEntity: NotificationEntity, color:Color = Color(0xFF8E97FD),
                    taskName:String = "Badminton", onOptionClick:() ->Unit ={}){
    Log.d("NotificationBox","started")
    Column(modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
        .clip(RoundedCornerShape(15.dp))
    ){
        when(notificationEntity.type){
            MassageType.IMAGE -> {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        color.copy(0.3f),
                        RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp)
                    )
                    .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(text = taskName,
                        fontWeight = FontWeight.SemiBold,
                        color =  color
                    )
                    Row(modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically){
                        Text(text = notificationEntity.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                            color = Color.Gray
                        )

                        Button(
                            onClick = onOptionClick,
                            modifier = Modifier
                                .size(35.dp,25.dp)
                                .clip(CircleShape)
                                .padding(start = 10.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray.copy(0.3f),
                                contentColor = Color.Black
                            )
                        ){
                            Icon(imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(10.dp))
                ){
                    AsyncImage(
                        model = notificationEntity.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp),
                        clipToBounds = true
                    )
                }
            }

            MassageType.NOTES -> {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(
                        color.copy(0.3f),
                        RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp)
                    )
                    .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Text(text = taskName,
                        fontWeight = FontWeight.SemiBold,
                        color =  color
                    )
                    Row(modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically){
                        Text(text = notificationEntity.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                            color = Color.Gray
                        )

                        Button(
                            onClick = onOptionClick,
                            modifier = Modifier
                                .size(35.dp,25.dp)
                                .clip(CircleShape)
                                .padding(start = 10.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray.copy(0.3f),
                                contentColor = Color.Black
                            )
                        ){
                            Icon(imageVector = Icons.Default.MoreVert,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(20.dp),
                                tint = Color.Gray
                            )
                        }
                    }
                }

                notificationEntity.notesTitle?.let { title ->
                    Text(text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 40.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .padding(10.dp)
                    )

                    Text(text = notificationEntity.msg,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp, 0.dp, 10.dp, 10.dp)
                    )

                }



            }

            MassageType.ACHIEVEMENT -> {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(color.copy(0.3f), RoundedCornerShape(15.dp))
                    .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){

                    Row(verticalAlignment = Alignment.CenterVertically){
                        Icon(painter = painterResource(R.drawable.celebration),
                            contentDescription = null,
                            tint = color
                        )
                        Text(text = notificationEntity.msg,
                            fontWeight = FontWeight.SemiBold,
                            color =  color
                        )

                    }

                    Text(text = notificationEntity.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                        color = Color.Gray
                    )
                }
            }

            MassageType.START ->{
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(color.copy(0.3f), RoundedCornerShape(15.dp))
                    .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){

                    Row(verticalAlignment = Alignment.CenterVertically){
                        Icon(painter = painterResource(R.drawable.flag),
                            contentDescription = null,
                            tint = color
                        )
                        Text(text = notificationEntity.msg,
                            fontWeight = FontWeight.SemiBold,
                            color =  color,
                            modifier = Modifier
                                .padding(start = 10.dp)
                        )

                    }

                    Text(text = notificationEntity.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                        color = Color.Gray
                    )
                }
            }
            else -> {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(color.copy(0.3f), RoundedCornerShape(15.dp))
                    .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){

                    Row(verticalAlignment = Alignment.CenterVertically){
                        Icon(painter = painterResource(R.drawable.local_fire_department),
                            contentDescription = null,
                            tint = color
                        )
                        Text(text = notificationEntity.msg,
                            fontWeight = FontWeight.SemiBold,
                            color =  color
                        )

                    }

                    Text(text = notificationEntity.date.format(DateTimeFormatter.ofPattern("dd MMM")),
                        color = Color.Gray
                    )
                }
            }

        }

    }

}


@Composable
fun ImageScreen(viewModel: MainViewModel,navController: NavHostController){
    val imageUri:Uri? = viewModel.getViewingImageUri()
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .safeDrawingPadding()
    ){
        Box(modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Closing the image view",
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.Center)
                    .clickable{navController.popBackStack()},
                tint = Color.White
            )
        }
        imageUri?.let{
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
            )
        } ?: Toast.makeText(LocalContext.current,"Image not found",Toast.LENGTH_SHORT).show()
    }
}








@Composable
fun WelcomeScreen(navController: NavHostController){
    var isCompleted by remember{ mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) {
        isCompleted = true
    }


    val animateLogoScale = animateFloatAsState(
        targetValue = if(isCompleted) 1f else 2.2f,
        label = "",
        animationSpec = tween(500,1900)
    )
    val animatedLogoTranslation = animateFloatAsState(
        targetValue = if(isCompleted) 0f else 800f,
        animationSpec = tween(500,1900),
        label = ""
    )
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedAlpha = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000,2000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val animatedAlpha2 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000,1000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val animatedAlpha3 = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    Column(modifier = Modifier
        .fillMaxSize()
        .background(primary_Light_Color)
        .safeContentPadding()
//        .safeDrawingPadding()
        .padding(20.dp)
    ){
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){
            Image(modifier = Modifier
                .size(100.dp)
                .graphicsLayer(
                    scaleX = animateLogoScale.value,
                    scaleY = animateLogoScale.value,
                    translationY = animatedLogoTranslation.value
                ),
                painter = painterResource(R.drawable.habit_logo),
                contentDescription = "The Logo"
            )
            Text(text = "Hi,Welcome",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(text= "to the Main Habits",
                color = Color.White,
                fontSize = 25.sp,
                fontStyle = FontStyle.Italic
            )

            Text(text= stringResource(R.string.welcoming_msg),
                modifier = Modifier.padding(25.dp,20.dp),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Box(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)){
                Image(painter = painterResource(R.drawable.bird),
                    contentDescription = null,
                    modifier = Modifier.graphicsLayer(
                        translationX = 100f,
                        translationY =300f,
                    ))

                Image(painter = painterResource(R.drawable.bird),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(
                            translationY = 120f,
                            translationX = 600f
                        ))
                Image(painter = painterResource(R.drawable.cloud),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .graphicsLayer(
                            translationY = 250f,
                            translationX = 870f,
                            rotationZ = 0f,
                            scaleX = 1.2f,
                            scaleY = 1.3f
                        ))

                Image(painter = painterResource(R.drawable.cloud),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer(
                            translationY = 10f,
                            translationX = 280f,
                            rotationZ = 0f,
                            scaleX = 1.2f,
                            scaleY = 1.3f
                        ))
                Image(painter = painterResource(R.drawable.ecllips),
                    contentDescription = null,
                    modifier = Modifier
                        .size(30.dp)
                        .graphicsLayer(
                            translationY = 80f
                        ))
                Image(painter = painterResource(R.drawable.ecllips),
                    contentDescription = null)

            }
            Box(modifier = Modifier
                .size(380.dp)
                .clip(CircleShape)
                .background(Color.White.copy(animatedAlpha3.value + 0.1f))){
                Box(modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .align(Alignment.Center)
                    .background(Color.White.copy(animatedAlpha2.value + 0.1f))){
                    Box(modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .background(Color.White.copy(animatedAlpha.value + 0.1f))){

                    }

                }
                Image(painter = painterResource(R.drawable.welcoming_women),
                    contentDescription = null,
                    modifier = Modifier
                        .graphicsLayer()
                        .align(Alignment.Center))
            }
        }
    }
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom){
        Image(painter = painterResource(R.drawable.the_reactangle),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()

        )
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .background(primary_Dark_Color)
            .padding(bottom =15.dp)
        ){
            Button(onClick = {
                navController.navigate(onboardingScreen)
            },
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(50.dp)
                    .fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = primary_Dark_Color),
                shape = RoundedCornerShape(20.dp)
            ){
                Text(text ="Get Started",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold)
            }
        }

    }

}


@Composable
fun OnboardingScreen(navController: NavHostController){


    val context = LocalContext.current
    var onboardingPage by remember{ mutableIntStateOf(0) }
    val animatedProgress by animateFloatAsState(
        targetValue = onboardingPage.toFloat(),
        label = "",
        animationSpec = tween(1000,easing = FastOutSlowInEasing)
    )
    val onboardingImage by remember{
        derivedStateOf {
            when(onboardingPage){
                1 -> R.drawable.onboading1
                2 -> R.drawable.onboarding2
                3 -> R.drawable.onboarding3
                else -> R.drawable.onboarding4
            }
        }
    }

    val onboardingHeading by remember{
        derivedStateOf {
            when(onboardingPage){
                1 -> R.string.track_your_goal_heading
                2 -> R.string.get_burn_heading
                3 -> R.string.eat_well_heading
                else -> R.string.morning_yoga_heading
            }
        }
    }

    val onboardingMsg by remember{
        derivedStateOf {
            when(onboardingPage){
                1 -> R.string.track_your_goal_text
                2 -> R.string.get_burn_text
                3 -> R.string.eat_well_text
                else -> R.string.morning_yoga_text
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        onboardingPage += 1
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color.White)
//        .safeDrawingPadding()
        .padding(bottom = 20.dp)
    ) {
        Image(painter = painterResource(onboardingImage),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.4f),
            contentScale = ContentScale.FillWidth
        )
        Text(
            text = stringResource(onboardingHeading),
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 20.dp),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(text = stringResource(onboardingMsg),
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 0.dp),
            textAlign = TextAlign.Left
        )

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)){
            Box(modifier = Modifier.align(Alignment.BottomEnd)
            ){
                Button(onClick = {
                    if(onboardingPage<4) onboardingPage += 1
                    else if(onboardingPage == 4){
                        PrefsHelper.setSeenWelcome(context)
                        navController.navigate(Home)
                    }
                } ,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary_Light_Color,
                        contentColor = Color.White
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(5.dp)
                ) {
                    Icon(imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = "",
                        modifier = Modifier.fillMaxSize())
                }
                CircularProgressIndicator(
                    progress = 0.25f * animatedProgress ,
                    modifier = Modifier.size(80.dp),
                    color = primary_Dark_Color,
                    strokeWidth = 4.dp,
                    trackColor = ProgressIndicatorDefaults.circularTrackColor,
                    strokeCap = StrokeCap.Round,
                )
            }
        }
    }
}