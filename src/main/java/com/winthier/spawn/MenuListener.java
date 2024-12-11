package com.winthier.spawn;

import com.cavetale.core.menu.MenuItemEntry;
import com.cavetale.core.menu.MenuItemEvent;
import com.cavetale.mytems.Mytems;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

@RequiredArgsConstructor
public final class MenuListener implements Listener {
    private final SpawnPlugin plugin;
    public static final String MENU_KEY = "spawn:spawn";
    public static final String MENU_PERMISSION = "spawn.spawn";

    protected void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onMenuItem(MenuItemEvent event) {
        if (!event.getPlayer().hasPermission(MENU_PERMISSION)) {
            return;
        }
        event.addItem(builder -> builder
                      .priority(MenuItemEntry.Priority.HOTBAR)
                      .key(MENU_KEY)
                      .command("spawn")
                      .icon(Mytems.STAR.createIcon(List.of(text("Spawn", GOLD)))));
    }
}
