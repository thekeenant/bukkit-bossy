# Bossy

Bossy is an easy-to-use and awesome developer- and player-friendly Bukkit Boss Bar API (wow that's a mouthful).

**Dependencies:**
* [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)
* Any derivative of Bukkit 1.8 ([PaperSpigot!](https://tcpr.ca/downloads/paperspigot))


**Demonstration:**

![http://i.imgur.com/KTH0af4.gif](http://i.imgur.com/KTH0af4.gif)

**Usage:** It's as easy as this!

```
Bossy bossy = new Bossy(plugin);
bossy.set(player, "Your message", 0.5F);
bossy.setText(player, "Change the message");
bossy.setPercent(player, 1F); // change health/percent (between 0 and 1)
bossy.hide(player);
bossy.show(player);
```

**Static Methods:** If you really must use static methods... You can access the same methods as before execpt static-ly with `BossyAPI`.

```
BossyAPI.set(player, "Your message", 0.5F);
BossyAPI.setText(player, "Change the message");
BossyAPI.setPercent(player, 1F); // change health/percent (between 0 and 1)
BossyAPI.hide(player);
BossyAPI.show(player);
```

*P.S.* If you are using static methods, it's probably smart to put this in your `onEnable()` method (so BossyAPI knows what plugin is using it).

```
BossyAPI.init(this);
```
