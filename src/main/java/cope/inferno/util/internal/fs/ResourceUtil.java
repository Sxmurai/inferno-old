package cope.inferno.util.internal.fs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ResourceUtil {
    public static InputStream getResourceStream(String location) {
        return ResourceUtil.class.getResourceAsStream(location);
    }

    public static BufferedImage getResourceAsImage(String location) {
        InputStream inputStream = getResourceStream(location);
        if (inputStream == null) {
            return null;
        }

        try {
            BufferedImage image = ImageIO.read(inputStream);
            inputStream.close();
            return image;
        } catch (IOException e) {
            return null;
        }
    }
}
