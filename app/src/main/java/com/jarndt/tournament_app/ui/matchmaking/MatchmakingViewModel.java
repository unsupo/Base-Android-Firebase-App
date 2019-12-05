package com.jarndt.tournament_app.ui.matchmaking;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MatchmakingViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MatchmakingViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}