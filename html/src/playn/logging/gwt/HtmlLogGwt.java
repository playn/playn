package playn.logging.gwt;

import playn.html.HtmlLog;

import java.util.logging.Logger;

/**
 * Logs output to GWT's emulation of the java.util.logging.Logger.
 */
class HtmlLogGwt extends HtmlLog {
  private final Logger logger = Logger.getLogger("PlayN");

  // Instantiated via GWT.create()
  private HtmlLogGwt() {
  }

  @Override
  protected void logImpl(Level level, String msg, Throwable e) {
    switch (level) {
      case DEBUG: logger.log(java.util.logging.Level.FINE, msg, e); break;
      case WARN:  logger.log(java.util.logging.Level.WARNING, msg, e); break;
      case ERROR: logger.log(java.util.logging.Level.SEVERE, msg, e); break;
      default:    logger.log(java.util.logging.Level.INFO, msg, e); break;
    }
  }
}
