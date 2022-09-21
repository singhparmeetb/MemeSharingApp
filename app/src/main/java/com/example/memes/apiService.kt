package com.example.memes

import retrofit2.Call
import retrofit2.http.GET

interface apiService {
    @GET("/gimme")
    fun getPost():Call<dataModel>
}