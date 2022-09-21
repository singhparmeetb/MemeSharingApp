package com.example.memes

import android.app.DownloadManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.DnsResolver
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.transition.Visibility
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.Exception
import java.time.temporal.TemporalAdjusters.next

class MainActivity : AppCompatActivity() {
    var memeUrl:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val loader= ImageLoader.Builder(this).build()
        loadMeme(loader)
        findViewById<Button>(R.id.nextButton).setOnClickListener{next(loader)}
        findViewById<Button>(R.id.shareButton).setOnClickListener{share()}
        findViewById<FloatingActionButton>(R.id.downloadButton).setOnClickListener{DownloadMeme()}
    }

    private fun share(){
        val intent=Intent(Intent.ACTION_SEND)
        intent.type="text/plain"
        intent.putExtra(Intent.EXTRA_TEXT,"Checkout this meme I found on Reddit! ${memeUrl}")
        val chooser=Intent.createChooser(intent,"Share this meme on")
        startActivity(chooser)
    }

    fun loadMeme(b:ImageLoader){
        val serviceGenerator=ServiceGenerator.buildService(apiService::class.java)
        val call=serviceGenerator.getPost()
        call.enqueue(object: Callback<dataModel>{
            override fun onResponse(call: Call<dataModel>, response: Response<dataModel>) {
                if(response.isSuccessful){
                    memeUrl=response.body()!!.url.toString()
                    val req=ImageRequest.Builder(this@MainActivity)
                        .data(memeUrl)
                        .target(
                            onStart = {
                                findViewById<ProgressBar>(R.id.loading).visibility=View.VISIBLE
                            },
                            onSuccess = {result->
                                findViewById<ProgressBar>(R.id.loading).visibility=View.INVISIBLE
                                findViewById<ImageView>(R.id.Meme).setImageDrawable(result)
                            }

                        )
                        .build()
                    b.enqueue(req)
                //a.load(response.body()?.url.toString())
                }
            }

            override fun onFailure(call: Call<dataModel>, t: Throwable) {
                t.printStackTrace()
                Log.i("Unsecessssssssssss","failed")
            }
        })
    }
    private fun next(b:ImageLoader){loadMeme(b)}
    private fun DownloadMeme(){
        val filename="meme${(1..100).random()}a${(100..200).random()}"
        try {
            var downloadManager:DownloadManager?=null
            downloadManager=getSystemService(DOWNLOAD_SERVICE)as DownloadManager
            val downloadUri= Uri.parse(memeUrl)
            val request=DownloadManager.Request(downloadUri)
            request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                .setAllowedOverRoaming(false)
                .setTitle(filename)
                .setMimeType("image/jpeg")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, File.separator+filename+".jpg"
                )
            downloadManager.enqueue(request)
            Toast.makeText(this,R.string.downloadStarted,Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            Toast.makeText(this,R.string.downloadFailed,Toast.LENGTH_SHORT).show()
        }
    }
}


