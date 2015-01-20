//
// $Id$

package playn.tests.core;

import java.util.ArrayList;
import java.util.List;

import playn.core.*;
import playn.scene.*;

/**
 * Tests sound playback support.
 */
public class SoundTest extends Test {

  public SoundTest (TestsGame game) {
    super(game, "Sound", "Tests playing and looping sounds.");
  }

  @Override public void init() {
    float x = 50;
    final CanvasLayer actions = new CanvasLayer(game.graphics, 300, 300);

    final Sound fanfare = loadSound("sounds/fanfare");
    x = addButton("Play Fanfare", new Runnable() {
      public void run() {
        fanfare.play();
        addAction(actions, "Played Fanfare.");
      }
    }, x, 100);

    Sound lfanfare = loadSound("sounds/fanfare");
    lfanfare.setLooping(true);
    x = addLoopButtons(actions, "Fanfare", lfanfare, x);

    Sound bling = loadSound("sounds/bling");
    bling.setLooping(true);
    x = addLoopButtons(actions, "Bling", bling, x);

    game.rootLayer.addAt(actions, 50, 150);
  }

  protected Sound loadSound(final String path) {
    Sound sound = game.assets.getSound(path);
    sound.state.onFailure(logFailure("Sound loading error: " + path));
    return sound;
  }

  protected float addLoopButtons(final CanvasLayer actions, final String name, final Sound sound,
                                 float x) {
    x = addButton("Loop " + name, new Runnable() {
      public void run() {
        if (!sound.isPlaying()) {
          sound.play();
          addAction(actions, "Starting looping " + name + ".");
        }
      }
    }, x, 100);
    x = addButton("Stop Loop " + name, new Runnable() {
      public void run() {
        if (sound.isPlaying()) {
          sound.stop();
          addAction(actions, "Stopped looping " + name + ".");
        }
      }
    }, x, 100);
    return x;
  }

  protected void addAction(CanvasLayer actions, String action) {
    _actions.add(0, action);
    if (_actions.size() > 10)
      _actions.subList(10, _actions.size()).clear();

    Canvas canvas = actions.begin();
    canvas.clear();
    StringBuilder buf = new StringBuilder();
    for (String a : _actions) {
      if (buf.length() > 0) buf.append("\n");
      buf.append(a);
    }
    canvas.setFillColor(0xFF000000);

    float y = 0;
    for (TextLayout layout : game.graphics.layoutText(
      buf.toString(), game.ui.TEXT_FMT, new TextWrap(300))) {
      canvas.fillText(layout, 0, y);
      y += layout.ascent() + layout.descent() + layout.leading();
    }
    actions.end();
  }

  protected final List<String> _actions = new ArrayList<String>();
}
