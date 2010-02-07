package cz.i.kramerius.gwtviewers.client.panels.utils;

import java.util.ArrayList;

import cz.i.kramerius.gwtviewers.client.DataHandler;
import cz.i.kramerius.gwtviewers.client.SimpleImageTO;
import cz.i.kramerius.gwtviewers.client.panels.ImageMoveWrapper;

public class NoVisibleFillHelper {

	public static void fillRightNoVisible(ImageRotatePool pool) {
		// ukazovatko pozice 
		int current = pool.getPointer();
		// levy je pozice -1
		int left = current-1;
		int right = pool.getPointer()+ pool.getViewPortSize();
		ArrayList<ImageMoveWrapper> nvis = pool.getNoVisibleImages();
		int size = nvis.size();
		int maxIndex = size / 2; maxIndex += size % 2;
		for (int i = 1; i <= maxIndex; i++) {
			int loadingIndex = right + i;
			int nvisPosition = size -i;
			if (loadingIndex < DataHandler.getMax()) {
				SimpleImageTO sit = DataHandler.getData().get(loadingIndex);
				modifyImageMoveWrapper(nvis.get(nvisPosition), sit, "nvis_right_"+nvisPosition);
			} else {
				SimpleImageTO sit = DataHandler.getNaImage();
				modifyImageMoveWrapper(nvis.get(nvisPosition), sit, "nvis_right_"+nvisPosition);
			}
		}
	}
	
	public static void fillLeftNoVisible(ImageRotatePool pool) {
		int current = pool.getPointer();
		int left = current-1;
		ArrayList<ImageMoveWrapper> nvis = pool.getNoVisibleImages();
		int size = nvis.size();
		int maxIndex = size / 2;
		for (int i = 1; i <= maxIndex; i++) {
			int loadingIndex = left - i;
			int nvisPosition = i-1;
			if (loadingIndex >=0) {
				SimpleImageTO sit = DataHandler.getData().get(loadingIndex);
				modifyImageMoveWrapper(nvis.get(nvisPosition), sit, "nvis_left_"+nvisPosition);
			} else {
				SimpleImageTO sit = DataHandler.getNaImage();
				modifyImageMoveWrapper(nvis.get(nvisPosition), sit, "nvis_left_"+nvisPosition);
			}
		}
	}
	
	public static void modifyImageMoveWrapper(ImageMoveWrapper wrapper, SimpleImageTO ito, String id) {
		wrapper.setWidth(ito.getWidth());
		wrapper.setHeight(ito.getHeight());
		wrapper.setFirst(ito.isFirstPage());
		wrapper.setLast(ito.isLastPage());
		wrapper.setUrl(ito.getUrl());
		wrapper.setImageIdent(ito.getIdentification());
		wrapper.setIndex(ito.getIndex());
	}

}
