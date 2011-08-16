package org.jbox2d.pooling;

import org.jbox2d.common.Settings;
import org.jbox2d.dynamics.contacts.CircleContact;
import org.jbox2d.dynamics.contacts.Contact;

public class CircleContactStack extends MutableStack<Contact, CircleContact> {
  
  private final WorldPool pool;

  public CircleContactStack(WorldPool argPool){
    super();
    pool = argPool;
    initStack(Settings.CONTACT_STACK_INIT_SIZE);
  }

  @Override
  protected CircleContact[] createArray(int argSize, CircleContact[] argOld) {
    if (argOld != null) {
      Contact[] sk = new CircleContact[argSize];
      for (int i = 0; i < argOld.length; i++) {
        sk[i] = argOld[i];
      }
      for (int i = argOld.length; i < argSize; i++) {
        sk[i] = new CircleContact(pool);
      }
      return (CircleContact[]) sk;
    } else {
      CircleContact[] sk = new CircleContact[argSize];
      for (int i = 0; i < argSize; i++) {
        sk[i] = new CircleContact(pool);
      }
      return sk;
    }
  }
}
