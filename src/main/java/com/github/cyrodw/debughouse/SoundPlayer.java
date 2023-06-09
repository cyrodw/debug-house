package com.github.cyrodw.debughouse;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Objects;

public class SoundPlayer {
    public static void playSound(String sound) {
        // Credit: https://thief.sourceforge.net/download.html
        String mediaURL = Objects.requireNonNull(SoundPlayer.class.getResource("/female_voicepack/" + sound)).toExternalForm();
        Media hit = new Media(mediaURL);
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
    }
}
