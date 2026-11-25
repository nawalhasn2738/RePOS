package sg.edu.nus.iss.se24_2ft.unit1.ca.bootstrap;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/** Filename configuration only; this class creates no directories and performs no I/O. */
public final class StorePaths {
    private final Path directory;
    public StorePaths(Path directory) { this.directory = Objects.requireNonNull(directory, "directory"); }
    public static StorePaths workingDirectory() { return new StorePaths(Paths.get("data")); }
    public Path directory() { return directory; }
    public Path storekeepers() { return directory.resolve("Storekeepers.dat"); }
    public Path categories() { return directory.resolve("Category.dat"); }
    public Path products() { return directory.resolve("Products.dat"); }
    public Path members() { return directory.resolve("Members.dat"); }
    public Path discounts() { return directory.resolve("Discounts.dat"); }
    public Path transactions() { return directory.resolve("Transactions.dat"); }
}
