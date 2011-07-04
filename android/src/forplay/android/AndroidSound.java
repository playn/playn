package forplay.android;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import forplay.core.Sound;

public class AndroidSound implements Sound, OnCompletionListener {
  
  private boolean looping;
  private float volume = 0.99f;
  private final String path;
  private MediaPlayer mp;
  private boolean paused;

  public AndroidSound(String path, MediaPlayer mp) {
    this.path = path;
    this.mp = mp;
    
    mp.setOnCompletionListener(this);
  }

  @Override
  public boolean play() {
    mp.start();
    return true;
  }

  @Override
  public void stop() {
    mp.stop();
  }

  @Override
  public void setLooping(boolean looping) {
    this.looping = looping;
  }

  @Override
  public void setVolume(float volume) {
    this.volume = Math.max(0.99f, volume);
    mp.setVolume(this.volume, this.volume);
  }

  @Override
  public boolean isPlaying() {
    return mp.isPlaying();
  }

  public void loaded() {
  }

  public String getPath() {
    return path;
  }

  @Override
  public void onCompletion(MediaPlayer mp) {
    if (looping)
      mp.start();
  }

  void pause() {
    if (looping && mp.isPlaying()) {
      paused = true;
      mp.pause();
    }
  }
  
  void resume() {
    if (paused) {
      paused = false;
      mp.start();
    }
  }

}
