package com.example.ap2_speakeasy.API;

import com.example.ap2_speakeasy.User;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ChatServiceAPI {

        @POST("Chats/")
        Call<Void> createChat(@Body  String username);

        @GET("Chats/")
        Call<List<User>> getChats();

        @GET("Chats/{id}")
        Call<Void> getChat(@Path("id") int id);

        @DELETE("Chats/{id}")
        Call<Void> deleteChat (@Path("id") int id);
}
