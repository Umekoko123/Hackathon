package com.example.argh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextField
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*

import kotlinx.coroutines.launch
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController









data class Task(
    val id: Int,
    val title: String,
    val isDone: Boolean = false
)

class AppState {
    var tasks by mutableStateOf(listOf<Task>())
        private set

    private var nextId = 1

    fun addTask(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        tasks = tasks + Task(id = nextId++, title = trimmed)
    }

    fun toggleTask(id: Int) {
        tasks = tasks.map { t ->
            if (t.id == id) t.copy(isDone = !t.isDone) else t
        }
    }

    fun deleteTask(id: Int) {
        tasks = tasks.filterNot { it.id == id }
    }

    val completedCount: Int
        get() = tasks.count { it.isDone }
}

//aloysious suck dickx
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val appState = remember { AppState() }
            AppNavHost(navController, appState)
        }
    }
}

@Composable

//test
fun AppNavHost(navController: NavHostController, appState: AppState) {
    NavHost(navController = navController, startDestination = Screen.Timer.route) {

        composable(Screen.Timer.route) { FocusTimerUI(navController,appState) }

        composable(Screen.Todo.route) { TodoScreen(appState) }

        composable(Screen.About.route) { AboutScreen() }
    }
}





sealed class Screen(val route: String, val title: String) {
    object Timer : Screen("timer", "Timer")
    object Todo : Screen("todo", "To-Do List")
    object About : Screen("about", "About")
}


//Main Timer UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTimerUI(navcontroller: NavHostController, appState: AppState) {
    var totalTime by remember { mutableIntStateOf(25 * 60) }
    var timeLeft by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }

    val minutes = timeLeft / 60
    val seconds = timeLeft % 60

    // Progress
    val progress = if (totalTime > 0) {
        (timeLeft.toFloat() / totalTime.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )
    //Drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()




    // Changing Time based on User
    var showDialog by remember { mutableStateOf(false) }
    var inputMinutes by remember { mutableStateOf("25") }
    var inputSeconds by remember { mutableStateOf("0") }




    // Countdown timer
    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        } else if (timeLeft <= 0) {
            isRunning = false
        }
    }
    //Navigation Drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet (
                modifier = Modifier
                    .width(150.dp)
                    .fillMaxHeight()
            ){
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Menu",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Divider()

                // Menu items
                val menuItems = listOf(Screen.Timer, Screen.Todo, Screen.About)

                menuItems.forEach { screen ->
                    Text(
                        text = screen.title,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navcontroller.navigate(screen.route){
                                    launchSingleTop = true
                                }
                                // Handle navigation here
                            }
                            .padding(16.dp)
                    )
                }
            }
        }
    ) {
        // Scaffold with TopBar
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("LOCKED") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(

                                //testing123
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues)
                    .padding(24.dp),
            ) {
                    Button(
                        onClick = { navcontroller.navigate(Screen.Todo.route) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Task Completed: ${appState.completedCount}", fontSize = 14.sp)
                    }
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(300.dp)
                    ){
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 16f
                        val radius = size.minDimension / 2f - strokeWidth / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        drawArc(
                            color = Color.DarkGray,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth),
                            topLeft = Offset(
                                center.x - radius - strokeWidth / 2,
                                center.y - radius - strokeWidth / 2
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                (radius + strokeWidth / 2) * 2,
                                (radius + strokeWidth / 2) * 2
                            )
                        )

                        val remainingAngle = 270f * animatedProgress
                        val startProgressAngle = 135f + 270f - remainingAngle
                        drawArc(
                            color = Color.White,
                            startAngle = startProgressAngle,
                            sweepAngle = remainingAngle,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = Offset(
                                center.x - radius - strokeWidth / 2,
                                center.y - radius - strokeWidth / 2
                            ),
                            size = androidx.compose.ui.geometry.Size(
                                (radius + strokeWidth / 2) * 2,
                                (radius + strokeWidth / 2) * 2
                            )
                        )

                        val startAngle = 135.0
                        val sweepAngle = 270.0
                        val currentAngle = startAngle + (sweepAngle * (1f - animatedProgress))
                        val angleRad = Math.toRadians(currentAngle)
                        val dotRadius = 30f
                        val dotX = center.x + radius * cos(angleRad).toFloat()
                        val dotY = center.y + radius * sin(angleRad).toFloat()

                        drawCircle(
                            color = Color.White,
                            radius = dotRadius,
                            center = Offset(dotX, dotY)
                        )
                    }

                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.clickable {
                            if (!isRunning) {
                                inputMinutes = (totalTime / 60).toString()
                                inputSeconds = (totalTime % 60).toString()
                                showDialog = true
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text(if (isRunning) "Pause" else "Focus", fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            isRunning = false
                            timeLeft = totalTime
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Reset", fontSize = 16.sp)
                    }}
                }
            }
        }
    }

    // Keep your existing dialog code
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Duration of Focus Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextField(
                        value = inputMinutes,
                        onValueChange = {
                            if ((it.isEmpty() || it.all { c -> c.isDigit() }) && it.length <= 2) {
                                inputMinutes = it
                            }
                        },
                        label = { Text("Minutes") },
                        singleLine = true
                    )
                    TextField(
                        value = inputSeconds,
                        onValueChange = {
                            if ((it.isEmpty() || it.all { c -> c.isDigit() }) && it.length <= 2) {
                                inputSeconds = it
                            }
                        },
                        label = { Text("Seconds") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val min = inputMinutes.toIntOrNull() ?: 0
                    val sec = inputSeconds.toIntOrNull() ?: 0
                    val newTotal = min * 60 + sec
                    if (newTotal > 0) {
                        totalTime = newTotal
                        timeLeft = newTotal
                    }
                    isRunning = false
                    showDialog = false
                }) {
                    Text("Set")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

//To Do Screen UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(appState: AppState) {
    var newTask by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Focus Tracker",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Add task row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newTask,
                onValueChange = { newTask = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("Add a task...") }
            )
            Button(
                onClick = {
                    appState.addTask(newTask)
                    newTask = ""
                }
            ) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task list
        if (appState.tasks.isEmpty()) {
            Text(
                text = "No tasks yet. Add one!",
                color = Color.Gray,
                fontSize = 16.sp
            )
        } else {
            appState.tasks.forEach { task ->
                TaskRow(
                    task = task,
                    onToggle = { appState.toggleTask(task.id) },
                    onDelete = { appState.deleteTask(task.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}


//TaskRowUI
@Composable
private fun TaskRow(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = Color(0xFF1A1A1A),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = { onToggle() }
                )
                Text(
                    text = task.title,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Text(
                text = "Delete",
                color = Color.Gray,
                modifier = Modifier.clickable { onDelete() }
            )
        }
    }
}


@Composable
fun AboutScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("About Screen", color = Color.White, fontSize = 24.sp)
    }
}

