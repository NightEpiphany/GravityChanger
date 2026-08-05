## About
This fabric mod ports the [Gravity Changer (qouteall fork)](https://modrinth.com/mod/gravity-api-fork) based on [Gravity API](https://modrinth.com/mod/gravity-api) to higher **1.21,26.2** versions.

## Additions
Currently on **beta**.  
Items are all for creative test only.  
Some of the testing items are not in the creative tab
- Gravity Changer (Stable)
- Gravity related Commands (Stable)
- Gravity Anchor (Stable)
- Gravity Core Block (Stable/26.2)
- ~~Gravity Plate (Unstable)~~ (Marked for removal)
- ~~Gravity Status Effect & Potions (Stable)~~ (Marked for removal)

## Commands (26.2)
``
/attribute @p gravity_changer:gravity_direction base set <directionValue>
``
Sets the base gravity direction.
- directionValue ref:
> 0 -> DOWN  
> 1 -> UP  
> 2 -> NORTH  
> 3 -> SOUTH  
> 4 -> WEST  
> 5 -> EAST  

``
/attribute @p gravity_changer:gravity_strength base set <value>
``

Sets the base gravity strength. Default value: **9.8**. Range: **5 to 15**.

Check the vanilla [``attribute`` command](https://minecraft.wiki/w/Commands/attribute) format for more info.

## Commands (1.21)

``
/gravity set_base_direction <direction> [entities]
``
 sets the base gravity direction. (The base direction can be overridden by other things including effects, gravity anchor and gravity plating). Without [entities] argument it will target the command sender (the same applies to all commands). Examples: /gravity set_base_direction up /gravity set_base_direction up @e[type=!minecraft:player]


``
/gravity set_base_strength <strength> [entities]
``
 sets the base gravity strength. The strength effects will multiply on the base strength (instead of overriding it). Examples: /gravity set_base_strength 0.5 /gravity set_base_strength 0.5 @e


``
/gravity view
``
 shows the base gravity direction and strength of the command sender.


``
/gravity reset [entities]
``
 reset the base gravity direction and strength.


``
/gravity randomize_base_direction [entities]
``
 sets the base direction as a random direction.


``
/gravity set_relative_base_direction <relativeDirection> [entities]
``
 sets the gravity direction as a direction relative to the entity's viewing direction. The <relativeDirection> can be forward, backward, left, right, up or down.


``
/gravity set_dimension_gravity_strength <strength>
``
 sets the dimensional gravity strength for the current dimension.


``
/gravity view_dimension_info
``
 shows the dimensional gravity strength for the current dimension.