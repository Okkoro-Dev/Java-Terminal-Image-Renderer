import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

public class Main {

    private static final BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(System.out), 64 * 1024
    );
    // NOTE: Buffered writer is a hell lot faster because it minimize syscall to only every 64KB

    private  static boolean validateImageFile(File file) {

        // Basic existence check
        if (!file.exists() || !file.isFile()) {
            System.err.println(" Error: File not found -> " + file.getAbsolutePath());
            return false;
        }

        // Header validation via ImageIO (checks file signature)
        try (ImageInputStream iis = ImageIO.createImageInputStream(file)) {

            if (iis == null) {
                System.err.println(" Error: Cannot read file stream -> " + file.getName());
                return false;
            }

            Iterator readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                System.err.println(" Error: File is not a supported image format -> " + file.getName());
                return false;
            }

        } catch (Exception e) {
            System.err.println(" Error reading image header: " + e.getMessage());
            return false;
        }

        return true;
    }

    public static BufferedImage scaleImage(BufferedImage original, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = resized.createGraphics();
    
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
    
        return resized;
    }

    // pre-load char "0"-"255" so we don't need to call Integer.toString() at all 
    private static final String[] ColourLookUp = new String[256]; static {
        for (int i = 0; i < 256; i++) {
            ColourLookUp[i] = Integer.toString(i);
        }
    }

    public static void main(String[] args) throws IOException {
        // Check if the argument exist
        if (args.length == 0) {
            System.err.println(" Usage: java main <path-to-image> [width] [height]");
            return;
        }

        File imageFile = new File(args[0]);

        // Check if the file exist or valid
        if (!validateImageFile(imageFile)){
            return;
        }

        BufferedImage image = ImageIO.read(imageFile);

        // customize image width, if argument left blank, default to 80 char wide
        int targetWidth  = (args.length > 1) ? Integer.parseInt(args[1]) : 80;
        
        // if the height argument is null, set it to a number that follow the image ratio
        int targetHeight = (args.length > 2) ? Integer.parseInt(args[2]) : 
            Math.round((float)image.getHeight()/image.getWidth() * targetWidth
        );

        image = scaleImage(image, targetWidth, targetHeight);

        int width     = image.getWidth();
        int height    = image.getHeight();
        int imageSize = width * height;

        int[] pixels = new int[imageSize];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        int lastFg;
        int lastBg;
        boolean isFirstPixel;

        for (int y = 0; y < height; y += 2) {
            boolean hasBottomRow = y + 1 < height;
            
            lastFg = 0;
            lastBg = 0;
            isFirstPixel = true;
            
            for (int x = 0; x < width; x++) {
                int fgIndex  = y * width + x;
                int bgIndex = hasBottomRow ? (y + 1) * width + x : fgIndex;

                int argbTop    = pixels[fgIndex];
                int argbBottom = pixels[bgIndex];

                int aTop    = (argbTop >>> 24);
                int aBottom = (argbBottom >>> 24);

                // end iteration early if the pixel (both top & bottom) is transparent
                if (aTop == 0 && aBottom == 0) {
                    if (lastFg != 0 || lastBg != 0) {
                        writer.write("\u001B[0m ");
                        lastFg = 0;
                        lastBg = 0;
                    } else {
                        writer.write(' ');
                    }
                    continue;
                }

                // if the color of both pixel is the same as before, use previous color information
                if (argbTop == lastFg && argbBottom == lastBg && !isFirstPixel) {
                    writer.write(aTop > 0 ? '\u2580' : '\u2584');
                    continue;
                }

                writer.write("\u001B[38;2;");
                writer.write(aTop > 0 ? ColourLookUp[((argbTop >> 16) & 0xFF)] : ColourLookUp[((argbBottom >> 16) & 0xFF)]);
                writer.write(';');
                writer.write(aTop > 0 ? ColourLookUp[((argbTop >> 8) & 0xFF)] : ColourLookUp[((argbBottom >> 8) & 0xFF)]);
                writer.write(';');
                writer.write(aTop > 0 ? ColourLookUp[(argbTop & 0xFF)] : ColourLookUp[(argbBottom & 0xFF)]);

                if (aBottom > 0 && aTop > 0) {
                    writer.write(";48;2;");
                    writer.write(ColourLookUp[((argbBottom >> 16) & 0xFF)]); writer.write(';');
                    writer.write(ColourLookUp[((argbBottom >> 8) & 0xFF)]);  writer.write(';');
                    writer.write(ColourLookUp[(argbBottom & 0xFF)]);
                } else {
                    writer.write(";49"); 
                }

                writer.write('m');
                writer.write(aTop > 0 ? '\u2580' : '\u2584');
                
                // before next iteration, record current FG & BG ARGB value to be use in the next iteration
                lastFg = aTop > 0 ? argbTop : argbBottom;
                lastBg = aBottom > 0 && aTop > 0 ? argbBottom : 0;
                isFirstPixel = false;
            }
            writer.write("\u001B[0m\n");
        }
        writer.flush();
    }
}