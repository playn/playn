package playn.robovm;

import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIApplicationDelegateAdapter;
import org.robovm.apple.uikit.UIApplicationLaunchOptions;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIWindow;

import playn.core.Game;
import playn.robovm.RoboPlatform.Config;

public abstract class RoboApplicationAdaptor extends UIApplicationDelegateAdapter {
  
  private UIWindow window;
  
  @Override
  public boolean didFinishLaunching(UIApplication app, UIApplicationLaunchOptions launchOpts) {
      window = new UIWindow(UIScreen.getMainScreen().getBounds());
      Config config = new Config();
      Game game = createWith(config);
      RoboRootViewController gameController = new RoboRootViewController(config);
      gameController.run(game);
      // Setup our game controller as the root controller
      window.setRootViewController(gameController);
      window.makeKeyAndVisible();
      addStrongRef(window);
      return true;
  }
  
  protected abstract Game createWith(Config config);
}
