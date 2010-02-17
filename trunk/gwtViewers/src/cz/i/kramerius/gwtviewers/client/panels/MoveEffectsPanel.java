package cz.i.kramerius.gwtviewers.client.panels;

import java.util.ArrayList;
import java.util.Collections;

import org.adamtacy.client.ui.effects.transitionsphysics.LinearTransitionPhysics;
import org.adamtacy.client.ui.effects.transitionsphysics.TransitionPhysics;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import cz.i.kramerius.gwtviewers.client.panels.fx.Rotate;
import cz.i.kramerius.gwtviewers.client.panels.utils.CalculationHelper;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotateCalculatedPositions;
import cz.i.kramerius.gwtviewers.client.panels.utils.ImageRotatePool;
import cz.i.kramerius.gwtviewers.client.selections.Selector;
import cz.i.kramerius.gwtviewers.client.selections.SelectorImpl;

/**
 * FX panel for moving pictures
 * @author pavels
 */
public class MoveEffectsPanel extends  Composite {
	
	private ImageRotatePool imageRotatePool;
	private ImageRotateCalculatedPositions imageRotateCalculatedPositions;
	private AbsolutePanel absolutePanel = new AbsolutePanel();
	private ViewConfiguration configuration;

	private Selector imgSelector;
	private ArrayList<MoveListener> listeners = new ArrayList<MoveListener>();
	
	public MoveEffectsPanel(ImageMoveWrapper[] viewPortImages, 
							ImageMoveWrapper[] noVisibleImgs, 
							ImageMoveWrapper left, 
							ImageMoveWrapper right,   
							ViewConfiguration conf) {
		super();

		this.imageRotateCalculatedPositions = new ImageRotateCalculatedPositions(viewPortImages, noVisibleImgs, left, right);
		this.imageRotatePool = new ImageRotatePool(viewPortImages, noVisibleImgs, left, right);
		
		this.configuration = conf;

		this.calulateNextPositions();
		this.storeCalculatedPositions();
		
		for (int i = 0; i < viewPortImages.length; i++) {
			ImageMoveWrapper img = viewPortImages[i];
			ImageMoveWrapper calculated = this.imageRotateCalculatedPositions.getViewPortCalulcatedPosition(i);
			img.setX(calculated.getX());
			img.setY(calculated.getY());
			this.absolutePanel.add(img.getWidget(), img.getX(), img.getY());
		}
		
		
		ImageMoveWrapper leftSideImage = this.imageRotatePool.getLeftSideImage();
		this.absolutePanel.add(leftSideImage.getWidget(), leftSideImage.getX(), leftSideImage.getY());
		

		
		ImageMoveWrapper rightSideImage = this.imageRotatePool.getRightSideImage();
		this.absolutePanel.add(rightSideImage.getWidget(), rightSideImage.getX(), rightSideImage.getY());

		for (int i = 0; i < noVisibleImgs.length; i++) {
			ImageMoveWrapper img = noVisibleImgs[i];
			this.absolutePanel.add(img.getWidget(), img.getX(), img.getY());


		}		
		this.absolutePanel.setWidth(this.configuration.getViewPortWidth()+"px");
		this.absolutePanel.setHeight(this.configuration.getViewPortHeight()+"px");
		
		SimplePanel gpane = new SimplePanel();
		gpane.setWidth(this.configuration.getViewPortWidth()+"px");
		gpane.setHeight(this.configuration.getViewPortHeight()+"px");
		this.absolutePanel.add(gpane, 0, 0);
		gpane.getElement().getStyle().setZIndex(ImageRotatePool.PANE_VIEW_Z_INDEX);
		
		//this.imgSelector = new MiddleImgSelector(configuration.getCenterWidth());

		initWidget(this.absolutePanel);
	}
	

	
	public Rotate moveLeft(double duration) {
		ArrayList<ImageMoveWrapper> viewPortImages = this.imageRotatePool.getViewPortImages();
		boolean rollLeft = this.imageRotatePool.rollLeft();
		if (rollLeft) {
			this.calulateNextPositions();
			Rotate left = new Rotate(this.configuration, this.imageRotatePool,this.imageRotateCalculatedPositions, viewPortImages);
			left.initCompositeEffect();
			left.setDuration(duration);
			left.play();
			this.storeCalculatedPositions();
			fireMoveLeft(true);
			return left;
		} else {
			return null;
		}
		
	}
	
