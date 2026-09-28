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

        // Header validation via ImageIO (checks actual file signature)
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

            System.err.println(" Usage: java main <path-to-image> [target-width]");
            return;
        }

        File imageFile = new File(args[0]);

        // Check if the file exist or valid
        if (!validateImageFile(imageFile)){
            return;
        }

        BufferedImage image = ImageIO.read(imageFile);

        int targetWidth = (args.length > 1) ? Integer.parseInt(args[1]) : 80;
        int targetHeight = Math.round((float)image.getHeight()/image.getWidth() * targetWidth);

        image = scaleImage(image, targetWidth, targetHeight);

        int width     = image.getWidth();
        int height    = image.getHeight();
        int imageSize = width * height;

        int redOffset   = 0 * imageSize;
        int greenOffset = 1 * imageSize;
        int blueOffset  = 2 * imageSize;
        int alphaOffset = 3 * imageSize;

        byte[] pixelData = new byte[4 * imageSize];

        for (int y = 0; y < height; y++) {

            int rowOffset = y * width;
            for (int x = 0; x < width; x++) {

                int rgb = image.getRGB(x, y);

                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8)  & 0xFF;
                int b =  rgb        & 0xFF;

                int index = rowOffset + x;
                
                pixelData[alphaOffset + index] = (byte) a;
                pixelData[redOffset   + index] = (byte) r;
                pixelData[greenOffset + index] = (byte) g;
                pixelData[blueOffset  + index] = (byte) b;
            }
        }

        for (int y = 0; y < height; y += 2) {

            boolean hasBottomRow = (y + 1 < height);

            for (int x = 0; x < width; x++) {

                int index  = y * width + x;
                int index2 = hasBottomRow ? (y + 1) * width + x : index;
                
                // Alpha information
                int a     = pixelData[alphaOffset + index]  & 0xFF;
                int a2    = hasBottomRow ? pixelData[alphaOffset + index2] & 0xFF : 0;

                // Primary (Foreground) colour
                int r     = pixelData[redOffset   + index] & 0xFF;
                int g     = pixelData[greenOffset + index] & 0xFF;
                int b     = pixelData[blueOffset  + index] & 0xFF;

                // Secondary (Background) colour
                int r2    = pixelData[redOffset   + index2] & 0xFF;
                int g2    = pixelData[greenOffset + index2] & 0xFF;
                int b2    = pixelData[blueOffset  + index2] & 0xFF;


                if (a == 0 && a2 == 0){
                    writer.write("\u001B[0m");
                    writer.write(' ');
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