package main.normaliser;

import main.display.DisplayableModule;
import main.encoder.processor.FilterConstants;
import main.interfaces.INormaliser;
import main.utils.Circle;
import main.utils.ImageData;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Magda on 30/06/2017.
 */
public class OpenCVNormaliser extends DisplayableModule implements INormaliser {
    //Daugman's rubber sheet model
    //https://en.wikipedia.org/wiki/Bilinear_interpolation
    //https://www.ripublication.com/gjbmit/gjbmitv1n2_01.pdf -> publication with equations for normalisation

    public OpenCVNormaliser() {
        super(moduleName);
    }

    private ImageData adjustIrisEdges(ImageData imageData) {
        int irisRadius = 90;
        int pupilRadius = 40;
        imageData.getFirstIrisCircle().setRadius(irisRadius);
        imageData.getFirstPupilCircle().setRadius(pupilRadius);


        if (imageData.getFirstPupilCircle().getRadius() > pupilRadius) {
            imageData.getFirstPupilCircle().setRadius(pupilRadius);
        }
        if (imageData.getFirstIrisCircle().getRadius() > irisRadius) {
            imageData.getFirstIrisCircle().setRadius(irisRadius);
        }

        return imageData;
    }

    @Override
    public ImageData normalize(ImageData imageData) {
        checkForInputErrors(imageData);

        Mat imageMat = imageData.getImageMat();
        int rows = FilterConstants.NORMALISED_HEIGHT;
        int cols = FilterConstants.NORMALISED_WIDTH;
        int type = imageData.getImageMat().type();
        int size = (int) (imageMat.total() * imageMat.step1(0));

        Mat normMat = new Mat(rows, cols, type);

        byte[] pxlArray = new byte[size];

        Circle pupil = imageData.getFirstPupilCircle();
        Circle iris = imageData.getFirstIrisCircle();

        for (int r = 0; r < rows; r++) {
            for (int th = 0; th < cols; th++) {
                Point p = CoordinateConverter.toXY(r, th, pupil, iris, cols, rows);

                if (withinBounds(p, imageMat)) {
                    imageMat.get((int) Math.round(p.x), (int) Math.round(p.y), pxlArray);
                    normMat.put(r, th, pxlArray);
                }
            }
        }

        Imgproc.equalizeHist(normMat, normMat);

        imageData.setNormMat(normMat);

        showNormalisedArea(imageData);

        display.displayIf(normMat, displayTitle("normalised"));
        return imageData;
    }

    private void checkForInputErrors(ImageData imageData) {
        assert imageData.getImageMat() != null;
        assert imageData.getImageMat().total() > 0;
        if (imageData.irisesFound() == 0)
            throw new UnsupportedOperationException("No iris found; can't normalise");
        if (imageData.pupilsFound() == 0)
            throw new UnsupportedOperationException("No pupil found; can't normalise");
    }

    private boolean withinBounds(Point p, Mat imageMat) {
        return p.x >= 0 && p.x < imageMat.width() && p.y >= 0 && p.y < imageMat.height();
    }

    private void showNormalisedArea(ImageData imageData) {
        Circle pupil = imageData.getFirstPupilCircle();
        Circle iris = imageData.getFirstIrisCircle();
        pupil.setX(iris.getX());
        pupil.setY(iris.getY());
        Mat image = imageData.getImageMat();

        display.displayIf(image, new Circle[]{pupil, iris}, displayTitle("area before normalisation"));
    }
}
