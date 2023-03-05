# DLAW

Spigot plugin for Discord based whitelisting over a custom discord bot

### Features

- Cloud synced account linking
- Direct server integration (join, leave, death, advancements, player chat)
- On death cords DM
- Customisable colors for embeds
- Player count in bot status
- HTTP REST API (for web developers)

#### Discord Slash Commands

These are all the available discord commands:

- `/verify` Links a Minecraft Java account to a Discord account
- `/unverify` Removes the existing account link
- `/ip` Returns the server IP (from `config.yml`)
- `/lookup` Allows you to find a Discord account from a Minecraft username
- `/rcon` Executes a command on the server
- `/seed` Returns the world seed
- `/status` Displays current online players and server version

#### HTTP Endpoints

This feature is very useful for integration data and status from your minecraft server on your webpage. For general
users this feature might not be needed

> The web server can be enabled or disabled in `config.yml` based on your needs

- `GET /api/status` Complete server status
- `GET /api/status/players` Online player list
- `GET /api/status/plugins` List of plugins
- `GET /api/status/world` World information
- `GET /api/players` List of all players who joined the server
- `GET /api/user?uuid=` Discord account information from Minecraft account UUID

### Setup

Download the latest version from [releases](https://github.com/Pequla/DLAW/releases/latest) and put it in your plugins
folder.

Make sure to start the minecraft server once, after so go to `/plugins/DLAW/config.yml` and edit the lines corresponding
to your settings

In order to create a Discord Bot and get its token you need to go [here](https://discord.com/developers/applications).
After creating the application also create the bot, copy the token to clipboard for later use in configs. Make sure to
enable all privileged intentions like PRESENCE_UPDATE, GUILD_MEMBERS and MESSAGE_CONTENT. After that go and generate the
invite url with scopes `bot` and `application.commands`. For permission, you can choose Administrator since it's your
private bot

#### Configuration example

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