	void fireMovePointerLeft() {
		for (MoveListener listener : this.listeners) {
			listener.onPointerLeft(this.imageRotatePool);
		}
	}

	void fireMovePointerRight() {
		for (MoveListener listener : this.listeners) {
			listener.onPointerRight(this.imageRotatePool);
		}
	}
	
	void fireMoveLeft(boolean effectsPlayed) {
		for (MoveListener listener : this.listeners) {
			listener.onMoveLeft(this.imageRotatePool, effectsPlayed);
		}
	}

	void fireMoveRight(boolean effectsPlayed ) {
		for (MoveListener listener : this.listeners) {
			listener.onMoveRight(this.imageRotatePool,effectsPlayed);
		}
	}
	
	public void moveLeftOnlyPointer() {
		this.imageRotatePool.rollLeftPointer();
		this.fireMovePointerLeft();
	}
	
	public void rollToPointer() {
		this.imageRotatePool.initWithPointer(this.imageRotatePool.getPointer());
	}
	
	public void moveRightOnlyPointer() {
		this.imageRotatePool.rollRightPointer();
		this.fireMovePointerRight();
	}
	
	public void moveRightWithoutEffect() {
		boolean rollRight = this.imageRotatePool.rollRight();
		if (rollRight) {
			this.calulateNextPositions();
			this.storeCalculatedPositions();
			fireMoveRight(false);
		}
	}

	public void moveLeftWithoutEffect() {
		boolean rollLeft = this.imageRotatePool.rollLeft();
		if (rollLeft) {
			this.calulateNextPositions();
			this.storeCalculatedPositions();
			fireMoveLeft(false);
		}
	}
	

	public Rotate moveRight(double duration) {
		ArrayList<ImageMoveWrapper> viewPortImages = this.imageRotatePool.getViewPortImages();
			boolean rollRight = this.imageRotatePool.rollRight();
			if (rollRight) {
				this.calulateNextPositions();
				Rotate right = new Rotate(this.configuration, this.imageRotatePool,this.imageRotateCalculatedPositions, viewPortImages);
				right.initCompositeEffect();
				right.setDuration(duration);
				right.play();
				this.storeCalculatedPositions();
				fireMoveRight(true);
				return right;
			} else {
				return null;
			}
		
	}
	
	public ImageRotatePool getRotatePool() {
		return this.imageRotatePool;
	}

	public void calulateNextPositions() {
		CalculationHelper.computePositions(this.imageRotatePool, this.imageRotateCalculatedPositions,this.configuration);
	}


	public void storeCalculatedPositions() {
		CalculationHelper.storePositions(this.imageRotatePool, this.imageRotateCalculatedPositions, this.configuration);
	}

	public void debugViewPort(String where) {
		System.out.println("--"+where);
		ArrayList<ImageMoveWrapper> imgs = this.imageRotatePool.getViewPortImages();
		for (ImageMoveWrapper img : imgs) {
			System.out.println("\t"+img.getX()+","+img.getY()+"["+img.getImageIdent()+"]");
		}
	}



	public boolean canMoveLeft() {
		return this.imageRotatePool.canRollLeft();
	}
	
	public boolean canMoveRight() {
		return this.imageRotatePool.canRollRight();
	}



	public void rollToPage(int currentValue, double duration,boolean playEffect) {
		System.out.println("Roll to page "+currentValue);
		System.out.println("Play effect "+playEffect);
		System.out.println("Pointer points to "+this.imageRotatePool.getPointer());
		int pocetKroku = currentValue - this.imageRotatePool.getPointer();
		if (pocetKroku > 0) {
			for (int i = 0; i < pocetKroku; i++) { 
				if (playEffect)  {
					moveLeft(duration);
				} else  {
					moveLeftOnlyPointer();
					//moveLeftWithoutEffect();
				}
			}
		}
		if (pocetKroku < 0) {
			for (int i = pocetKroku; i < 0; i++) { 
				if (playEffect) {
					moveRight(duration);
				}  else {
					moveRightOnlyPointer();
					//moveRightWithoutEffect();
				}
			}
		}
	}
	
	
	public void addMoveListener(MoveListener listner) {
		this.listeners.add(listner);
	}

	public void removeMoveListener(MoveListener listener) {
		this.listeners.remove(listener);
	}
	
	public Selector getImgSelector() {
		if (imgSelector == null) {
			this.imgSelector = new SelectorImpl();
		}
		return imgSelector;
	}



	public void setImgSelector(Selector imgSelector) {
		this.imgSelector = imgSelector;
	}
	
}
