package itemalchemy.expansion.compat.port;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import java.util.function.Predicate;

/** Restores numeric validation removed from vanilla FilteredEditBox in 26.2. */
public final class FilteredEditBox extends EditBox {
    public FilteredEditBox(Font font, int x, int y, int width, int height, Component label) {
        super(font, x, y, width, height, label);
    }
    public void setTextPredicate(Predicate<String> predicate) {
        final String[] accepted = {getValue()};
        final boolean[] restoring = {false};
        setResponder(value -> {
            if (restoring[0]) return;
            if (predicate.test(value)) accepted[0] = value;
            else {
                int cursor = getCursorPosition();
                restoring[0] = true;
                try { setValue(accepted[0]); setCursorPosition(Math.min(cursor, accepted[0].length())); }
                finally { restoring[0] = false; }
            }
        });
    }
}
