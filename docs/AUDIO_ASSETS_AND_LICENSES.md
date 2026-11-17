# Shadow Quest: The Lost Realm — Audio Assets & Licensing

This document details all audio tracks, sound effects, and voice narration assets implemented in **Phase 5.3**, including their generation methods, technical specifications, and licenses.

---

## 1. Voice Narration Assets

All voice narration files cover the exact script lines across the seven-scene opening cinematic defined in `OpeningScene.java`.

- **Voice Talent / Synthesis Engine**: Microsoft SAPI (Desktop Speech Synthesizer)
- **Voice Character**: "Microsoft David Desktop" (English - United States)
- **Style / Direction**: Cinematic storytelling delivery, neutral male, clear articulation, deliberate dramatic pacing.
- **Audio Format**: 16-bit Mono Linear PCM WAV, 22,050 Hz sampling rate.
- **License**: Generated using Windows standard system speech synthesis runtime. Royalty-free for local interactive execution.

| Filename | Scene | Purpose / Narration Script | Duration | Scene Limit |
|---|---|---|---|---|
| `scene1.wav` | Scene 1: Exploration | *"Arin followed a quiet trail beneath the sunlit trees. Beyond the familiar paths, something caught his eye."* | 5.95s | 7.0s (1.05s margin) |
| `scene2.wav` | Scene 2: Discovery | *"An ancient portal stood hidden among the roots. As Arin drew closer, its silent stones began to glow."* | 5.87s | 7.0s (1.13s margin) |
| `scene3.wav` | Scene 3: Entering | *"Arin stepped toward the light. One step carried him beyond the world he knew."* | 4.68s | 6.0s (1.32s margin) |
| `scene4.wav` | Scene 4: Arrival | *"The warmth of the forest vanished. Arin had arrived in Eldoria. Dark trees rose through a strange turquoise mist."* | 6.02s | 7.0s (0.98s margin) |
| `scene5.wav` | Scene 5: Montage | *"For several days, Arin searched the unfamiliar realm. Each new trail led deeper into Eldoria, but never home."* | 7.45s | 12.0s (4.55s margin) |
| `scene6.wav` | Scene 6: Mission | *"Arin was trapped. The Shadow Lord controlled Eldoria's dark forces. To return home, he must defeat those forces and restore the ancient portals."* | 8.50s | 12.0s (3.50s margin) |
| `scene7.wav` | Scene 7: Gameplay | *"His journey would begin in Mystic Forest. Find a way forward."* | 3.50s | 4.0s (0.50s margin) |

---

## 2. Background Music Tracks

All background music tracks were synthesized using the project's native algorithmic multi-oscillator synthesizer (`AudioSynthesizer.java`). Each track features seamless loop crossfades and bespoke dark fantasy orchestration.

- **Generation Method**: Original procedural synthesis using multi-oscillator additive harmonic arrays, organic LFO modulation, resonant low-pass filtering, and tail-matching crossfade buffers.
- **Audio Format**: 16-bit Mono Linear PCM WAV, 22,050 Hz.
- **License**: 100% Original work, MIT / Public Domain equivalent. Completely royalty-free without commercial encumbrance or external copyright claims.
- **Attribution**: Built specifically for *Shadow Quest: The Lost Realm*.

| Filename | Track Identifier | Purpose & Mood | Musical Key / Instrumentation | Duration |
|---|---|---|---|---|
| `menu.wav` | `AudioTrack.MENU` | Main Menu background atmosphere | E minor; deep organ drone (E2, B2), ambient wind noise, and mystical bell motifs | 10.0s loop |
| `cutscene.wav` | `AudioTrack.CUTSCENE` | Opening cinematic background music | D minor; cinematic strings swell, rolling harp arpeggios, gentle emotional progression | 12.0s loop |
| `forest.wav` | `AudioTrack.FOREST` | Level 1: Mystic Forest exploration | A minor; low wood drone, vibrato wooden flute lead, rustling leaves, high chimes | 10.0s loop |
| `castle.wav` | `AudioTrack.CASTLE` | Level 2: Shadow Castle atmosphere | C minor; gothic pipe organ resonance, dark minor choir chords, low bass rumble | 10.0s loop |
| `boss.wav` | `AudioTrack.BOSS` | Boss battle music (Giant Goblin & Shadow Lord) | D minor; driving 140 BPM battle drums, pulsing bass ostinato, brass tension stabs | 8.0s loop |
| `victory.wav` | `AudioTrack.VICTORY` | Shadow Lord defeat & victory fanfare | C major; triumphant brass triads (C4, E4, G4, C5) resolving to radiant chime shimmer | 6.0s sting |
| `game_over.wav` | `AudioTrack.GAME_OVER` | Player defeat screen | A minor; deep somber funeral gong toll, descending minor decay, low drone | 5.0s sting |

