package niv.burning.api;

@FunctionalInterface
public interface BurningStorageListener {
    void burningStorageChanged(BurningStorage burningStorage);
}
