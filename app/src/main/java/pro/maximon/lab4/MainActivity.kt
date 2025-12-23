package pro.maximon.lab4

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.koin.androidx.compose.koinViewModel
import pro.maximon.lab4.models.StudentItem
import pro.maximon.lab4.theme.AndroidAppTheme
import pro.maximon.lab4.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AndroidAppTheme {
                AppView()
            }
        }
    }
}

@Composable
fun AppView(
    viewModel: MainViewModel = koinViewModel(),
) {
    val students by viewModel.items.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var selectedStudent by remember { mutableStateOf<StudentItem?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(students) {
        selectedStudent = selectedStudent?.let { selected ->
            students.find { it.id == selected.id }
        }
    }

    BackHandler(enabled = !isLandscape && selectedStudent != null) {
        selectedStudent = null
    }

    Scaffold(
        floatingActionButton = {
            if (isLandscape || selectedStudent == null) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить студента")
                }
            }
        }
    ) { innerPadding ->
        if (isLandscape) {
            Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                Box(modifier = Modifier.weight(1f)) {
                    StudentList(
                        students = students,
                        onStudentClick = { student -> selectedStudent = student },
                        onDeleteClick = { student -> viewModel.removeItem(student.id) }
                    )
                }
                VerticalDivider()
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = selectedStudent?.id,
                        label = "LandscapeDetails",
                        transitionSpec = {
                            (fadeIn() + slideInHorizontally { it / 3 })
                                .togetherWith(fadeOut() + slideOutHorizontally { -it / 3 })
                        }
                    ) { studentId ->
                        if (studentId != null) {
                            val student = students.find { it.id == studentId }
                            if (student != null) {
                                StudentDetails(
                                    student = student,
                                    onUpdate = { viewModel.updateItem(it.id, it) }
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Выберите студента",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            AnimatedContent(
                targetState = selectedStudent?.id,
                label = "ScreenTransition",
                modifier = Modifier.padding(innerPadding),
                transitionSpec = {
                    if (targetState != null) {
                        (fadeIn() + slideInHorizontally { it })
                            .togetherWith(fadeOut() + slideOutHorizontally { -it })
                    } else {
                        (fadeIn() + slideInHorizontally { -it })
                            .togetherWith(fadeOut() + slideOutHorizontally { it })
                    }
                }
            ) { studentId ->
                if (studentId == null) {
                    StudentList(
                        students = students,
                        onStudentClick = { selectedStudent = it },
                        onDeleteClick = { viewModel.removeItem(it.id) }
                    )
                } else {
                    val student = students.find { it.id == studentId }
                    if (student != null) {
                        StudentDetails(
                            student = student,
                            onUpdate = { viewModel.updateItem(it.id, it) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddStudentDialog(
            onDismissRequest = { showAddDialog = false },
            onAdd = { name, skipped, completed ->
                viewModel.addStudent(name, skipped, completed)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun StudentList(
    students: List<StudentItem>,
    onStudentClick: (StudentItem) -> Unit,
    onDeleteClick: (StudentItem) -> Unit
) {
    if (students.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Нет студентов",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    "Нажмите + чтобы добавить",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(students, key = { it.id }) { student ->
                StudentCard(
                    student = student,
                    onClick = { onStudentClick(student) },
                    onDeleteClick = { onDeleteClick(student) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
fun StudentAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 56,
) {
    val textStyle = when {
        size >= 120 -> MaterialTheme.typography.displayMedium
        size >= 80 -> MaterialTheme.typography.displaySmall
        else -> MaterialTheme.typography.headlineSmall
    }

    Surface(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                style = textStyle,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun StudentCard(
    student: StudentItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(name = student.name)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        color =
                            if (student.skippedLessons > 0) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "Пропуски: ${student.skippedLessons}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color =
                                if (student.skippedLessons > 0) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "ЛР: ${student.completedWorks}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun StudentDetails(
    student: StudentItem,
    onUpdate: (StudentItem) -> Unit
) {
    var isEditingName by remember { mutableStateOf(false) }
    var editedName by remember(student.id) { mutableStateOf(student.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StudentAvatar(name = student.name, size = 120)

        Spacer(modifier = Modifier.height(24.dp))

        if (isEditingName) {
            OutlinedTextField(
                value = editedName,
                onValueChange = { editedName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("ФИО") },
                singleLine = true,
                trailingIcon = {
                    Row {
                        IconButton(onClick = {
                            if (editedName.isNotBlank()) {
                                onUpdate(student.copy(name = editedName.trim()))
                            }
                            isEditingName = false
                        }) {
                            Icon(
                                Icons.Default.Check,
                                "",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    editedName = student.name
                    isEditingName = true
                }
            ) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Default.Edit,
                    "",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        EditableStatCard(
            title = "Пропущено занятий",
            value = student.skippedLessons,
            onValueChange = { newValue ->
                onUpdate(student.copy(skippedLessons = newValue))
            },
            containerColor =
                if (student.skippedLessons > 0) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.secondaryContainer,
            contentColor =
                if (student.skippedLessons > 0) MaterialTheme.colorScheme.onErrorContainer
                else MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.height(16.dp))

        EditableStatCard(
            title = "Выполнено ЛР",
            value = student.completedWorks,
            onValueChange = { newValue ->
                onUpdate(student.copy(completedWorks = newValue))
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun EditableStatCard(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = contentColor.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledIconButton(
                    onClick = { if (value > 0) onValueChange(value - 1) },
                    enabled = value > 0,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = contentColor.copy(alpha = 0.2f),
                        contentColor = contentColor
                    )
                ) {
                    Icon(painterResource(R.drawable.ic_remove), "")
                }

                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    modifier = Modifier.widthIn(min = 60.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                FilledIconButton(
                    onClick = { onValueChange(value + 1) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = contentColor.copy(alpha = 0.2f),
                        contentColor = contentColor
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Увеличить")
                }
            }
        }
    }
}

@Composable
fun AddStudentDialog(
    onDismissRequest: () -> Unit,
    onAdd: (name: String, skippedLessons: Int, completedWorks: Int) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var skippedLessons by rememberSaveable { mutableIntStateOf(0) }
    var completedWorks by rememberSaveable { mutableIntStateOf(0) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier.widthIn(min = 280.dp, max = 560.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Добавить студента",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ФИО") },
                    singleLine = true
                )

                NumberInputRow(
                    label = "Пропущенные занятия",
                    value = skippedLessons,
                    onValueChange = { skippedLessons = it }
                )

                NumberInputRow(
                    label = "Выполненные ЛР",
                    value = completedWorks,
                    onValueChange = { completedWorks = it }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Отмена")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onAdd(name.trim(), skippedLessons, completedWorks)
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Добавить")
                    }
                }
            }
        }
    }
}

@Composable
fun NumberInputRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledIconButton(
                onClick = { if (value > 0) onValueChange(value - 1) },
                enabled = value > 0
            ) {
                Icon(
                    painterResource(R.drawable.ic_remove), " "
                )
            }

            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 48.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            FilledIconButton(
                onClick = { onValueChange(value + 1) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Увеличить")
            }
        }
    }
}