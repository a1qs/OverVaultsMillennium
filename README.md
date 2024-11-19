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
## OverVaults Millennium Edition
###### *made for the Millennium VHSMP Server*

### New Features
In this version of OverVaults, forked from [OverVaults](https://github.com/IridiumIO/OverVaults) there are a few additional features, such as:

* Portals may now be automatically ***activated***, having various traits
    * These will activate based on a timer (can be a random range) set in the `overvaults-server.toml` config file
    * While its active, the chunks that the portal is in will be loaded during the duration
    * All OverVault Portals may either be a Raw Vault, or a Normal Vault with special modifiers
    * Each OverVault Portal can have various modifiers assigned to it.
    * Each `x` seconds (can be a range) it may remove a random modifier assigned to that OverVault Portal (Shrinks 1 modifier stack, e.g. 10x Energizing -> 9x Energizing)
    * Once removed, it will spawn a Vault Dweller (or boss if enough have spawned, configurable), which gradually improve their stats with the higher tier they go
    * There may only be one active portal at a time. (does not affect the activateAllPortals command)
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
* If a Portal has been broken by a player/any other way, on the next check for modifier removal, the portal will be verified, and if its not present, its associated data will be removed and the counter for an active OverVault will restart
- Added new commands
    - `/overvaults structures activateAllPortals` 
        - Activates all the valid structures in the world, this command is only meant for debug purposes and may mess things up if used on an active server
    - `/overvaults structures activateStructureWithIndex`
      - Activates a structure in the world with the specified index
    - `/overvaults structures activateRandomPortal`
        - Activates a random Overvault portal the same it would as the random tick counter.
    - `/overvaults structures deactivateActivePortal`
        - Finds the first active portal and deactivates it (removing it from the data information, currently doesnt remove the portal blocks)
    - `/overvaults structures getActiveOverVault` 
        - Returns the active Overvault structure with its index, position, tp command, etc.
    - `/overvaults structures getClosestStructure`
        - Returns the closest Overvault structure to the player.
    - `/overvaults structures getNextOverVaultSpawn`
        - Returns the time required for the next Overvault to spawn alongside its initial starting time.
    - `/overvaults structures getRandomStructure`
        - Returns a random structure that is considered valid in any dimension.
    - `/overvaults structures getStructureList {ResourceLocation: dimension}`
        - Lists all the valid structures coordinates found in the given dimension alongside its Index & a command to teleport to it.
    - `/overvaults structures getStructureWithIndex {Integer: index}`
        - Returns the Structure that matches the given index.
    - `/overvaults structures removeStructureWithIndex {Integer: index}`
        - Removes the Structure that matches the given index.
    - `/overvaults lootTables addLootTableEntry {ResourceLocation: LootTable} {Integer: WeightPool} {String: NewItemId} {Integer: NewItemWeight} {Integer: NewItemMinRoll} {Integer: NewItemMaxRoll}`
        - Adds a Loot table entry with the given parameters to the LootTable, may not function with every Loottable due to how they differ inbetween eachother.
    - `/overvaults lootTables removeItemFromLootTable {ResourceLocation: LootTable} {String: RemovalItemId}`
        - Removes the first matching LootTable entry with the given Item id.
