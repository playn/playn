//
// $Id$

package playn.tests.core;

import java.util.ArrayList;
import java.util.List;

import playn.core.Sound;
import playn.core.CanvasImage;
import playn.core.ImageLayer;
import static playn.core.PlayN.*;
import playn.core.TextFormat;

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

    final Sound fanfare = assets().getSound("sounds/fanfare");
    x = addButton("Play Fanfare", new Runnable() {
      public void run() {
        fanfare.play();
        addAction("Played Fanfare.");
      }
    }, x);

    Sound lfanfare = assets().getSound("sounds/fanfare");
    lfanfare.setLooping(true);
    x = addLoopButtons("Fanfare", lfanfare, x);

    Sound bling = assets().getSound("sounds/bling");
    bling.setLooping(true);
    x = addLoopButtons("Bling", bling, x);

    graphics().rootLayer().addAt(graphics().createImageLayer(_actionsImage), 50, 150);
  }

  protected float addLoopButtons (final String name, final Sound sound, float x) {
    x = addButton("Loop " + name, new Runnable() {
      public void run() {
        if (!sound.isPlaying()) {
          sound.play();
          addAction("Starting looping " + name + ".");
        }
      }
    }, x);
    return addButton("Stop Loop " + name, new Runnable() {
      public void run() {
        if (sound.isPlaying()) {
          sound.stop();
          addAction("Stopped looping " + name + ".");
        }
      }
    }, x);
  }

  protected float addButton (String text, Runnable onClick, float x) {
    ImageLayer button = createButton(text, onClick);
    graphics().rootLayer().addAt(button, x, 100);
    return x + button.width() + 10;
  }

  protected void addAction (String action) {
    _actions.add(0, action);
    if (_actions.size() > 10)
      _actions.subList(10, _actions.size()).clear();
    _actionsImage.canvas().clear();
    StringBuilder buf = new StringBuilder();
    for (String a : _actions)
      buf.append(a).append("\n");
    _actionsImage.canvas().setFillColor(0xFF000000).
      fillText(graphics().layoutText(buf.toString(), ACTIONS_FMT), 0, 0);
  }

  protected final List<String> _actions = new ArrayList<String>();
  protected final CanvasImage _actionsImage = graphics().createImage(300, 300);
  protected static final TextFormat ACTIONS_FMT = TEXT_FMT.withWrapWidth(300);
}
