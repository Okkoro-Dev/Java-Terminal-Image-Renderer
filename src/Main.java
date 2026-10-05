import java.io.File;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Iterator;

public class Main {

    private static final BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(System.out), 64 * 1024
    );

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
    
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
    
        return resized;
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

        int targetWidth  = (args.length > 1) ? Integer.parseInt(args[1]) : 80;
        int targetHeight = (args.length > 2) ? Integer.parseInt(args[2]) : Math.round((float)image.getHeight()/image.getWidth() * targetWidth);

        image = scaleImage(image, targetWidth, targetHeight);

        int width     = image.getWidth();
        int height    = image.getHeight();
        int imageSize = width * height;

        int alphaOffset = 0 * imageSize;
        int redOffset   = 1 * imageSize;
        int greenOffset = 2 * imageSize;
        int blueOffset  = 3 * imageSize;

        int[] pixels = new int[imageSize];
        image.getRGB(0, 0, width, height, pixels, 0, width);
        
        byte[] pixelData = new byte[4 * imageSize];

        for (int i = 0; i < imageSize; i++) {
            int rgb = pixels[i];

            pixelData[alphaOffset + i] = (byte) (rgb >> 24);
            pixelData[redOffset   + i] = (byte) (rgb >> 16);
            pixelData[greenOffset + i] = (byte) (rgb >> 8);
            pixelData[blueOffset  + i] = (byte) rgb;
        }


        for (int y = 0; y < height; y += 2) {
            boolean hasBottomRow = (y + 1 < height);

            for (int x = 0; x < width; x++) {
                int index  = y * width + x;
                int index2 = hasBottomRow ? (y + 1) * width + x : index;

                int prevIndex  = x != 0 ? y * width + x - 1 : 0;
                int prevIndex2 = hasBottomRow ? (y + 1) * width + x - 1 : 0;

                // Primary (Foreground) colour
                int a      = pixelData[alphaOffset + index] & 0xFF;
                int r      = pixelData[redOffset   + index] & 0xFF;
                int g      = pixelData[greenOffset + index] & 0xFF;
                int b      = pixelData[blueOffset  + index] & 0xFF;

                // Secondary (Background) colour
                int a2     = pixelData[alphaOffset + index2] & 0xFF;
                int r2     = pixelData[redOffset   + index2] & 0xFF;
                int g2     = pixelData[greenOffset + index2] & 0xFF;
                int b2     = pixelData[blueOffset  + index2] & 0xFF;

                // Previous pixel data
                int prevA  = pixelData[alphaOffset + prevIndex] & 0xFF;
                int prevR  = pixelData[redOffset   + prevIndex] & 0xFF;
                int prevG  = pixelData[greenOffset + prevIndex] & 0xFF;
                int prevB  = pixelData[blueOffset  + prevIndex] & 0xFF;

                int prevA2 = pixelData[alphaOffset + prevIndex2] & 0xFF;
                int prevR2 = pixelData[redOffset   + prevIndex2] & 0xFF;
                int prevG2 = pixelData[greenOffset + prevIndex2] & 0xFF;
                int prevB2 = pixelData[blueOffset  + prevIndex2] & 0xFF;

                // Use previous colour information
                if (x > 0 
                    && prevR == r && prevG == g && prevB == b && prevA == a 
                    && prevR2 == r2 && prevG2 == g2 && prevB2 == b2 && prevA2 == a2)
                {
                    if (a == 0 && a2 == 0){
                        writer.write("\u001B[0m ");
                        continue;
                    }
                    
                    writer.write("\u2580");
                    continue; 
                }

                if (a == 0 && a2 == 0) {
                    writer.write("\u001B[0m ");
                    continue;
                }

                writer.write("\u001B[38;2;");
                writer.write(Integer.toString(r)); writer.write(';');
                writer.write(Integer.toString(g)); writer.write(';');
                writer.write(Integer.toString(b));

                if (a2 == 0) {
                    writer.write('m');
                    writer.write('\u2580');
                    continue;
                }

                writer.write(";48;2;");
                writer.write(Integer.toString(r2)); writer.write(';');
                writer.write(Integer.toString(g2)); writer.write(';');
                writer.write(Integer.toString(b2)); writer.write('m');

                writer.write('\u2580');
            }
            writer.write("\u001B[0m\n");
        }
        writer.flush();
    }
}