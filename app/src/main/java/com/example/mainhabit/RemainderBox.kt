package com.example.mainhabit

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.time.TimePickerDefaults
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun SetReminderBox(
    viewModel: MainViewModel,
    modifier: Modifier,
    color: Color,
    time: LocalTime,
    isReminder: Boolean,
    list:List<Int>,
    returnValue: (time: LocalTime, reminder: Boolean, list: List<Int>) -> Unit
){
    var pickedTime by remember{ mutableStateOf(time) }
    val formatedTime by remember {
        derivedStateOf {
            DateTimeFormatter
                .ofPattern("hh:mm a")
                .format(pickedTime)
        }
    }
    val timeDialogState = rememberMaterialDialogState()


    var isToggle by remember { mutableStateOf(isReminder) }
    val dayOrder = listOf("Mo","Tu","We","Th","Fr","Sa","Su")
    val dayMap = remember {
        mutableStateMapOf(
            "Mo" to true,
            "Tu" to true,
            "We" to true,
            "Th" to true,
            "Fr" to true,
            "Sa" to true,
            "Su" to true
        )
    }
    LaunchedEffect(Unit) {
        list.forEach {
            dayMap[dayOrder[it-1]] = true
        }
    }
    val list = remember{
        derivedStateOf{}
        dayMap
            .filterValues { it }
            .keys
            .map{ day -> viewModel.dayLabelToIndex(day)}
    }
    LaunchedEffect(isToggle,pickedTime,dayMap) {
        if(isToggle){
            returnValue(pickedTime,true,list)
        }
    }
    val animateHeight by  animateDpAsState(
        targetValue = if(isToggle) 150.dp else 70.dp,
        animationSpec = tween(200)
    )
    Column(modifier = Modifier
        .fillMaxWidth()
        .height(animateHeight)
        .padding(horizontal = 10.dp)
        .border(1.dp, Color.LightGray, RoundedCornerShape(15.dp))
        .clip(RoundedCornerShape(15.dp))
        .background(Color.White)
        .padding(horizontal = 15.dp)
        .then(modifier),
        verticalArrangement = Arrangement.SpaceBetween){

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isToggle) 50.dp else 70.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = "Reminder",
                fontWeight = FontWeight.SemiBold
            )
            Switch(
                checked = isToggle,
                onCheckedChange = { isToggle = it },
                modifier = Modifier
                    .scale(0.9f),
                colors = SwitchDefaults.colors(
                    checkedTrackColor = color.copy(0.3f),
                    checkedThumbColor = color,
                    checkedBorderColor = color.copy(0.3f),
                    uncheckedThumbColor = Color.Black,
                    uncheckedTrackColor = Color.White,
                    uncheckedBorderColor = Color.Black
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text(
                text = "Time",
                fontWeight = FontWeight.SemiBold
            )
            Box(modifier = Modifier
                .size(height = 40.dp, width = 80.dp)
                .background(
                    color,
                    RoundedCornerShape(10.dp)
                )
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    timeDialogState.show()
                }
            ){
                Text(text = formatedTime.uppercase(),
                    modifier = Modifier
                        .align(Alignment.Center),
                    color = Color.White
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween

        ){
            Text(
                text = "Day",
                fontWeight = FontWeight.SemiBold
            )
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(start = 10.dp)
                .background(Color.White)
                .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically)
            {
                for (key in dayOrder) {
                    Box(
                        modifier = Modifier
                            .size(42.dp,30.dp)
                            .background(
                                if (dayMap[key] == true) color else Color(0x6A4D4B4B),
                                RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp)
                            .clickable(enabled = isToggle) {
                                dayMap[key] = !dayMap[key]!!
                            }
                    ) {
                        Text(
                            text = key,
                            modifier = Modifier
                                .align(Alignment.Center),
                            color = Color.White,
                            fontSize = 13.sp,
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp ,30.dp))
                }
            }

        }

    }
    MaterialDialog(
        dialogState = timeDialogState,
        shape = MaterialTheme.shapes.small,
        buttons = {
            positiveButton("Set", TextStyle(color = color))
            negativeButton("Cancel", TextStyle(color = color))
        }
    ) {
        timepicker(
            initialTime = LocalTime.NOON,
            title = "Select Time",
            colors = TimePickerDefaults.colors(
                activeBackgroundColor = color,
                inactiveBackgroundColor = Color.White,
                activeTextColor = Color.Black,
                inactiveTextColor = Color.Black,
                selectorColor = color
            )


        ){
            pickedTime = it
        }
    }
}