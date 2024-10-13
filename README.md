# Burning

**Burning** is a library mod that adds a unified interface for managing the amount of burning fuel within most, if not all, furnace-like blocks from both vanilla Minecraft and other mods.

Uses Fabric's Lookup and Transaction APIs.

## Documentation

**Burning** revolves around the [`BurningStorage`](src/main/java/niv/burning/api/BurningStorage.java) interface, which manages insertions and extraction of burning fuel, and the [`Burning`](src/main/java/niv/burning/api/Burning.java) class, which represent the burning fuel itself.

You may read the documentation or the source code to learn more.

### Getting Started (for normal users)

Install it as you'd with any other mod, and without further configuration,  every block which entity extends the `AbstractFurnaceBlockEntity` (Mojang mappings' name) abstract class, be them the vanilla _Furnace_, _Blast Furnace_, or _Smoker_, or any third-party block, will get a `BurningStorage` automatically and thus will be ready to interop with every other mod using this library.

### Dynamic Storages (for experienced users)

Some mods add block entities that can burn fuel but that, for one reason or another, don't extend the `AbstractFurnaceBlockEntity` abstract class, and thus, that won't automatically get a `BurningStorage`.

Fret not, for **Burning** makes available the `burning:dynamic_storage` dynamic registry, which, through data packs and a bit of reflection, resolves this specific problem.

How to do so.

For every such block entity, you must figure out a couple of things beforehand, that is:
* The name of that block entity's type.
* The name of the field of that block entity that is functionally equivalent to `AbstractFurnaceBlockEntity.litTime` (Mojang mappings' name).
* The name of the field of that block entity that is functionally equivalent to `AbstractFurnaceBlockEntity.litDuration` (Mojang mappings' name).

Then you must create a data pack like the one in the following example.

<details>
<summary>Data Pack Structure Example</summary>

```tree
<datapack_name>.zip
├── data
│   └── <datapack_name>
│       └── burning
│           └── dynamic_storage
│               └── <block_entity_type_1>.json
│               └── <block_entity_type_2>.json
│               └── ...
├── pack.mcmeta
└── pack.png (optional)
```

</details>

</br>

<details>
<summary>JSON File Content Example</summary>

```json
{
    // The name of that block entity's type
    "type": "example_mod:custom_furnace_entity_type",
    // The name of the field of that block entity that is functionally equivalent to `litTime`
    "lit_time": "burnTime",
    // The name of the field of that block entity that is functionally equivalent to `litDuration`
    "lit_duration": "fuelTime"
}
```

</details>

### Add Dependencies (for mod developers)

Add the following to your `gradle.build`.

```gradle
repositories {
    // Add Modrinth maven repository
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    // Add Burning dependency
    modImplementation "maven.modrinth:burning:<burning_version>"
}
```

For more information about the Modrinth maven repository read [here](https://support.modrinth.com/en/articles/8801191-modrinth-maven).

### Basic Usage (for mod developers)

Most of the time, as a mod developer using this library, you will only need to access already registered burning storages and transfer burning fuel between them. Here's how to do it.

Get a burning storage:

```java
import niv.burning.api.BurningStorage;

// What you need
Level world;
BlockPos pos;

// How to
@Nullable
BurningStorage storage = BurningStorage.SIDED.find(world, pos, null);
```

Note: for most use cases, `find`'s third argument, which should be a `Direction`, is ignored by the underlying implementation, so it's safe to pass null.

Transfer burning fuel between two storages:

```java
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;

// What you need
BurningStorage source, target;

// Create the maximum amount of burning fuel to transfer, for instance, half a COAL worth of burning fuel
Burning burning = Burning.of(Items.COAL).withValue(800);
// or if you are using COAL, BLAZE_ROD, or LAVA_BUCKET
Burning burning = Burning.COAL.withValue(800);

// How to
Burning transferred = BurningStorage.transfer(
        source, // transfer from this storage
        target, // to this storage
        burning, // up to this amount of burning fuel
        null); // creating a new transaction in doing so
// `transferred` will have the same fuel as `burning`
```

Transfer an exact amount or nothing:

```java
import niv.burning.api.Burning;
import niv.burning.api.BurningStorage;

BurningStorage source, target;

Burning burning = Burning.COAL.withValue(800);

try (Transaction transaction = Transaction.openOuter()) {
    Burning transferred = BurningStorage.transfer(source, target, burning, transaction);
    if (burning.equals(transferred)) {
        transaction.commit();
    }
}
```

Read the Fabric's Transaction API to understand the last example better.

### Use a burning storage in your block entity (for mod developers)

Instead of implementing the two fields functionally equivalent to `AbstractFurnaceBlockEntity`'s `litTime` and `litDuration`, do the following:

```java
import niv.burning.api.base.SimpleBurningStorage;

public class NewBlockEntity extends BlockEntity {
    // Add a simple burning storage to the entity
    public final SimpleBurningStorage simpleBurningStorage = SimpleBurningStorage.getForBlockEntity(this);

    // (Optional) Add a simple container data for the burning storage
    public final ContainerData containerData = SimpleBurningStorage.getDefaultContainerData(this.simpleBurningStorage);

    // And don't forget to add the following
    @Override
    protected void loadAdditional(CompoundTag compoundTag, Provider provider) {
        // ...
        this.simpleBurningStorage.load(compoundTag, provider);
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, Provider provider) {
        // ...
        this.simpleBurningStorage.save(compoundTag, provider)
    }
}
```

Don't forget to register it:

```java
import niv.burning.api.BurningStorage;

// What you need
BlockEntityType<NewBlockEntity> NEW_BLOCK_ENTITY;

// How to
BurningStorage.SIDED.registerForBlockEntity((newBlockEntity, direction) -> newBlockEntity.storage, NEW_BLOCK_ENTITY);
```

Then you can access the equivalent `litTime` and `litDuration` like:

```java
this.storage.getCurrentBurning(); // as the `litTime` equivalent
this.storage.setCurrentBurning(800);
this.storage.getMaxBurning(); // as the `litDuration` equivalent
this.storage.setMaxBurning(1600);
```
