package com.example.draggableview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val view = findViewById<DraggableView>(R.id.image)
        view.setOnClickListener {
            Log.d("TTT", "click on image")
        }
    }
}