package com.example.googlemaps


import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import android.Manifest
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.text.ListFormatter.Width
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextLayoutInput
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavGraph
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.googlemaps.Repositories.CityRepository
import com.example.googlemaps.Repositories.CountryRepository
import com.example.googlemaps.Repositories.SessionRepository
import com.example.googlemaps.Repositories.SettingsRepository
import com.example.googlemaps.Repositories.UsersRepository
import com.example.googlemaps.Services.LocationForegroundService
import com.example.googlemaps.Services.globalMap
import com.example.googlemaps.entities.City
import com.example.googlemaps.entities.Country
import com.example.googlemaps.entities.Session
import com.example.googlemaps.entities.Settings
import com.example.googlemaps.entities.Users
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.ktx.model.cameraPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.sql.Time
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import java.util.concurrent.TimeUnit

class CountryViewModel(application: Application) : AndroidViewModel(application){

    val repository : CountryRepository
    var countryList : LiveData<List<Country>>

    init{

        var countryDao = UserRoomDatabase.getInstance(application).CountryDao()
        repository = CountryRepository(countryDao)
        countryList = countryDao.getAllCountries()
    }

    fun addData(country: Country){
        repository.addCountry(country)
    }

    fun changeData(){
        CoroutineScope(Dispatchers.IO).launch {
            var country = repository.getAllCountries().first().filter { it.name == "Russia" }?.first()
            country?.name = "USA"
            country?.let { repository.changeData(it) }
        }
    }

    fun addAllToFirebase(){
        val database = Firebase.database
        var myRef = database.getReference("Countries")
        CoroutineScope(Dispatchers.IO).launch {
           var countries = repository.getAllCountries().first()
            countries.forEach{
                myRef.child(it.id.toString()).setValue(it)
            }
        }
    }

    fun getCountryByName(name: String) : Country {
        var result = CoroutineScope(Dispatchers.IO).async {
            repository.getCountryByName(name).first()
        }
        return runBlocking {
            result.await()
        }
    }

    fun getAllCountriesList() : List<Country>{
        var z = repository.getAllCountries()

        var x = CoroutineScope(Dispatchers.IO).async {
            z.first()
        }
        return runBlocking {
            x.await()
        }
    }
}

class CityViewModel(application: Application) : AndroidViewModel(application){

    val repository : CityRepository
    var cityList : LiveData<List<City>>

    init{
        val cityDao = UserRoomDatabase.getInstance(application).CityDao()
        repository = CityRepository(cityDao)
        cityList = repository.cityList
    }

    fun addCity(city: City){
        repository.addCity(city)
    }



}

class UsersViewModel(application: Application) : AndroidViewModel(application){
    val repository : UsersRepository
    val userList : LiveData<List<Users>>

    init{
        val usersDao = UserRoomDatabase.getInstance(application).UsersDao()
        repository = UsersRepository(usersDao = usersDao)
        userList = repository.users
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application){
    val repository : SettingsRepository
    val settingsList : LiveData<List<Settings>>

    init{
        val settingsDao = UserRoomDatabase.getInstance(application).SettingsDao()
        repository = SettingsRepository(settingsDao)
        settingsList = repository.settings
    }
}

class SessionsViewModel(application: Application) : AndroidViewModel(application) {
    val repository: SessionRepository
    val settingsList: LiveData<List<Session>>

    init {
        val sessionDao = UserRoomDatabase.getInstance(application).SessionDao()
        repository = SessionRepository(sessionDao)
        settingsList = repository.sessions
    }
}

class MapViewModel : ViewModel() {

    private val _polylinePoints = mutableStateListOf<LatLng>()
    var polylinePoints: List<LatLng> by mutableStateOf(_polylinePoints.toList())

    fun addPoint(point: LatLng) {
        _polylinePoints.add(point)
        polylinePoints = _polylinePoints.toList()
    }

    fun clearPoints(){
        _polylinePoints.clear()
        polylinePoints = _polylinePoints.toList()
    }
}

class MainActivity : FragmentActivity() {

