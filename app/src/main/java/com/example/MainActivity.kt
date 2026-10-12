package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.MediaRepository
import com.example.data.local.AppDatabase
import com.example.ui.MainScreen
import com.example.ui.theme.CloudStreamTheme
import com.example.ui.viewmodel.CatalogViewModel
import com.example.ui.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(applicationContext) }
    private val repository by lazy { MediaRepository(database) }

    private val catalogViewModel: CatalogViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CatalogViewModel(repository) as T
            }
        }
    }

    private val playerViewModel: PlayerViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PlayerViewModel(repository) as T
            }
        }
    }

    @androidx.compose.material3.ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CloudStreamTheme {
                MainScreen(
                    catalogViewModel = catalogViewModel,
                    playerViewModel = playerViewModel
                )
            }
        }
    }
}
