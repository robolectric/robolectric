/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os;

/**
 * Vibrator implementation that does nothing.
 *
 * This class exists because in Android 4.1, android.os.Vibrator became an
 * abstract class. We need to instantiate a concrete subclass of it for
 * testing, so this was copied from the 4.1 sdk source. The @Override
 * annotations are removed so that this will compile when linked against older
 * versions of android which didn't have those messages.
 *
 */
public class NullVibrator extends Vibrator {
    private static final NullVibrator sInstance = new NullVibrator();

    private NullVibrator() {
    }

    public static NullVibrator getInstance() {
        return sInstance;
    }

    public boolean hasVibrator() {
        return false;
    }

    public void vibrate(long milliseconds) {
    }

    public void vibrate(long[] pattern, int repeat) {
        if (repeat >= pattern.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void cancel() {
    }
}
