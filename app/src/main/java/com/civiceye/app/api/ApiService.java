package com.civiceye.app.api;

import java.util.Map;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/register")
    Call<ResponseBody> registerUser(@Body Map<String, String> request);

    @POST("api/login")
    Call<ResponseBody> loginUser(@Body Map<String, String> request);

    @POST("api/detect")
    Call<ResponseBody> detectIssue(@Body Map<String, Object> request);

    @POST("api/report")
    Call<ResponseBody> submitReport(@Body Map<String, Object> request);

    @GET("api/my-complaints")
    Call<ResponseBody> getMyComplaints(@Query("user_id") int userId);

    @GET("api/issues")
    Call<ResponseBody> getAllIssues();

    @PUT("api/status")
    Call<ResponseBody> updateStatus(@Body Map<String, Object> request);
}