---

## 3. Gameplay Sound Effects

All 28 sound effects were synthesized using physical modeling, frequency modulation, transient impulse modeling, ADSR envelopes, and filtered noise arrays.

- **Generation Method**: Original procedural physical modeling (`AudioSynthesizer.java`).
- **Audio Format**: 16-bit Mono Linear PCM WAV, 22,050 Hz.
- **License**: 100% Original work, MIT / Public Domain equivalent. Completely royalty-free.

### A. Player Sound Effects
| Filename | Event | Acoustic Characteristics |
|---|---|---|
| `sword_pickup.wav` | Arin picks up the sword | Metallic draw sheen rising from 440 Hz to 1240 Hz with crystalline harmonic resonance |
| `sword_swing.wav` | Spacebar sword attack swing | Aerodynamic whoosh with shaped bandpass white noise and descending blade swipe |
| `sword_hit.wav` | Blade strikes an enemy | Sharp high metallic transient (1200 Hz + 2400 Hz) layered over dense thud impact |
| `player_hurt.wav` | Arin takes combat damage | Low vocal impact grunt with pitch drop (140 Hz to 80 Hz) and body thud |
| `player_death.wav` | Arin dies (Game Over) | Heavy bass thud with decaying sub-bass drop and somber acoustic decay |
| `heal.wav` | Health potion consumed | Ascending magical harp sweep with soothing bubbly liquid chime |
| `sword_upgrade.wav` | Giant Goblin defeat +5 ATK | Ascending golden divine power surge (220 Hz to 880 Hz) with overtone harmonics |

### B. Enemy Sound Effects
| Filename | Event | Acoustic Characteristics |
|---|---|---|
| `goblin_attack.wav` | Goblin attacks | Raspy goblin snarl with fast dagger swipe |
| `goblin_death.wav` | Goblin defeated | High-pitched goblin screech decaying into dirt collapse |
| `wolf_attack.wav` | Shadow Wolf attacks | Guttural wolf bark with aggressive jaw bite snap |
| `wolf_death.wav` | Shadow Wolf defeated | Descending mournful wolf yelp |
| `giant_goblin_attack.wav` | Giant Goblin club slam | Heavy low-frequency earthquake club impact with sub-bass rumble |
| `giant_goblin_death.wav` | Giant Goblin defeated | Deep monster roar followed by massive body collapse |
| `shadow_knight_attack.wav` | Shadow Knight broadsword | Heavy dark-steel blade cleave with ringing armor clang |
| `shadow_knight_death.wav` | Shadow Knight defeated | Clattering plate-armor collapse and hollow soul dissipation |
| `dark_archer_attack.wav` | Dark Archer bow shot | High-tension bowstring release with whistling shadow arrow twang |
| `dark_archer_death.wav` | Dark Archer defeated | Ethereal whisper and fading shadow cloak flutter |
| `wraith_attack.wav` | Wraith siphon strike | High-pitched icy spectral screech with soul siphon hiss |
| `wraith_death.wav` | Wraith defeated | Resonant ghostly wail dissolving into ethereal mist |

### C. Portal & Boss Sound Effects
| Filename | Event | Acoustic Characteristics |
|---|---|---|
| `portal_unlock.wav` | Ancient portal activates | Ancient stone grinding resonance paired with high magical awakening chime |
| `portal_enter.wav` | Arin enters the portal | Swirling dimensional vortex whoosh with frequency warp modulation |
| `portal_ambient.wav` | Swirling portal idle hum | Hypnotic low-frequency pulsating dimensional hum |
| `shadow_lord_intro.wav` | Shadow Lord fight engages | Ominous dark choir swell with thunderous low void resonance |
| `shadow_lord_melee.wav` | Void Scythe melee strike | Deep aerodynamic dark matter blade slice with sweeping overtone |
| `shadow_lord_fire.wav` | Flamethrower breath | Continuous roaring hellfire stream with turbulent pink/white noise |
| `shadow_lord_fire_hit.wav` | Flamethrower hits Arin | Searing burn impact sizzle combined with explosive flame burst |
| `shadow_lord_death.wav` | Shadow Lord defeated | Cataclysmic void implosion with collapsing multi-harmonic soul shatter |

### D. Interface Sound Effects
| Filename | Event | Acoustic Characteristics |
|---|---|---|
| `pause.wav` | ESC pressed to pause | Clean high-frequency glass tap with time-freeze acoustic tick |
| `resume.wav` | Gameplay resumed | Crisp upward unpause tick with quick release |
| `button_click.wav` | Menu button clicked | Snappy wooden/slate UI click |
| `confirm.wav` | Confirmation dialog accepted | Resonant two-tone ascending affirmative chime (D5 -> A5) |
