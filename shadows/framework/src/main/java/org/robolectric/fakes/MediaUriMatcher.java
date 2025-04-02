/*
 * Copyright (C) 2022 The Android Open Source Project
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

package org.robolectric.fakes;

import android.content.UriMatcher;
import android.net.Uri;

/**
 * A simplified fork of MediaProvider's LocalUriMatcher.
 *
 * <p>This version only supports public files Uris, no support for hidden Uris or photo picker Uris
 */
class MediaUriMatcher {

  // WARNING: the values of IMAGES_MEDIA, AUDIO_MEDIA, and VIDEO_MEDIA and AUDIO_PLAYLISTS
  // are stored in the "files" table, so do not renumber them unless you also add
  // a corresponding database upgrade step for it.
  static final int IMAGES_MEDIA = 1;
  static final int IMAGES_MEDIA_ID = 2;
  static final int IMAGES_MEDIA_ID_THUMBNAIL = 3;
  static final int IMAGES_THUMBNAILS = 4;
  static final int IMAGES_THUMBNAILS_ID = 5;

  static final int AUDIO_MEDIA = 100;
  static final int AUDIO_MEDIA_ID = 101;
  static final int AUDIO_MEDIA_ID_GENRES = 102;
  static final int AUDIO_MEDIA_ID_GENRES_ID = 103;
  static final int AUDIO_GENRES = 106;
  static final int AUDIO_GENRES_ID = 107;
  static final int AUDIO_GENRES_ID_MEMBERS = 108;
  static final int AUDIO_GENRES_ALL_MEMBERS = 109;
  static final int AUDIO_PLAYLISTS = 110;
  static final int AUDIO_PLAYLISTS_ID = 111;
  static final int AUDIO_PLAYLISTS_ID_MEMBERS = 112;
  static final int AUDIO_PLAYLISTS_ID_MEMBERS_ID = 113;
  static final int AUDIO_ARTISTS = 114;
  static final int AUDIO_ARTISTS_ID = 115;
  static final int AUDIO_ALBUMS = 116;
  static final int AUDIO_ALBUMS_ID = 117;
  static final int AUDIO_ARTISTS_ID_ALBUMS = 118;
  static final int AUDIO_ALBUMART = 119;
  static final int AUDIO_ALBUMART_ID = 120;
  static final int AUDIO_ALBUMART_FILE_ID = 121;

  static final int VIDEO_MEDIA = 200;
  static final int VIDEO_MEDIA_ID = 201;
  static final int VIDEO_MEDIA_ID_THUMBNAIL = 202;
  static final int VIDEO_THUMBNAILS = 203;
  static final int VIDEO_THUMBNAILS_ID = 204;

  static final int MEDIA_SCANNER = 500;

  static final int FS_ID = 600;
  static final int VERSION = 601;

  static final int FILES = 700;
  static final int FILES_ID = 701;

  static final int DOWNLOADS = 800;
  static final int DOWNLOADS_ID = 801;

  // MediaProvider Command Line Interface
  static final int CLI = 100_000;

  private final UriMatcher mPublic = new UriMatcher(UriMatcher.NO_MATCH);

  int matchUri(Uri uri) {
    return mPublic.match(uri);
  }

