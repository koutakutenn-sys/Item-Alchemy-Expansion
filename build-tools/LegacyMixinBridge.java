import java.nio.file.*;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/**
 * ItemAlchemy 1.3.9 is transformed by LittleIntermediaryFallback AFTER mixins.
 * Match its pre-transform descriptors only in mixins targeting that dependency.
 * Merged bytecode is then converted to native names by its existing runtime bridge.
 * Native Minecraft/MCPitanLib mixins and all ordinary addon classes stay native.
 */
public class LegacyMixinBridge {
    public static void main(String[] args) throws Exception {
        Map<String, String> reverse = new HashMap<>();
        for (String line : Files.readAllLines(Path.of(args[0]))) {
            String[] pair = line.split("\t");
            reverse.put(pair[1], pair[0]);
        }
        Remapper remapper = new Remapper(Opcodes.ASM9) {
            @Override public String map(String name) { return reverse.getOrDefault(name, name); }
            @Override public Object mapValue(Object value) {
                if (value instanceof String s) {
                    for (var entry : reverse.entrySet()) s = s.replace("L" + entry.getKey() + ";", "L" + entry.getValue() + ";");
                    return s;
                }
                return super.mapValue(value);
            }
        };
        Path root = Path.of(args[1]);
        if (!Files.exists(root)) return;
        try (var files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".class")).toList()) {
                String name = file.getFileName().toString();
                if (!name.startsWith("Mixin") || name.equals("MixinSimpleInventoryScreen.class")) continue;
                ClassWriter writer = new ClassWriter(0);
                new ClassReader(Files.readAllBytes(file)).accept(new ClassRemapper(writer, remapper), 0);
                Files.write(file, writer.toByteArray());
                System.out.println("Prepared legacy dependency mixin: " + name);
            }
        }
    }
}