    //region startTestVariables
    val countries = listOf("Russia", "USA", "Emperium Of Humanity")
    val citiesRussia = listOf("Tyumen", "Moskov", "Saint Petersburg", "Noyabrks", "Ekaterinburg")
    val citiesUSA = listOf("Portland", "Austin","Denver","Cleveland","Charlotte")
    val emperiumCities = listOf("Cadia","Macragge","Nocturne","Fenris","Vaal")
//    val countriesRepo = CountryRepository(cityDao)
    //endregion

    //region auth variables
    private var email = mutableStateOf("")
    private var password = mutableStateOf("")
    private var city = mutableStateOf(City())
    private var country = mutableStateOf(Country())
    private var name = mutableStateOf("")
    private var unitOfMeasurement = mutableStateOf("")
    //endregion

    //region start variables

    private var x = 0.003
    private var currentMarker : Marker? = null

    private var isProjectStarted = mutableStateOf(false)
    private var auth = Firebase.auth
    private var spinnerElements = arrayOf("метр", "фут")
    private var database = Firebase.database.reference
    private val coroutineScope  = CoroutineScope(Dispatchers.IO)
    private var unitOfDistance = 0.0;
    private var stackOfViews = ArrayDeque<View>();
    private var firstCameraMove = true;

    private lateinit var navController : NavHostController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var applicationUser : Users
    private lateinit var userSettings : Settings
    private lateinit var lastLocation: Location
    private lateinit var session: Session
    private lateinit var currentLatLng : LatLng
    private lateinit var map: GoogleMap
    companion object {
        private const val LOCATION_PERMISSION_CODE = 1
    }
    var permissionChecked = false
    //endregion

    //region start variables with delegates
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permission ->
        if(permission[Manifest.permission.ACCESS_FINE_LOCATION] == true){
            val serviceIntent = Intent(this, LocationForegroundService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }

    }

    private val locationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent?.getDoubleExtra("latitude", 0.0) ?: return
            val longitude = intent.getDoubleExtra("longitude", 0.0)

            val location = LatLng(latitude + x, longitude)
            if (isProjectStarted.value){
                mapViewModel.addPoint(location)
            }
            updateLocationOnMap(latitude, longitude)
        }
    }
