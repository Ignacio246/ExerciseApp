package com.example.syncdata.presentation

import android.Manifest.permission.ACTIVITY_RECOGNITION
import android.Manifest.permission.BODY_SENSORS
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.compose.material.*
import com.example.syncdata.presentation.theme.WearAppTheme
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.syncdata.R
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference

class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {
    private val STEP_SENSOR_CODE = 10
    private lateinit var database: DatabaseReference
    private lateinit var ambientModeSupport: AmbientModeSupport.AmbientController

    //TODO ambient mode support
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this, BODY_SENSORS
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("permisos", "Permisos Concedidos")
            //TODO
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(ACTIVITY_RECOGNITION, BODY_SENSORS),
            STEP_SENSOR_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions, grantResults
        )
        when (requestCode) {
            STEP_SENSOR_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("DENIED", "Permisos Denegados")
                } else {
                    Log.i("GRANTED", "Permisos Concedidos")
                }
            }
        }
        fun getAmbientCallBack(): AmbientModeSupport.AmbientCallback =
            MyAmbientCallback()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPermissions()
        ambientModeSupport = AmbientModeSupport.attach(this)
        setContent {
            WearApp()
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()
}


private class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {
    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
    }

    override fun onExitAmbient() {
        // super.onExitAmbient()
    }

    override fun onUpdateAmbient() {
        //super.onUpdateAmbient()
    }
}

object NavRoute {
    const val HOME = "HOME"
    const val HeartRate = "HeartTRate"
    const val PASOS = "PASOS"
    const val DISTANCIA = "DISTANCIA"
    const val INFO = "INFO"
    const val RegistroPasos = "RegustroPasos"
    const val SCREEN_3 = "screen3"
    const val SCREEN_4 = "screen4/{sexo}/{edad}"
    const val  detailScreen = "detailScreen/{sexo}/{edad}/{altura}/{peso}{metro}"
    const val SENTADILLAS = "SENTADILLAS"
    const val ABDOMINALES = "ABDOMINALES"
    const val GUARDARDATOS = "GUARDARDATOS"
}

