package pro.maximon.lab4.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pro.maximon.lab4.data.repository.StudentRepository
import pro.maximon.lab4.models.StudentItem
import kotlin.uuid.Uuid

class MainViewModel(
    private val studentRepository: StudentRepository,
) : ViewModel() {
    val items = studentRepository
        .getStudents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList(),
        )

    private val _editingState = MutableStateFlow<Uuid?>(null)
    val editingState = _editingState.asStateFlow()

    fun addItem() {
        val item = StudentItem(
            name = "Student ${items.value.size + 1}",
        )
        viewModelScope.launch {
            studentRepository.addStudent(item)
        }
    }

    fun addStudent(name: String, skippedLessons: Int, completedWorks: Int) {
        val item = StudentItem(
            name = name,
            skippedLessons = skippedLessons,
            completedWorks = completedWorks,
        )
        viewModelScope.launch {
            studentRepository.addStudent(item)
        }
    }

    fun startEditing(itemId: Uuid) {
        _editingState.value = itemId
    }

    fun stopEditing() {
        _editingState.value = null
    }

    fun removeItem(id: Uuid) {
        viewModelScope.launch {
            studentRepository.removeStudent(id)
        }
    }

    fun updateItem(id: Uuid, updatedItem: StudentItem) {
        val itemToSave = if (updatedItem.id == id) updatedItem else updatedItem.copy(id = id)
        viewModelScope.launch {
            studentRepository.updateStudent(itemToSave)
        }
    }
}