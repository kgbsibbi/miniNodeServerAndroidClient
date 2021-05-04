package com.codesample.mymemo;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.codesample.mymemo.data.Memo;
import com.codesample.mymemo.data.User;
import com.codesample.mymemo.databinding.ActivityMemoBinding;
import com.codesample.mymemo.server.Server;
import com.codesample.mymemo.ui.Util;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemoActivity extends AppCompatActivity {
    private ActivityMemoBinding binding;
    private Memo memo;
    private Call<Memo> getRequest;
    private Call<Void> saveRequest;
    private final ActivityResultLauncher<String[]> getFile = registerForActivityResult(
        new ActivityResultContracts.OpenDocument(),
        result -> {
            binding.imageView.setImageURI(result);
            binding.buttonDelete.setVisibility(View.VISIBLE);
            if(memo!=null) {
                Cursor cursor = getContentResolver().query(result, null, null, null, null);
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                memo.fileUri = result;
                memo.originalFileName = cursor.getString(nameIndex);
            }
        });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences preferences = getSharedPreferences("auth", MODE_PRIVATE);
        User user = new User();
        user.name = preferences.getString("name", "");
        binding.buttonUserInfo.setText(user.name);
        binding.buttonUserInfo.setOnClickListener(v-> Util.requestDialog(user, this));

        binding.buttonBrowse.setOnClickListener(v-> getFile.launch(new String[]{"image/*"}));
        binding.buttonSave.setOnClickListener(v->saveMemo());
        binding.buttonDelete.setOnClickListener(v->{
            v.setVisibility(View.INVISIBLE);
            binding.imageView.setImageDrawable(null);
            memo.fileUri=null;
        });
        int memoid = getIntent().getIntExtra("memoid", 0);
        if(memoid > 0){
            requestMemo(memoid);
        } else{
            memo = new Memo();
        }
    }

    private void requestMemo(int memoid){
        getRequest = Server.getInstance(this).getApi().getMemo(memoid);
        getRequest.enqueue(new Callback<Memo>() {
            @Override
            public void onResponse(@NotNull Call<Memo> call, @NotNull Response<Memo> response) {
                if(response.code()==200 && response.body()!=null){
                    memo = response.body();
                    Log.d("Memo", memo.title);
                    updateWidgets();
                }
            }

            @Override
            public void onFailure(@NotNull Call<Memo> call, @NotNull Throwable t) {
            }
        });
    }

    private void updateWidgets(){
        if(memo.title!=null)
            Objects.requireNonNull(binding.editTextTitle).setText(memo.title);
        if(memo.content!=null)
            Objects.requireNonNull(binding.editTextContent).setText(memo.content);
        if(memo.fileUrl!=null){
            String url = Server.BASE_URL+"/"+memo.fileUrl;
            Glide.with(this).load(url).into(binding.imageView);
            binding.buttonDelete.setVisibility(View.VISIBLE);
        }
    }

    private void saveMemo(){
        String title = Objects.requireNonNull(binding.editTextTitle.getText()).toString();
        String content = Objects.requireNonNull(binding.editTextContent.getText()).toString();
        if(title.isEmpty()|| content.isEmpty()){
            Toast.makeText(this, R.string.auth_required, Toast.LENGTH_SHORT).show();
            return;
        }
        MultipartBody.Part filePart = null;
        File file;
        if(memo.fileUri !=null){
            try {
                ParcelFileDescriptor fd = this.getContentResolver().openFileDescriptor(memo.fileUri, "r");
                InputStream is = new FileInputStream(fd.getFileDescriptor());
                file = new File(getCacheDir(), memo.originalFileName);
                OutputStream os = new FileOutputStream(file);
                IOUtils.copy(is, os);
                RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                filePart = MultipartBody.Part.createFormData("file", memo.originalFileName, body);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(memo.memoid==0){ // create
            saveRequest = Server.getInstance(this).getApi().addMemo(title, content, filePart);
        } else { // update
            saveRequest = Server.getInstance(this).getApi().updateMemo(memo.memoid, title, content, filePart);
        }
        saveRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                if(memo.originalFileName!=null) {
                    File cachedFile = new File(getCacheDir(), memo.originalFileName);
                    if (cachedFile.exists())
                        cachedFile.delete();
                }
                finish();
            }
            @Override
            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(getRequest!=null) getRequest.cancel();
        if(saveRequest!=null) saveRequest.cancel();
    }
}