package napplet;

import static java.awt.event.FocusEvent.FOCUS_GAINED;
import static java.awt.event.FocusEvent.FOCUS_LOST;
import static processing.core.PConstants.ARGB;

import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;

public class NitManager {

	List<Nit> nitList = new ArrayList<Nit>();
	List<Nit> killList = new ArrayList<Nit>();

	PApplet parentPApplet;

	Nit focusNit;
	Nit mouseNit;
	public int mouseX, mouseY;

	public NitManager(PApplet pap) {
		super();
		parentPApplet = pap;
		parentPApplet.registerPre(this);
		parentPApplet.registerDraw(this);
		parentPApplet.registerMouseEvent(this);
		parentPApplet.registerKeyEvent(this);
	}

	public void addNit(Nit nit) {
		nit.setParentPApplet(parentPApplet);
		nitList.add(nit);
		nit.setup();
	}

	public Nit containingNit(int x, int y) {
		Nit containingNit = null;
		for (Nit nit : nitList) {
			if (nit.isEmbedded()) {
				int xRel = x - nit.getPositionX();
				int yRel = y - nit.getPositionY();
				if (xRel >= 0 && yRel >= 0 && xRel < nit.getWidth()
						&& yRel < nit.getHeight()) {
					containingNit = nit;
				}
			}
		}
		return containingNit;
	}

	public void pre() {

	}

	public void draw() {
		for (Nit nit : nitList) {
			nit.runFrame();
		}
		while (killList.size() > 0) {
			nitList.remove(killList.get(0));
			killList.remove(0);
		}
	}

	void passMouseEvent(Nit nit, MouseEvent event) {
		event.translatePoint(-(nit.getPositionX()), -(nit.getPositionY()));
		nit.passMouseEvent(event);
	}

	public void mouseEvent(MouseEvent event) {
		mouseX = event.getX();
		mouseY = event.getY();

		Nit nit = containingNit(mouseX, mouseY);
		if ((event.getID() == java.awt.event.MouseEvent.MOUSE_DRAGGED)
				&& (mouseNit != null)) {
			passMouseEvent((NApplet) mouseNit, event);
		} else if (nit != null) {
			passMouseEvent((NApplet) nit, event);
			mouseNit = nit;
			if (nit != focusNit) {
				Nit focusGainingNit = (nit instanceof NApplet) ? nit : null;
				Nit focusLosingNit = (focusNit instanceof NApplet) ? focusNit
						: null;
				FocusEvent gainFocus = new FocusEvent(
						(NApplet) focusGainingNit, FOCUS_GAINED, false,
						(NApplet) focusLosingNit);
				FocusEvent loseFocus = new FocusEvent((NApplet) focusLosingNit,
						FOCUS_LOST, false, (NApplet) focusGainingNit);

				nit.focusGained(gainFocus);
				
				if (focusNit != null)
					focusNit.focusLost(loseFocus);
				focusNit = nit;
			}
		}
	}

	public void keyEvent(KeyEvent event) {
		if (focusNit != null) {
			focusNit.passKeyEvent(event);
		}
	}

	public void createNApplet(String nappletClassName, int x, int y) {
		createEmbeddedNApplet(nappletClassName, x, y);
	}

	public void createEmbeddedNApplet(String nappletClassName, int x, int y) {
		NApplet nap = NApplet.createNApplet(parentPApplet, nappletClassName,
				this);
		if (nap != null) {
			nap.initEmbeddedNApplet(parentPApplet, x, y);
			addNit(nap);
			nap.g.format = ARGB;
		}
	}

	public void createWindowedNApplet(String nappletClassName, int x, int y) {
		NApplet nap = NApplet.createNApplet(parentPApplet, nappletClassName,
				this);
		if (nap != null) {
			@SuppressWarnings("unused")
			NFrame nFrame = new NFrame(parentPApplet, nap, x, y);
			addNit(nap);
		}
	}

	public void killNit(Nit nit) {
		killList.add(nit);
	}

}
