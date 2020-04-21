package com.example.starwarsplanetmanager

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.iterator
import androidx.core.view.size
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_edit_planet.*
import kotlinx.android.synthetic.main.activity_planet_view.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.net.URL
import java.net.*

class EditPlanet : AppCompatActivity() {
    val PICK_IMAGE = 1
    var regions: HashMap<String, Int> = hashMapOf()
    var planet = Planet()
    var IMAGE_CHANGED = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_planet)

        planet = intent.getParcelableExtra<Planet>("PLANET")

        runBlocking {
            val imageJob = CoroutineScope(Dispatchers.IO).async {
                    getImage(planet.IDPlanet)
            }

            planet.Image = imageJob.await()
        }

        setPlanetImage(planet.Image!!, this.planetImage)

        CoroutineScope(Dispatchers.IO).launch {
            regions = getRegions()
        }

        this.editImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picutre"), PICK_IMAGE)
        }


        this.save.setOnClickListener {
            if (editName.text.toString() == "" || editType.text.toString() == "") {
                Toast.makeText(this, "Name and type must not be empty!", Toast.LENGTH_SHORT).show()
            } else {
                var planetDTO = PlanetDTO(planet)
                planetDTO.Name = editName.text.toString()
                planetDTO.Type = editType.text.toString()
                planetDTO.RegionID = regions[spinnerRegions.selectedItem]!!
                planetDTO.Government = spinnerGovs.selectedItem.toString()
                if (IMAGE_CHANGED) {
                    val bitmap = planetImage.drawable.toBitmap()
                    if (bitmap != null) {
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        val byteArray = stream.toByteArray()
                        planetDTO.Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                    }else {
                        planetDTO.Image = null
                    }
                } else{
                    planetDTO.Image = planet.Image
                }

                var check: String = ""
                if (planetDTO.Government != planet.Government) {
                    if (planetDTO.Government == "Confederates") {
                        check = "POST"
                    } else if (planetDTO.Government == "Galactic Republic") {
                        check = "DELETE"
                    }
                }
                val planetJson = makeJson(planetDTO)
                CoroutineScope(Dispatchers.IO).launch{
                    try {
                        httpRequest(planetDTO.IDPlanet, planetJson, "PUT", "Planets")
                        if (check == "POST") {
                            var governorDTO = GovernorDTO()
                            governorDTO.IDPlanet = planetDTO.IDPlanet
                            var armyTypeDTO = ArmyTypeDTO()
                            armyTypeDTO.IDPlanet = planetDTO.IDPlanet
                            val armyTypeBody = makeJson(armyTypeDTO)
                            val governorBody = makeJson(governorDTO)
                            httpRequest(planetDTO.IDPlanet, governorBody, check, "Governors")
                            httpRequest(planetDTO.IDPlanet, armyTypeBody, check, "ArmyTypes")
                        } else if (check == "DELETE") {
                            httpRequest(planetDTO.IDPlanet, "", check, "Governors")
                            httpRequest(planetDTO.IDPlanet, "", check, "ArmyTypes")
                        }
                        makeToast(true)
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                    }catch (exception: Exception){
                        makeToast(false)
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK){
            IMAGE_CHANGED = true
            val imageUri = data?.data
            //val bitmap = BitmapFactory.decodeFile(imageUri.toString())
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            this.planetImage.setImageBitmap(bitmap)
        }
    }

    private suspend fun getRegions(): HashMap<String, Int>{
        val GET: String = "https://hk-iot-team-02.azurewebsites.net/api/Regions/"
        val url: URL = URL(GET)
        val regionsJson = JSONArray(url.readText())
        var regions: HashMap<String, Int> = hashMapOf()
        var adapterRegions: MutableList<String> = mutableListOf()
        for(i in 0 until regionsJson.length()){
            regions[regionsJson.getJSONObject(i).getString("Name")] = regionsJson.getJSONObject(i).getInt("IDRegion")
            adapterRegions.add(regionsJson.getJSONObject(i).getString("Name"))
        }
        setRegions(adapterRegions, planet)
        setGovernments()
        setTexts(planet)
        return regions
    }


    private suspend fun setRegions(adapterRegions: MutableList<String>, planet: Planet) {
        withContext(Dispatchers.Main){
            val adapter = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item, adapterRegions)
            spinnerRegions.adapter = adapter
            for(i: Int in 0 until adapterRegions.size){
                if(adapterRegions[i] == planet.RegionName){
                    spinnerRegions.setSelection(i)
                    break
                }
            }
        }
    }
    private suspend fun setGovernments() {
        withContext(Dispatchers.Main) {
            val govs = arrayListOf<String>("Confederates", "Galactic Republic")
            val adapter = ArrayAdapter(applicationContext, R.layout.support_simple_spinner_dropdown_item, govs)
            spinnerGovs.adapter = adapter
            if(planet.Government == "Confederates"){
                spinnerGovs.setSelection(0)
            }else{
                spinnerGovs.setSelection(1)
            }
        }
    }
    private suspend fun setTexts(planet: Planet){
        withContext(Dispatchers.Main){
            editName.setText(planet.Name)
            editType.setText(planet.Type)
            for(i in 0 until spinnerRegions.size){
                if(spinnerRegions.getItemAtPosition(i) == planet.RegionName){
                    spinnerRegions.setSelection(i)
                    break
                }
            }

            for(i in 0 until spinnerGovs.size){
                if(spinnerGovs.getItemAtPosition(i) == planet.Government){
                    spinnerGovs.setSelection(i)
                    break
                }
            }
        }
    }

    private suspend fun httpRequest(id: Int, jsonBody: String, httpMethod: String, table: String){
        var url_string: String = ""
        if(httpMethod == "DELETE" || httpMethod == "PUT"){
            url_string = "https://hk-iot-team-02.azurewebsites.net/api/$table/$id"
        }else{
            url_string = "https://hk-iot-team-02.azurewebsites.net/api/$table"
        }

        val url: URL = URL(url_string)
        with(url.openConnection() as HttpURLConnection){
            requestMethod = httpMethod
            if(httpMethod != "DELETE"){
                this.setRequestProperty("content-type", "application/json")
            }
            val stream = OutputStreamWriter(outputStream)
            stream.write(jsonBody)
            stream.flush()
            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
            }
        }
    }

    private fun makeJson(obj: Any?): String{
        val gsonBuilder: GsonBuilder = GsonBuilder()
        gsonBuilder.serializeNulls()
        val gson: Gson = gsonBuilder.create()
        val jsonData = gson.toJson(obj)
        return jsonData
    }

    private suspend fun makeToast(response: Boolean){
        var message: String = ""
        if(response){
            message = "Changes saved successfully."
        }else{
            message = "Something went wrong."
        }
        withContext(Dispatchers.Main){
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun setPlanetImage(imageString: String, imageView: ImageView){
        try{
        val imageByte = Base64.decode(imageString, Base64.NO_WRAP)
        val bmp: Bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.size)
        imageView.setImageBitmap(bmp)
        }catch(ex: Exception){
            return
        }
    }

    private suspend fun getImage(id: Int): String{
        try{
            val url = URL("https://hk-iot-team-02.azurewebsites.net/api/Planets/$id")
            val planet = JSONObject(url.readText())
            return planet.getString("Image")
        } catch(ex: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(applicationContext, "Cannot load image", Toast.LENGTH_SHORT)
            }
        }
        return ""
    }




}
