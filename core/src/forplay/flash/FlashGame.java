package forplay.flash;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;

public abstract class FlashGame implements EntryPoint {

  @Override
  public final void onModuleLoad() {
    GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      
      @Override
      public void onUncaughtException(Throwable e) {
        // TODO Auto-generated method stub
        String msg = e.toString();
        for (StackTraceElement elt : e.getStackTrace()) {
          msg += "\n in " + elt.getMethodName() + "("+elt.getFileName()+":"+elt.getLineNumber()+")";
        }
        alert(msg);
      }
    });
    
    // Need to do everything else in a deferred command, so that
    // The uncaut exception handler has taken effect
//    Scheduler.get().scheduleDeferred(new ScheduledCommand() {

//      @Override
//      public void execute() {
        start();
//      }

//    });
  }

  private native void alert(String msg) /*-{
    flash.external.ExternalInterface.call("alert", msg);
  }-*/;
  
  
  public abstract void start();
}
