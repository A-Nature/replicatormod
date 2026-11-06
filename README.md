# Replicator Mod

A copy and paste mod for Minecraft 1.7.10 (Forge), a bit like WorldEdit. I made it for the kids at the summer camp I worked at so they could copy their builds and paste them around on the camp's MinecraftEdu server.

## Using it

There are two items. The Infinity Copier is for selecting and the Infinity Replicator is for pasting.

- Right click with the Copier to set one corner, and shift + right click to set the other
- Press R to rotate before you paste
- Shift + right click with the Replicator to open the menu of your saved clipboards, then use the Replicator to place the one you picked

Everyone gets their own clipboards, so you can name them and switch between them. You get a preview of where the paste will land, and you can undo it if it goes in the wrong spot. In survival mode it shows a checklist of the materials you need, and pasting uses them up.

## How it works

All the changes to the world happen on the server. The client just sends packets asking for things, so players can't overwrite each other. There are packets for saving a clipboard, picking one, setting the rotation and undoing, and the server sends back the clipboard list and the materials a build needs. Undo history only lasts for the session.

Client-only stuff like the screens and key bindings goes through a client proxy. I had a crash from GUI classes loading where they don't exist, and moving the client code behind a proxy fixed it.

## Chests, furnaces and signs

These are tile entities, so they carry extra data as NBT. When you copy, it calls `writeToNBT` on each one and saves the result with the block. When you paste, it changes the saved position to the new spot, rebuilds the tile entity with `TileEntity.createAndLoadEntity` and places it. The whole clipboard also gets saved to disk as NBT, with each block's data under a `TE` tag.

What gets copied depends on your game mode.

- Creative keeps what's inside chests and furnaces
- Survival strips it, so pasted chests and furnaces come out empty
- Signs always keep their text, since they aren't containers

## Files

- `ClipboardData.java` does the capturing, pasting, rotating and saving
- `PacketSaveClipboard.java` takes a copy request from the client and builds the clipboard on the server

## Install and build

You need Minecraft 1.7.10 and Forge. Build it with `./gradlew build` and the jar shows up in `build/libs`. Put it in the mods folder on the server and on each client. It only works on 1.7.10.
