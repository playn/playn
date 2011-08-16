package org.jbox2d.pooling;

public abstract class PoolingStack<E> {
	public abstract E pop();

	public abstract PoolContainer<E> pop(int argNum);

	public abstract void push(int argNum);

	 public static class PoolContainer<E>{
	    public static final int MAX_MEMBERS = 9;
	    
	    public E p0,p1,p2,p3,p4,p5,p6,p7,p8;
	    
	    public void populate(E[] argRay){
	      p0 = argRay[0];
	      p1 = argRay[1];
	      p2 = argRay[2];
	      p3 = argRay[3];
	      p4 = argRay[4];
	      p5 = argRay[5];
	      p6 = argRay[6];
	      p7 = argRay[7];
	      p8 = argRay[8];
	    }
	 }
}
