package com.enigma.imagegenerator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    EditText inputText;
    MaterialButton generateButton;
    ProgressBar progressBar;
    ImageView imageView;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputText = findViewById(R.id.editText_write);
        generateButton = findViewById(R.id.buttonGenerate);
        progressBar = findViewById(R.id.progress_circular);
        imageView = findViewById(R.id.image_view);

        generateButton.setOnClickListener(view -> {
            String input = inputText.getText().toString().trim();
            if(input.isEmpty()){
                inputText.setError("Input can't be empty");
            }
            callApi(input);
            inputText.setText("");
        });

    }

    void callApi(String input) {
        setInProgress(true);
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("prompt",input);
            jsonBody.put("size","256x256");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        RequestBody body = RequestBody.create(jsonBody.toString(),JSON);
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/images/generations")
                .header("Authorization", "Bearer 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")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(MainActivity.this, "Failed to generate image", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                Log.i("Response", response.body().string());
                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    String url = jsonObject.getJSONArray("data").getJSONObject(0).getString("url");
                    loadImage(url);
                    setInProgress(false);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }

    void loadImage(String url){
        //load image
        // as we are calling loadImage function in callback above we need to use ui thread
        runOnUiThread(()->{
            Glide.with(this).load(url).into(imageView);
        });
    }

    void setInProgress(boolean inProgress){
        runOnUiThread(()->{
            if(inProgress){
                progressBar.setVisibility(View.VISIBLE);
                generateButton.setVisibility(View.GONE);
            }else{
                progressBar.setVisibility(View.GONE);
                generateButton.setVisibility(View.VISIBLE);
            }
        });
    }
}