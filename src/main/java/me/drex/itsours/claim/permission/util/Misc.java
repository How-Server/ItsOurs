package me.drex.itsours.claim.permission.util;

import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.permission.node.ChildNode;
import me.drex.itsours.claim.permission.node.Node;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;

import java.util.function.Predicate;

public enum Misc {

    ELYTRA("elytra", Items.ELYTRA, changeContext -> ItsOurs.checkPermission(changeContext.source(), "itsours.itsours.elytra", 2));

    private final ChildNode node;

    Misc(String id, ItemConvertible icon) {
        this(id, icon, changeContext -> true);
    }

    Misc(String id, ItemConvertible icon, Predicate<Node.ChangeContext> predicate) {
        this.node = Node.literal(id).icon(icon).description("permission.misc." + id).predicate(predicate).build();
    }

    public ChildNode node() {
        return node;
    }

}
