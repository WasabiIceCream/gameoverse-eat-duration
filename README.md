# Gameoverse Eat Duration

Foods that heal more take longer to eat; light snacks go down faster. On this server
Hearty Meals heals a food's nutrition value in half-hearts, so nutrition is the scale.

Eating time = the item's own time x (`offset` + `slope` x nutrition), clamped to
[`minSeconds`, `maxSeconds`]. The clamp never makes an item slower than its own time.
Scaling the item's own time keeps Hearty Meals' halved drink/stew times and other mods'
fast foods. Items without a food component (potions, milk) are untouched.

`config/gameoverse_eat_duration.json` (defaults: offset 0.5, slope 0.125, 0.6-4 s, so
nutrition 4 stays at vanilla's 1.6 s). Restart to apply. The server sends its values to
each client on join; AutoModpack stops updating a player's `config/` after the first
download, so the client's own copy can't be relied on.

- `Item.getUseDuration`: scales the result (`ItemMixin`). Items that override
  `getUseDuration` without calling up to `Item` keep their own time.
- `ItemStack.onUseTick`: vanilla times chewing particles/sounds from the unscaled
  consume time, so they'd start late on long bites; `ItemStackMixin` applies the same
  rule to the scaled time.

On server start it writes `logs/gameoverse-eat-duration.tsv` (every food, nutrition,
old and new seconds). First run, 2026-09-26: 880 foods, 806 changed.

Both sides; install on the server (AutoModpack ships it). Build: `./gradlew build`. MIT.
