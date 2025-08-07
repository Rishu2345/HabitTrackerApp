package com.example.mainhabit



import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextMotion
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.mainhabit.ui.theme.primary_Dark_Color
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun NotesScreen(viewModel: MainViewModel, taskDao: TaskDao, navController: NavHostController){
    Log.d("NotesScreen","Started")

    val notificationEntity = viewModel.getNotificationToUpdate()
    val color = viewModel.getColor()
    val taskId = viewModel.getTaskId()
    val scope = rememberCoroutineScope()
    var pickedDate by remember{ mutableStateOf(LocalDate.now()) }
    val formatedDate by remember{
        derivedStateOf {
            DateTimeFormatter
                .ofPattern("MMMM dd,yyyy")
                .format(pickedDate)
        }
    }
    var title by remember{ mutableStateOf("")}
    var notes by remember{ mutableStateOf("") }

    LaunchedEffect(notificationEntity) {
        if (notificationEntity != null) {
            pickedDate = notificationEntity.date
            title = notificationEntity.notesTitle ?: ""
            notes = notificationEntity.msg
        }
    }
    val dateDialogState = rememberMaterialDialogState()
    MaterialDialog(
        dialogState = dateDialogState,
        buttons = {
            positiveButton("Ok", TextStyle(color = color))
            negativeButton("Cancel", TextStyle(color = color))
        },
        shape = RoundedCornerShape(20.dp)
    ) {
        datepicker(
            initialDate = pickedDate,
            colors = DatePickerDefaults.colors(
                headerBackgroundColor = color.copy(0.3f),
                headerTextColor = Color.DarkGray,
                calendarHeaderTextColor = Color.Black,
                dateActiveBackgroundColor = color,
                dateInactiveBackgroundColor = Color.White,
                dateActiveTextColor = Color.Black,
                dateInactiveTextColor = Color.Black,
            )
        ){
            pickedDate = it
        }
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .background(color.copy(0.3f))
        .safeDrawingPadding()
    ){
        Row(modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 20.dp, horizontal = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Icon(imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        notificationEntity?.let {
                            scope.launch{
                                taskDao.updateNotesNotification(
                                    notificationId = notificationEntity.id,
                                    date = pickedDate,
                                    msg = notes,
                                    notesTitle =title
                                )
                            }
                        }
                        if(notificationEntity == null){
                            scope.launch{
                                if(taskId == null || (notes.isEmpty() && title.isEmpty())) {
                                    navController.popBackStack()
                                }
                                else {
                                    taskDao.insertNotification(NotificationEntity(
                                            taskId = taskId,
                                            date = pickedDate,
                                            msg = notes,
                                            notesTitle = title,
                                            type = MassageType.NOTES
                                        )
                                    )
                                }
                            }
                        }
                        navController.popBackStack()
                    }
            )
            Icon(imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier
                    .size(30.dp)
                    .clickable {
                        notificationEntity?.let {
                            scope.launch{
                                taskDao.deleteNotification(notificationEntity.id)
                            }
                        }

                        navController.popBackStack()
                    },
                tint = Color.Red
            )
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
        ){
            Text(text = formatedDate,
                modifier = Modifier
                    .clickable{ dateDialogState.show() }
            )
        }

        TextField(value = title,
            onValueChange={title = it},
            modifier = Modifier
                .fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = 24.sp,
                textMotion = TextMotion.Animated
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            placeholder = {
                Text(text = "Title",
                    fontSize = 24.sp,
                    color = Color.Gray
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            )
        )

        TextField(value = notes,
            onValueChange={notes = it},
            modifier = Modifier
                .fillMaxSize(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = primary_Dark_Color
            ),
            textStyle = TextStyle(fontSize = 16.sp),
            placeholder = {
                Text(text = "Note",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            )
        )

    }
}

