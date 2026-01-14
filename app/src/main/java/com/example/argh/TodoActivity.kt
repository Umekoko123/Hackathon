package com.example.argh

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class TodoActivity : AppCompatActivity() {

    private lateinit var taskInput: EditText
    private lateinit var addBtn: Button
    private lateinit var listView: ListView

    private var tasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        val toolbar: Toolbar = findViewById(R.id.toolbarTodo)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        taskInput = findViewById(R.id.edtTask)
        addBtn = findViewById(R.id.btnAddTask)
        listView = findViewById(R.id.listTasks)

        tasks = TaskStorage.load(this)
        refreshList()

        addBtn.setOnClickListener {
            val text = taskInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val id = TaskStorage.nextId(this)
            tasks.add(Task(id, text, false))
            TaskStorage.save(this, tasks)
            taskInput.setText("")
            refreshList()
        }

        // Tap toggles done
        listView.setOnItemClickListener { _, _, position, _ ->
            tasks[position].isDone = !tasks[position].isDone
            TaskStorage.save(this, tasks)
            refreshList()
        }

        // Long press deletes
        listView.setOnItemLongClickListener { _, _, position, _ ->
            tasks.removeAt(position)
            TaskStorage.save(this, tasks)
            refreshList()
            true
        }
    }

    private fun refreshList() {
        val display = tasks.map { t ->
            if (t.isDone) "✅ ${t.title}" else "⬜ ${t.title}"
        }
        listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, display)
    }
}
