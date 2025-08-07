package com.example.mainhabit

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mainhabit.ui.theme.MainHabitTheme
import kotlinx.coroutines.flow.first
import java.time.LocalDate


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity","Started")
        val viewModel by viewModels<MainViewModel>()
        val db = AppDatabase.getDatabase(this)
        val taskDao = db.taskDao()
        val startDest = if (PrefsHelper.hasSeenWelcome(this)) Home else welcomingScreen
        viewModel.setTaskDao(taskDao)


        Log.d("MainActivity","is running")
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                !viewModel.isReady.value
            }
        }
        setContent {
            Log.d("SetContent","Started")
            viewModel.loadDateWithTask(LocalDate.now(),taskDao)
            viewModel.resetBrokenStreaks(taskDao)
            Log.d("SetContent","is running")
            MainHabitTheme {
                Log.d("Theme","is running")
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Log.d("Surface","is running")
                    MyNavGraph(viewModel = viewModel,taskDao = taskDao,startDest = startDest)
                }
            }
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}

const val Home = "Home"
const val Achievement = "Achievement"
const val InsertScreen = "insertScreen"
const val taskScreen = "taskScreen"
const val NotesScreen = "NotesScreen"
const val imageScreen = "imageScreen"

const val onboardingScreen = "onboarding Screen"

const val welcomingScreen = "welcomingScreen"

const val PREFS_NAME = "app_prefs"
const val KEY_HAS_SEEN_WELCOME = "has_seen_welcome"


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MyNavGraph(navController: NavHostController = rememberNavController(),viewModel: MainViewModel,taskDao: TaskDao,startDest:String) {
    Log.d("NavGraph","Started")
    NavHost(
        navController,
        startDestination = startDest,
        enterTransition   = { zoomIn() },
        exitTransition    = { zoomOut() },
        popEnterTransition = { zoomIn() },
        popExitTransition  = { zoomOut() }
    ) {
        composable(welcomingScreen){WelcomeScreen(navController)}
        composable(onboardingScreen){OnboardingScreen(navController)}
        composable(Home){HomeScreen(viewModel,navController,taskDao)}
        composable(InsertScreen){ TaskScreen(viewModel,taskDao,navController) }
        composable(Achievement){ AchievementScreen(viewModel,navController,taskDao) }
        composable(taskScreen){TaskDetailScreen(viewModel,navController,taskDao)}
        composable(NotesScreen){ NotesScreen(viewModel,taskDao,navController) }
        composable(imageScreen){ ImageScreen(viewModel,navController) }


    }
}



@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<NavBackStackEntry>.zoomIn(): EnterTransition =
    scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(durationMillis = 300)
    ) + fadeIn(animationSpec = tween(300))


@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<NavBackStackEntry>.zoomOut(): ExitTransition =
    scaleOut(
        targetScale = 1.2f,
        animationSpec = tween(durationMillis = 300)
    ) + fadeOut(animationSpec = tween(300))



