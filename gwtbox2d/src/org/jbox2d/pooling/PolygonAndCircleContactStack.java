package org.jbox2d.pooling;

import org.jbox2d.common.Settings;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.PolygonAndCircleContact;

public class PolygonAndCircleContactStack extends MutableStack<Contact, PolygonAndCircleContact> {
  
  private final WorldPool pool;

  public PolygonAndCircleContactStack(WorldPool argPool){
    super();
    pool = argPool;
    initStack(Settings.CONTACT_STACK_INIT_SIZE);
  }

  @Override
  protected PolygonAndCircleContact[] createArray(int argSize, PolygonAndCircleContact[] argOld) {
    if (argOld != null) {
      Contact[] sk = new PolygonAndCircleContact[argSize];
      for (int i = 0; i < argOld.length; i++) {
        sk[i] = argOld[i];
      }
      for (int i = argOld.length; i < argSize; i++) {
        sk[i] = new PolygonAndCircleContact(pool);
      }
      return (PolygonAndCircleContact[]) sk;
    } else {
      PolygonAndCircleContact[] sk = new PolygonAndCircleContact[argSize];
      for (int i = 0; i < argSize; i++) {
        sk[i] = new PolygonAndCircleContact(pool);
      }
      return sk;
    }
  }
}
