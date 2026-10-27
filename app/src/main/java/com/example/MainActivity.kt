package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AcademyDatabase
import com.example.data.AcademyRepository
import com.example.ui.AcademyApp
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AcademyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AcademyDatabase.getInstance(applicationContext)
        val repository = AcademyRepository(database.academyDao())

        setContent {
            MyApplicationTheme {
                val academyViewModel: AcademyViewModel = viewModel(
                    factory = AcademyViewModel.Factory(repository)
                )
                AcademyApp(viewModel = academyViewModel)
            }
        }
    }
}
