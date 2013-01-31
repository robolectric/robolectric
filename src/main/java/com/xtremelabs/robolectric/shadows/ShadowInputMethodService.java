package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.*;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(InputMethodService.class)
public class ShadowInputMethodService extends ShadowService {
    @RealObject
    InputMethodService realService;
    InputConnection inputConnection;
    EditorInfo editorInfo;

    boolean isVisible = true;

    @Implementation
    public LayoutInflater getLayoutInflater() {
        return (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Implementation
    public boolean isInputViewShown() {
        return isVisible;
    }

    @Implementation
    public void requestHideSelf(int flags) {
        isVisible = false;
    }

    @Implementation
    public EditorInfo getCurrentInputEditorInfo() {
        if (editorInfo == null) {
            editorInfo = new EditorInfo();
        }
        return editorInfo;
    }

    @Implementation
    public InputConnection getCurrentInputConnection() {
        if (inputConnection == null) {
            inputConnection = new InputConnection() {
                @Override
                public CharSequence getTextBeforeCursor(int n, int flags) {
                    return null;
                }

                @Override
                public CharSequence getTextAfterCursor(int n, int flags) {
                    return null;
                }

                @Override
                public CharSequence getSelectedText(int flags) {
                    return null;
                }

                @Override
                public int getCursorCapsMode(int reqModes) {
                    return 0;
                }

                @Override
                public ExtractedText getExtractedText(ExtractedTextRequest request, int flags) {
                    return null;
                }

                @Override
                public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                    return false;
                }

                @Override
                public boolean setComposingText(CharSequence text, int newCursorPosition) {
                    return false;
                }

                @Override
                public boolean setComposingRegion(int start, int end) {
                    return false;
                }

                @Override
                public boolean finishComposingText() {
                    return false;
                }

                @Override
                public boolean commitText(CharSequence text, int newCursorPosition) {
                    return false;
                }

                @Override
                public boolean commitCompletion(CompletionInfo text) {
                    return false;
                }

                @Override
                public boolean commitCorrection(CorrectionInfo correctionInfo) {
                    return false;
                }

                @Override
                public boolean setSelection(int start, int end) {
                    return false;
                }

                @Override
                public boolean performEditorAction(int editorAction) {
                    return false;
                }

                @Override
                public boolean performContextMenuAction(int id) {
                    return false;
                }

                @Override
                public boolean beginBatchEdit() {
                    return false;
                }

                @Override
                public boolean endBatchEdit() {
                    return false;
                }

                @Override
                public boolean sendKeyEvent(KeyEvent event) {
                    return false;
                }

                @Override
                public boolean clearMetaKeyStates(int states) {
                    return false;
                }

                @Override
                public boolean reportFullscreenMode(boolean enabled) {
                    return false;
                }

                @Override
                public boolean performPrivateCommand(String action, Bundle data) {
                    return false;
                }
            };
        }
        return inputConnection;
    }

}
