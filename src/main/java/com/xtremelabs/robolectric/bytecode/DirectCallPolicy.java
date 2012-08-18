package com.xtremelabs.robolectric.bytecode;

/**
 * Policy that defined how direct calls are handled.
 */
public interface DirectCallPolicy {

	/** NoDirectCallPolicy instance. */
	DirectCallPolicy NOP = new NoDirectCallPolicy();
	
	/**
	 * Decide whether call must be performed directly. 
	 * @param target target object
	 * @return true if call must be direct
	 */
	boolean shouldCallDirectly(Object target);

	/**
	 * Called after each object method invocation.
	 * @param target object which method has been invoked on
	 * @return policy to replace current instance
	 */
	DirectCallPolicy onMethodInvocationFinished(Object target);
	
	/** Throw by policy instances in case of illegal state exception. */
	public static class DirectCallException extends IllegalStateException {

		private static final long serialVersionUID = 4326926182637261742L;
		
		public DirectCallException(String msg) {
			super(msg);
		}
		
	}
	
	/** Direct call is not performed. */
	public static class NoDirectCallPolicy implements DirectCallPolicy  {
		
		private NoDirectCallPolicy() { /* hidden */ }
		
		@Override
		public boolean shouldCallDirectly(Object target) {
			return false;
		}
		@Override
		public DirectCallPolicy onMethodInvocationFinished(Object target) {
			return this;
		}
	}

	/** Direct call is performed once only. */
	public static class OneShotDirectCallPolicy implements DirectCallPolicy {
		
		/** Direct instance. */
		private Object expectedInstance;
		
		public OneShotDirectCallPolicy(Object directInstance) {
			this.expectedInstance = directInstance;
		}
		
		@Override
		public boolean shouldCallDirectly(Object target) {
			if (expectedInstance == null) { return false; }
			if (expectedInstance != target) {
				throw new DirectCallException("expected to perform direct call on <" + expectedInstance + "> but got <" + target + ">");
			}
			expectedInstance = null;
			return true;
		}
		
		@Override
		public DirectCallPolicy onMethodInvocationFinished(Object target) {
			return NOP;
		}
		
	}
	
	/** Direct call is performed within the invocation full stack. */
	public static class FullStackDirectCallPolicy implements DirectCallPolicy {

		@Override
		public boolean shouldCallDirectly(Object target) {
			return false;
		}
		
		@Override
		public DirectCallPolicy onMethodInvocationFinished(Object target) {
			return NOP;
		}
		
	}
	
}
