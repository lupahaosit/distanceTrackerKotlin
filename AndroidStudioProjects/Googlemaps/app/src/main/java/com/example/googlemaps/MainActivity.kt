package com.example.googlemaps


import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import android.Manifest
import android.content.Context
import android.media.Image
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import com.example.googlemaps.Repositories.CityRepository
import com.example.googlemaps.Repositories.CountryRepository
import com.example.googlemaps.Repositories.SessionRepository
import com.example.googlemaps.Repositories.SettingsRepository
import com.example.googlemaps.Repositories.UsersRepository
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
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.maps.android.SphericalUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Dictionary
import java.util.HashMap
import java.util.Locale

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

class MainActivity : FragmentActivity(), OnMapReadyCallback {

    //region startTestVariables
    val countries = listOf("Russia", "USA", "Emperium Of Humanity")
    val citiesRussia = listOf("Tyumen", "Moskov", "Saint Petersburg", "Noyabrks", "Ekaterinburg")
    val citiesUSA = listOf("Portland", "Austin","Denver","Cleveland","Charlotte")
    val emperiumCities = listOf("Cadia","Macragge","Nocturne","Fenris","Vaal")
//    val countriesRepo = CountryRepository(cityDao)
    //endregion

    //region start variables
    lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var x = 0.003
    private lateinit var lastLocation: Location
    private var currentMarker : Marker? = null
    private var totalDistance : Double = 0.0
    private var isProjectStarted = false
    private var auth = Firebase.auth
    private var spinnerElements = arrayOf("метр", "фут")
    private lateinit var country : Country
    private lateinit var city : City
    private lateinit var applicationUser : Users
    private lateinit var userSettings : Settings
    private var database = Firebase.database.reference
    private val coroutineScope  = CoroutineScope(Dispatchers.IO)
    private lateinit var session: Session
    private lateinit var currentLatLng : LatLng
    private lateinit var polyline : PolylineOptions
    private lateinit var currentPolyline : Polyline
    private val polylinePoints = mutableListOf<Polyline>()
    private var justStarted = true
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
            startLocationUpdates()
        }
    }
