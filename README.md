# DLAW

Spigot plugin for Discord based whitelisting over a custom discord bot

### Features

- Cloud synced account linking
- Direct server integration (join, leave, death, advancements, player chat)
- On death cords DM
- Customisable colors for embeds
- HTTP REST API (for web developers)

### Setup

Make sure to start the minecraft server once, after so go to `/plugins/DLAW/config.yml` and edit the lines corresponding to your settings

```yaml
discord:
  token: token-goes-here
  guild: 553568657410883604
  channel:
    chat: 1024426048932302878
  role:
    staff: 707598912768442489
api:
  enable: true
  port: 8010
minecraft:
  address: fluxsmp.pequla.com
color:
  system: 65535
  join: 65280
  leave: 16711680
  death: 16749128
  advancement: 16751104
```