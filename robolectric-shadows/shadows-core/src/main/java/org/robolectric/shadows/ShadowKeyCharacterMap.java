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
  private static final Map<Character, Integer> CHAR_TO_KEY_CODE = new HashMap<>();
  private static final Map<Integer, Character> KEY_CODE_TO_CHAR = new HashMap<>();

  static {
    CHAR_TO_KEY_CODE.put('0', KeyEvent.KEYCODE_0);
    CHAR_TO_KEY_CODE.put('1', KeyEvent.KEYCODE_1);
    CHAR_TO_KEY_CODE.put('2', KeyEvent.KEYCODE_2);
    CHAR_TO_KEY_CODE.put('3', KeyEvent.KEYCODE_3);
    CHAR_TO_KEY_CODE.put('4', KeyEvent.KEYCODE_4);
    CHAR_TO_KEY_CODE.put('5', KeyEvent.KEYCODE_5);
    CHAR_TO_KEY_CODE.put('6', KeyEvent.KEYCODE_6);
    CHAR_TO_KEY_CODE.put('7', KeyEvent.KEYCODE_7);
    CHAR_TO_KEY_CODE.put('8', KeyEvent.KEYCODE_8);
    CHAR_TO_KEY_CODE.put('9', KeyEvent.KEYCODE_9);
    CHAR_TO_KEY_CODE.put('A', KeyEvent.KEYCODE_A);
    CHAR_TO_KEY_CODE.put('B', KeyEvent.KEYCODE_B);
    CHAR_TO_KEY_CODE.put('C', KeyEvent.KEYCODE_C);
    CHAR_TO_KEY_CODE.put('D', KeyEvent.KEYCODE_D);
    CHAR_TO_KEY_CODE.put('E', KeyEvent.KEYCODE_E);
    CHAR_TO_KEY_CODE.put('F', KeyEvent.KEYCODE_F);
    CHAR_TO_KEY_CODE.put('G', KeyEvent.KEYCODE_G);
    CHAR_TO_KEY_CODE.put('H', KeyEvent.KEYCODE_H);
    CHAR_TO_KEY_CODE.put('I', KeyEvent.KEYCODE_I);
    CHAR_TO_KEY_CODE.put('J', KeyEvent.KEYCODE_J);
    CHAR_TO_KEY_CODE.put('K', KeyEvent.KEYCODE_K);
    CHAR_TO_KEY_CODE.put('L', KeyEvent.KEYCODE_L);
    CHAR_TO_KEY_CODE.put('M', KeyEvent.KEYCODE_M);
    CHAR_TO_KEY_CODE.put('N', KeyEvent.KEYCODE_N);
    CHAR_TO_KEY_CODE.put('O', KeyEvent.KEYCODE_O);
    CHAR_TO_KEY_CODE.put('P', KeyEvent.KEYCODE_P);
    CHAR_TO_KEY_CODE.put('Q', KeyEvent.KEYCODE_Q);
    CHAR_TO_KEY_CODE.put('R', KeyEvent.KEYCODE_R);
    CHAR_TO_KEY_CODE.put('S', KeyEvent.KEYCODE_S);
    CHAR_TO_KEY_CODE.put('T', KeyEvent.KEYCODE_T);
    CHAR_TO_KEY_CODE.put('U', KeyEvent.KEYCODE_U);
    CHAR_TO_KEY_CODE.put('V', KeyEvent.KEYCODE_V);
    CHAR_TO_KEY_CODE.put('W', KeyEvent.KEYCODE_W);
    CHAR_TO_KEY_CODE.put('X', KeyEvent.KEYCODE_X);
    CHAR_TO_KEY_CODE.put('Y', KeyEvent.KEYCODE_Y);
    CHAR_TO_KEY_CODE.put('Z', KeyEvent.KEYCODE_Z);

    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_0, '0');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_1, '1');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_2, '2');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_3, '3');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_4, '4');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_5, '5');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_6, '6');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_7, '7');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_8, '8');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_9, '9');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_A, 'A');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_B, 'B');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_C, 'C');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_D, 'D');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_E, 'E');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_F, 'F');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_G, 'G');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_H, 'H');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_I, 'I');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_J, 'J');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_K, 'K');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_L, 'L');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_M, 'M');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_N, 'N');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_O, 'O');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_P, 'P');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_Q, 'Q');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_R, 'R');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_S, 'S');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_T, 'T');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_U, 'U');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_V, 'V');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_W, 'W');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_X, 'X');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_Y, 'Y');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_Z, 'Z');
  }

  @Implementation
  public static KeyCharacterMap load(int deviceId) {
    return ReflectionHelpers.callConstructor(KeyCharacterMap.class);
  }

  @Implementation
  public KeyEvent[] getEvents(char[] charArray) {
    int eventsPerChar = 2;
    KeyEvent[] events = new KeyEvent[charArray.length * eventsPerChar];

    for (int i = 0; i < charArray.length; i++) {
      events[eventsPerChar * i] = getDownEvent(charArray[i]);
      events[eventsPerChar * i + 1] = getUpEvent(charArray[i]);
    }

    return events;
  }

  @Implementation
  public int getKeyboardType() {
    return KeyCharacterMap.FULL;
  }

  @Implementation
  public int get(int keyCode, int metaState) {
    return Character.toLowerCase(KEY_CODE_TO_CHAR.get(keyCode));
  }

  public KeyEvent getDownEvent(char a) {
    return new KeyEvent(KeyEvent.ACTION_DOWN, CHAR_TO_KEY_CODE.get(Character.toUpperCase(a)));
  }

  public KeyEvent getUpEvent(char a) {
    return new KeyEvent(KeyEvent.ACTION_UP, CHAR_TO_KEY_CODE.get(Character.toUpperCase(a)));
  }
}
