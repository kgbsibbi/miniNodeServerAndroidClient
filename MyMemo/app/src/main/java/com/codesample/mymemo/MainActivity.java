package com.codesample.mymemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codesample.mymemo.data.User;
import com.codesample.mymemo.databinding.ActivityMainBinding;
import com.codesample.mymemo.server.Server;
import com.codesample.mymemo.server.ServerApi;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private SharedPreferences preferences;
    private Call<User> loginRequest;
    private Call<Void> registerRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferences = getSharedPreferences("auth", MODE_PRIVATE);
        String token = preferences.getString("token", null);
        if(token != null){
            displayUI(false);
            autoLogin();
        }

        binding.radioButtonRegister.setOnClickListener(v->binding.textName.setVisibility(View.VISIBLE));
        binding.radioButtonLogin.setOnClickListener(v->binding.textName.setVisibility(View.INVISIBLE));
        binding.buttonOk.setOnClickListener(v->onButton());
    }

    private void autoLogin(){
        loginRequest = Server.getInstance(this).getApi().autoLogin();
        loginRequest.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()==200 && response.body()!=null){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("name", response.body().name);
                    editor.apply();
                    startActivity(new Intent(MainActivity.this, ListActivity.class));
                    finish();
                } else {
                    displayUI(true);
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                displayUI(true);
            }
        });
    }

    private void displayUI(boolean display){
        if(display){
            binding.mainLayout.setVisibility(View.VISIBLE);
        } else {
            binding.mainLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void onButton(){
        String id = Objects.requireNonNull(binding.editTextUserid.getText()).toString();
        String password = Objects.requireNonNull(binding.editTextPassword.getText()).toString();
        if(id.isEmpty() || password.isEmpty()){
            Toast.makeText(this, R.string.auth_required, Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User(id, password);
        if(binding.radioButtonRegister.isChecked()){
            String name = Objects.requireNonNull(binding.editTextName.getText()).toString();
             if(name.isEmpty()){
                 Toast.makeText(this, R.string.auth_required, Toast.LENGTH_SHORT).show();
                 return;
             }
            user.name = name;
            registerRequest = Server.getInstance(this).getApi().addUser(user);
            registerRequest.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if(response.code()==200)
                        login(user);
                }
                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                }
            });
        } else {
            login(user);
        }
    }

    private void login(User user){
        loginRequest = Server.getInstance(this).getApi().login(user);
        loginRequest.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if(response.code()==200 && response.body()!=null){
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", response.body().token);
                    editor.putString("name", response.body().name);
                    editor.putString("userid", user.userid);
                    editor.apply();
                    startActivity(new Intent(MainActivity.this, ListActivity.class));
                    finish();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(loginRequest!=null) loginRequest.cancel();
        if(registerRequest!=null) registerRequest.cancel();
    }
}