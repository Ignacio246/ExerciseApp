package com.example.syncdata

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}
data class workoutData(val name: String = "", val valor: Int = 0)

class workOutviewmodel : ViewModel() {
    private val database = Firebase.database("https://appejercicio-8f7c8-default-rtdb.firebaseio.com/")
    private var _workoutData = mutableStateOf<List<workoutData>>(emptyList())
    val workoutData: State<List<workoutData>> = _workoutData

    fun getData() {
        database.getReference("workout").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _workoutData.value = snapshot.getValue<List<workoutData>>()!!
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Error getting data", error.toException())
                }
            }
        )
    }

    fun writeToDB(Workout: workoutData, index: Int) {
        val database = Firebase.database("https://appejercicio-8f7c8-default-rtdb.firebaseio.com/")
        val myRef = database.getReference("workout")
        listOf(Workout).forEach() {
            myRef.child(index.toString()).setValue(it)
        }
    }
}


@Composable
fun workOutList(workoutData: List<workoutData>) {
    //viewModel.getData()
    //val index = viewModel.workoutData.value.size
    Column {
        workoutData.forEach {
            Text(text = it.name)
            Text(text = it.valor.toString())
        }
    }
}


@Composable
fun workOutScreen(viewModel: workOutviewmodel ) {
    viewModel.getData()
    val index = viewModel.workoutData.value.size
    Column {
        TopAppBar(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(onClick = {
                viewModel.writeToDB(workoutData("Calorias", 1), index)
            }
            ) {
                Text(text = "Calorias", Modifier.background(Color.Yellow) ,color = Color.Gray )
            }
            Button(onClick = {
                viewModel.writeToDB(workoutData("Ritmo_cardiaco", 80), index)
            }) {

                Text(text = "RC", Modifier.background(Color.Yellow) ,color = Color.Gray)
            }
            Button(onClick = {
                viewModel.writeToDB(workoutData("Pasos", 1), index)
            }) {
                Text(text = "Pasos",Modifier.background(Color.Yellow) ,color = Color.Gray)
            }
            Button(onClick = {
                viewModel.writeToDB(workoutData("Distancia", 1), index)
            }) {
                Text(text = "Distancia",Modifier.background(Color.Yellow) ,color = Color.Gray)
            }
        }
        workOutList(viewModel.workoutData.value)
    }

}

@Composable
fun WearApp() {
    val viewModel = workOutviewmodel()
    workOutScreen(viewModel)
}



