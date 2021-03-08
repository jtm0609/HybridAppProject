package com.example.myapplication

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {
    @GET("store/apps/details")
    fun getVersion(@Query("id") pack:String): Call<String>
}