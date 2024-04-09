package com.semauluc.recipes

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import java.io.ByteArrayOutputStream


class RecipeFragment2 : Fragment() {

    var secilenGorsel: Uri? = null
    var secilenBitmap : Bitmap? = null
    private lateinit var gorselSec: ImageView
    private lateinit var yemekIsmiText: EditText
    private lateinit var yemekMalzemeText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        return inflater.inflate(R.layout.fragment_tarif, container, false)
    }
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            yemekIsmiText = view.findViewById(R.id.yemekIsmiText)
            yemekMalzemeText = view.findViewById(R.id.yemekMalzemeText)

            val myButton: Button = view.findViewById(R.id.myButton)
            gorselSec =  view.findViewById(R.id.imageViewww)


            myButton.setOnClickListener {
                kaydet(it)
            }
            gorselSec.setOnClickListener {
                gorselSec(it)
            }

            arguments?.let{
                var gelenBilgi = RecipeFragment2Args.fromBundle(it).bilgi

                if(gelenBilgi.equals("menudengeldim")) {
                    //yeni bir yemek ekleyemeye geldi
                    yemekIsmiText.setText("")
                    yemekMalzemeText.setText("")
                    myButton.visibility = View.VISIBLE

                    val gorselSecmeArkaPlani = BitmapFactory.decodeResource(context?.resources,R.drawable.gorselsecimi)
                    imageView.setImageBitmap(gorselSecmeArkaPlani)
                }else{
                    //daha önce oluştutulmuş olan yemeğe geldi
                    myButton.visibility = View.INVISIBLE

                    val secilenId = RecipeFragment2Args.fromBundle(it).id
                    context?.let {
                        try{
                            val db = it.openOrCreateDatabase("Yemekler",Context.MODE_PRIVATE,null)
                            val cursor =db.rawQuery("SELECT * FROM yemekler WHERE id = ?", arrayOf(secilenId.toString()))

                            val yemekIsmiIndex = cursor.getColumnIndex("yemekIsmi")
                            val yemekMalzemesiIndex = cursor.getColumnIndex("yemekMalzemesi")
                            val yemekGorseli = cursor.getColumnIndex("gorsel")

                            while(cursor.moveToNext()) {
                                yemekIsmiText.setText (cursor.getString(yemekIsmiIndex))
                                yemekMalzemeText.setText(cursor.getString(yemekMalzemesiIndex))

                                val byteDizisi = cursor.getBlob(yemekGorseli)
                                val bitMap = BitmapFactory.decodeByteArray(byteDizisi,0,byteDizisi.size)
                                imageView.setImageBitmap(bitMap)

                            }

                            cursor.close()
                        }catch (e: Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }

        }


    fun kaydet(view: View) {
        //SQLite'a kaydetme
        println("tıklandı-buton")

        val yemekIsmi = yemekIsmiText.text.toString()
        val yemekMalzemeleri =yemekMalzemeText.text.toString()

        if (secilenBitmap != null) {
            val kucukBitmap = kucukBitmapOlustur(secilenBitmap!! , maximumBoyut = 300)

            val outputStream = ByteArrayOutputStream()
            kucukBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteDizisi = outputStream.toByteArray()

            try {
                   context?.let{
                       val database = it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null )
                       database.execSQL("CREATE TABLE IF NOT EXISTS yemekler (id INTEGER PRIMARY KEY, yemekismi VARCHAR, yemekmalzemesi VARCHAR, gorsel BLOB)")

                       val sqlString = "INSERT INTO yemekler (yemekismi , yemekmalzemesi, gorsel) VALUES (?, ?, ?)"
                       val statement = database.compileStatement(sqlString)
                       statement.bindString(1, yemekIsmi)
                       statement.bindString(2, yemekMalzemeleri)
                       statement.bindBlob(3, byteDizisi)
                       statement.execute()
                   }

            }catch (e:Exception) {
                e.printStackTrace()
            }

            val action = RecipeFragment2Directions.actionTarifFragmentToListeFragment()
            Navigation.findNavController(view).navigate(action)
        }

    }

    fun gorselSec(view: View) {

        println("tıklandı-gorsel")

        activity?.let {
            if (ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                // izin verilmedi, izim istemmemiz lazım
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)
            }else {
                //izin verilmiş tekrar istemeden galeriye git
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent, 2)
            }
        }

    }

    override fun onRequestPermissionsResult(

        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galeriIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeriIntent, 2)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
           secilenGorsel = data.data
            try {

                context?.let {
                    if(secilenGorsel != null) {

                        if (Build.VERSION.SDK_INT >=28 ) {
                            val source = ImageDecoder.createSource(it.contentResolver, secilenGorsel!!)
                            secilenBitmap = ImageDecoder.decodeBitmap(source)
                            gorselSec.setImageBitmap(secilenBitmap)
                        }else {
                            secilenBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver, secilenGorsel)
                            gorselSec.setImageBitmap(secilenBitmap)
                        }

                    }
                }


            }catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun kucukBitmapOlustur(kullanicininSectigiBitmap : Bitmap , maximumBoyut: Int) : Bitmap {

        var width = kullanicininSectigiBitmap.width
        var heigth = kullanicininSectigiBitmap.height
        val bitmapOrani: Double = width.toDouble() / heigth.toDouble()
        if (bitmapOrani > 1) {
            //Görsel Yatay
            val kisaltilmisHeight = width / bitmapOrani
            heigth = kisaltilmisHeight.toInt()
        }else {
            //Görsel Dikey
            heigth = maximumBoyut
            val kisaltilmisWidth = heigth * bitmapOrani
            width = kisaltilmisWidth.toInt()
        }

        return Bitmap.createScaledBitmap(kullanicininSectigiBitmap, width / 2 , heigth/2 ,true  )
    }


}