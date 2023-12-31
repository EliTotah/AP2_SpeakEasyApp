package com.example.ap2_speakeasy;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.ap2_speakeasy.API.UserAPI;
import com.example.ap2_speakeasy.Dao.AppDB;
import com.example.ap2_speakeasy.Dao.ContactDao;
import com.example.ap2_speakeasy.FireBase.SingeltonFireBase;
import com.example.ap2_speakeasy.ViewModels.ContactViewModel;
import com.example.ap2_speakeasy.adapters.ContactListAdapter;
import com.example.ap2_speakeasy.databinding.ActivityChatContactsBinding;
import com.example.ap2_speakeasy.entities.Contact;
import com.example.ap2_speakeasy.entities.Message;
import com.example.ap2_speakeasy.entities.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ChatContactsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, TextWatcher {
    private ActivityChatContactsBinding binding;
    String activeUserName;
    String userToken;

    private Boolean isNightMode = null;
    private AppDB db;
    private List<Contact> contacts;
    private ContactDao contactDao;
    private ContactListAdapter adapter;

    private ContactViewModel viewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();

        // Retrieve the initial value of the preference and set the theme
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isNightMode = sharedPreferences.getBoolean("dark_mode", false);
        changeTheme(isNightMode);

        // Register the shared preference change listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (intent != null) {
             activeUserName = getIntent().getStringExtra("activeUserName");
             userToken = getIntent().getStringExtra("token");
        }

        UserAPI userAPI = new UserAPI();
        userAPI.getUserDetails(activeUserName, userToken, callback -> {
            if(callback == 200) {
                User u = userAPI.getUser();
                if (u!=null) {
                    binding.userDisplayName.setText(u.getDisplayName());
                    binding.userImage.setImageBitmap(decodeImage(u.getProfilePic()));
                }
                else {
                    //error
                }
            }
            else {
                //error
            }
        });

        FloatingActionButton settingsButton = binding.settingsButton;
        settingsButton.setOnClickListener(v -> {
            // Start the SettingActivity
            Intent intentSettings = new Intent(ChatContactsActivity.this, SettingActivity.class);
            intentSettings.putExtra("camefrom","contacts");
            startActivity(intentSettings);
        });

        this.db = DatabaseManager.getDatabase();

        this.contactDao = db.contactDao();
        this.viewModel = new ContactViewModel(userToken);
        this.contacts = new ArrayList<>();

        ListView lvContacts = binding.listViewChats;
        adapter = new ContactListAdapter(getApplicationContext(), (ArrayList<Contact>) this.contacts, isNightMode);
        adapter.setNightMode(isNightMode);

        viewModel.getContacts().observe(this, adapter::setContacts);

        lvContacts.setAdapter(adapter);
        lvContacts.setClickable(true);

        binding.searchEditText.addTextChangedListener(this);

        MutableLiveData<String> contactFirebase = SingeltonFireBase.getContactFirebase();
        MutableLiveData<Message> messageMutableLiveData = SingeltonFireBase.getMessageFirebase();

        messageMutableLiveData.observe(this,contacts -> {
            if(contacts!=null) {
                viewModel.getContacts();
            }
        });
        contactFirebase.observe(this,contact -> {
            if (contact != null) {
                //viewModel.addContact(contact);
                viewModel.getContacts();
            }
        });

        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Contact clicked = contacts.get(position);
                Intent intent2 = new Intent(ChatContactsActivity.this, ChatWindowActivity.class);
                intent2.putExtra("token", userToken);
                intent2.putExtra("friendDisplayName", clicked.getUser().getDisplayName());
                intent2.putExtra("friendPic", clicked.getUser().getProfilePic());
                intent2.putExtra("chatID", String.valueOf(clicked.getId()));
                intent2.putExtra("activeUserName", activeUserName);
                startActivity(intent2);
            }
        });

        binding.addContactButton.setOnClickListener(view -> showAddContactDialog());

        binding.logoutButton.setOnClickListener(view -> {
            finish();
        });
    }
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("dark_mode")) {
            isNightMode = sharedPreferences.getBoolean(key, false);
            changeTheme(isNightMode);
        }
    }

    private void showAddContactDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_add_contact, null);
        dialogBuilder.setView(dialogView);

        EditText usernameEditText = dialogView.findViewById(R.id.usernameEditText);
        dialogBuilder.setPositiveButton("OK", (dialogInterface, i) -> {


            String username = usernameEditText.getText().toString().trim();
            if (!username.isEmpty()) {
                viewModel.insertContact(usernameEditText.getText().toString());
            } else {
                Toast.makeText(ChatContactsActivity.this, "Please enter a username",
                        Toast.LENGTH_SHORT).show();
            }
        });

        dialogBuilder.setNegativeButton("Cancel", null);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        adapter.notifyDataSetChanged();
    }

    private Bitmap decodeImage(String imageString) {
        try {
            byte[] imageBytes = Base64.decode(imageString, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            Toast.makeText(AP2_SpeakEasy.context,
                    "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();;
        }
        return null;
    }

    private void changeTheme(boolean isNightMode) {
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the shared preference change listener
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // Not needed in this case
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Get the search query
        String query = s.toString().trim();

        // Filter the contacts based on the query
        List<Contact> filteredContacts = new ArrayList<>();

        // Update the contacts list with the current database contacts
        contacts.clear();
        contacts.addAll(viewModel.getContacts().getValue());

        if (query.isEmpty()) {
            // If the query is empty, show all contacts
            filteredContacts.addAll(contacts);
        } else {
            for (Contact contact : contacts) {
                if (contact.getUser().getDisplayName().toLowerCase().startsWith(query.toLowerCase())) {
                    filteredContacts.add(contact);
                }
            }
        }

        // Update the contact list in the adapter
        adapter.setContacts(filteredContacts);
        adapter.notifyDataSetChanged();
        }

        @Override
        public void afterTextChanged(Editable s) {
            // Not needed in this case
        }
    @Override
    protected void onResume() {
        super.onResume();
        if(isReturn.getInstance().getIsReturn()){
            finish();
        }
        // Get contacts list from web-api
        new Thread(() -> viewModel.getContacts()).start();
    }

}