@Composable
fun WearApp() {
    WearAppTheme {
        val listState = rememberScalingLazyListState()
        Scaffold(timeText = {
            if (!listState.isScrollInProgress) {
                TimeText()

            }
        },
            vignette = {
                Vignette(vignettePosition = VignettePosition.Top)
            },
            positionIndicator = {
                PositionIndicator(scalingLazyListState = listState)
            }
        ) {
            ScalingLazyColumn(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            {
                item {

                    val navController = rememberSwipeDismissableNavController()

                    SwipeDismissableNavHost(
                        navController = navController,
                        startDestination = NavRoute.HOME
                    ) {
                        composable(NavRoute.HOME) {
                            HOME(navController)
                        }
                        composable(NavRoute.HeartRate) {
                            HeartRate(navController)
                        }
                        composable(NavRoute.PASOS) {
                            PASOS(navController)
                        }
                        composable(NavRoute.DISTANCIA) {
                            DISTANCIA(navigation = navController)
                        }
                        composable(NavRoute.INFO) {
                            INFO(navigation = navController)
                        }
                        composable(NavRoute.RegistroPasos) {
                            RegistroPasos(navigation = navController)
                        }
                        composable(NavRoute.SCREEN_3) {
                            Screen3(navigation = navController)
                        }
                        composable("Screen4/{sexo}/{edad}"){ backStackEntry ->
                            backStackEntry.arguments?.getString("sexo")
                            Screen4(
                                sexo = backStackEntry.arguments?.getString("sexo") ?: "0",
                                edad = backStackEntry.arguments?.getString("edad") ?: "0",
                                navigation = navController
                            )
                        }
                        composable("detailScreen/{sexo}/{edad}/{altura}/{peso}{metro}")  { backStackEntry ->
                            backStackEntry.arguments?.getString("sexo")
                            detailScreen(
                                sexo = backStackEntry.arguments?.getString("sexo") ?: "0",
                                edad = backStackEntry.arguments?.getString("edad") ?: "0",
                                altura = backStackEntry.arguments?.getString("altura") ?: "0",
                                peso = backStackEntry.arguments?.getString("peso") ?: "0",
                                metros = backStackEntry.arguments?.getString("metro") ?: "0",
                                navigation = navController
                            )
                        }
                        /*
                        composable(NavRoute.SENTADILLAS) {
                            SENTADILLAS()
                        }
                        composable(NavRoute.ABDOMINALES) {
                            ABDOMINALES()
                        }

                         */

                    }

                }
            }

        }


    }
}
/*
@Composable
fun SENTADILLAS() {
    var ritmocardiaco = getHeartRate()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Sentadillas",
            color = Color.Cyan,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = dist().toString(),
            color = Color.Cyan,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = "Calorias quemadas",
            color = MaterialTheme.colors.error,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = caloriasquemadas.toString(),
            color = MaterialTheme.colors.error,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = "Ritmo cardiaco",
            color = MaterialTheme.colors.error,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = ritmocardiaco,
            color = MaterialTheme.colors.error,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
    }
}

@Composable
fun ABDOMINALES() {
    var ritmocardiaco = getHeartRate()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Abdominales",
            color = Color.Cyan,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = dist().toString(),
            color = Color.Cyan,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = "Calorias quemadas",
            color = MaterialTheme.colors.error,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = caloriasquemadas.toString(),
            color = MaterialTheme.colors.error,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = "Ritmo cardiaco",
            color = MaterialTheme.colors.error,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = ritmocardiaco,
            color = MaterialTheme.colors.error,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
    }
}
*/
@Composable
fun HOME(navigation: NavController,
         modifier: Modifier = Modifier,
         iconModifier: Modifier = Modifier) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(35.dp))
        Text(text = "App de Ejercicio", style = MaterialTheme.typography.title2)
        Spacer(modifier = Modifier.height(5.dp))
        Chip(
            label = { Text(text = "No. de pasos") },
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.PASOS) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.steps),
                    contentDescription = "airplane",
                    modifier = iconModifier
                )
            }
            )
        Chip(
            label = { Text(text = "Ritmo Cardiaco") },
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.HeartRate) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ecg_heart),
                    contentDescription = "airplane",
                    modifier = iconModifier
                )
            }
        )
        Chip(
            label = { Text(text = "Distancia") },
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .fillMaxSize(),
            onClick = { navigation.navigate(NavRoute.RegistroPasos) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.social_distance),
                    contentDescription = "airplane",
                    modifier = iconModifier
                )
            }
            )
        Chip(
            label = { Text(text = "Calorias") },

            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .fillMaxSize(),
            onClick = { navigation.navigate(NavRoute.INFO) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.outline_whatshot_24),
                    contentDescription = "airplane",
                    modifier = iconModifier
                )
            }
            )
        Chip(
            label = { Text(text = "Status") },
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.DISTANCIA) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.settings_accessibility),
                    contentDescription = "airplane",
                    modifier = iconModifier
                )
            }
            )
        /*
        Chip(
            label = { Text(text = "Sentadillas") },
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .fillMaxSize(),
            onClick = { navigation.navigate(NavRoute.SENTADILLAS) },

            )
        Chip(
            label = { Text(text = "Abdominales") },
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .fillMaxSize(),
            onClick = { navigation.navigate(NavRoute.ABDOMINALES) },

            )

         */
        Chip(
            label = { Text(text = "Guardar datos") },
            modifier = Modifier
                .padding(5.dp)
                .fillMaxWidth()
                .fillMaxSize(),
            onClick = { navigation.navigate(NavRoute.GUARDARDATOS) },

            )

        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun HeartRate(navigation: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Icon(
            imageVector = Icons.Rounded.Favorite, contentDescription = " ",
            Modifier
                .size(30.dp)
                .wrapContentSize(
                    align = Alignment.Center
                ),
            tint = MaterialTheme.colors.onError
        )
        Text(
            text = "Ritmo cardiaco",
            color = MaterialTheme.colors.primary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp)
        )
        Text(
            text = getHeartRate(),
            color = MaterialTheme.colors.error,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )

    }
}


@Composable
fun PASOS(navigation: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Pasos",
            color = MaterialTheme.colors.primary,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Text(
            text = getSteps().toString(),
            color = MaterialTheme.colors.primaryVariant,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun DISTANCIA(navigation: NavController): Int  {
    var count = remember {
        mutableStateOf(0)
    }
    val hrstatus = getHeartRate()
    val steps = getSteps()
    var dist = (steps.toDouble() * 0.762).toInt()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Distancia recorrida:\n" +
                    "\n${dist()} " + "m",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Calorias quemadas:\n" +
                    "\n${caloriasquemadas.toString()} " ,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Numero de pasos:\n" +
                    "\n${getSteps()} ",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Ritmo cardiaco:\n" +
                    "\n${getHeartRate()} ",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.primary,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {navigation.navigate("HOME")}) {
            Text(text = "Guardar")
        }
        val database = Firebase.database
        var distanciarecorrida = database.getReference("distancia recorrida")
        var calorias = database.getReference("Calorias quemadas")
        var numpasos = database.getReference("numero de pasos")
        var ritmocardiaco = database.getReference("ritmo cardiaco")
        distanciarecorrida.setValue("$dist")
        calorias.setValue("$caloriasquemadas")
        numpasos.setValue("$steps")
        ritmocardiaco.setValue("$hrstatus")


    }
    return dist

}

