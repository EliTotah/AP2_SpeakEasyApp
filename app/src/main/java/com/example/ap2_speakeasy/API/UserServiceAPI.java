package com.example.ap2_speakeasy.API;

import com.example.ap2_speakeasy.entities.User;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserServiceAPI {
    @POST("Tokens")
    Call<ResponseBody> login(@Body Map<String, String> user);

    @POST("Users")
    Call<ResponseBody> signup(@Body Map<String, String> user);

    @GET("Users/{id}")
    Call<User> getUser (@Header("Authorization")String token, @Path("id") String username);

}

