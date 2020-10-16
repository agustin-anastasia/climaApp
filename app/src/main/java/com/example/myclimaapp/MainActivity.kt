package com.example.myclimaapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import org.json.JSONObject
import java.net.URL
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    private var PERMISSION_ID = 465

    val apikey: String = "d6b737fd8131e678e585ac82309519fb"
    val exclude: String = "minutely,hourly,alerts"
    val units: String = "metric"
    val language: String = "es"

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
        weatherTask().execute()

        Log.d("Debug", "ubicacion " + getLastLocation()[0].toString())


    }

    inner class weatherTask():AsyncTask<String,Void,String>(){
        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: String?): String {
            var response: String
            response = URL("https://api.openweathermap.org/data/2.5/onecall?lat=-34.9042587&lon=-56.1718929&lang=$language&units=$units&exclude=$exclude&appid=$apikey").readText(Charsets.UTF_8)
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val jsonObj = JSONObject(result)
            val current = jsonObj.getJSONObject("current")
            val weather = current.getJSONArray("weather").getJSONObject(0)
            val daily = jsonObj.getJSONArray("daily")
            val daily_array = daily.getJSONObject(0)


            val temp_daily_min = daily_array.getJSONObject("temp").getString("min") + "°C"
            val temp_daily_max = daily_array.getJSONObject("temp").getString("max") + "°C"
            val temp = "Temp: " + current.getString("temp")+"°C"
            val pressure = "Presion atmosférica:  " + current.getString("pressure")+" hPa"
            val humidity = "Humedad:   " + current.getString("humidity")+" %"
            val weatherDescription = "Descripción: " +  weather.getString("description")

            findViewById<TextView>(R.id.tvTemp).text = temp
            findViewById<TextView>(R.id.tvMaxTempNum).text = temp_daily_max
            findViewById<TextView>(R.id.tvMinTempNum).text = temp_daily_min
            findViewById<TextView>(R.id.tvHumedad).text = humidity
            findViewById<TextView>(R.id.tvPresion).text = pressure
            findViewById<TextView>(R.id.tvDescripcion).text = weatherDescription

        }
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    fun getLastLocation():Array<String>{
        var locations = Array<String>(2){"it = $it"} /*locations = [latitud,longitude]*/
        //chequea permisos
        if(CheckPermission()){
            if(isLocationEnabled()){
                //ultima ubicacion
                fusedLocationProviderClient.lastLocation.addOnCompleteListener {task ->
                    var location = task.result
                    if(location == null){
                        //nueva location
                        NewLocationData()
                    }else{
                        Log.d("Debug:","Your Location:" + location.longitude)
                        locations[0] = location.latitude.toString()
                        locations[1] = location.longitude.toString()
                    }
                }
            }else{
                Toast.makeText(this, "Please enable your location service", Toast.LENGTH_SHORT).show()
            }
        }else{
            //solicita permisos
            RequestPermission()
        }
        return locations

    }


    @SuppressLint("MissingPermission")
    private fun NewLocationData(){
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())

    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            var lastLocation: Location = locationResult.lastLocation
            Log.d("Debug", "your last location:" + lastLocation.longitude.toString())
                    getCityName(lastLocation.latitude,lastLocation.longitude)
        }
    }


    //esta funcion chequea los permisos del usuario
    private fun CheckPermission():Boolean {
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    //solicitar permisos del user
    private fun RequestPermission(){
        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION),PERMISSION_ID)
    }

    //chequea ubicacion esta activada
    private fun isLocationEnabled():Boolean{
        var locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled((LocationManager.NETWORK_PROVIDER))
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == PERMISSION_ID){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("debug", "You have the Permission")
            }
        }
    }

    private fun getCityName(lat: Double, long: Double):String{
        var cityName:String = ""
        var countryName = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var Address = geoCoder.getFromLocation(lat,long,3)

        cityName = Address.get(0).locality
        countryName = Address.get(0).countryName
        Log.d("Debug:" , "Your city: " + cityName  + " ; your Country: " + countryName)
        return cityName
    }



}