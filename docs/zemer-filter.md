# Zemer filter

An opt-in, music-only artist whitelist. When enabled in Settings, playback and
every music-browsing surface (Home, Search, Library, playlists, related
tracks, Explore, the player queue, endless radio, Android Auto, the On Repeat
widget) are restricted to artists on a Zemer-curated allow-list.

Default: off. Toggled via `PlayerPreferences.zemerFilterEnabled`.

## How it works

All of the logic lives in [`ZemerFilter`](../app/src/main/java/com/arubr/smsvcodes/data/music/zemer/ZemerFilter.kt),
a single object with three checks (`allowsTrack`, `allowsArtist`,
`allowsPlaylist`) plus `filterTracks` / `filterArtists` / `filterPlaylists`
convenience wrappers. A track or artist passes only if one of its YouTube
channel ids is in the loaded allow-list.

**Fails closed.** If the allow-list hasn't loaded yet and the filter is
enabled, every check returns `false` — nothing plays or shows rather than
leaking unfiltered content. `enabled == false` (the default) always passes
everything through unchanged.

### Enforcement points

List-level filtering is a UX nicety; the thing that actually prevents a
blocked track from playing is enforcement inside
[`EnhancedMusicPlayerManager`](../app/src/main/java/com/arubr/smsvcodes/player/EnhancedMusicPlayerManager.kt):

- `playTrack()` refuses to start a track that fails `allowsTrack()`, and
  filters whatever queue it's given.
- `updateQueue()` filters what gets queued next (never evicts what's already
  playing).
- `updateAutomixItems()` / `appendAutomixItems()` filter endless radio's
  "up next" pool, so continuous playback can't drift into unwhitelisted
  tracks over time.

The same enforcement is duplicated for the two paths that can start playback
without going through that class at all:

- Android Auto — [`AutoLibrary`](../app/src/main/java/com/arubr/smsvcodes/service/auto/AutoLibrary.kt)
  (browse tree, search) and [`AutoLibraryBrowser`](../app/src/main/java/com/arubr/smsvcodes/service/auto/AutoLibraryBrowser.kt)
  (`setMediaItems`/`addMediaItems`, which the car can hand straight to the
  player).
- The On Repeat home-screen widget — [`OnRepeatSource`](../app/src/main/java/com/arubr/smsvcodes/widget/onrepeat/OnRepeatSource.kt).

List-level filtering is applied everywhere else content is fetched or
composed: Home/trending/search/charts/albums/playlists in
[`InnertubeMusicService`](../app/src/main/java/com/arubr/smsvcodes/data/newmusic/InnertubeMusicService.kt)
(via `convertToMusicTrack`'s own `allowsTrack` check, so most of this file
needs no explicit filter calls), the legacy `YouTubeMusicService` fallbacks
and the Explore screen's new-release albums in
[`MusicViewModel`](../app/src/main/java/com/arubr/smsvcodes/ui/screens/music/MusicViewModel.kt),
and the local-history-derived shelves (On Repeat, Rotation, Rediscover, Deep
Cuts, Artists For You) in that same file's `refreshLocalShelves()` — these
are built from the user's own listening history/graph rather than a live
fetch, so they never pass through `convertToMusicTrack` and need their own
explicit filtering.

## The allow-list: a bundled snapshot, not a live fetch

`ZemerFilter` originally fetched `content.zemer.io/whitelist` over the
network on every enable. **That endpoint is gated:** since 2026-10-28, Zemer
requires an `X-Zemer-Partner` key for server-side calls to
`content.zemer.io` and `search.zemer.io` (confirmed against a real Zemer
partner's open-source client, [Shalom-Karr/SK-Music](https://github.com/Shalom-Karr/SK-Music),
whose `docs/zemer-partner.md` documents the requirement). Without a key,
`/whitelist` returns Cloudflare's block page to *any* client, including a
plain browser — this was confirmed directly, not inferred. Flow has no such
key, and there is no client-side way around a deliberate access gate like
this; getting one requires Zemer granting Flow a partnership.

Until that happens, `ZemerFilter` loads its allow-list from a bundled
resource instead: [`res/raw/zemer_whitelist.txt`](../app/src/main/res/raw/zemer_whitelist.txt),
one YouTube channel id per line, read via `openRawResource(R.raw.zemer_whitelist)`
at `start()`. No network call, loads instantly and works offline, same
fail-closed behavior if the resource is ever missing or empty.

### Where the snapshot came from

Built by hand, not from Zemer's gated API:

1. Zemer publishes an unrelated, ungated page — `zemer.io/artists` — listing
   every artist in their catalog by name/slug (no channel ids).
2. Each name was matched against the YouTube Data/YouTube Music APIs to find
   its channel id, run as a multi-day, multi-pass pipeline (an initial pass,
   a refined re-pass with better source attribution, and a smaller
   cross-source QA batch for spot-checking).
3. The passes were merged into one list, one row per artist name, keeping the
   highest-confidence match for each.
4. Where a single artist name matched **more than one** distinct channel id
   across passes (49 names), each distinct channel id was kept as its own
   entry (`Artist Name`, `Artist Name-2`, `Artist Name-3`, ...) rather than
   guessing which one was correct.
5. The final channel ids (1,662 distinct, after dedup) became
   `zemer_whitelist.txt`.

### Known limitations

- **Point-in-time snapshot, not a live sync.** It will not reflect new
  artists Zemer adds, or existing artists Zemer removes, after it was built.
  There is no update mechanism — refreshing it means repeating the matching
  process and replacing the file.
- **No moderation metadata.** The original `content.zemer.io/whitelist` and
  `/blockedContentIds` responses carried additional flags (e.g. an
  `isFemale` exclusion) that this snapshot has no equivalent for, since that
  data only ever existed in the gated API, not on the public artists page.
- **Inherits name-matching noise.** The 49 split (`-2`, `-3`, ...) entries in
  particular may include some genuinely wrong channel matches alongside
  legitimately-distinct channels for artists who share a name — this wasn't
  manually verified per-entry.

### If Flow ever gets a partner key

Re-adding a live fetch is straightforward: restore a network path in
`ZemerFilter.kt` that sends `X-Zemer-Partner: <key>` only to
`content.zemer.io`'s whitelist/blocked-content paths (see SK-Music's
`zemerHeaders()` helper for the pattern — an explicit per-host, per-path
allow-list for the header, never attached elsewhere), with the key read from
a secret that never enters the repo (e.g. a signing config property or
gradle property sourced from CI secrets, not a committed file). The bundled
snapshot should stay as a fallback for a failed fetch rather than being
deleted outright, since it still beats failing fully closed.
