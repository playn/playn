package forplay.html;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

import forplay.core.ForPlay;

public abstract class HtmlGame implements EntryPoint {

  /*
   * TODO(fredsa): consider adding an onerror page handler, for non-GWT
   * originated exceptions
   */
  @Override
  public final void onModuleLoad() {
    GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      @Override
      public void onUncaughtException(Throwable e) {
        ForPlay.log().error("Uncaught Exception: ", e);
      }
    });

    // Need to do everything else in a deferred command, so that
    // the uncaught exception handler has taken effect
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

      @Override
      public void execute() {
        start();
      }

    });
  }

  public abstract void start();
}
