package com.aegis.shield.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface TwilioApiService {
    @FormUrlEncoded
    @POST("{accountSid}/Messages.json")
    suspend fun sendSms(
        @Header("Authorization") authorization: String,
        @Path("accountSid") accountSid: String,
        @Field("To") to: String,
        @Field("From") from: String,
        @Field("Body") body: String,
    ): Response<ResponseBody>
}
