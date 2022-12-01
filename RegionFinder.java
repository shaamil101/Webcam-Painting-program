import javax.swing.plaf.synth.Region;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * Region growing algorithm: finds and holds regions in an image.
 * Each region is a list of contiguous points with colors similar to a target color.
 */
public class RegionFinder {
	private static final int maxColorDiff = 30;				// how similar a pixel color must be to the target color, to belong to a region
	private static final int minRegion = 50; 				// how many points in a region to be worth considering

	private BufferedImage image;                            // the image in which to find regions
	private BufferedImage visited;

	private BufferedImage recoloredImage;                   // the image with identified regions recolored

	private ArrayList<ArrayList<Point>> regions;			// a region is a list of points
															// so the identified regions are in a list of lists of points

	public RegionFinder() {
		this.image = null;
	}

	public RegionFinder(BufferedImage image) {
		visited = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		regions = new ArrayList<ArrayList<Point>>();
		this.image = image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		visited = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		regions = new ArrayList<ArrayList<Point>>();
	}

	public BufferedImage getImage() {
		return image;
	}

	public BufferedImage getRecoloredImage() {
		return recoloredImage;
	}

	/**
	 * Sets regions to the flood-fill regions in the image, similar enough to the trackColor.
	 */
	public void findRegions(Color targetColor) {
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				if (visited.getRGB(i, j) == 1) continue;

				if (colorMatch(targetColor, new Color(image.getRGB(i, j)))) {
					ArrayList<Point> region = floodFill(i, j, targetColor);
					if (region.size() >= minRegion) regions.add(region);
				}
			}
		}
	}

	public ArrayList<Point> floodFill(int i, int j, Color targetColor) {
		ArrayList<Point> region = new ArrayList<Point>();
		Queue<Point> neighbours = new ArrayDeque<Point>();

		neighbours.add(new Point(i, j));
		visited.setRGB(i, j, 1);

		while (!neighbours.isEmpty()) {
			Point p = neighbours.poll();


			if (colorMatch(targetColor, new Color(image.getRGB(p.x, p.y)))) {
				region.add(p);

				for (int x = Math.max(0, p.x - 1); x < Math.min(image.getWidth(), p.x + 2); x++)
					for (int y = Math.max(0, p.y - 1); y < Math.min(image.getHeight(), p.y + 2); y++){
						if (x == p.x && y == p.y) continue;
						if (visited.getRGB(x, y) == 0) {
							neighbours.add(new Point(x, y));
							visited.setRGB(x, y, 1);
						}
					}
			}

		}

		return region;

	}

	/**
	 * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold, which you can vary).
	 */
	private static boolean colorMatch(Color c1, Color c2) {
		return Math.abs(c1.getRed() - c2.getRed()) + Math.abs(c1.getGreen() - c2.getGreen()) + Math.abs(c1.getBlue() - c2.getBlue()) <= maxColorDiff;
	}

	/**
	 * Returns the largest region detected (if any region has been detected)
	 */
	public ArrayList<Point> largestRegion() {
		int largestRegionSize = -1;
		ArrayList<Point> largestRegionFound = null;

		for (ArrayList<Point> reg : regions) {
			if (reg.size() > largestRegionSize) {
				largestRegionSize = reg.size();
				largestRegionFound = reg;
			}
		}

		return largestRegionFound;
	}

	/**
	 * Generates a random RGB color
	 * @return a random color
	 */
	public static Color randomColor() {
		return new Color((int)(Math.random() * 256 * 256 * 256));
	}
	/**
	 * Sets recoloredImage to be a copy of image, 
	 * but with each region a uniform random color, 
	 * so we can see where they are
	 */
	public void recolorImage() {
		// First copy the original
		recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
		// Now recolor the regions in it

		for (ArrayList<Point> r : regions) {
			Color col = randomColor();
			for (Point p : r) {
				recoloredImage.setRGB(p.x, p.y, col.getRGB());
			}
		}

	}
}
