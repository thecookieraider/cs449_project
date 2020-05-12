package com.ketloz.lolbot;

import android.arch.lifecycle.ViewModel;

import java.util.List;

import no.stelar7.api.r4j.basic.constants.api.Platform;
import no.stelar7.api.r4j.impl.R4J;
import no.stelar7.api.r4j.pojo.lol.match.Match;
import no.stelar7.api.r4j.pojo.lol.summoner.Summoner;

public class MainActivityViewModel extends ViewModel {
    public List<Match> matches = null;
    public Platform platform = Platform.NA1;
    public R4J api;
    public String summoner;
    public Summoner apiSummoner;
}