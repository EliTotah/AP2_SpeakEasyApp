package com.example.ap2_speakeasy.repositories;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.ap2_speakeasy.AP2_SpeakEasy;
import com.example.ap2_speakeasy.API.ChatAPI;
import com.example.ap2_speakeasy.Dao.AppDB;
import com.example.ap2_speakeasy.Dao.ContactDao;
import com.example.ap2_speakeasy.DatabaseManager;
import com.example.ap2_speakeasy.LoginActivity;
import com.example.ap2_speakeasy.MainActivity;
import com.example.ap2_speakeasy.entities.Contact;
import com.example.ap2_speakeasy.entities.Message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactRepository {

    private ContactDao contactDao;
    private ContactListData contactListData;
    private ChatAPI chatAPI;
    private String token;
    private AppDB db;

    public ContactRepository(String token) {
        this.token = token;
        this.db = DatabaseManager.getDatabase();
        this.contactDao = db.contactDao();
        this.contactListData = new ContactListData();
        this.chatAPI = new ChatAPI();
    }

    public LiveData<List<Contact>> getAll() {
        reload();
        return contactListData;
    }

    public void insertContact(String username) {
        chatAPI.createChat(token,username,contactListData);
    }

    public  void addContact(Contact c) {
        contactDao.insert(c);
        //contactListData.postValue(contactsList);
    }

    public void reload() {
        ChatAPI chatAPI = new ChatAPI();
        chatAPI.getAllChats(contactListData,token);
        contactDao.deleteContacts();
        List<Contact> contactsList = contactListData.getValue();
        if (contactsList != null) {
            for (Contact c : contactsList) {
                addContact(c);
            }
        }
        contactListData.postValue(contactDao.index());
    }


    class ContactListData extends MutableLiveData<List<Contact>> {
        public ContactListData() {
            super();
           setValue(contactDao.index());

        }

        @Override
        protected void onActive() {
            super.onActive();
            new Thread(() -> {
                chatAPI.getAllChats(this,token);
            }).start();
        }
    }
}
