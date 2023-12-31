package com.example.ap2_speakeasy.converts;

import androidx.room.TypeConverter;

import com.example.ap2_speakeasy.entities.Message;
import com.google.gson.Gson;

public class messageConverter {
    @TypeConverter
    public static Message fromJsonString(String value) {
        return new Gson().fromJson(value, Message.class);
    }

    @TypeConverter
    public static String toJsonString(Message message) {
        return new Gson().toJson(message);
    }
}
