package com.michalrus.nofatty.ui;

import scala.Function1;

import javax.swing.*;
import java.awt.*;

// Why?
//
//02:07:41 < michalrus> So someone (Oracle, actually) wrote this in Java:
//02:07:46 < michalrus>     interface Interface<T> { void whoo(List<? extends T> list); }
//02:07:50 < michalrus>     class Clazz extends Interface<Object> { @Override void whoo(List<?> list) {} }
//02:07:58 < michalrus> No idea how `?` can override `? extends Object`. And scalac, when given this:
//02:08:02 < michalrus>     class MyClass extends Clazz { override def whoo(list: List[_]) {} }
//02:08:06 < michalrus> … complains:
//02:08:13 < michalrus>     class MyClass needs to be abstract, since method whoo in trait Interface of type (x$1: List[_ <: Object])Unit is not defined
//02:08:18 < michalrus> Is there any way out of this? =)
//02:22:13 < tpolecat> michalrus: does it work with [_ <: Object]?
//02:22:51 < michalrus> There’s another error then, let me generate it…
//02:24:51 < michalrus> tpolecat, oh, yeah, method whoo overrides nothing.
//02:25:50 < michalrus> And if I try to define both, there’s erasure error, since after erasure they both have the same type.
//02:27:45 < michalrus> I ended up writing this fragment in Java.
//02:39:47 < tpolecat> michalrus: yeah i can't figure out a way to do it. the overloading in the superclass is causing issues too
//02:42:17 < michalrus> Uh-huh… tpolecat, thank you for taking the time. :)
//02:42:55 < tpolecat> i do a lot of java interop and a lot of swing so i thought maybe i had seen this before, but i guess not
//
// #scala @ freenode

public final class DefaultListCellRendererModifier extends DefaultListCellRenderer {
    final Function1<Object, Object> modify;

    public DefaultListCellRendererModifier(Function1<Object, Object> modify) {
        this.modify = modify;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, modify.apply(value), index, isSelected, cellHasFocus);
    }
}
