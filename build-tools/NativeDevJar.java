import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.*;

/** Produces a compile-only view of Item Alchemy using its runtime bridge's class names. */
public class NativeDevJar {
    public static void main(String[] args) throws Exception {
        Map<String,String> mapping = new HashMap<>();
        for (String line : Files.readAllLines(Path.of(args[0]))) {
            String[] p = line.split("\t");
            mapping.put(p[0], p[1]);
        }
        Remapper remapper = new Remapper(Opcodes.ASM9) {
            @Override public String map(String name) { return mapping.getOrDefault(name,name); }
        };
        try (JarFile jar = new JarFile(args[1]); JarOutputStream out = new JarOutputStream(Files.newOutputStream(Path.of(args[2])))) {
            for (var entries = jar.entries(); entries.hasMoreElements();) {
                var entry = entries.nextElement();
                if (entry.isDirectory()) continue;
                byte[] bytes = jar.getInputStream(entry).readAllBytes();
                if (entry.getName().endsWith(".class")) {
                    ClassWriter writer = new ClassWriter(0);
                    new ClassReader(bytes).accept(new ClassRemapper(writer, remapper), 0);
                    bytes = writer.toByteArray();
                }
                out.putNextEntry(new JarEntry(entry.getName()));
                out.write(bytes);
                out.closeEntry();
            }
        }
    }
}
