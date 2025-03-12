package com.example.googlemaps


import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import android.Manifest
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
import androidx.activity.result.contract.ActivityResultContracts
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Calendar

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
        getDataOnFirstStart()
//        var countriesAmount = countryViewModel.getAllCountriesList().count()
//        if (countriesAmount == 0){
//            setStartValues()
//        }
//            val currentuser = auth.currentUser
//        if (currentuser!= null){
//            var z = usersViewModel.repository.getUserByEmail(currentuser!!.email.toString())
//            applicationUser = z
//            userSettings = settingsViewModel.repository.getUsersSettings(applicationUser.id)
//            showApplicationView()
//            updateFirebaseDb()
//        }
//        else{
//            toLoginPage()
//        }

    }

    private fun setStartValues(){
        val mainViewModel = ViewModelProvider(this).get(CountryViewModel::class)
        val cityViewModel = ViewModelProvider(this).get(CityViewModel::class)

        countries.forEach(){
            mainViewModel.addData(Country(name = it))
        }

        citiesRussia.forEach(){
            cityViewModel.addCity(City(name = it, 1L))
        }

        citiesUSA.forEach(){
            cityViewModel.addCity(City(name = it, 2L))
        }

        emperiumCities.forEach(){
            cityViewModel.addCity(City(name = it, 3L))
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
                        var c = usersViewModel.repository.getAllUsers()
                        var f = c.count()
                        applicationUser = usersViewModel.repository.getUserByEmail(currentUser.email!!)
                        userSettings = Settings(unit = unitOfMeasurement.toString(), userId = applicationUser.id)
                        settingsViewModel.repository.setUsersSettings(userSettings)
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
                for (location in p0.locations){
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
        val currentLatLng = LatLng(location.latitude, location.longitude)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 18f))
        if (currentMarker!= null){
            previousPosition = LatLng(currentMarker?.position!!.latitude, currentMarker?.position!!.longitude)
        }
        currentMarker?.remove()
        currentMarker = map.addMarker(MarkerOptions().position(currentLatLng))
        if (isProjectStarted) {

            previousPosition?.let {
                map.addPolyline(PolylineOptions().add(previousPosition, currentLatLng))
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

    }

    private fun ClickOnStartButton() {
        var startButton = findViewById<Button>(R.id.startButton)
        if (isProjectStarted){
            isProjectStarted = false
            session.endAt = Calendar.getInstance().time
            startButton.text = "Старт"
            sessionsViewModel.repository.addSession(session)


        }
        else{
            isProjectStarted = true
            startButton.text = "Стоп"
            val view = findViewById<TextView>(R.id.textView3)
            totalDistance = 0.0
            session = Session(userId = applicationUser.id)
            view.text = "Преодоленное расстояние: ${totalDistance.toLong()} ${userSettings.distanceUnit}"

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
        settingsViewModel.repository.updateUserSettings(userSettings)
        showApplicationView()
        reloadMapFragment()
    }

    private fun logOut(){
        FirebaseAuth.getInstance().signOut()
        toLoginPage()
    }

    private fun updateFirebaseDb(){
        var counriesToAdd = countryViewModel.getAllCountriesList()
        var citiestoAdd = runBlocking {
            coroutineScope.async {
                cityViewModel.repository.getAllCities().first()
            }.await()
        }

        var usersToAdd = usersViewModel.repository.getAllUsers()
        var settingsToAdd = settingsViewModel.repository.getUsersSettings(applicationUser.id)
        var sessionsToAdd = sessionsViewModel.repository.getAllSessions()

        counriesToAdd.forEach(){
            database.child("countries").child(it.id.toString()).setValue(it)
        }

       citiestoAdd.forEach(){
           database.child("cities").child(it.id.toString()).setValue(it)
       }

        usersToAdd.forEach(){
            database.child("users").child(it.id.toString()).setValue(it)
        }

        sessionsToAdd.forEach(){
            database.child("sessions").child(it.userId.toString()).child(it.id.toString()).setValue(it)
        }

        database.child("setiings").child(settingsToAdd.userId.toString()).setValue(settingsToAdd)



    }

    private fun getDataOnFirstStart(){
        var tempDatabase = FirebaseDatabase.getInstance()
        var ref = tempDatabase.getReference("countries")
        ref.get().addOnSuccessListener { snapshot ->
            val name = snapshot.child("name").getValue(String::class.java)
            var id = snapshot.child("id")
            countryViewModel.addData(Country(name = name!!))
        }
    }
}






