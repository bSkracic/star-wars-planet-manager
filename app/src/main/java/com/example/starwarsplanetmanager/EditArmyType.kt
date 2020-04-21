package com.example.starwarsplanetmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_edit_army_type.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class EditArmyType : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_army_type)

        val planet = intent.getParcelableExtra<Planet>("PLANET")
        setForcesNumbers(planet)

        this.button_save_changes.setOnClickListener {
            try {
                var armyTypeDTO = ArmyTypeDTO()
                armyTypeDTO.IDPlanet = planet.IDPlanet
                armyTypeDTO.B1 = edit_b1.text.toString().toInt()
                armyTypeDTO.B2 = edit_b2.text.toString().toInt()
                armyTypeDTO.B3 = edit_b3.text.toString().toInt()
                armyTypeDTO.BX = edit_bx.text.toString().toInt()
                armyTypeDTO.D1S1 = edit_d1s1.text.toString().toInt()
                armyTypeDTO.HKB3 = edit_hkb3.text.toString().toInt()
                armyTypeDTO.IG110 = edit_ig110.text.toString().toInt()
                armyTypeDTO.OG9 = edit_og9.text.toString().toInt()
                armyTypeDTO.T4 = edit_t4.text.toString().toInt()
                CoroutineScope(Dispatchers.IO).launch {
                    try{
                        sendHttpRequest(planet.IDPlanet, makeJson(armyTypeDTO))
                        makeToast(true)
                    }catch(ex: Exception){
                        makeToast(false)
                    }
                }
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(this, "Forces number must not be empty and must be number!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setForcesNumbers(planet: Planet){
        this.edit_b1.setText(planet.ArmyType?.B1.toString())
        this.edit_b2.setText(planet.ArmyType?.B2.toString())
        this.edit_b3.setText(planet.ArmyType?.B3.toString())
        this.edit_bx.setText(planet.ArmyType?.BX.toString())
        this.edit_bl.setText(planet.ArmyType?.BL.toString())
        this.edit_d1s1.setText(planet.ArmyType?.D1S1.toString())
        this.edit_hkb3.setText(planet.ArmyType?.HKB3.toString())
        this.edit_ig110.setText(planet.ArmyType?.IG110.toString())
        this.edit_og9.setText(planet.ArmyType?.OG9.toString())
        this.edit_t4.setText(planet.ArmyType?.T4.toString())
    }

    private suspend fun sendHttpRequest(id: Int, jsonBody: String){
        val url: URL = URL("https://hk-iot-team-02.azurewebsites.net/api/ArmyTypes/$id")
        with(url.openConnection() as HttpURLConnection){
            requestMethod = "PUT"
            this.setRequestProperty("content-type", "application/json")
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

}
