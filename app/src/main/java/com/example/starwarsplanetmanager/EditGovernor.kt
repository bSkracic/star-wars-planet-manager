package com.example.starwarsplanetmanager

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_edit_governor.*
import kotlinx.android.synthetic.main.activity_edit_planet.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.CoroutineContext

class EditGovernor : AppCompatActivity() {

    val PICK_IMAGE = 1
    val SHOOT_IMAGE = 2
    var IMAGE_CHANGED = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_governor)

        val planet = intent.getParcelableExtra<Planet>("PLANET")

        runBlocking{
            val gimageJob = CoroutineScope(Dispatchers.IO).async {
                getGovernorImage(planet.IDPlanet)
            }

            planet.Governor?.Image = gimageJob.await()
        }

        setImage(planet.Governor?.Image!!, this.editGovernorImage)

        this.editGovernorName.setText(planet.Governor?.Name)
        this.editGovernorSurname.setText(planet.Governor?.Surname)

        this.buttonSaveChanges.setOnClickListener {
            if(editGovernorName.text.toString() == "" || editGovernorSurname.text.toString() == "") {
                Toast.makeText(this, "Name and surname must not be empty!", Toast.LENGTH_SHORT).show()
            }else{
                var governorDTO = GovernorDTO()
                governorDTO.IDPlanet = planet.IDPlanet
                governorDTO.Name = editGovernorName.text.toString()
                governorDTO.Surname = editGovernorSurname.text.toString()
                if (IMAGE_CHANGED) {
                    val bitmap = editGovernorImage.drawable.toBitmap()
                    if (bitmap != null) {
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        val byteArray = stream.toByteArray()
                        governorDTO.Image = Base64.encodeToString(byteArray, Base64.NO_WRAP)
                    }else {
                        governorDTO.Image = null
                    }
                } else{
                    governorDTO.Image = planet.Image
                }

                var check: Boolean = false
                CoroutineScope(Dispatchers.IO).launch {
                    try{
                        sendHttpRequest(governorDTO.IDPlanet, makeJson(governorDTO))
                        makeToast(true)
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                    }catch (ex: Exception) {
                        makeToast(false)
                    }
                }
            }
        }

        this.imageFromGallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picutre"), PICK_IMAGE)
        }

        this.imageFromCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, SHOOT_IMAGE)
        }
    }



    private suspend fun sendHttpRequest(id: Int, jsonBody: String){
        val url: URL = URL("https://hk-iot-team-02.azurewebsites.net/api/Governors/$id")
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

    private suspend fun getGovernorImage(id: Int): String{
        try{
            val url = URL("https://hk-iot-team-02.azurewebsites.net/api/Governors/$id")
            val governor = JSONObject(url.readText())
            return governor.getString("Image")
        } catch(ex: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(applicationContext, "Cannot load image", Toast.LENGTH_SHORT)
            }
        }
        return ""
    }

    private fun setImage(imageString: String, imageView: ImageView){
        try{
            val imageByte = Base64.decode(imageString, Base64.NO_WRAP)
            val bmp: Bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.size)
            imageView.setImageBitmap(bmp)
        }catch(ex: Exception){
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK){
            IMAGE_CHANGED = true
            val imageUri = data?.data
            //val bitmap = BitmapFactory.decodeFile(imageUri.toString())
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            this.editGovernorImage.setImageBitmap(bitmap)
        } else if(requestCode == SHOOT_IMAGE && resultCode == Activity.RESULT_OK){
            IMAGE_CHANGED = true
            var photo = data?.extras?.get("data") as Bitmap
            this.editGovernorImage.setImageBitmap(photo)
        }
    }



}
