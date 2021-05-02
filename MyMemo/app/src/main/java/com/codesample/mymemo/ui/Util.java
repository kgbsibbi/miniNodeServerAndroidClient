package com.codesample.mymemo.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.codesample.mymemo.MainActivity;
import com.codesample.mymemo.R;
import com.codesample.mymemo.data.User;
import com.codesample.mymemo.server.Server;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Util {
    public static void requestDialog(User user, AppCompatActivity activity){
        SharedPreferences preferences = activity.getSharedPreferences("auth", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Intent intent = new Intent(activity, MainActivity.class);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setMessage(user.name)
                .setNeutralButton(R.string.auth_delete, (d, w)->{
                    Server.getInstance(activity).getApi().deleteUser(user.userid).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            editor.clear();
                            editor.apply();
                            activity.startActivity(intent);
                            activity.finish();
                        }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) { }
                    });
                })
                .setNegativeButton(R.string.auth_logout, (d, w)->{
                    editor.clear();
                    editor.apply();
                    activity.startActivity(intent);
                    activity.finish();
                })
                .create();
        dialog.show();
    }
}
