package com.ketloz.lolbot;

public interface Operation<T> {
    T perform(MainActivityViewModel model);
}
