package org.robolectric;

// PLEASE DO NOT MOVE this file into its "proper" directory.
// If it is in its proper directory the compiler suite
// will automatically include it in the compilation when
// other source files include it. However, for testing purpose
// sometimes we don't want it included.
/**
 * Placeholder class to allow generated source to compile
 * without having to create a test dependency on Robolectric itself.
 * Contains Anything interface.
 * 
 * @author Fr Jeremy Krieg
 */
public class Robolectric {
  public interface Anything {}

  public static class DocumentedObject {
  }
}