@Composable
fun dist(): Int{
    var count = remember {
        mutableStateOf(0)
    }
    val steps = getSteps()
    var dist = (steps.toDouble() * 0.762).toInt()

    return dist
}

@Composable
fun getSteps(): Float {
    val ctx = LocalContext.current
    val sensorManager: SensorManager =
        ctx.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager
    val HeartRateSensor: Sensor = sensorManager.getDefaultSensor(
        Sensor.TYPE_HEART_RATE
    )
    var ststatus = remember {
        mutableStateOf(0f)
    }
    val heartRateSensorListener = object : SensorEventListener {
        override fun onSensorChanged(p0: SensorEvent?) {
            p0 ?: return
            p0.values.firstOrNull()?.let {
                ststatus.value = p0.values[0]
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            println("onAccuracyChanged  : Sensor : $p0; accuracy $p1")
        }

    }
    sensorManager.registerListener(
        heartRateSensorListener,
        HeartRateSensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )
    return ststatus.value
}

@Composable
fun getHeartRate(): String {
    val ctx = LocalContext.current
    val sensorManager: SensorManager =
        ctx.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager
    val HeartRateSensor: Sensor = sensorManager.getDefaultSensor(
        Sensor.TYPE_HEART_RATE
    )
    var hrtatus = remember {
        mutableStateOf("")
    }
    val heartRateSensorListener = object : SensorEventListener {
        override fun onSensorChanged(p0: SensorEvent?) {
            p0 ?: return
            p0.values.firstOrNull()?.let {
                hrtatus.value = p0.values[0].toString()
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            println("onAccuracyChanged  : Sensor : $p0; accuracy $p1")
        }

    }
    sensorManager.registerListener(
        heartRateSensorListener,
        HeartRateSensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )
    return hrtatus.value
}


@Composable
fun INFO(navigation: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Registro de informacion",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = { navigation.navigate("Screen3") }, colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Gray,
        )) {
            Icon(imageVector = Icons.Rounded.ArrowForward,
                contentDescription = "Next",
                tint = Color.Cyan)
        }

    }
}
public var distanciarecorrida = 0
public  var caloriasquemadas = 0
@Composable
fun detailScreen(sexo: String, edad: String, altura: String, peso: String,metros: String, navigation: NavController) {
    if (sexo == "Hombre"){
        var pasos = getSteps()
        var sexoChoose = 1
        var metros = metros.toFloat()
        var peso = peso.toFloat()
        var edad = edad.toFloat()
        var altura = altura.toFloat()
        var metro = (.60*3)
        var datos  = (66.5 + (13.75 + peso) + (5.003 * altura) - (6.775 * edad))
        var quemado = (pasos * 0.97)/1500
        var quemadoKcal = quemado * peso
        var totalquemado = quemadoKcal + datos
        caloriasquemadas = totalquemado.toInt()
        var distancia = metro
        distanciarecorrida = distancia.toInt()


        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Usted a quemado: ${totalquemado.toInt()} calorias",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    else{
        var pasos = getSteps()
        var sexoChoose = 1
        var peso = peso.toFloat()
        var edad = edad.toFloat()
        var altura = altura.toFloat()
        var datos = (655.1 + (9.563 + peso) + (1.85 * altura) - (4.676 * edad))
        var quemado = (pasos * 0.97)/1500
        var quemadoKcal = quemado * peso
        var totalquemado = quemadoKcal + datos

        caloriasquemadas = totalquemado.toInt()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "Usted a quemado: ${totalquemado.toInt()} calorias",
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}


@Composable
fun Screen3(navigation: NavHostController) {
    val sexo = listOf("Hombre", "Mujer" )
    val State = rememberPickerState(sexo.size)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        Picker(
            state = State,
            modifier = Modifier.size(100.dp, 100.dp)
        ) {

            Text(sexo[it], modifier = Modifier.padding(10.dp))
        }

    }
    //picker 2
    val edad = listOf("1", "2","3", "4", "5", "6", "7", "8", "9", "10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"
        ,"32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59","60","61","62","63","64","65","66","67","68","69","70","71","72","73","74"
        ,"75","76","77","78","79","80","81","82","83","84","85","86","87","88","89","90","91","92","93","94","95","96","97","98","99","100")
    val State2 = rememberPickerState(edad.size)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Picker(
            state = State2,
            modifier = Modifier.size(100.dp, 100.dp)
        ) {
            Text(edad[it], modifier = Modifier.padding(10.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(100.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            var position = State.selectedOption
            var position2 = State2.selectedOption
            var sexoChoose = sexo[position]
            var edadChoose = edad[position2]
            Button(
                onClick = {
                    navigation.navigate("screen4/$sexoChoose/$edadChoose")
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.LightGray
                )
            ) {
                Icon(imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.Cyan)

            }

        }
    }
}
@Composable
fun RegistroPasos(navigation: NavController) {
    val loc1 = LatLng(20.0365, -98.357)
    val loc2 = LatLng(20.0815, -98.3673)
    val locationA: Location = Location("Santiago")
    locationA.latitude = loc1.latitude
    locationA.longitude = loc1.longitude
    val locationB: Location = Location("Tulancingo")
    locationB.latitude = loc2.latitude
    locationB.longitude = loc2.longitude

    var distance: Float = locationA.distanceTo(locationB)
    distance /=1000

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Distancia entre dos puntos: ${distance.toString()} metros",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
    }
    Button(onClick = { navigation.navigate("HOME") })
    {
        Text(text = "Guardar")
    }
    val database = Firebase.database
    var distancia = database.getReference("distancia")
    distancia.setValue("$distance")
}



@Composable
fun Screen4( sexo: String, edad: String,navigation: NavController) {
    val sexo = sexo
    val edad = edad
    val altura = listOf("100", "101","102","103","104","105","106","107","108","109","110","111","112","113","114","115","116","117","118","119","120","121","122","123","124","125","126","127","128","129","130","131","132","133","134","135","136","137","138","139","140","141","142","143","144","145","146","147"
        ,"148","149","150","151","152","153","154","155","156","157","158","159","160","161","162","163","164","165","166","167","168","169","170","171","172","173","174","175","176","177","178","179","180","181","182","183","184","185","186","187","188","189","190","191","192","193","194","195","196","197","198","199","200",
    )
    val state = rememberPickerState(altura.size)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {

        Picker(
            state = state,
            modifier = Modifier.size(100.dp, 100.dp)
        ) {
            Text(altura[it], modifier = Modifier.padding(10.dp))
        }

    }
    //picker 2

    val peso = listOf("1", "2","3", "4", "5", "6", "7", "8", "9", "10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31"
        ,"32","33","34","35","36","37","38","39","40","41","42","43","44","45","46","47","48","49","50","51","52","53","54","55","56","57","58","59","60","61","62","63","64","65","66","67","68","69","70","71","72","73","74"
        ,"75","76","77","78","79","80","81","82","83","84","85","86","87","88","89","90","91","92","93","94","95","96","97","98","99","100")
    val state2 = rememberPickerState(peso.size)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Picker(
            state = state2,
            modifier = Modifier.size(100.dp, 100.dp)
        ) {
            Text(peso[it], modifier = Modifier.padding(10.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(100.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            var position = state.selectedOption
            var position2 = state2.selectedOption
            var altura = altura[position]
            var peso  = peso[position2]
            Button(
                onClick = {
                    navigation.navigate("detailScreen/${sexo.toString()}/${edad.toString()}/${altura.toString()}/${peso.toString()}")
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Gray
                )
            ) {
                Icon(imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.Cyan)

            }

        }
    }
}

/*Firebase*/

    data class workoutData(val name: String = "", val valor: Int = 0)

    class workOutviewmodel : ViewModel() {
        private val database =
            Firebase.database("https://appejercicio-8f7c8-default-rtdb.firebaseio.com/")
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
            val database =
                Firebase.database("https://appejercicio-8f7c8-default-rtdb.firebaseio.com/")
            val myRef = database.getReference("workout")
            listOf(Workout).forEach() {
                myRef.child(index.toString()).setValue(it)
            }
        }
    }

    @Composable
    fun workOutScreen(viewModel: workOutviewmodel) {
        val index = viewModel.workoutData.value.size
        Box(modifier = Modifier.fillMaxWidth()) {
            ScalingLazyColumn() {
                item {
                    Button(onClick = {
                        viewModel.writeToDB(workoutData("carrera", 10), index)
                    }) {
                        Text(text = "Add to Firebase")
                    }
                }
            }
        }
    }

