package org.jbox2d.pooling;

import org.jbox2d.common.Settings;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.contacts.PolygonContact;

public class PolygonContactStack extends MutableStack<Contact, PolygonContact> {
  
  private final WorldPool pool;

  public PolygonContactStack(WorldPool argPool){
    super();
    pool = argPool;
    initStack(Settings.CONTACT_STACK_INIT_SIZE);
  }

  @Override
  protected PolygonContact[] createArray(int argSize, PolygonContact[] argOld) {
    if (argOld != null) {
      Contact[] sk = new PolygonContact[argSize];
      for (int i = 0; i < argOld.length; i++) {
        sk[i] = argOld[i];
      }
      for (int i = argOld.length; i < argSize; i++) {
        sk[i] = new PolygonContact(pool);
      }
      return (PolygonContact[]) sk;
    } else {
      PolygonContact[] sk = new PolygonContact[argSize];
      for (int i = 0; i < argSize; i++) {
        sk[i] = new PolygonContact(pool);
      }
      return sk;
    }
  }
}
