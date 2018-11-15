package org.robolectric.shadows;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(KeyCharacterMap.class)
public class ShadowKeyCharacterMap {
  private static final Map<Character, Integer> CHAR_TO_KEY_CODE = new HashMap<>();
  private static final Map<Character, Integer> CHAR_TO_KEY_CODE_SHIFT_ON = new HashMap<>();

  private static final Map<Integer, Character> KEY_CODE_TO_CHAR = new HashMap<>();
  private static final Map<Integer, Character> KEY_CODE_TO_CHAR_SHIFT_ON = new HashMap<>();

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
    CHAR_TO_KEY_CODE.put(' ', KeyEvent.KEYCODE_SPACE);
    CHAR_TO_KEY_CODE.put('-', KeyEvent.KEYCODE_MINUS);
    CHAR_TO_KEY_CODE.put('+', KeyEvent.KEYCODE_PLUS);
    CHAR_TO_KEY_CODE.put('@', KeyEvent.KEYCODE_AT);
    CHAR_TO_KEY_CODE.put('.', KeyEvent.KEYCODE_PERIOD);
    CHAR_TO_KEY_CODE.put(',', KeyEvent.KEYCODE_COMMA);
    CHAR_TO_KEY_CODE.put('[', KeyEvent.KEYCODE_LEFT_BRACKET);
    CHAR_TO_KEY_CODE.put(']', KeyEvent.KEYCODE_RIGHT_BRACKET);
    CHAR_TO_KEY_CODE.put('\'', KeyEvent.KEYCODE_APOSTROPHE);
    CHAR_TO_KEY_CODE.put(')', KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN);
    CHAR_TO_KEY_CODE.put('(', KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN);
    CHAR_TO_KEY_CODE.put('#', KeyEvent.KEYCODE_POUND);
    CHAR_TO_KEY_CODE.put('*', KeyEvent.KEYCODE_STAR);
    CHAR_TO_KEY_CODE.put('/', KeyEvent.KEYCODE_SLASH);
    CHAR_TO_KEY_CODE.put('=', KeyEvent.KEYCODE_EQUALS);
    CHAR_TO_KEY_CODE.put('`', KeyEvent.KEYCODE_GRAVE);
    CHAR_TO_KEY_CODE.put('\\', KeyEvent.KEYCODE_BACKSLASH);

    CHAR_TO_KEY_CODE_SHIFT_ON.put('_', KeyEvent.KEYCODE_MINUS);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('{', KeyEvent.KEYCODE_LEFT_BRACKET);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('}', KeyEvent.KEYCODE_RIGHT_BRACKET);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('\"', KeyEvent.KEYCODE_APOSTROPHE);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('!', KeyEvent.KEYCODE_1);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('$', KeyEvent.KEYCODE_4);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('%', KeyEvent.KEYCODE_5);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('^', KeyEvent.KEYCODE_6);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('&', KeyEvent.KEYCODE_7);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('?', KeyEvent.KEYCODE_SLASH);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('|', KeyEvent.KEYCODE_BACKSLASH);
    CHAR_TO_KEY_CODE_SHIFT_ON.put('~', KeyEvent.KEYCODE_GRAVE);

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
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_SPACE, ' ');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_MINUS, '-');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_PLUS, '+');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_AT, '@');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_PERIOD, '.');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_COMMA, ',');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_LEFT_BRACKET, '[');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_RIGHT_BRACKET, ']');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_APOSTROPHE, '\'');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_NUMPAD_RIGHT_PAREN, ')');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_NUMPAD_LEFT_PAREN, '(');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_POUND, '#');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_STAR, '*');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_SLASH, '/');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_EQUALS, '=');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_GRAVE, '`');
    KEY_CODE_TO_CHAR.put(KeyEvent.KEYCODE_BACKSLASH, '\\');

    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_MINUS, '_');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_LEFT_BRACKET, '{');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_RIGHT_BRACKET, '}');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_APOSTROPHE, '\"');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_1, '!');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_4, '$');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_5, '%');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_6, '^');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_7, '&');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_SLASH, '?');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_BACKSLASH, '|');
    KEY_CODE_TO_CHAR_SHIFT_ON.put(KeyEvent.KEYCODE_GRAVE, '~');
  }

  @Implementation
  protected static KeyCharacterMap load(int deviceId) {
    return ReflectionHelpers.callConstructor(KeyCharacterMap.class);
  }

  @Implementation
  protected KeyEvent[] getEvents(char[] chars) {
    if (chars == null) {
      throw new IllegalArgumentException("chars must not be null.");
    }
    int eventsPerChar = 2;
    KeyEvent[] events = new KeyEvent[chars.length * eventsPerChar];

    for (int i = 0; i < chars.length; i++) {
      events[eventsPerChar * i] = getDownEvent(chars[i]);
      events[eventsPerChar * i + 1] = getUpEvent(chars[i]);
    }

    return events;
  }

  @Implementation
  protected int getKeyboardType() {
    return KeyCharacterMap.FULL;
  }

  @Implementation
  protected int get(int keyCode, int metaState) {
    boolean metaShiftOn = (metaState & KeyEvent.META_SHIFT_ON) != 0;

    if (keyCode == KeyEvent.KEYCODE_UNKNOWN) {
      return 0;
    } else if (metaShiftOn) {
      return KEY_CODE_TO_CHAR_SHIFT_ON.containsKey(keyCode)
          ? KEY_CODE_TO_CHAR_SHIFT_ON.get(keyCode)
          : KEY_CODE_TO_CHAR.get(keyCode);
    } else {
      return Character.toLowerCase(KEY_CODE_TO_CHAR.get(keyCode));
    }
  }

  public KeyEvent getDownEvent(char a) {
    return new KeyEvent(
        0,
        0,
        KeyEvent.ACTION_DOWN,
        toCharKeyCode(a),
        0,
        getMetaState(a),
        KeyCharacterMap.VIRTUAL_KEYBOARD,
        0);
  }

  public KeyEvent getUpEvent(char a) {
    return new KeyEvent(
        0,
        0,
        KeyEvent.ACTION_UP,
        toCharKeyCode(a),
        0,
        getMetaState(a),
        KeyCharacterMap.VIRTUAL_KEYBOARD,
        0);
  }

  private int toCharKeyCode(char a) {
    if (CHAR_TO_KEY_CODE.containsKey(Character.toUpperCase(a))) {
      return CHAR_TO_KEY_CODE.get(Character.toUpperCase(a));
    } else if (CHAR_TO_KEY_CODE_SHIFT_ON.containsKey(a)) {
      return CHAR_TO_KEY_CODE_SHIFT_ON.get(a);
    } else {
      return 0;
    }
  }

  private int getMetaState(char a) {
    if (Character.isUpperCase(a) || CHAR_TO_KEY_CODE_SHIFT_ON.containsKey(a)) {
      return KeyEvent.META_SHIFT_ON;
    } else {
      return 0;
    }
  }
}
