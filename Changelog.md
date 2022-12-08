# Changelog

### Version 1.19.3 - 1.6.3
 - port to 1.19.3

### Version 1.19.2 - 1.6.3
 - fix error where there was no interaction when right clicking on a block
   - affected for example that chests could not be opened with an empty hand 

### Version 1.19.2 - 1.6.2
 - right-clicking grown crops with a hoe increases the amount of drops depending on the tier of the hoe
   - can be disabled in the config
   - better hoe => more drops
- added a config option to filter out items that should not be dropped when the crop is harvested by right-clicking
  - disabled by default
  - applies to harvesting with empty hand and with a hoe
  - example: can be used to remove `minecraft:wheat_seeds` from the drops of wheat-crops
- added config option to disable spawner clearing when placed
 - move config to json format
   - this way the config on forge and fabric has the same format
 - add command `/improvedvanilla`
   - `/improvedvanilla config show` - provides a clickable link, to find the config file easily (works only in singleplayer)
   - `/improvedvanilla config reload` - reloads the config file from the file system
   - `/improvedvanilla config reset` - resets the config to the default values

### Version 1.19 - 1.6.1
 - fix `Mod Loading has failed` error

### Version 1.19 - 1.6.0
 - port to 1.19

### Version 1.18.2 - 1.6.0
 - add recipes for following items (using mostly forge and minecraft tags to be compatible with other mods)
     - bell
     - crying obsidian
     - dead bushes
     - iron/gold and diamond horse armor
     - glow ink sac
     - glow lichen
     - saddle
     - string
     - gilded blackstone

### Version 1.18.1 - 1.5.1
 - port to 1.18.1
 - improve spawner drops

### Version 1.17.1 - 1.5.0
 - add forgotten well structure
 - add jungle temple structure
 - add underground temple structure
 - moved config to normal config folder
 