  MediaUriMatcher(String auth) {
    // Warning: Do not move these exact string matches below "*/.." matches.
    // If "*/.." match is added to mPublic children before "picker/#/#", then while matching
    // "picker/0/10", UriMatcher matches "*" node with "picker" and tries to match "0/10"
    // with children of "*".
    // UriMatcher does not look for exact "picker" string match if it finds * node before
    // it. It finds the first best child match and proceeds the match from there without
    // looking at other siblings.

    mPublic.addURI(auth, "cli", CLI);

    mPublic.addURI(auth, "*/images/media", IMAGES_MEDIA);
    mPublic.addURI(auth, "*/images/media/#", IMAGES_MEDIA_ID);
    mPublic.addURI(auth, "*/images/media/#/thumbnail", IMAGES_MEDIA_ID_THUMBNAIL);
    mPublic.addURI(auth, "*/images/thumbnails", IMAGES_THUMBNAILS);
    mPublic.addURI(auth, "*/images/thumbnails/#", IMAGES_THUMBNAILS_ID);

    mPublic.addURI(auth, "*/audio/media", AUDIO_MEDIA);
    mPublic.addURI(auth, "*/audio/media/#", AUDIO_MEDIA_ID);
    mPublic.addURI(auth, "*/audio/media/#/genres", AUDIO_MEDIA_ID_GENRES);
    mPublic.addURI(auth, "*/audio/media/#/genres/#", AUDIO_MEDIA_ID_GENRES_ID);
    mPublic.addURI(auth, "*/audio/genres", AUDIO_GENRES);
    mPublic.addURI(auth, "*/audio/genres/#", AUDIO_GENRES_ID);
    mPublic.addURI(auth, "*/audio/genres/#/members", AUDIO_GENRES_ID_MEMBERS);
    // TODO: not actually defined in API, but CTS tested
    mPublic.addURI(auth, "*/audio/genres/all/members", AUDIO_GENRES_ALL_MEMBERS);
    mPublic.addURI(auth, "*/audio/playlists", AUDIO_PLAYLISTS);
    mPublic.addURI(auth, "*/audio/playlists/#", AUDIO_PLAYLISTS_ID);
    mPublic.addURI(auth, "*/audio/playlists/#/members", AUDIO_PLAYLISTS_ID_MEMBERS);
    mPublic.addURI(auth, "*/audio/playlists/#/members/#", AUDIO_PLAYLISTS_ID_MEMBERS_ID);
    mPublic.addURI(auth, "*/audio/artists", AUDIO_ARTISTS);
    mPublic.addURI(auth, "*/audio/artists/#", AUDIO_ARTISTS_ID);
    mPublic.addURI(auth, "*/audio/artists/#/albums", AUDIO_ARTISTS_ID_ALBUMS);
    mPublic.addURI(auth, "*/audio/albums", AUDIO_ALBUMS);
    mPublic.addURI(auth, "*/audio/albums/#", AUDIO_ALBUMS_ID);
    // TODO: not actually defined in API, but CTS tested
    mPublic.addURI(auth, "*/audio/albumart", AUDIO_ALBUMART);
    // TODO: not actually defined in API, but CTS tested
    mPublic.addURI(auth, "*/audio/albumart/#", AUDIO_ALBUMART_ID);
    // TODO: not actually defined in API, but CTS tested
    mPublic.addURI(auth, "*/audio/media/#/albumart", AUDIO_ALBUMART_FILE_ID);

    mPublic.addURI(auth, "*/video/media", VIDEO_MEDIA);
    mPublic.addURI(auth, "*/video/media/#", VIDEO_MEDIA_ID);
    mPublic.addURI(auth, "*/video/media/#/thumbnail", VIDEO_MEDIA_ID_THUMBNAIL);
    mPublic.addURI(auth, "*/video/thumbnails", VIDEO_THUMBNAILS);
    mPublic.addURI(auth, "*/video/thumbnails/#", VIDEO_THUMBNAILS_ID);

    mPublic.addURI(auth, "*/media_scanner", MEDIA_SCANNER);

    // NOTE: technically hidden, since Uri is never exposed
    mPublic.addURI(auth, "*/fs_id", FS_ID);
    // NOTE: technically hidden, since Uri is never exposed
    mPublic.addURI(auth, "*/version", VERSION);

    mPublic.addURI(auth, "*/file", FILES);
    mPublic.addURI(auth, "*/file/#", FILES_ID);

    mPublic.addURI(auth, "*/downloads", DOWNLOADS);
    mPublic.addURI(auth, "*/downloads/#", DOWNLOADS_ID);
  }
}
