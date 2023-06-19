package com.example.ap2_speakeasy.API;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.ap2_speakeasy.AP2_SpeakEasy;
import com.example.ap2_speakeasy.Dao.AppDB;
import com.example.ap2_speakeasy.Dao.ContactDao;
import com.example.ap2_speakeasy.DatabaseManager;
import com.example.ap2_speakeasy.LoginActivity;
import com.example.ap2_speakeasy.entities.ChatUserAdd;
import com.example.ap2_speakeasy.entities.Contact;
import com.example.ap2_speakeasy.R;
import com.example.ap2_speakeasy.entities.User;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
//import retrofit2.Response;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatAPI {
    Retrofit retrofit;
    ChatServiceAPI chatServiceAPI;

    private MutableLiveData responeAnswer;

    public ChatAPI() {

        /////////////
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        /////////////////

        retrofit = new Retrofit.Builder()
                .baseUrl(AP2_SpeakEasy.context.getString(R.string.BaseUrl))
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        chatServiceAPI = retrofit.create(ChatServiceAPI.class);
        responeAnswer = new MutableLiveData<>();
    }

    public void createChat(String token, String username,MutableLiveData<List<Contact>> contacts) {
        Call<ChatUserAdd> call = chatServiceAPI.createChat(token,Map.of("username",username));
        call.enqueue(new Callback<ChatUserAdd>() {
            @Override
            public void onResponse(Call<ChatUserAdd> call, Response<ChatUserAdd> response) {
                if (response.isSuccessful()) {
                    AppDB db = DatabaseManager.getDatabase();
                    ContactDao contactDao = db.contactDao();
                    ChatUserAdd chatUserAdd = response.body();
                    //Map<String, String> map = response.body();
                    if (chatUserAdd != null) {
                        /*String idString = chatUserAdd.get("id");
                        int id = Integer.parseInt(idString);

                        Gson gson = new Gson();
                        String userName = chatUserAdd.get("user");
                        User user = gson.fromJson(userName, User.class);*/
                        Contact c = new Contact(chatUserAdd.getId(),chatUserAdd.getUser(),null);
                        contactDao.insert(c);
                        List<Contact> currentUsers = contacts.getValue();
                        // Add the new User object to the current list
                        if (currentUsers != null) {
                            currentUsers.add(c);
                        } else {
                            currentUsers = new ArrayList<>();
                            currentUsers.add(c);
                        }
                        contacts.setValue(currentUsers);
                        Log.e("api call30",response.body().toString());
                        responeAnswer.setValue("ok");
                    }
                    else {
                        Log.e("api call31","booooooo");
                    }
                }
                else {
                    int a = response.code();
                    responeAnswer.setValue(response.errorBody().toString());
                    Log.e("api call32",response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<ChatUserAdd> call, Throwable t) {
                String err = t.getMessage();
                if (err!=null){
                    Log.e("api call34","ERROR: " + err );
                    Log.e("api call37", "ERROR: ", t);
                }
                else {
                    Log.e("api call35","Unknown error");
                }
            }
        });
    }

    public void getAllChats(MutableLiveData<List<Contact>> contactListData, String token) {
        Call<List<Contact>> call = chatServiceAPI.getChats(token);
        call.enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                if (response.isSuccessful()) {
                    Log.e("contactlist",contactListData.toString());
                    contactListData.setValue(response.body());
                }
                else {
                    Log.e("api call12","booooooo");
                }
                //callBackFlag.complete(response.code());
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable t) {
                String err = t.getMessage();
                if (err!=null){
                    Log.e("api call13","ERROR: " + err );
                }
                else {
                    Log.e("api call14","Unknown error");
                }
            }
        });
    }

    public void getUserDetails() {

    }

/*
    public Call<Contact> getChatById(int id) {
        return chatServiceAPI.getChat(id);
        /*call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Handle the successful response here
                } else {
                    // Handle the error response here
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle the failure here
            }
        });
    }
    public void deleteChat(int id) {
        chatServiceAPI.deleteChat(id);
        /*call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Handle the successful response here
                } else {
                    // Handle the error response here
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle the failure here
            }
        });
    }*/
}
