package main.encoder;

import main.display.DisplayableModule;
import main.encoder.processor.GaborFilterFactory;
import main.encoder.processor.IGaborFilter;
import main.interfaces.IEncoder;
import main.utils.ImageData;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by Magda on 04/07/2017.
 */
public class OpenCVEncoder extends DisplayableModule implements IEncoder {

//    https://cvtuts.wordpress.com/2014/04/27/gabor-filters-a-practical-overview/ -> gabor filter parameters info
//    http://docs.opencv.org/3.0-beta/modules/imgproc/doc/filtering.html

    //TODO learn about phase information
    //    https://cvtuts.wordpress.com/2014/04/27/gabor-filters-a-practical-overview/ -> gabor filter parameters info

    public OpenCVEncoder() {
        super(moduleName);
    }

    private List<Mat> results;

    public List<Mat> getResults() {
        return results;
    }

    @Override
    public ByteCode encode(ImageData imageData) {
        assert imageData.getNormMat() != null;
        assert imageData.getFilterConstants() != null;
        assert imageData.getGaborFilterType() != null;

        Mat image = imageData.getNormMat();

        IGaborFilter gaborFilter = GaborFilterFactory.getFilter(imageData);
        results = gaborFilter.process(image);

        display.displayIf(image, displayTitle("original image"), 2);
        Mat lastResult = results.get(results.size() - 1);
/*
        for (Mat result : results)
            display.displayIf(result, displayTitle("gabor filter " + results.indexOf(result)), 2);
*/

        display.displayIf(lastResult, displayTitle("gabor filter"), 2);

        Mat displayMat = new Mat(image.width(), image.cols(), image.type());
        Imgproc.threshold(lastResult, displayMat, 255 / 2, 255, Imgproc.THRESH_BINARY);

        ByteCode code = new ByteCode(displayMat);

        display.displayIf(displayMat, displayTitle("binarised image"), 2);
        display.displayIf(code.toDisplayableMat(), displayTitle("code"), 2);

        return code;
    }
}
