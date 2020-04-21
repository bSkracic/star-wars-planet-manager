package com.example.starwarsplanetmanager

import android.content.Intent
import android.content.ServiceConnection
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.GradientDrawable
import android.media.audiofx.DynamicsProcessing
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Base64
import android.view.ViewParent
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toDrawable
import kotlinx.android.synthetic.main.activity_planet_view.*
import kotlinx.android.synthetic.main.activity_planet_view_landscape.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.util.concurrent.Executor

class PlanetView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val planet = intent.getParcelableExtra<Planet>("PLANET")
        var orientation = ""
        runBlocking {
            val imageJob = CoroutineScope(Dispatchers.IO).async{
                getImage(planet.IDPlanet)
            }
            planet.Image = imageJob.await()
            if(planet.Government == "Confederates"){
                val governorImageJob = CoroutineScope(Dispatchers.IO).async {
                    getGovernorImage(planet.IDPlanet)
                }
                planet.Governor?.Image = governorImageJob.await()
            }
        }

        if(Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_planet_view)
            setGovernmentImage(this.governmentImage, planet.Government)
            setWidgetStatusPortrait(planet)
            if(planet.Image != null){
                setImage(planet.Image!!, this.imageView)
            }
            orientation = "Portrait"

        }else{
            setContentView(R.layout.activity_planet_view_landscape)
            setGovernmentImage(this.imageGov, planet.Government)
            setWidgetStatusLandscapeBasic(planet)
            setForcesImage()
            if(planet.Image != null){
                setImage(planet.Image!!, this.planet_image)
            }
            if(planet.Government == "Confederates"){
                setWidgetStatusLandscape(planet)
                setImage(planet.Governor?.Image!!, this.governor_image)
            }
            orientation = "Landscape"
        }

        if(orientation == "Portrait") {
            this.buttonEditPlanet.setOnClickListener {
                val intent = Intent(this, EditPlanet::class.java).apply {
                    planet.Image = null
                    putExtra("PLANET", planet)
                }
                startActivity(intent)
            }
        } else {
            this.buttonEditPlanet2.setOnClickListener {
                val intent = Intent(this, EditPlanet::class.java).apply {
                    planet.Image = null
                    putExtra("PLANET", planet)
                }
                startActivity(intent)
            }

            if(planet.Government == "Galactic Republic"){
                this.buttonEditArmyType.isClickable = false
                this.buttonEditGovernor.isClickable = false
                Toast.makeText(this, "Governor and forces information not available for planet of the Republic!", Toast.LENGTH_SHORT).show()
            } else {
                this.buttonEditGovernor.setOnClickListener {
                    val intent = Intent(this, EditGovernor::class.java).apply {
                        planet.Image = null
                        putExtra("PLANET", planet)
                    }
                    startActivity(intent)
                }

                this.buttonEditArmyType.setOnClickListener {
                    val intent = Intent(this, EditArmyType::class.java).apply {
                        planet.Image = null
                        putExtra("PLANET", planet)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun setWidgetStatusPortrait(planet: Planet){
        textName.text = planet.Name
        governmentText.text = planet.Government
        regionText.text = planet.RegionName
    }

    private fun setWidgetStatusLandscapeBasic(planet: Planet){
        name_text.text = "Name: " + planet.Name
        region_text.text = "Region: " + planet.RegionName
        goverment_text.text = "Government: " + planet.Government
    }

    private fun setWidgetStatusLandscape(planet: Planet){
        governor_name_text.text = "Name: " + (planet.Governor?.Name ?: String)
        governor_surname_text.text = "Surname: " + (planet.Governor?.Surname ?: String)
        B1.text = "B1:\n" + planet.ArmyType?.B1.toString()
        B2.text =  "B2:\n" + planet.ArmyType?.B2.toString()
        B3.text = "B3:\n" + planet.ArmyType?.B3.toString()
        BX.text = "BX:\n" + planet.ArmyType?.BX.toString()
        BL.text = "BL:\n" + planet.ArmyType?.BL.toString()
        D1S1.text = "D1S1:\n" + planet.ArmyType?.D1S1.toString()
        HKB3.text = "HKB3:\n" + planet.ArmyType?.HKB3.toString()
        IG110.text = "IG110:\n" + planet.ArmyType?.IG110.toString()
        OG9.text = "OG9:\n" + planet.ArmyType?.OG9.toString()
        T4.text = "T4:\n" + planet.ArmyType?.T4.toString()
    }

    private fun setForcesImage(){
        this.imageB1.setImageResource(R.mipmap.model_b1_round)
        this.imageB2.setImageResource(R.mipmap.model_b2_round)
        this.imageB3.setImageResource(R.mipmap.model_b3_round)
        this.imageBX.setImageResource(R.mipmap.model_bx_round)
        this.imageBL.setImageResource(R.mipmap.model_bl_round)
        this.imageD1S1.setImageResource(R.mipmap.model_d1s1_round)
        this.imageHKB3.setImageResource(R.mipmap.model_hkb3_round)
        this.imageIG110.setImageResource(R.mipmap.model_ig110_round)
        this.imageOG9.setImageResource(R.mipmap.model_og9_round)
        this.imageT4.setImageResource(R.mipmap.model_t4_round)
    }

    private fun setGovernmentImage(imageView: ImageView, government: String){
        if(government == "Galactic Republic") {
            imageView.setImageResource(R.mipmap.galactic_republic_foreground)
        }else{
            imageView.setImageResource(R.mipmap.confederacy_foreground)
        }
    }

    private fun setImage(imageString: String, imageView: ImageView){
        try{
            val imageByte = Base64.decode(imageString, Base64.NO_WRAP)
            val bmp: Bitmap = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.size)
            imageView.setImageBitmap(bmp)
            imageView.scaleType = ImageView.ScaleType.FIT_END
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
}
