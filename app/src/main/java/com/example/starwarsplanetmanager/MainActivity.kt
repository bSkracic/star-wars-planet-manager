package com.example.starwarsplanetmanager
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //planet map
        var planets: HashMap<String, Int> = hashMapOf()
        var regions: HashMap<String, Int> = hashMapOf()
        //set spinner with governments
        val govs: Array<String> = arrayOf("Any", "Galactic Republic", "Confederates")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, govs)
        spinnerGov.adapter = adapter

        val tempRegions: Array<String> = arrayOf("Any")
        val tempAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, tempRegions)
        spinnerReg.adapter = tempAdapter
        //set spinner with regions and get all the planets
        CoroutineScope(Dispatchers.IO).launch {
            try{
                regions = getRegions()
                planets = getPlanets("", 0, "Any")
            }catch(ex: Exception){
                makeErrorToast()
            }

        }

        //search planet function
        this.button.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                try{
                    planets = getPlanets(search.text.toString(), regions[spinnerReg.selectedItem.toString()]!!, spinnerGov.selectedItem.toString())
                }catch(ex: Exception){
                    makeErrorToast()
                }
            }
        }

        //pass IDPlanet to new activity for selected item
        this.listView.setOnItemClickListener { parent, view, position, id ->
            val planetName = parent.getItemAtPosition(position).toString()
            val idPlanet: Int = planets[planetName]!!
            var planet = Planet()
            runBlocking {
                val planetJob = CoroutineScope(Dispatchers.IO).async {
                    getPlanet(idPlanet)
                }
                planet = planetJob.await()
            }

            val intent: Intent = Intent(this, PlanetView::class.java).apply{
                putExtra("PLANET", planet)
            }
            startActivity(intent)
        }

    }

    private suspend fun getRegions() : HashMap<String, Int>{
        val planetsGet: URL = URL("https://hk-iot-team-02.azurewebsites.net/api/Regions")
        val regionList = JSONArray(planetsGet.readText())
        var regions: HashMap<String, Int> = hashMapOf()
        regions["Any"] = 0
        for(i in 0 until regionList.length())
        {
            regions[regionList.getJSONObject(i).getString("Name")] = regionList.getJSONObject(i).getInt("IDRegion")
        }
        var adapterRegions: MutableList<String> = mutableListOf()
        for(item in regions){
            adapterRegions.add(item.key)
        }
        withContext(Dispatchers.Main){
            val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_dropdown_item, adapterRegions)
            spinnerReg.adapter = adapter
            for(i in 0 until adapterRegions.size){
                if(adapterRegions[i] == "Any"){spinnerReg.setSelection(i) }
            }

        }
        return regions
    }

    private suspend fun getPlanets(_name: String, _region: Int, _government: String) : HashMap<String, Int> {
        val url = URL("https://hk-iot-team-02.azurewebsites.net/api/Planets")
        var planetList = JSONArray(url.readText())
        var planets: HashMap<String, Int> = hashMapOf()
        if (_name == "" && _government == "Any" && _region == 0) {
            for (i in 0 until planetList.length()) {
                val name = planetList.getJSONObject(i).getString("Name")
                val planetID = planetList.getJSONObject(i).getInt("IDPlanet")
                planets[name] = planetID
            }
        } else {
            for (i in 0 until planetList.length()) {
                val region = planetList.getJSONObject(i).getInt("RegionID")
                val government = planetList.getJSONObject(i).getString("Government")
                if ((_region == region || _region == 0) && (_government == government || _government == "Any")) {
                    val name = planetList.getJSONObject(i).getString("Name")
                    if (isInName(_name, name)) {
                        val planetID = planetList.getJSONObject(i).getInt("IDPlanet")
                        planets[name] = planetID
                    }
                }
            }
        }
        if(planets.isEmpty()){
            withContext(Dispatchers.Main){
                Toast.makeText(applicationContext, "No planet matches your search requirements", Toast.LENGTH_SHORT).show()
            }
        }
        var adapterPlanets: MutableList<String> = mutableListOf()
        for(item in planets)
        {
            adapterPlanets.add(item.key)
        }
        withContext(Dispatchers.Main) {
            val adapter = ArrayAdapter(applicationContext, android.R.layout.simple_list_item_1, adapterPlanets)
            listView.adapter = adapter
        }

        return planets
    }

    private suspend fun getPlanet(id: Int): Planet{
        val GET_Planet = "https://hk-iot-team-02.azurewebsites.net/api/Planets/$id"
        val url: URL = URL(GET_Planet)
        val jsonPlanet = JSONObject(url.readText())
        var planet: Planet = Planet(jsonPlanet)
        var GET_Region = "https://hk-iot-team-02.azurewebsites.net/api/Regions/"
        var GET_Governor = "https://hk-iot-team-02.azurewebsites.net/api/Governors/"
        var GET_ArmyType = "https://hk-iot-team-02.azurewebsites.net/api/ArmyTypes/"
        planet.setRegionName(JSONObject(URL(GET_Region  + planet.RegionID.toString()).readText()))
        if(planet.Government == "Confederates"){
            planet.setGovernor(JSONObject(URL(GET_Governor + planet.IDPlanet.toString()).readText()))
            planet.setArmyType(JSONObject(URL(GET_ArmyType + planet.IDPlanet.toString()).readText()))
        }

        return planet
    }

    private fun isInName(request: String, name: String): Boolean {
        if (request == "") {
            return true
        }
        for (i in 0 until (name.length - request.length + 1)) {
            var part: String = ""
            for (j in 0 until request.length) {
                part += name[i + j]
            }
            if (part == request) {
                return true
            }
        }
        return false
    }

    private suspend fun makeErrorToast(){
        withContext(Dispatchers.Main){
            Toast.makeText(applicationContext, "Something went wrong, check your internet connection!", Toast.LENGTH_SHORT).show()
        }
    }

}

