# build-tools

Helper sources used by `build.gradle` to build this addon for Minecraft 26.2. Nothing here is
shipped inside the mod jar, and no game or dependency jar is ever modified.

## runtime-class-map.tsv

Two tab-separated columns, `net/minecraft/class_XXXX` (intermediary) to Mojang official name,
in registration order. It is a snapshot of the class map registered by
[LittleIntermediaryFallback](https://github.com/Pitan76/little-intermediary-fallback)
(`net/pitan76/littleintermediaryfallback/asm/MappingRegistry`), the component that rewrites the
1.20.1 build of Item Alchemy to 26.2 names at runtime. Minecraft 26.2 has no intermediary
mappings of its own, so this table is what lets this addon keep matching the dependency's
pre-rewrite descriptors.

Reproduction (verified against littleintermediaryfallback 1.0.2.262-SNAPSHOT, the copy embedded
in MCPitanLib 4.0.7-fix.1):

1. unpack `META-INF/jars/littleintermediaryfallback-*.jar` from the MCPitanLib jar and extract
   `net/pitan76/littleintermediaryfallback/asm/MappingRegistry.class`;
2. run `javap -c -p MappingRegistry.class` and keep every adjacent pair of
   `// String net/minecraft/class_...` followed by `// class net/minecraft/...` (one `addClass`
   call);
3. collapse duplicate keys keeping the last entry, matching the runtime `Map.put` semantics.

That reproduces the committed file exactly, order included.

## LegacyMixinBridge.java

Rewrites this addon's mixins that target Item Alchemy back to intermediary descriptors, because
the dependency's bridge runs after Mixin. Uses the table above.

## NativeDevJar.java

Produces the compile-only view of the Item Alchemy jar
(`build/itemalchemy-1.3.9-native-dev.jar`) in official names, using the same table.