//endregion

    //region viewModels
    private lateinit var cityViewModel : CityViewModel
    private lateinit var countryViewModel : CountryViewModel
    private lateinit var usersViewModel : UsersViewModel
    private lateinit var settingsViewModel : SettingsViewModel
    private lateinit var sessionsViewModel : SessionsViewModel

    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cityViewModel = ViewModelProvider(this)[CityViewModel::class]
        countryViewModel = ViewModelProvider(this)[CountryViewModel::class]
        usersViewModel = ViewModelProvider(this)[UsersViewModel::class]
        settingsViewModel = ViewModelProvider(this)[SettingsViewModel::class]
        sessionsViewModel = ViewModelProvider(this)[SessionsViewModel::class]
        //logOut()
        //setStartValues()
        var z = cityViewModel.repository.getAllCitiesList().count()
        if (z == 0){
            getDataOnFirstStart()
        }
        //logOut()
        val currentuser = auth.currentUser
        if (currentuser!= null){
            var z = usersViewModel.repository.getUserByEmail(currentuser!!.email.toString())
            applicationUser = z
            userSettings = settingsViewModel.repository.getUsersSettings(applicationUser.email)
            showApplicationView()
        }
        else{
            toLoginPage()
        }

    }

    private fun showApplicationView() {
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setMapButtons()
        setHeaderButtons()
    }

    private fun reloadMapFragment() {
        val fragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.map, fragment)
            .commit()
        //onMapReady(map)
        fragment.getMapAsync (this)
        startLocationUpdates()
    }

    private fun toRegisterPage() {
        setContentView(R.layout.register_page)
        setRegisterButtons()
    }

    private fun toLoginPage(){
        setContentView(R.layout.login_page)
        setLoginButtons()
    }

    private fun toCountryPage(){
        setContentView(R.layout.country_registration)
        setCountryCityButtons()
        setContinueButton()
    }

    private fun setLoginButtons() {
        var loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener(object : OnClickListener {
            override fun onClick(v: View?) {
                login()
            }
        })
        var registerButton = findViewById<Button>(R.id.registrationTransferButton)
        registerButton.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                toCountryPage()
            }
        })

    }

    private fun setRegisterButtons() {
        var spinner = findViewById<Spinner>(R.id.unitOfMeasurementSpinner)
        var adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerElements)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        var loginTransferButton = findViewById<Button>(R.id.loginTransferButton)
        loginTransferButton.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                toLoginPage()
            }
        })

        var registrationButton = findViewById<Button>(R.id.registrationButton)
        registrationButton.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
               registerUser()
            }

        })
    }

    private fun setCountryCityButtons(){
        //region country Spinner set
        var spinner = findViewById<Spinner>(R.id.countryChooseSpinner)
        var countries = countryViewModel.getAllCountriesList()
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            countries.map { it }
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent!!.selectedItem as Country
                country = parent.selectedItem as Country
                setCitySpinner()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        //endregion
    }

    private fun setCitySpinner(){
        var citySpinner = findViewById<Spinner>(R.id.cityChooseSpinner)
        var cities = cityViewModel.repository.getAllCitiesByCountry(country.id)

        val cityAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            cities.map { it })

        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = cityAdapter

        citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                city = parent!!.selectedItem as City
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun setContinueButton(){
        val continueButton = findViewById<Button>(R.id.continueRegistrationButton)
        continueButton.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                toRegisterPage()
            }

        })
    }

    private fun login() {
        var email = findViewById<EditText>(R.id.editTextTextEmailAddress).text
        var password = findViewById<EditText>(R.id.editTextTextPassword).text
        auth.signInWithEmailAndPassword(email.toString(), password.toString())
            .addOnCompleteListener(this){
                task -> if(task.isSuccessful){
                  Log.d("Success LOGIN", "user logging")
                    val user = auth.currentUser
                    applicationUser = usersViewModel.repository.getUserByEmail(email.toString())
                    var k = settingsViewModel.repository.getAllSettingsList()
                    var z = settingsViewModel.repository.getUsersSettings(applicationUser.email)
                    userSettings = z
                    showApplicationView()
                    reloadMapFragment()
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
        var email = findViewById<EditText>(R.id.editTextTextEmailAddress).text.toString().trim()
        var password = findViewById<EditText>(R.id.editTextTextPassword).text.toString().trim()
        var name = findViewById<EditText>(R.id.nameInput).text.toString().trim()
        var unitOfMeasurement = findViewById<Spinner>(R.id.unitOfMeasurementSpinner).selectedItem
        if (!email.isEmpty() && !password.isEmpty() && !name.isEmpty()){
            auth.createUserWithEmailAndPassword(email.toString(), password.toString())
                .addOnCompleteListener(this) {
                    task ->
                    if (task.isSuccessful){
                        val user = auth.currentUser
                        var currentUser = Users(email = email,
                            password = password,
                            name = name, cityId = city.id)
                        usersViewModel.repository.addUser(currentUser)
                        applicationUser = currentUser
                        firebaseUserAdd()
                        userSettings = Settings(unit = unitOfMeasurement.toString(), userEmail = applicationUser.email!!)
                        settingsViewModel.repository.setUsersSettings(userSettings)
                        addCurrentSettings()
                        showApplicationView()
                        reloadMapFragment()
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

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        checkPermission()
    }

    private fun checkPermission(){
       if(ContextCompat.checkSelfPermission(this,
               android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
       {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
       }
       else{
           startLocationUpdates()
       }
   }

    private fun startLocationUpdates(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest  = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500).build()

        val locationCallback = object : LocationCallback(){
            override fun onLocationResult(p0: LocationResult) {
                for (location in p0.locations) {
                    updateLocationOnMap(location)

                }
            }
        }
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }

    }

    private fun updateLocationOnMap(location : Location) {

        var  previousPosition : LatLng? = null
        currentLatLng = LatLng(location.latitude, location.longitude)
        if (justStarted){
            focusCamera()
            justStarted = false
        }
        if (currentMarker!= null){
            previousPosition = LatLng(currentMarker?.position!!.latitude, currentMarker?.position!!.longitude)
        }
        currentMarker?.remove()
        currentMarker = map.addMarker(MarkerOptions().position(currentLatLng))
        if (isProjectStarted) {

            previousPosition?.let {
                polyline = PolylineOptions().add(previousPosition, currentLatLng)
                polylinePoints.add( map.addPolyline(polyline))
                totalDistance += SphericalUtil.computeDistanceBetween(
                    previousPosition,
                    currentLatLng
                )
                val view = findViewById<TextView>(R.id.textView3)
                view.setText("Преодоленное расстояние: ${totalDistance.toLong().toString()} ${userSettings.distanceUnit}")
            }
        }



    }

    private fun setMapButtons(){
        val startButton = findViewById<Button>(R.id.startButton)
        startButton.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                ClickOnStartButton()
            }
        })
        val view = findViewById<TextView>(R.id.textView3)
        view.setText("Преодоленное расстояние: 0 ${userSettings.distanceUnit}")

        val focusOnUserButton = findViewById<ImageView>(R.id.locationPicture)

        focusOnUserButton.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                focusCamera()
            }

        })

    }

    private fun ClickOnStartButton() {
        var startButton = findViewById<Button>(R.id.startButton)
        if (isProjectStarted){
            isProjectStarted = false
            session.endAt = Calendar.getInstance().time
            startButton.text = "Старт"
            sessionsViewModel.repository.addSession(session)
            firebaseAddSession()
        }
        else{
            isProjectStarted = true
            startButton.text = "Стоп"
            val view = findViewById<TextView>(R.id.textView3)
            totalDistance = 0.0
            session = Session(userEmail = applicationUser.email!!)
            view.text = "Преодоленное расстояние: ${totalDistance.toLong()} ${userSettings.distanceUnit}"
            if (polylinePoints.count() != 0){
                polylinePoints.forEach{ it.remove() }
            }


        }
    }

    private fun setHeaderButtons(){
        val historyButton = findViewById<Button>(R.id.historyButton)
        val userName = findViewById<TextView>(R.id.userNameView)
        val imageView = findViewById<ImageView>(R.id.settingButton)

        userName.text = applicationUser.name

        historyButton.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                toHistoryPage()
            }
        })

        imageView.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                toSettingsPage()
            }

        })
    }

    private fun <T> verticalSpinnerAdapter(items : List<T>) : SpinnerAdapter{
        var adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun toHistoryPage(){

    }

    private fun toSettingsPage(){
        if (isProjectStarted){
            val startButton = findViewById<Button>(R.id.startButton)
            startButton.performClick()
        }
        setContentView(R.layout.settings_layout)
//        setCountryCityButtons()
        val unitSpinner = findViewById<Spinner>(R.id.unitOfMeasurementSpinner)
        var SpinnerAdapter = verticalSpinnerAdapter(spinnerElements.toList())
        unitSpinner.adapter = SpinnerAdapter
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        logoutButton.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                logOut()
            }
        })

        val saveButton = findViewById<Button>(R.id.saveChanges)
        saveButton.setOnClickListener(object : OnClickListener{
            override fun onClick(v: View?) {
                saveChanges(userEmail = applicationUser.email!!)
            }

        })
    }

    private fun saveChanges(userEmail : String){
        val unitSpinner = findViewById<Spinner>(R.id.unitOfMeasurementSpinner)
        var user = usersViewModel.repository.getUserByEmail(userEmail)
        userSettings.distanceUnit = unitSpinner.selectedItem.toString()
        addCurrentSettings()
        settingsViewModel.repository.updateUserSettings(userSettings)
        showApplicationView()
        reloadMapFragment()
    }

    private fun logOut(){
        FirebaseAuth.getInstance().signOut()
        toLoginPage()
    }

    private fun firebaseUserAdd(){
        database.child("users").child(applicationUser.email.split('.')[0]).setValue(applicationUser)
    }

    private fun addCurrentSettings(){
        database.child("settings").child(userSettings.userEmail!!.split('.')[0]).setValue(userSettings)
    }

    private fun firebaseAddSession(){
        database.child("sessions").child(session.userEmail!!.split('.')[0]).child(session.id.toString()).setValue(session)
    }

    private fun getDataOnFirstStart() {
        val tempDatabase = FirebaseDatabase.getInstance()

        // Создаем корутину
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Запускаем все асинхронные задачи параллельно
                fetchCountries(tempDatabase)
                fetchCities(tempDatabase)
                fetchUsers(tempDatabase)
                fetchSessions(tempDatabase)
                fetchSettings(tempDatabase)

                // Все данные получены, можно продолжать работу
                Log.d("Data loaded", "All data loaded successfully")
            } catch (e: Exception) {
                Log.e("Data loading error", "Error loading data: ${e.message}")
            }
        }
    }

    private suspend fun fetchCountries(database: FirebaseDatabase) {
        val countryRef = database.getReference("countries")
        val snapshot = countryRef.get().await()
        snapshot.children.map {
            it.children.map { item ->
                item.value
            }
        }.forEach {
            val id = it[0] as Long
            val name = it[1] as String
            countryViewModel.addData(Country(id = id, name = name))
        }
        Log.d("Countries added", "Countries added")
    }

    private suspend fun fetchCities(database: FirebaseDatabase) {
        val cityRef = database.getReference("cities")
        val snapshot = cityRef.get().await()
        snapshot.children.map {
            it.children.map { item ->
                item.value
            }
        }.forEach {
            val id = it[1] as Long
            val name = it[2].toString()
            val countryId = it[0] as Long
            cityViewModel.addCity(City(id = id, name = name, countryId = countryId))
        }
        Log.d("Cities added", "Cities added")
    }

    private suspend fun fetchUsers(database: FirebaseDatabase) {
        val userRef = database.getReference("users")
        val snapshot = userRef.get().await()
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
                usersViewModel.repository.addUser(Users(email, name, password, cityId, Date(createdAt)))
            }
        }
    }

    private suspend fun fetchSessions(database: FirebaseDatabase) {
        val sessionRef = database.getReference("sessions")
        val snapshot = sessionRef.get().await()
        if (snapshot.value != null) {
            snapshot.children.map {
                it.children.map { item ->
                    item.children.map {
                        it.value
                    }
                }.forEach {
                    val distance = it[0] as Long
                    val tempEndAt = it[1] as HashMap<*, *>
                    val endAt = Date(tempEndAt["time"] as Long)
                    val tempStartAt = it[3] as HashMap<*, *>
                    val startAt = Date(tempStartAt["time"] as Long)
                    val userEmail = it[4] as String

                    sessionsViewModel.repository.addSession(Session(startAt, distance.toInt(), userEmail, endAt))
                }
            }
        }
    }

    private suspend fun fetchSettings(database: FirebaseDatabase) {
        val settingsRef = database.getReference("settings")
        val snapshot = settingsRef.get().await()
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


    private fun focusCamera(){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
    }
    }
    //region internet Check(Ussless at current moment)
//    private fun checkInterntConnection(context: Context) : Boolean{
//        var connctionManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val network = connctionManager.activeNetwork ?: return false
//        val capabilities = connctionManager.getNetworkCapabilities(network) ?: return false
//        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//
//    }
    // endregion







