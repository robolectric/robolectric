package org.robolectric.shadows;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

import java.util.HashMap;
import java.util.Map;

/**
 * Shadow for {@link android.view.KeyCharacterMap}.
 */
@Implements(KeyCharacterMap.class)
public class ShadowKeyCharacterMap {

  @Implementation
  public static KeyCharacterMap load(int deviceId) {
    return ReflectionHelpers.callConstructor( KeyCharacterMap.class );
  }

  // hacked, a bit.  does not support caps
  @Implementation
  public KeyEvent[] getEvents(char[] charArray){
    int eventsPerChar = 2;
    KeyEvent[] events = new KeyEvent[charArray.length * eventsPerChar];

    for(int  i = 0; i < charArray.length; i++){
      events[eventsPerChar * i] = getDownEvent( charArray[i] );
      events[eventsPerChar * i + 1] = getUpEvent( charArray[i] );
    }

    return events;
  }

  @Implementation
  public int getKeyboardType(){
    // Not sure where VIRTUAL enters into this, but we've got to return this here for editability
    return KeyCharacterMap.FULL;
  }

  public KeyEvent getDownEvent(char a){
    return new KeyEvent( KeyEvent.ACTION_DOWN, charToKeyCodeMap.get( Character.toUpperCase( a ) ) );
  }

  public KeyEvent getUpEvent(char a){
    return new KeyEvent( KeyEvent.ACTION_UP, charToKeyCodeMap.get( Character.toUpperCase( a ) ) );
  }

  private static Map<Character, Integer> charToKeyCodeMap = initKeyCodeMap();

  private static Map<Character, Integer> initKeyCodeMap() {
    Map<Character, Integer> map = new HashMap<>(  );

    map.put( '0', KeyEvent.KEYCODE_0 );
    map.put( '1', KeyEvent.KEYCODE_1 );
    map.put( '2', KeyEvent.KEYCODE_2 );
    map.put( '3', KeyEvent.KEYCODE_3 );
    map.put( '4', KeyEvent.KEYCODE_4 );
    map.put( '5', KeyEvent.KEYCODE_5 );
    map.put( '6', KeyEvent.KEYCODE_6 );
    map.put( '7', KeyEvent.KEYCODE_7 );
    map.put( '8', KeyEvent.KEYCODE_8 );
    map.put( '9', KeyEvent.KEYCODE_9 );
    map.put( 'A', KeyEvent.KEYCODE_A );
    map.put( 'B', KeyEvent.KEYCODE_B );
    map.put( 'C', KeyEvent.KEYCODE_C );
    map.put( 'D', KeyEvent.KEYCODE_D );
    map.put( 'E', KeyEvent.KEYCODE_E );
    map.put( 'F', KeyEvent.KEYCODE_F );
    map.put( 'G', KeyEvent.KEYCODE_G );
    map.put( 'H', KeyEvent.KEYCODE_H );
    map.put( 'I', KeyEvent.KEYCODE_I );
    map.put( 'J', KeyEvent.KEYCODE_J );
    map.put( 'K', KeyEvent.KEYCODE_K );
    map.put( 'L', KeyEvent.KEYCODE_L );
    map.put( 'M', KeyEvent.KEYCODE_M );
    map.put( 'N', KeyEvent.KEYCODE_N );
    map.put( 'O', KeyEvent.KEYCODE_O );
    map.put( 'P', KeyEvent.KEYCODE_P );
    map.put( 'Q', KeyEvent.KEYCODE_Q );
    map.put( 'R', KeyEvent.KEYCODE_R );
    map.put( 'S', KeyEvent.KEYCODE_S );
    map.put( 'T', KeyEvent.KEYCODE_T );
    map.put( 'U', KeyEvent.KEYCODE_U );
    map.put( 'V', KeyEvent.KEYCODE_V );
    map.put( 'W', KeyEvent.KEYCODE_W );
    map.put( 'X', KeyEvent.KEYCODE_X );
    map.put( 'Y', KeyEvent.KEYCODE_Y );
    map.put( 'Z', KeyEvent.KEYCODE_Z );

    return map;
  }

  private static Map<Integer, Character> keyCodeToCharMap = initKeyCodeToCharMap();

  private static Map<Integer, Character> initKeyCodeToCharMap() {
    Map<Integer, Character> map = new HashMap<>(  );

    map.put( KeyEvent.KEYCODE_0, '0' );
    map.put( KeyEvent.KEYCODE_1, '1' );
    map.put( KeyEvent.KEYCODE_2, '2' );
    map.put( KeyEvent.KEYCODE_3, '3' );
    map.put( KeyEvent.KEYCODE_4, '4' );
    map.put( KeyEvent.KEYCODE_5, '5' );
    map.put( KeyEvent.KEYCODE_6, '6' );
    map.put( KeyEvent.KEYCODE_7, '7' );
    map.put( KeyEvent.KEYCODE_8, '8' );
    map.put( KeyEvent.KEYCODE_9, '9' );
    map.put( KeyEvent.KEYCODE_A, 'A' );
    map.put( KeyEvent.KEYCODE_B, 'B' );
    map.put( KeyEvent.KEYCODE_C, 'C' );
    map.put( KeyEvent.KEYCODE_D, 'D' );
    map.put( KeyEvent.KEYCODE_E, 'E' );
    map.put( KeyEvent.KEYCODE_F, 'F' );
    map.put( KeyEvent.KEYCODE_G, 'G' );
    map.put( KeyEvent.KEYCODE_H, 'H' );
    map.put( KeyEvent.KEYCODE_I, 'I' );
    map.put( KeyEvent.KEYCODE_J, 'J' );
    map.put( KeyEvent.KEYCODE_K, 'K' );
    map.put( KeyEvent.KEYCODE_L, 'L' );
    map.put( KeyEvent.KEYCODE_M, 'M' );
    map.put( KeyEvent.KEYCODE_N, 'N' );
    map.put( KeyEvent.KEYCODE_O, 'O' );
    map.put( KeyEvent.KEYCODE_P, 'P' );
    map.put( KeyEvent.KEYCODE_Q, 'Q' );
    map.put( KeyEvent.KEYCODE_R, 'R' );
    map.put( KeyEvent.KEYCODE_S, 'S' );
    map.put( KeyEvent.KEYCODE_T, 'T' );
    map.put( KeyEvent.KEYCODE_U, 'U' );
    map.put( KeyEvent.KEYCODE_V, 'V' );
    map.put( KeyEvent.KEYCODE_W, 'W' );
    map.put( KeyEvent.KEYCODE_X, 'X' );
    map.put( KeyEvent.KEYCODE_Y, 'Y' );
    map.put( KeyEvent.KEYCODE_Z, 'Z' );

    return map;
  }

  @Implementation
  public int get(int keyCode, int metaState) {
    // TODO shift
    return Character.toLowerCase( keyCodeToCharMap.get( keyCode ) );
  }

}
