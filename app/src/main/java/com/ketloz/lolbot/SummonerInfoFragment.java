package com.ketloz.lolbot;

import android.annotation.TargetApi;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.annotation.Target;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import no.stelar7.api.r4j.basic.constants.types.GameQueueType;
import no.stelar7.api.r4j.basic.constants.types.TierDivisionType;
import no.stelar7.api.r4j.impl.R4J;
import no.stelar7.api.r4j.pojo.lol.championmastery.ChampionMastery;
import no.stelar7.api.r4j.pojo.lol.league.LeagueEntry;
import no.stelar7.api.r4j.pojo.lol.staticdata.champion.StaticChampion;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SummonerInfoFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SummonerInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SummonerInfoFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    public SummonerInfoFragment() {
        // Required empty public constructor
    }

    private MainActivityViewModel model;
    public static SummonerInfoFragment newInstance() {
        SummonerInfoFragment fragment = new SummonerInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    public Bitmap getRemoteImage(final URL aURL) {
        try {
            final URLConnection conn = aURL.openConnection();
            conn.connect();
            final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            final Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            return bm;
        } catch (IOException e) {}
        return null;
    }

    @Override
    @TargetApi(25)
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_summoner_info, container, false);
        model = ViewModelProviders.of(requireActivity()).get(MainActivityViewModel.class);

        List<ChampionMastery> mastered = model.api.getLoLAPI().getMasteryAPI().getTopChampions(model.platform, model.apiSummoner.getSummonerId(), 3);
        List<LeagueEntry> leagues = model.api.getLoLAPI().getLeagueAPI().getLeagueEntries(model.platform, model.apiSummoner.getSummonerId());
        long level = model.apiSummoner.getSummonerLevel();
        int profileIcon = model.apiSummoner.getProfileIconId();
        LeagueEntry solo = leagues.stream().filter(l -> l.getQueueType() == GameQueueType.RANKED_SOLO_5X5).findFirst().get();

        ImageView profileIconView = (ImageView)view.findViewById(R.id.profile_icon);
        try {

            profileIconView.setImageBitmap(getRemoteImage(new URL(model.api.getImageAPI().getProfileIcon(profileIcon + "", null))));
        } catch (Exception e) {
            System.err.println(e);
        }

        TextView levelView = view.findViewById(R.id.summoner_level);
        levelView.setText("Summoner Level " + level);

        TextView rank = view.findViewById(R.id.rank);
        rank.setText(solo.getTierDivisionType().prettyName());

        ImageView master1 = view.findViewById(R.id.mastery_champion_1);
        ImageView master2 = view.findViewById(R.id.mastery_champion_2);
        ImageView master3 = view.findViewById(R.id.mastery_champion_3);

        try {
            master1.setImageBitmap(getRemoteImage(new URL(model.api.getImageAPI().getSquare(model.api.getDDragonAPI().getChampion(mastered.get(0).getChampionId()), null))));
        } catch (Exception e) {
            System.err.println(e);
        }

        try {
            master2.setImageBitmap(getRemoteImage(new URL(model.api.getImageAPI().getSquare(model.api.getDDragonAPI().getChampion(mastered.get(1).getChampionId()), null))));
        } catch (Exception e) {
            System.err.println(e);
        }

        try {
            master3.setImageBitmap(getRemoteImage(new URL(model.api.getImageAPI().getSquare(model.api.getDDragonAPI().getChampion(mastered.get(2).getChampionId()), null))));
        } catch (Exception e) {
            System.err.println(e);
        }

        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
