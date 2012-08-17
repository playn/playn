package playn.core;

/** Some internal utilities for dispatching events. */
interface Dispatcher {

  /** Dispatches events to a single layer. */
  static final Dispatcher SINGLE = new Dispatcher() {
    @Override
    public <L, E extends Events.Position.Impl> void dispatch(
        AbstractLayer layer, Class<L> listenerType, E event,
        AbstractLayer.Interaction<L, E> interaction) {
      @SuppressWarnings("unchecked") E localized = (E)event.localize(layer);
      layer.interact(listenerType, interaction, localized);
    }
  };

  /** Dispatches events to a layer and all its parents. */
  static final Dispatcher PROPAGATING = new Dispatcher() {
    @Override
    public <L, E extends Events.Position.Impl> void dispatch(
        AbstractLayer layer, Class<L> listenerType, E event,
        AbstractLayer.Interaction<L, E> interaction) {
      @SuppressWarnings("unchecked") E localized = (E)event.localize(layer);
      layer.interact(listenerType, interaction, localized);
      while (layer.parent() != null) {
        layer = (AbstractLayer)layer.parent();
        layer.interact(listenerType, interaction, localized);
      }
    }
  };

  static class Util {
    public static Dispatcher select(boolean propagating) {
      return propagating ? PROPAGATING : SINGLE;
    }
  }

  /** Issues an interact call to a layer and listener with a localized copy of the
   * given event.*/
  <L, E extends Events.Position.Impl> void dispatch(
    AbstractLayer layer, Class<L> listenerType,
    E event, AbstractLayer.Interaction<L, E> interaction);
}
