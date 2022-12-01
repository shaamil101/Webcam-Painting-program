import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Webcam-based drawing 
 * @author Nicolás Iair Schaievitch, Fall 2022, PS1
 * @author Shaamil Shaw Alem, Fall 2022, PS1
 */
public class CamPaintExtra extends Webcam {
	private char displayMode = 'w';			// what to display: 'w': live webcam, 'r': recolored image, 'p': painting
	private boolean paintingIsPaused = false; // when true, brush does not paint. Toggle with 'x'
	private RegionFinder finder;			// handles the finding
	private Color targetColor;          	// color of regions of interest (set by mouse press)
	private Color paintColor = Color.blue;	// the color to put into the painting from the "brush". Set with 'n'
	private BufferedImage painting;			// the resulting masterpiece
	private Color[] brushColors;
	private final int COLOR_CHANGE_DURATION = 15;
	private int step;

	/**
	 * Initializes the region finder and the drawing
	 */
	public CamPaintExtra() {
		finder = new RegionFinder();
		targetColor = new Color(0);
		brushColors = new Color[]{RegionFinder.randomColor(), RegionFinder.randomColor(),RegionFinder.randomColor()};
		step = 0;
		clearPainting();
	}

	/**
	 * Resets the painting to a blank image
	 */
	protected void clearPainting() {
		painting = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	/**
	 * DrawingGUI method, here drawing one of live webcam, recolored image, or painting, 
	 * depending on display variable ('w', 'r', or 'p')
	 */
	@Override
	public void draw(Graphics g) {
		if (displayMode == 'r' || displayMode == 'w') {
			g.drawImage(image, 0, 0, null);
		} else {
			g.drawImage(painting, 0, 0, null);
		}

	}

	/**
	 * Webcam method, here finding regions and updating the painting.
	 */
	@Override
	public void processImage() {
		finder.setImage(image);
		if (displayMode == 'r') {
			finder.findRegions(targetColor);
			finder.recolorImage();
			image = finder.getRecoloredImage();
		} else if (displayMode == 'p' && !paintingIsPaused) {
			finder.findRegions(targetColor);
			ArrayList<Point> brush = finder.largestRegion();
			if (brush == null) return;
			for (Point p : brush) {
				painting.setRGB(p.x, p.y, paintColor.getRGB());
			}
			// computes index in the brushColors array
			int colorIdx = step / COLOR_CHANGE_DURATION;
			// interpolates between the current and next color
			paintColor = interpolateColors(1 - (step % COLOR_CHANGE_DURATION) * 1.0 / COLOR_CHANGE_DURATION, brushColors[colorIdx % brushColors.length], brushColors[(colorIdx + 1) % brushColors.length]);
			// adds one and does mod. arithmetic to prevent it from growing indefinitely
			step = ++step % (COLOR_CHANGE_DURATION * brushColors.length);
		}
	}

	/**
	 * Overrides the DrawingGUI method to set the track color.
	 */
	@Override
	public void handleMousePress(int x, int y) {
		if (image == null) return;
		targetColor = new Color(image.getRGB(x, y));
	}

	/**
	 * DrawingGUI method, here doing various drawing commands
	 */
	@Override
	public void handleKeyPress(char k) {
		if (k == 'p' || k == 'r' || k == 'w') { // display: painting, recolored image, or webcam
			displayMode = k;
		}
		else if (k == 'c') { // clear
			clearPainting();
		}
		else if (k == 'o') { // save the recolored image
			saveImage(finder.getRecoloredImage(), "pictures/recolored.png", "png");
		}
		else if (k == 's') { // save the painting
			saveImage(painting, "pictures/painting.png", "png");
		} else if (k == 'x') { // pauses/resumes drawing
			paintingIsPaused = !paintingIsPaused;
		}
		else {
			System.out.println("unexpected key "+k);
		}
	}

	/**
	 * Linearly interpolates between two colors
	 * @param ratio the ratio of c1 : c2 to use
	 * @param c1 first color
	 * @param c2 second color
	 * @return interpolated color
	 */
	private static Color interpolateColors(double ratio, Color c1, Color c2) {
		return new Color((int)(ratio * c1.getRed() + (1 - ratio) * c2.getRed()), (int)(ratio * c1.getGreen() + (1 - ratio) * c2.getGreen()),(int)(ratio * c1.getBlue() + (1 - ratio) * c2.getBlue()));
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new CamPaintExtra();
			}
		});

	}
}
