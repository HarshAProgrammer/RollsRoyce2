package com.rackluxury.rollsroyce.reddit.events;

public class ChangeSpoilerBlurEvent {
    public boolean needBlurSpoiler;

    public ChangeSpoilerBlurEvent(boolean needBlurSpoiler) {
        this.needBlurSpoiler = needBlurSpoiler;
    }
}
