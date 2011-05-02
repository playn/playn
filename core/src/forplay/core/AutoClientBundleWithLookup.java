package forplay.core;

import com.google.gwt.resources.client.ResourcePrototype;


public interface AutoClientBundleWithLookup {

  /**
   * Find a resource by the name of the function in which it is declared.
   *
   * @param name the name of the desired resource
   * @return the resource, or <code>null</code> if no such resource is defined.
   */
  ResourcePrototype getResource(String name);

  /**
   * A convenience method to iterate over all ResourcePrototypes contained in the ClientBundle.
   */
  ResourcePrototype[] getResources();

}
