package com.xtremelabs.robolectric.shadows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import android.content.UriMatcher;
import android.net.Uri;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(UriMatcher.class)
public class ShadowUriMatcher {

  public static class MatchNode {
    public int code = UriMatcher.NO_MATCH;
    private int which;
    private String text;
    public List<MatchNode> children = new ArrayList<MatchNode>();

    public MatchNode(int code) {
      this.code = code;
    }
  }

  public MatchNode rootNode;

  public void __constructor__(int code) {
    rootNode = new MatchNode(code);
  }
  
  @Implementation
  public void addURI(String authority, String path, int code) {
    if (code < 0) {
      throw new IllegalArgumentException("code " + code + " is invalid: it must be positive");
    }
    String[] tokens = path != null ? PATH_SPLIT_PATTERN.split(path) : null;
    int numTokens = tokens != null ? tokens.length : 0;
    MatchNode node = rootNode;
    for (int i = -1; i < numTokens; i++) {
      String token = i < 0 ? authority : tokens[i];
      Collection<ShadowUriMatcher.MatchNode> children = node.children;
      boolean b = false;
      for (MatchNode child : children) {
        if (token.equals(child.text)) {
          node = child;
          b = true;
          break;
        }
      }
      if (!b) {
        // Child not found, create it
        MatchNode child = new MatchNode(UriMatcher.NO_MATCH);
        if (token.equals("#")) {
          child.which = NUMBER;
        } else if (token.equals("*")) {
          child.which = TEXT;
        } else {
          child.which = EXACT;
        }
        child.text = token;
        node.children.add(child);
        node = child;
      }
    }
    node.code = code;
  }

  static final Pattern PATH_SPLIT_PATTERN = Pattern.compile("/");

  @Implementation
  public int match(Uri uri) {
    final List<String> pathSegments = uri.getPathSegments();
    final int li = pathSegments.size();

    MatchNode node = rootNode;

    if (li == 0 && uri.getAuthority() == null) {
      return rootNode.code;
    }

    for (int i = -1; i < li; i++) {
      String u = i < 0 ? uri.getAuthority() : pathSegments.get(i);
      List<MatchNode> list = node.children;
      if (list == null) {
        break;
      }
      node = null;
      int lj = list.size();
      for (int j = 0; j < lj; j++) {
        MatchNode n = list.get(j);
        which_switch: switch (n.which) {
          case EXACT :
            if (n.text.equals(u)) {
              node = n;
            }
            break;
          case NUMBER :
            int lk = u.length();
            for (int k = 0; k < lk; k++) {
              char c = u.charAt(k);
              if (c < '0' || c > '9') {
                break which_switch;
              }
            }
            node = n;
            break;
          case TEXT :
            node = n;
            break;
        }
        if (node != null) {
          break;
        }
      }
      if (node == null) {
        return UriMatcher.NO_MATCH;
      }
    }

    return node.code;
  }

  private static final int EXACT = 0;
  private static final int NUMBER = 1;
  private static final int TEXT = 2;

}
