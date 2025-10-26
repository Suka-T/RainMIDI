package gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

abstract class RainControl implements MouseListener, MouseMotionListener {
    abstract boolean onPress(MouseEvent e);
}
