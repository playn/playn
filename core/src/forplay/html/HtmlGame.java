package forplay.html;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

public abstract class HtmlGame implements EntryPoint {

  @Override
  public final void onModuleLoad() {
    Log.setUncaughtExceptionHandler();

    // Need to do everything else in a deferred command, so that
    // The uncaught exception handler has taken effect
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

      @Override
      public void execute() {
        start();
      }

    });
  }

  public abstract void start();
}
