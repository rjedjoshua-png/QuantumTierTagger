# CommunityTierTagger

A Minecraft Fabric & NeoForge mod that displays player PvP tiers from any **CommunityTierTagger-compatible** community directly in their nametag.

Hooks up to [QuantumTierlist](https://quantum-tierlist.vercel.app) — every Discord server that runs the Tierlist bot is automatically available in the mod's community switcher.

## Features

- **Live tier tags** in nametags, chat, and player list
- **Community switcher** — pick which community's tierlist to display
- **Gamemode switcher** — Sword, Axe, Mace, Crystal, NethPot, UHC, Pot, SMP, DiaSMP, Cart
- **Player search screen** — look up any player by IGN
- **Peak tier tracking** — shows your all-time best tier
- **Custom colors** — full color config per tier

## Requirements

- Minecraft 26.3+
- Fabric Loader 0.19+ or NeoForge 26.3.0.0-beta+
- [ukulib](https://modrinth.com/mod/ukulib) 2.2.0+

## Community Tierlist Support

CommunityTierTagger works with any backend that implements the API schema at `/api/v2/*`:

- `GET /api/tenants` — list of communities
- `GET /api/v2/mode/list` — gamemode list
- `GET /api/v2/profile/{uuid}` — player profile
- `GET /api/v2/profile/{uuid}/rankings` — player rankings
- `GET /api/v2/profile/by-name/{name}` — search by IGN

The default backend is [QuantumTierlist](https://quantum-tierlist.vercel.app).

## License

MPL-2.0 — see [LICENSE](LICENSE) for details.

Original TierTagger by [uku](https://github.com/uku3lig) and [netiyiy](https://github.com/netiyiy).
