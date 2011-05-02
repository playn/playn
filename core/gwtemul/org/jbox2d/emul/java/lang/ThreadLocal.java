package java.lang;

public class ThreadLocal<T> {

	private T object;
	
	protected T initialValue() {
		return null;
	}
	
	public T get() {
	  if(object == null) {
	    object = initialValue();
	  }
		return object;
	}
	
	public void set(T value) {
		object = value;
	}
}
