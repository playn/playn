/*******************************************************************************
 * Copyright (c) 2011, Daniel Murphy
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL DANIEL MURPHY BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.jbox2d.pooling;

public abstract class MutableStack<E, T extends E> implements IDynamicStack<E> {

	private T[] stack;
	private int index;
	private int size;

	public MutableStack(){
		index = 0;
		size = 0;
	}

	protected void initStack(int argSize) {
	  index = argSize - 1;
	  size = argSize;
	  stack = createArray(argSize, null);
	}


	protected abstract T[] createArray(int argSize, T[] argOld);

	/* (non-Javadoc)
	 * @see org.jbox2d.pooling.IDynamicStack#pop()
	 */
	@Override
  public final E pop(){
		if(index >= size){
			stack = createArray(size * 2, stack);
			size = stack.length;
		}
		return stack[index++];
	}

	/* (non-Javadoc)
	 * @see org.jbox2d.pooling.IDynamicStack#push(E)
	 */
	@Override
  @SuppressWarnings("unchecked")
	public final void push(E argObject){
		assert(index > 0);
		stack[--index] = (T)argObject;
	}
}
