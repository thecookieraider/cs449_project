package com.ketloz.lolbot;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ketloz.lolbot.MatchListFragment.OnListFragmentInteractionListener;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import no.stelar7.api.r4j.impl.R4J;
import no.stelar7.api.r4j.pojo.lol.match.Match;
import no.stelar7.api.r4j.pojo.lol.match.Participant;
import no.stelar7.api.r4j.pojo.lol.match.ParticipantIdentity;
import no.stelar7.api.r4j.pojo.lol.staticdata.perk.StaticPerk;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

public class MyMatchListRecyclerViewAdapter extends RecyclerView.Adapter<MyMatchListRecyclerViewAdapter.ViewHolder> {

    private final List<Match> matches;
    private final OnListFragmentInteractionListener listener;
    private final Summoner summoner;
    private final R4J api;

    public MyMatchListRecyclerViewAdapter(List<Match> items, Summoner summoner, R4J api, OnListFragmentInteractionListener listener) {
        matches = items;
        this.api = api;
        this.summoner = summoner;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_matchlist, parent, false);
        return new ViewHolder(view);
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
    @TargetApi(26)
    public void onBindViewHolder(final ViewHolder holder, int position) {
            try {

                holder.match = this.matches.get(position);

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                holder.matchType.setText(holder.match.getMatchMode().prettyName());
                holder.matchTimeAgo.setText(dtf.format(holder.match.getMatchCreationAsDate()));

                Participant player = holder.match.getParticipant(summoner).orElse(new Participant());
                String resultText = holder.match.didWin(player) ? "Victory" : "Defeat";

                holder.matchResult.setText(resultText);

                String profileIconUri = this.api.getImageAPI().getSquare(player.getChampion(), null);
                holder.matchProfileIcon.setImageBitmap(this.getRemoteImage(new URL(profileIconUri)));

                String summonerIcon1Uri = this.api.getImageAPI().getSummonerSpell(player.getSpell1(), null);
                holder.matchSummonerSpell1.setImageBitmap(this.getRemoteImage(new URL(summonerIcon1Uri)));

                String summonerIcon2Uri = this.api.getImageAPI().getSummonerSpell(player.getSpell2(), null);
                holder.matchSummonerSpell2.setImageBitmap(this.getRemoteImage(new URL(summonerIcon2Uri)));

                StaticPerk runeIcon1 = this.api.getDDragonAPI().getPerk(player.getPerks().getPerkPrimaryStyle());
                holder.matchRune1.setImageBitmap(this.getRemoteImage(new URL(runeIcon1.getIcon())));

                StaticPerk runeIcon2 = this.api.getDDragonAPI().getPerk(player.getPerks().getPerkSubStyle());
                holder.matchRune2.setImageBitmap(this.getRemoteImage(new URL(runeIcon2.getIcon())));

                holder.matchFinalLevel.setText("Level" + player.getStats().getChampLevel());

                final long totalKillsOnTeam = holder.match.getParticipants().stream()
                        .map(p -> p.getStats().getKills())
                        .reduce(0L, (t, i) -> i + t);

                String kp = (double) player.getStats().getKills() / (double) totalKillsOnTeam * 100D + "%";
                holder.matchKp.setText(kp + "KP");

                String matchItem1Uri = api.getImageAPI().getItem(String.valueOf(player.getStats().getItem0()), null);
                holder.matchItem1.setImageBitmap(this.getRemoteImage(new URL(matchItem1Uri)));

                String matchItem2Uri = api.getImageAPI().getItem(String.valueOf(player.getStats().getItem1()), null);
                holder.matchItem2.setImageBitmap(this.getRemoteImage(new URL(matchItem2Uri)));

                String matchItem3Uri = api.getImageAPI().getItem(String.valueOf(player.getStats().getItem2()), null);
                holder.matchItem3.setImageBitmap(this.getRemoteImage(new URL(matchItem3Uri)));

                String matchItem4Uri = api.getImageAPI().getItem(String.valueOf(player.getStats().getItem3()), null);
                holder.matchItem4.setImageBitmap(this.getRemoteImage(new URL(matchItem4Uri)));

                String matchItem5Uri = api.getImageAPI().getItem(String.valueOf(player.getStats().getItem4()), null);
                holder.matchItem5.setImageBitmap(this.getRemoteImage(new URL(matchItem5Uri)));

                String matchItem6Uri = api.getImageAPI().getItem(String.valueOf(player.getStats().getItem5()), null);
                holder.matchItem6.setImageBitmap(this.getRemoteImage(new URL(matchItem6Uri)));

                String matchItem7Uri = api.getImageAPI().getItem(String.valueOf(player.getStats().getItem6()), null);
                holder.matchItemTrinket.setImageBitmap(this.getRemoteImage(new URL(matchItem7Uri)));

                List<Participant> participants = holder.match.getParticipants();
                List<ParticipantIdentity> identities = holder.match.getParticipantIdentities();

                String matchPlayer1ImageUri = api.getImageAPI().getSquare(participants.get(0).getChampion(), null);
                holder.matchPlayer1Image.setImageBitmap(this.getRemoteImage(new URL(matchPlayer1ImageUri)));
                holder.matchPlayer1Name.setText(identities.get(0).getSummonerName());

                String matchPlayer2ImageUri = api.getImageAPI().getSquare(participants.get(1).getChampion(), null);
                holder.matchPlayer2Image.setImageBitmap(this.getRemoteImage(new URL(matchPlayer2ImageUri)));
                holder.matchPlayer2Name.setText(identities.get(1).getSummonerName());

                String matchPlayer3ImageUri = api.getImageAPI().getSquare(participants.get(2).getChampion(), null);
                holder.matchPlayer3Image.setImageBitmap(this.getRemoteImage(new URL(matchPlayer3ImageUri)));
                holder.matchPlayer3Name.setText(identities.get(2).getSummonerName());

                String matchPlayer4ImageUri = api.getImageAPI().getSquare(participants.get(3).getChampion(), null);
                holder.matchPlayer4Image.setImageBitmap(this.getRemoteImage(new URL(matchPlayer4ImageUri)));
                holder.matchPlayer4Name.setText(identities.get(3).getSummonerName());

                String matchPlayer5ImageUri = api.getImageAPI().getSquare(participants.get(4).getChampion(), null);
                holder.matchPlayer5Image.setImageBitmap(this.getRemoteImage(new URL(matchPlayer5ImageUri)));
                holder.matchPlayer5Name.setText(identities.get(4).getSummonerName());

                String matchPlayer6ImageUri = api.getImageAPI().getSquare(participants.get(5).getChampion(), null);
                holder.matchPlayer6Image.setImageBitmap(this.getRemoteImage(new URL(matchPlayer6ImageUri)));
                holder.matchPlayer6Name.setText(identities.get(5).getSummonerName());

                String matchPlayer7ImageUri = api.getImageAPI().getSquare(participants.get(6).getChampion(), null);
                holder.matchPlayer7Image.setImageBitmap(this.getRemoteImage(new URL(matchPlayer7ImageUri)));
                holder.matchPlayer7Name.setText(identities.get(6).getSummonerName());

                String matchPlayer8ImageUri = api.getImageAPI().getSquare(participants.get(7).getChampion(), null);
                holder.matchPlayer8Image.setImageBitmap(this.getRemoteImage(new URL(matchPlayer8ImageUri)));
                holder.matchPlayer8Name.setText(identities.get(7).getSummonerName());

                String matchPlayer9ImageUri = api.getImageAPI().getSquare(participants.get(8).getChampion(), null);
                holder.matchPlayer9Image.setImageBitmap(this.getRemoteImage(new URL(matchPlayer9ImageUri)));
                holder.matchPlayer9Name.setText(identities.get(8).getSummonerName());

                String matchPlayer10ImageUri = api.getImageAPI().getSquare(participants.get(9).getChampion(), null);
                holder.matchPlayer10Image.setImageBitmap(this.getRemoteImage(new URL(matchPlayer10ImageUri)));
                holder.matchPlayer10Name.setText(identities.get(9).getSummonerName());

                holder.view.setOnClickListener(v -> {
                    if (null != listener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        listener.onListFragmentInteraction(holder.match);
                    }
                });
            } catch (Exception e) {
                System.out.println(e);
            }
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public Match match;
        public TextView matchType;
        public TextView matchTimeAgo;
        public TextView matchResult;
        public ImageView matchProfileIcon;
        public ImageView matchSummonerSpell1;
        public ImageView matchSummonerSpell2;
        public ImageView matchRune1;
        public ImageView matchRune2;
        public TextView matchFinalLevel;
        public TextView matchKp;
        public TextView matchFinalCs;
        public ImageView matchItem1;
        public ImageView matchItem2;
        public ImageView matchItem3;
        public ImageView matchItem4;
        public ImageView matchItem5;
        public ImageView matchItem6;
        public ImageView matchItemTrinket;

        public ImageView matchPlayer1Image;
        public TextView matchPlayer1Name;

        public ImageView matchPlayer2Image;
        public TextView matchPlayer2Name;

        public ImageView matchPlayer3Image;
        public TextView matchPlayer3Name;

        public ImageView matchPlayer4Image;
        public TextView matchPlayer4Name;

        public ImageView matchPlayer5Image;
        public TextView matchPlayer5Name;

        public ImageView matchPlayer6Image;
        public TextView matchPlayer6Name;

        public ImageView matchPlayer7Image;
        public TextView matchPlayer7Name;

        public ImageView matchPlayer8Image;
        public TextView matchPlayer8Name;

        public ImageView matchPlayer9Image;
        public TextView matchPlayer9Name;

        public ImageView matchPlayer10Image;
        public TextView matchPlayer10Name;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.matchType = view.findViewById(R.id.match_type);
            this.matchTimeAgo = view.findViewById(R.id.match_time_ago);
            this.matchResult = view.findViewById(R.id.match_result);
            this.matchProfileIcon = view.findViewById(R.id.match_profile_icon);
            this.matchSummonerSpell1 = view.findViewById(R.id.match_summoner_spell_1);
            this.matchSummonerSpell2 = view.findViewById(R.id.match_summoner_spell_2);
            this.matchRune1 = view.findViewById(R.id.match_rune_1);
            this.matchRune2 = view.findViewById(R.id.match_rune_2);
            this.matchFinalLevel = view.findViewById(R.id.match_final_level);
            this.matchKp = view.findViewById(R.id.match_kp);
            this.matchFinalCs = view.findViewById(R.id.match_final_cs);
            this.matchItem1 = view.findViewById(R.id.match_item_1);
            this.matchItem2 = view.findViewById(R.id.match_item_2);
            this.matchItem3 = view.findViewById(R.id.match_item_3);
            this.matchItem4 = view.findViewById(R.id.match_item_4);
            this.matchItem5 = view.findViewById(R.id.match_item_5);
            this.matchItem6 = view.findViewById(R.id.match_item_6);
            this.matchItemTrinket = view.findViewById(R.id.match_item_trinket);

            this.matchPlayer1Image = view.findViewById(R.id.match_player_1_image);
            this.matchPlayer1Name = view.findViewById(R.id.match_player_1_name);

            this.matchPlayer2Image = view.findViewById(R.id.match_player_2_image);
            this.matchPlayer2Name = view.findViewById(R.id.match_player_2_name);

            this.matchPlayer3Image = view.findViewById(R.id.match_player_3_image);
            this.matchPlayer3Name = view.findViewById(R.id.match_player_3_name);

            this.matchPlayer4Image = view.findViewById(R.id.match_player_4_image);
            this.matchPlayer4Name = view.findViewById(R.id.match_player_4_name);

            this.matchPlayer5Image = view.findViewById(R.id.match_player_5_image);
            this.matchPlayer5Name = view.findViewById(R.id.match_player_5_name);

            this.matchPlayer6Image = view.findViewById(R.id.match_player_6_image);
            this.matchPlayer6Name = view.findViewById(R.id.match_player_6_name);

            this.matchPlayer7Image = view.findViewById(R.id.match_player_7_image);
            this.matchPlayer7Name = view.findViewById(R.id.match_player_7_name);

            this.matchPlayer8Image = view.findViewById(R.id.match_player_8_image);
            this.matchPlayer8Name = view.findViewById(R.id.match_player_8_name);

            this.matchPlayer9Image = view.findViewById(R.id.match_player_9_image);
            this.matchPlayer9Name = view.findViewById(R.id.match_player_9_name);

            this.matchPlayer10Image = view.findViewById(R.id.match_player_10_image);
            this.matchPlayer10Name = view.findViewById(R.id.match_player_10_name);
        }
    }
}
