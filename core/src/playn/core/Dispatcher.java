/**
 * Copyright 2012 The PlayN Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
      while (layer.parent() != null && !localized.flags().getPropagationStopped()) {
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
