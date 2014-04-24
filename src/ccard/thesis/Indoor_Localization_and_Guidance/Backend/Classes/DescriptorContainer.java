package ccard.thesis.Indoor_Localization_and_Guidance.Backend.Classes;

import android.graphics.Bitmap;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.Descriptor;
import ccard.thesis.Indoor_Localization_and_Guidance.Backend.Interfaces.ImageContainer;
import org.opencv.core.Mat;
import org.opencv.features2d.KeyPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris Card on 4/23/14.
 */
public class DescriptorContainer implements ImageContainer {

    private ArrayList<KeyPoint> keyPoints;
    private Mat descriptor;

    public DescriptorContainer(){
        keyPoints = new ArrayList<KeyPoint>();
    }

    @Override
    public KeyPoint getKeyPoint(int index) {
        return ((index >=0 && index < keyPoints.size()) ?
                keyPoints.get(index) : null);
    }

    @Override
    public ArrayList<KeyPoint> getKeyPoints() {
        return keyPoints;
    }

    @Override
    public Mat getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean hasImageToDraw() {
        return false;
    }

    @Override
    public Bitmap render(boolean withKeyPoints) {
        return null;
    }

    @Override
    public Bitmap renderComparision(ImageContainer im2, ArrayList<MyDMatch> matches) {
        return null;
    }

    @Override
    public boolean calcDescriptor(Descriptor des) {
        return false;
    }

    @Override
    public boolean setDescriptor(Mat descriptor) {
        if (descriptor.empty()) return false;
        descriptor.copyTo(this.descriptor);
        descriptor.release();
        return true;
    }

    @Override
    public boolean setKeypoints(List<KeyPoint> keyPoints) {
        if (keyPoints.isEmpty()) return false;
        this.keyPoints.clear();
        this.keyPoints.addAll(keyPoints);
        return true;
    }
}
