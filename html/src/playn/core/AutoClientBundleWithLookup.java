package playn.core;

import com.google.gwt.resources.client.ResourcePrototype;

public interface AutoClientBundleWithLookup {

  /**
   * Find a resource by the name of the function in which it is declared. For example, place this
   * file in your 'resources/images' directory:
   *
   * <pre>{@code
   * public interface ImagesAutoBundle extends AutoClientBundleWithLookup {
   *  static ImagesAutoBundle INSTANCE = GWT.create(ImagesAutoBundle.class);
   * }
   * }</pre>
   *
   * @param name the name of the desired resource
   * @return the resource, or {@code null} if no such resource is defined.
   */
  ResourcePrototype getResource(String name);

  /**
   * A convenience method to iterate over all ResourcePrototypes contained in the ClientBundle.
   */
  ResourcePrototype[] getResources();

}