//endregion

    //region mutableStates
    private var  mutableLatLng  = mutableStateOf(LatLng(0.0, 0.0))
    private var mutableStartButtonText = mutableStateOf("Старт")
    private var totalDistance = mutableStateOf(0.0)
    private var userSessions = mutableStateOf(emptyList<Session>())
    private var resultCardVisibility = mutableStateOf(false)
    private var justStarted = mutableStateOf(false)
    //endregion

    //region viewModels
    private lateinit var cityViewModel : CityViewModel
    private lateinit var countryViewModel : CountryViewModel
    private lateinit var usersViewModel : UsersViewModel
    private lateinit var settingsViewModel : SettingsViewModel
    private lateinit var sessionsViewModel : SessionsViewModel
    private val mapViewModel : MapViewModel by viewModels()

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cityViewModel = ViewModelProvider(this)[CityViewModel::class]
        countryViewModel = ViewModelProvider(this)[CountryViewModel::class]
        usersViewModel = ViewModelProvider(this)[UsersViewModel::class]
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class]
        sessionsViewModel = ViewModelProvider(this)[SessionsViewModel::class]
        lifecycleScope.launch {
            val cityCount = cityViewModel.repository.getAllCitiesList().count()
            if (cityCount == 0) {
                getDataOnFirstStart() // suspend-функция
            }
            //logOut()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val user = usersViewModel.repository.getUserByEmail(currentUser.email.toString())
                    applicationUser = user
                    userSettings = settingsViewModel.repository.getUsersSettings(applicationUser.email)
                    userSessions.value = sessionsViewModel.repository.getAllSessions().filter { it.userEmail == applicationUser.email }
                    unitOfMeasurement.value = userSettings.distanceUnit!!
                    setUnitOfDistanceValue()
                    checkPermission()

            }
        setContent {
            navController = rememberNavController()
            NavHost(navController = navController, startDestination =  if (currentUser == null) "login" else "home") {
                composable("home") {
                    elementWithHeader { googleMap(mapViewModel) }
                }
                composable("champions") {

                    elementWithHeader { championsPageList(findChampions()) }
                }
                composable("history") {
                    elementWithHeader { historyPageList() }
                }
                composable("settings") {
                    elementWithHeader { settings() }
                }
                composable("login") {
                    loginPage()
                }
                composable("placeChoose") {
                    placeChoose()
                }

                composable("registerPage") {
                    registerPage()
                }
            }


            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(locationReceiver, IntentFilter("UPDATE_LOCATION"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(locationReceiver)
    }

    private fun login() {
        if (email.value.isEmpty() || password.value.isEmpty()){
            Toast.makeText(
                baseContext,
                "Заполните все поля",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        auth.signInWithEmailAndPassword(email.value, password.value.toString())
            .addOnCompleteListener(this){
                task -> if(task.isSuccessful){
                  Log.d("Success LOGIN", "user logging")
                    val user = auth.currentUser
                    applicationUser = usersViewModel.repository.getUserByEmail(email.value)
                    var k = usersViewModel.repository.getAllUsers()
                    var z = settingsViewModel.repository.getUsersSettings(applicationUser.email)
                    userSettings = z
                    navController.navigate("home")
                    userSessions.value = sessionsViewModel.repository.getAllSessions().filter { it.userEmail == applicationUser.email }
                    checkPermission()

                }else{
                    Log.d("Error login", "Email of Paswword incorrect on login")
                    Toast.makeText(
                        baseContext,
                        "Authentication failed.",
                        Toast.LENGTH_SHORT,
                    ).show()
                    }
            }
    }

    private fun registerUser() {

        if (!email.value.isEmpty() && !password.value.isEmpty() && !name.value.isEmpty()){
            auth.createUserWithEmailAndPassword(email.value, password.value)
                .addOnCompleteListener(this) {
                    task ->
                    if (task.isSuccessful){
                        val user = auth.currentUser
                        var currentUser = Users(email = email.value,
                            password = password.value,
                            name = name.value, cityId = city.value.id)
                        usersViewModel.repository.addUser(currentUser)
                        applicationUser = currentUser
                        firebaseUserAdd()
                        userSettings = Settings(unit = unitOfMeasurement.value, userEmail = applicationUser.email!!)
                        settingsViewModel.repository.setUsersSettings(userSettings)
                        addCurrentSettings()
                        navController.navigate("home")
                        checkPermission()
                    }
                    else{
                        Toast.makeText(
                            baseContext,
                            "Autentifiation failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
        }else{
            Toast.makeText(
                baseContext,
                "Заполните все поля",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun checkPermission(){
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
        {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
        }
        else{
            val serviceIntent = Intent(this, LocationForegroundService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        }
    }

    private fun updateLocationOnMap(latitude : Double, longitude : Double ) {

        var  previousPosition : LatLng? = null
        if (::currentLatLng.isInitialized){
            if (firstCameraMove){
                justStarted.value = !justStarted.value
                firstCameraMove = !firstCameraMove
            }
            previousPosition = LatLng(currentLatLng.latitude , currentLatLng.longitude)
        }

        currentLatLng = LatLng(latitude, longitude)
        mutableLatLng.value = currentLatLng
        x +=0.0001

        if (isProjectStarted.value && previousPosition != null) {
            totalDistance.value += SphericalUtil.computeDistanceBetween(
                previousPosition,
                currentLatLng)
       }
    }

    private fun saveChanges(userEmail : String){
        var user = usersViewModel.repository.getUserByEmail(userEmail)
        addCurrentSettings()
        settingsViewModel.repository.updateUserSettings(userSettings)
    }

    private fun setUnitOfDistanceValue(){
               if (userSettings.distanceUnit == "метр"){
            unitOfDistance = 1.0
        }
        else{
            unitOfDistance = 0.3048
        }
    }

    private fun logOut(){
        FirebaseAuth.getInstance().signOut()
    }

    private fun firebaseUserAdd(){
        database.child("users").child(applicationUser.email.split('.')[0]).setValue(applicationUser)
    }

    private fun addCurrentSettings(){
        database.child("settings").child(userSettings.userEmail!!.split('.')[0]).setValue(userSettings)
    }

    private fun firebaseAddSession(){
        database.child("sessions").child(session.userEmail!!.split('.')[0]).child(session.startedAt.toString()).setValue(session)
    }

    private suspend fun getDataOnFirstStart() {

        val tempDatabase = FirebaseDatabase.getInstance()
        runBlocking {
            coroutineScope{
                var countries = async {fetchCountries(tempDatabase)}
                var cities = async {fetchCities(tempDatabase)}
                var users = async {fetchUsers(tempDatabase)}
                var sessions = async {fetchSessions(tempDatabase)}
                var settings = async {fetchSettings(tempDatabase)}

                countries.await()
                cities.await()
                users.await()
                sessions.await()
                settings.await()
            }

        }
        Log.d("success", "all data added")
    }

    private suspend fun fetchCountries(database: FirebaseDatabase) {
        val countryRef = database.getReference("countries")
        val snapshot = countryRef.get()
            .addOnSuccessListener {snapshot ->
                Log.d("Firebase Snapshot", snapshot.value.toString())
        snapshot.children.map {
            it.children.map { item ->
                item.value
            }
        }.forEach {
            Log.d("Countries added", "Countries added")
            val id = it[0] as Long
            val name = it[1] as String
            countryViewModel.addData(Country(id = id, name = name))
        }
            }

        countryRef.get().addOnFailureListener{
            Log.e("firebase error","fail data listener")
        }

    }

    private suspend fun fetchCities(database: FirebaseDatabase) {
        val cityRef = database.getReference("cities")
        val snapshot = cityRef.get().addOnSuccessListener { snapshot ->
            snapshot.children.map {
                it.children.map { item ->
                    item.value
                }
            }.forEach {
                Log.d("Cities added", "Cities added")
                val id = it[1] as Long
                val name = it[2].toString()
                val countryId = it[0] as Long
                cityViewModel.addCity(City(id = id, name = name, countryId = countryId))
            }
        }

    }

    private suspend fun fetchUsers(database: FirebaseDatabase) {
        val userRef = database.getReference("users")
        val snapshot = userRef.get().addOnSuccessListener { snapshot ->
        if (snapshot.value != null) {
            snapshot.children.map {
                it.children.map {
                    it.value
                }
            }.forEach {
                val cityId = it[0] as Long
                val tempMap = it[1] as HashMap<*, *>
                val createdAt = tempMap["time"] as Long
                val email = it[2] as String
                val name = it[3] as String
                val password = it[4] as String
                usersViewModel.repository.addUser(
                    Users(
                        email,
                        password,
                        name,
                        cityId,
                        Date(createdAt)
                    )
                )
            }
        }
        }
    }

    private suspend fun fetchSessions(database: FirebaseDatabase) {
        val sessionRef = database.getReference("sessions")
        val snapshot = sessionRef.get().addOnSuccessListener { snapshot ->
        if (snapshot.value != null) {
            snapshot.children.map {
                it.children.map { item ->
                    item.children.map {
                        it.value
                    }
                }.forEach {
                    val distance = it[0].toString().toInt()
                    val tempEndAt = it[1] as HashMap<*, *>
                    val endAt = Date(tempEndAt["time"] as Long)
                    val tempStartAt = it[3] as HashMap<*, *>
                    val startAt = Date(tempStartAt["time"] as Long)
                    val userEmail = it[4] as String

                    sessionsViewModel.repository.addSession(
                        Session(
                            startAt,
                            distance,
                            userEmail,
                            endAt
                        )
                    )
                }
            }
            }
        }
    }

    private suspend fun fetchSettings(database: FirebaseDatabase) {
        val settingsRef = database.getReference("settings")
        val snapshot = settingsRef.get().addOnSuccessListener { snapshot ->
        if (snapshot.value != null) {
            snapshot.children.map {
                it.children.map {
                    it.value
                }
            }.forEach {
                val unit = it[0] as String
                val id = it[1] as Long
                val userEmail = it[2] as String
                settingsViewModel.repository.setUsersSettings(Settings(unit, userEmail))
            }
        }
        }
    }

    private fun startButtonClick(){
        if (!isProjectStarted.value) {
            mutableStartButtonText.value = "Стоп"
            isProjectStarted.value = !isProjectStarted.value
            mapViewModel.clearPoints()
            totalDistance.value = 0.0
            session = Session(applicationUser.email)

        }else{
            resultCardVisibility.value = true
            session.endAt = Calendar.getInstance().time
            session.distance = totalDistance.value.toInt()
            userSessions.value += session
            sessionsViewModel.repository.addSession(session)
            firebaseAddSession()
            isProjectStarted.value = !isProjectStarted.value;
            mutableStartButtonText.value = "Старт"
        }
    }

    private fun findChampions() :  List<Pair<Users?, Int>> {
        var topSessions = sessionsViewModel.repository.getAllSessions().sortedByDescending { it.distance }.take(10)
        var usersMap = usersViewModel.repository.getAllUsers().associateBy { it.email }
        var result = topSessions.map { session ->
            usersMap[session.userEmail].let{ user ->
                user to session.distance
            }
        }
        return result
    }

    //region UI
    @Composable
    private fun historyPageList(){
        LazyColumn {
            itemsIndexed(userSessions.value){ index, item ->
                Text("${index}. ${applicationUser.name} - ${(item.distance / unitOfDistance).toInt()} ${userSettings.distanceUnit}")
            }
        }
    }

    @Composable
    private fun championsPageList(topList : List<Pair<Users?, Int>>){
        LazyColumn {
            itemsIndexed(topList){ index, item ->
                Text("${index}. ${item.first!!.name} - ${(item.second / unitOfDistance).toInt()} ${userSettings.distanceUnit}")

            }
        }
    }

    @Composable
    private fun Header(){
        Row(Modifier
            .background(color = Color(0xFF757575))
            .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround){

           Button(onClick = {
               navController.navigate("history")
           }, colors = ButtonDefaults.buttonColors(Color(0xFF757575))) {
               Text("История", color = Color.Black)
           }

            Button(onClick = ({navController.navigate("home")}) , colors = ButtonDefaults.buttonColors(Color(0xFF757575))){
                Text("В путь", color = Color.Black)
            }
            Button(onClick = {navController.navigate("champions") }, colors = ButtonDefaults.buttonColors(Color(0xFF757575))) {
                Image(imageVector = ImageVector.vectorResource(R.drawable.free_icon_trophy_1152912), "trophy",
                modifier = Modifier
                    .width(30.dp)
                    .width(30.dp)
                    .background(Color(0xFF757575)))

            }
            Button(onClick = { navController.navigate("settings") }, colors = ButtonDefaults.buttonColors(Color(0xFF757575))) {
                Image(ImageVector.vectorResource(R.drawable.gear_alt_svgrepo_com), "settings",
                    modifier = Modifier
                        .width(30.dp)
                        .width(30.dp))
            }
        }
    }

    @Composable
    private fun settings() {
        var expanded by remember { mutableStateOf(false) }
        var selectedItemUnit by remember { mutableStateOf(spinnerElements[0]) }
        Box(modifier = Modifier.fillMaxSize()){
            Column (modifier = Modifier.align(Alignment.Center)){
                Row(
                    modifier = Modifier.clickable {
                        expanded = !expanded
                    },
                ) {
                     Text(text = unitOfMeasurement.value)
                    Icon(imageVector = Icons.Filled.ArrowDropDown, "downList")
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        spinnerElements.forEach { itemSpinner ->
                            DropdownMenuItem(
                                text = { Text(itemSpinner) },
                                onClick = {
                                    expanded = false
                                    selectedItemUnit = itemSpinner
                                    unitOfMeasurement.value = selectedItemUnit
                                    userSettings.distanceUnit = unitOfMeasurement.value
                                    saveChanges(applicationUser.email)
                                }
                            )
                        }
                    }
                }
                Button(onClick = {
                    logOut()
                    navController.navigate("login")
                }) {
                    Text("Выйти")
                }
            }
        }

    }

    @Composable
    private fun googleMap(mapViewModel : MapViewModel){
        var cameraPositionState = rememberCameraPositionState()
        var coroutineCamera = rememberCoroutineScope()
        var points = mapViewModel.polylinePoints

        fun centerCamera(){
            coroutineCamera.launch {
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f),
                    durationMs = 300
                )
            }
        }

        if (justStarted.value){
            centerCamera()
            justStarted.value = !justStarted.value
        }

        Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    Marker(state = MarkerState(mutableLatLng.value))

                    Polyline(points = points,
                            color = Color.Red,
                            width = 8f)

                }
                if (resultCardVisibility.value) {
                    Card(modifier = Modifier.align(Alignment.Center)) {
                        resultWindow()
                    }

                }
                IconButton (
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(-10.dp, -350.dp),
                    onClick ={
                    centerCamera()
                }) {
                    Image(
                        painter = painterResource(id = R.drawable.location), "searchButton",
                        modifier = Modifier
                            .width(45.dp)
                            .height(45.dp)

                    )
                }
            Text("Преодоленное расстояние: ${(totalDistance.value.toLong() / unitOfDistance).toInt()} ${userSettings.distanceUnit}",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(0.dp, -70.dp))
            Button(modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(0.dp, -20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray, contentColor = Color(0xFF0285FF)),
                shape = RectangleShape,
                onClick = {startButtonClick()}) {
                Text(mutableStartButtonText.value)
            }
        }
    }

    @Composable
    private fun loginPage(){
        Box(modifier = Modifier.fillMaxSize()){
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                TextField(email.value, onValueChange = { newEmail ->
                    email.value = newEmail

                }, placeholder = { Text("Почта") })
                Spacer(modifier = Modifier.height(30.dp))
                TextField(password.value,
                    placeholder = { Text("Пароль") },
                    visualTransformation = PasswordVisualTransformation(),
                    onValueChange = { newPassword ->
                    password.value = newPassword

                })
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(contentColor = Color(0xFF0285FF), containerColor = Color.LightGray),
                    shape = RectangleShape,
                    onClick = {login()}
                ) {
                    Text("Войти")
                }
                Button(
                    colors = ButtonDefaults.buttonColors(contentColor = Color(0xFF0285FF), containerColor = Color.LightGray),
                    shape = RectangleShape,
                    onClick = {navController.navigate("placeChoose")}
                ) {
                    Text("Регистрация")
                }
            }

        }
    }

    @Composable
    private fun placeChoose(){
        var countries = countryViewModel.getAllCountriesList()
        var cities = cityViewModel.repository.getAllCitiesList()
        var selectedItemCountry by remember {mutableStateOf(countries.first())}
        var selectedItemCity by remember {mutableStateOf(cities.first())}
        var expandedCountries by remember {mutableStateOf(false)}
        var expandedCities by remember {mutableStateOf(false)}
        var localCities by remember { mutableStateOf(cities.filter { it.countryId == selectedItemCountry.id }) }
        Box(modifier = Modifier.fillMaxSize()){
            Column(modifier = Modifier.align(Alignment.Center)){
                Row(
                    modifier = Modifier.clickable {
                        expandedCountries = !expandedCountries
                    },
                ) {
                    Text(text = selectedItemCountry.name!!)
                    Icon(imageVector = Icons.Filled.ArrowDropDown, "downList")
                    DropdownMenu(
                        expanded = expandedCountries,
                        onDismissRequest = { expandedCountries = false }
                    ) {
                        countries.forEach { itemCountry ->
                            DropdownMenuItem(
                                text = { Text(itemCountry.name!!) },
                                onClick = {
                                    expandedCountries = false
                                    selectedItemCountry = itemCountry
                                    localCities = cities.filter { it.countryId == selectedItemCountry.id }
                                    selectedItemCity = localCities.first()

                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
                Row (
                    modifier = Modifier.clickable {
                        expandedCities = !expandedCities
                    },
                ){
                    Text(text = selectedItemCity.name!!)
                    Icon(imageVector = Icons.Filled.ArrowDropDown, "downList")
                    DropdownMenu(
                        expanded = expandedCities,
                        onDismissRequest = { expandedCities = false }
                    ) {
                        localCities.forEach { itemCity ->
                            DropdownMenuItem(
                                text = { Text(itemCity.name!!) },
                                onClick = {
                                    expandedCities = false
                                    selectedItemCity = itemCity
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(50.dp))
                Button(
                    onClick = {
                        city.value = selectedItemCity
                        country.value = selectedItemCountry
                        navController.navigate("registerPage")
                    }
                ){
                    Text("Дальше")
                }
            }
        }
    }

    @Composable
    private fun registerPage(){
        var expanded by remember { mutableStateOf(false) }
        var selectedItemUnit by remember { mutableStateOf(spinnerElements[0]) }
         Box(modifier = Modifier.fillMaxSize()){
             Column(modifier = Modifier.align(Alignment.Center)) {
                 TextField(email.value,
                     onValueChange = {email.value = it},
                     placeholder = { Text("Почта") })
                 Spacer(modifier = Modifier.height(50.dp))
                 TextField(password.value,
                     onValueChange = {password.value = it},
                     placeholder = {Text("Пароль")},
                     visualTransformation = PasswordVisualTransformation())
                 Spacer(modifier = Modifier.height(50.dp))
                 TextField(name.value,
                     onValueChange = {name.value = it},
                     placeholder = {Text("Имя")})
                 Spacer(modifier = Modifier.height(50.dp))
                 Row(
                     modifier = Modifier.clickable {
                         expanded = !expanded
                     },
                 ) {
                     Text(text = selectedItemUnit)
                     Icon(imageVector = Icons.Filled.ArrowDropDown, "downList")
                     DropdownMenu(
                         expanded = expanded,
                         onDismissRequest = { expanded = false }
                     ) {
                         spinnerElements.forEach { itemSpinner ->
                             DropdownMenuItem(
                                 text = { Text(selectedItemUnit) },
                                 onClick = {
                                     expanded = false
                                     selectedItemUnit = itemSpinner
                                 }
                             )
                         }
                     }
                 }
                 Spacer(modifier = Modifier.height(50.dp))
                 Button(onClick = {
                     unitOfMeasurement.value = selectedItemUnit
                     registerUser()
                 }) {
                     Text("Зарегестрироваться")
                 }
                 Spacer(modifier = Modifier.height(50.dp))
                 Button(onClick = {navController.navigate("login")}
                 ){
                     Text("Войти")
                 }
             }
         }
    }

    @Composable
    private fun elementWithHeader(component : @Composable () -> Unit){
        setUnitOfDistanceValue()
        Column {
            Header()
            component()
        }
    }

    @Composable
    private fun resultWindow(){
        val durationMillis = session.endAt!!.time - session.startedAt!!.time
        val durationSeconds = durationMillis.toDouble() / 1000
        val averageSpeedKmH = (totalDistance.value / durationSeconds * 3.6)
        Card (
            modifier = Modifier
                .width(300.dp)
                .height(240.dp)
        ){
            Column (modifier = Modifier
                .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text("Итоговая дистанция: ${(totalDistance.value / unitOfDistance).toInt()} ${unitOfMeasurement.value}ов")
                Text("Время в пути: ${(durationSeconds / 60).toInt()} минут")
                Text("Средняя скорость: ${averageSpeedKmH.toInt()} км/ч")
                Text("Потраченные каллории: ${(1.3 / 60 * durationSeconds  * averageSpeedKmH).toInt() }")
                Button(onClick = {
                    resultCardVisibility.value = false
                })
                {
                    Text("Принял")
                }
            }
        }
    }


//endregion

}

