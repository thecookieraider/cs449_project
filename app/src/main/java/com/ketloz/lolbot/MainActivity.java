 package com.ketloz.lolbot;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import no.stelar7.api.r4j.basic.APICredentials;
import no.stelar7.api.r4j.basic.constants.api.Platform;
import no.stelar7.api.r4j.impl.R4J;

 public class MainActivity extends AppCompatActivity {
    private ApiRequestTask apiThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        APICredentials creds = new APICredentials(getResources().getString(R.string.riot_api_key));
        final MainActivityViewModel model = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        model.api = new R4J(creds);

        EditText searchBox = findViewById(R.id.searchBox);

        final MainActivity that = this;

        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
                model.summoner = v.getText().toString();
                apiThread = ApiManager.startDownload(that);
            }

            return true;
        });

        Button regionButton = findViewById(R.id.regionButton);
        regionButton.setText(Platform.NA1.getValue());
        regionButton.setOnClickListener(v -> {

        });
    }

     @Override
     public void onDetachedFromWindow() {
         this.apiThread = null;
         super.onDetachedFromWindow();
     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }
}
