*For the Vault Hunters Minecraft Modpack*

Adds ruined vault portals to the landscape in the overworld.

Each ruin has a smattering of vault stone and some chromatic iron as well, for those wishing to get started vaulting without going underground for resources.
Underwater ruins have more chromatic iron, and portals have a chance to spawn with a completed crystal.

-----------

*Something happens when you find your first ruined vault portal in the world...*

-----------

This is not designed to replace the need to get vault stone and chromatic iron; rather it exists more as an exploratory way to obtain some early-game resources.
Additional structures are planned (more rare portal types, and maybe rare shrines to the vault gods, or abandoned armories?)  but these have to remain subtle to avoid breaking out of scope of what VH is about.

It can be added to existing worlds, however only new chunks will be able to spawn the portals. 

-----------
## OverVaults Millenium Edition
###### *made for the Millenium VHSMP Server*

### New Features
In this version of OverVaults, forked from [OverVaults](https://github.com/IridiumIO/OverVaults) there are a few additional features, such as:

* Portals may now be automatically ***activated***, having various traits
    * These will activate based on a timer set in the `overvaults-server.toml` config file
    * While its active, the chunks that the portal is in will be loaded during the duration
    * All OverVault Portals may either be a Raw Vault, or a Normal Vault with special modifiers
    * Each OverVault Portal can have various modifiers assigned to it.
    * Each `x` seconds it may remove a random modifier assigned to that OverVault Portal (Shrinks 1 modifier stack, e.g. 10x Energizing -> 9x Energizing)
    * There may only be one active portal at a time.
    * A message gets sent to each player except for players currently inside a Vault, noting the approx. position of the Portal (last 2 digits obfuscated)
    * Players with a Vault Compass will have their compasses pointing towards the Portal if they are in the same dimension
    * Each Dimension has the same chance of being chosen, assuming they all have a valid Portal, then one of the Portals in the Dimension will be chosen to be activated
    * * Upon entering a portal, a new timer will start for a new OverVault
* OverVaults may now generate inside the `Nether` & in the `End` Dimension.
    * Active OverVaults may have higher chances to get Nether/End configured themes (Nether/Void themes by default)
    * All Portals in the Nether & the End have no crystal and always have a complete frame, making all of them valid.
    * ###### _Note: Nether Generation is currently very finnicky and may generate in some weird places_ 
* Only Portals that have a complete frame will be deemed valid and have a chance to be an active OverVault
* Upon entering, the players Vault Level will be used for the vault who entered
* Portal Tile Entities & Chunks force-loaded while there is an active OverVault will be saved and perserved on server crashes/restarts
* Added new Commands:
    * `/overvaults getActiveOverVault` -> returns the current active Vault with position, rotation, tp command and more
    * `/overvaults getNextOverVaultSpawn` -> returns the time until a new attempt is made to spawn an active OverVault
    * `/overvaults getRandomStructure` -> returns a random valid overvault structure with position, rotation, etc.
    * `/overvaults getStructureWithIndex` -> returns a valid overvault structure with the given index
    * `/overvaults removeItemFromLootTable {"ResourceLocation"} {"modid:itemname"}` -> removes the first item found in the loottable that matches - suggests the ResourceLocation if the mod is installed client-side
    * `/overvaults addLootTableEntry {"ResourceLocation"} {"WeightPool"} {"modid:itemname"} {"ItemWeight"} {"ItemMinRoll"} {"ItemMaxRoll"}` -> adds a Item entry to the selected loottable to the pool selected via the weight with the given values - suggests the ResourceLocation & the WeightPool if the mod is installed client-side 

