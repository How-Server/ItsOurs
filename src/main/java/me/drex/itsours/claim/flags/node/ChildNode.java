package me.drex.itsours.claim.flags.node;

import net.minecraft.item.ItemConvertible;
import net.minecraft.text.MutableText;

public interface ChildNode extends Node {

    ItemConvertible getIcon();

    MutableText getDescription();

    String getId();

    boolean contains(ChildNode other);

}
