//
// $Id$

package playn.tests.core;

import java.util.ArrayList;
import java.util.List;

import playn.core.CanvasImage;
import playn.core.Sound;
import playn.core.TextLayout;
import playn.core.util.Callback;
import playn.core.TextWrap;
import static playn.core.PlayN.*;

/**
 * Tests sound playback support.
 */
public class SoundTest extends Test {

  @Override
  public String getName() {
    return "SoundTest";
  }

  @Override
  public String getDescription() {
    return "Tests playing and looping sounds.";
  }

  @Override
  public void init() {
    float x = 50;

    final Sound fanfare = loadSound("sounds/fanfare");
    x = addButton("Play Fanfare", new Runnable() {
      public void run() {
        fanfare.play();
        addAction("Played Fanfare.");
      }
    }, x, 100);

    Sound lfanfare = loadSound("sounds/fanfare");
    lfanfare.setLooping(true);
    x = addLoopButtons("Fanfare", lfanfare, x);

    Sound bling = loadSound("sounds/bling");
    bling.setLooping(true);
    x = addLoopButtons("Bling", bling, x);

    graphics().rootLayer().addAt(graphics().createImageLayer(_actionsImage), 50, 150);
  }

  protected Sound loadSound(final String path) {
    Sound sound = assets().getSound(path);
    sound.addCallback(new Callback<Sound>() {
      public void onSuccess(Sound sound) {} // noop
      public void onFailure(Throwable cause) {
        log().warn("Sound loading error: " + path, cause);
      }
    });
    return sound;
  }

  protected float addLoopButtons(final String name, final Sound sound, float x) {
    x = addButton("Loop " + name, new Runnable() {
      public void run() {
        if (!sound.isPlaying()) {
          sound.play();
          addAction("Starting looping " + name + ".");
        }
      }
    }, x, 100);
    x = addButton("Stop Loop " + name, new Runnable() {
      public void run() {
        if (sound.isPlaying()) {
          sound.stop();
          addAction("Stopped looping " + name + ".");
        }
      }
    }, x, 100);
    return x;
  }

  protected void addAction(String action) {
    _actions.add(0, action);
    if (_actions.size() > 10)
      _actions.subList(10, _actions.size()).clear();
    _actionsImage.canvas().clear();
    StringBuilder buf = new StringBuilder();
    for (String a : _actions) {
      if (buf.length() > 0) buf.append("\n");
      buf.append(a);
    }
    _actionsImage.canvas().setFillColor(0xFF000000);

    float y = 0;
    for (TextLayout layout : graphics().layoutText(buf.toString(), TEXT_FMT, new TextWrap(300))) {
      _actionsImage.canvas().fillText(layout, 0, y);
      y += layout.ascent() + layout.descent() + layout.leading();
    }
  }

  protected final List<String> _actions = new ArrayList<String>();
  protected final CanvasImage _actionsImage = graphics().createImage(300, 300);
}
