package com.codesample.mymemo.server;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Server {
    public static final String BASE_URL= "http://10.0.2.2:3000";
    private static Server instance;
    private final ServerApi api;

    private Server(Context context){
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HeaderInterceptor(context.getApplicationContext()))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ServerApi.class);
    }

    public static Server getInstance(Context context){
        if(instance==null) instance = new Server(context);
        return instance;
    }

    public ServerApi getApi(){ return api; }
}
