
package com.ikalagaming.gui;

import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;

/**
 * A custom caret that looks better than the default one. This code is from
 * http://www.java2s.com/Code/Java/Swing-JFC/Fanciercustomcaretclass.htm
 *
 * @author Ches Burks
 *
 */
public class MyCaret extends DefaultCaret {

	private static final long serialVersionUID = -389070432822516041L;

	@Override
	protected synchronized void damage(Rectangle r) {
		super.damage(r);
		if (r == null)
			return;

		// give values to x,y,width,height (inherited from java.awt.Rectangle)
		x = r.x;
		y = r.y;
		height = r.height;
		// A value for width was probably set by paint(), which we leave alone.
		// But the first call to damage() precedes the first call to paint(), so
		// in this case we must be prepared to set a valid width, or else
		// paint()
		// will receive a bogus clip area and caret will not get drawn properly.
		if (width <= 0)
			width = getComponent().getWidth();

		repaint(); // calls getComponent().repaint(x, y, width, height)
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		JTextComponent comp = getComponent();
		if (comp == null)
			return;

		int dot = getDot();
		Rectangle r = null;
		char dotChar;
		try {
			r = comp.modelToView(dot);
			if (r == null)
				return;
			dotChar = comp.getText(dot, 1).charAt(0);
		}
		catch (BadLocationException e) {
			return;
		}

		if ((x != r.x) || (y != r.y)) {
			// paint() has been called directly, without a previous call to
			// damage(), so do some cleanup. (This happens, for example, when
			// the
			// text component is resized.)
			repaint(); // erase previous location of caret
			x = r.x; // Update dimensions (width gets set later in this method)
			y = r.y;
			height = r.height;
		}
		if (!(g.getColor() == comp.getCaretColor())) {
			g.setColor(comp.getCaretColor());
		}
		g.setXORMode(comp.getBackground()); // do this to draw in XOR mode

		if (dotChar == '\n') {
			if (isVisible())
				g.fillRect(r.x, r.y, width, r.height);
			width = r.height / 2 + 2;
			return;
		}

		if (dotChar == '\t') {
			try {
				Rectangle nextr = comp.modelToView(dot + 1);
				if ((r.y == nextr.y) && (r.x < nextr.x)) {
					width = nextr.x - r.x;
					if (isVisible())
						g.fillRect(r.x, r.y, width, r.height);
					width = r.height / 2 + 2;
					return;
				}
				else
					dotChar = ' ';
			}
			catch (BadLocationException e) {
				dotChar = ' ';
			}
		}

		width = g.getFontMetrics().charWidth(dotChar);
		if (isVisible())
			g.fillRect(r.x, r.y, width, r.height);

	}
}
