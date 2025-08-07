package com.example.mainhabit

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mainhabit.ui.theme.primary_Light_Color
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun CustomCalender(markedDates: List<LocalDate> = emptyList(),color: Color = primary_Light_Color){
    Log.d("CustomCalender","started")
    val currentMonth = YearMonth.now()
    val state = rememberCalendarState(
        startMonth = currentMonth.minusMonths(12),
        endMonth   = currentMonth.plusMonths(12),
        firstVisibleMonth = currentMonth,
        firstDayOfWeek    = DayOfWeek.MONDAY,
        outDateStyle      = OutDateStyle.EndOfRow  
    )
    val scope = rememberCoroutineScope()


    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                scope.launch { state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.previousMonth) }
            }) {
                Icon(painterResource(R.drawable.keyboard_double_arrow_left), contentDescription = "Previous month",tint = color)
            }
            Text(
                text = "${state.firstVisibleMonth.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${state.firstVisibleMonth.yearMonth.year}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {
                scope.launch { state.animateScrollToMonth(state.firstVisibleMonth.yearMonth.nextMonth) }
            }) {
                Icon(painterResource(R.drawable.keyboard_double_arrow_right), contentDescription = "Next month",tint = color)
            }
        }

        HorizontalCalendar(
            state = state,
            dayContent = { day -> DayCell(day, markedDates,color) }
        )
    }
}

@Composable
fun DayCell(day: CalendarDay, markedDates: List<LocalDate>,color:Color ) {
    val isMarked = day.date in markedDates
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .then(
                if (isMarked && day.position == DayPosition.MonthDate)
                    Modifier.padding(10.dp)
                        .background(color.copy(0.5f), shape = CircleShape)

                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate) Color.Black else Color.LightGray
        )
    }
}

