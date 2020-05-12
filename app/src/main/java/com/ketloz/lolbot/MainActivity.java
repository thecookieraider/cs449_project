 package com.ketloz.lolbot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import no.stelar7.api.r4j.basic.APICredentials;
import no.stelar7.api.r4j.basic.calling.DataCall;
import no.stelar7.api.r4j.basic.constants.api.Platform;
import no.stelar7.api.r4j.impl.R4J;
import no.stelar7.api.r4j.pojo.lol.match.Match;
import no.stelar7.api.r4j.pojo.lol.match.MatchReference;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

 public class MainActivity extends AppCompatActivity implements RegionDialog.RegionDialogListener, MatchListFragment.OnListFragmentInteractionListener, SummonerInfoFragment.OnFragmentInteractionListener {
    private ApiRequestTask apiThread;
    private MainActivityViewModel model;
    private APICredentials creds;

    @Override
    public void onRegionClick(DialogFragment dialog, Platform platform) {
        model.platform = platform;

        Button regionButton = findViewById(R.id.regionButton);
        regionButton.setText(platform.getValue());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_main);

        creds = new APICredentials(getResources().getString(R.string.riot_api_key));

        model = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        model.api = new R4J(creds);
        DataCall.setCacheProvider(null);

        EditText searchBox = findViewById(R.id.searchBox);

        final MainActivity that = this;

        searchBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
                // Check if no view has focus:
                View view = this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                model.summoner = v.getText().toString();

                ApiManager.startDownload(this::downloadMatches, that, model);
            }

            return true;
        });

        Button regionButton = findViewById(R.id.regionButton);
        regionButton.setText(Platform.NA1.getValue());
        model.platform = Platform.NA1;
        regionButton.setOnClickListener(v -> {
            RegionDialog dialog = new RegionDialog();
            dialog.show(getFragmentManager(), "RegionDialog");
        });
    }

    public List<Match> downloadMatches(MainActivityViewModel model) {
         try {
             System.out.println("Retrieving summoner...");
             Summoner summoner = model.api.getLoLAPI().getSummonerAPI().getSummonerByName(model.platform, model.summoner);
             System.out.println("Summoner retrieved");

             System.out.println("Retrieving match references...");
             List<MatchReference> matchRefs = model.api.getLoLAPI().getMatchAPI().getMatchList(model.platform, summoner.getAccountId(), 0, 1);
             System.out.println("Match references retrieved");

             System.out.println("Converting match references into full matches");
             List<Match> matches = new ArrayList<>();

             for (MatchReference match:
                  matchRefs) {
                 matches.add(match.getFullMatch());
             }

             System.out.println("Done converting");
             model.matches = matches;
             model.apiSummoner = summoner;
             System.out.println("Matches:" + matches);

             return matches;
         } catch (Exception e) {
             System.out.println(e.getMessage());
             return null;
         }
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

    @Override
     protected void onStop() {
        ApiManager.cancelAll();
        super.onStop();
    }

     @Override
     public void onListFragmentInteraction(Match item) {

     }

     @Override
     public void onFragmentInteraction(Uri uri) {

     }
 }
