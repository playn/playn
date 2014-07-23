using System;
using MonoTouch.Foundation;
using MonoTouch.UIKit;

using playn.ios;
using playn.core;
using playn.tests.core;

namespace playn.tests.ios
{
  [Register ("AppDelegate")]
  public partial class AppDelegate : UIApplicationDelegate {
    public override bool FinishedLaunching (UIApplication app, NSDictionary options) {
      IOSPlatform.Config config = new IOSPlatform.Config();
      config.orients = IOSPlatform.SupportedOrients.LANDSCAPES;
      IOSPlatform.register(app, config);
      PlayN.run(new TestsGame());
      return true;
    }
  }

  public class Application {
    static void Main (string[] args) {
      UIApplication.Main (args, null, "AppDelegate");
    }
  }
}
