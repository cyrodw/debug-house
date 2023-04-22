package com.github.cyrobdw.debughouse;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.Objects;

public class SoundPlayer {
    public static void playSound(String sound) {
        String mediaURL = Objects.requireNonNull(SoundPlayer.class.getResource("/female_voicepack/" + sound)).toExternalForm();
        Media hit = new Media(mediaURL);
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
    }
}
