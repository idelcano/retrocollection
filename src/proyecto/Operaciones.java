/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author idelcano
 */
public class Operaciones {

    public Operaciones() {
    } 

    public static ImageIcon redimensionarImagenes(String imgruta,int ancho,int alto) throws IOException {
        Image img = null;
        try {
            img = ImageIO.read(new File(imgruta));

        } catch (IOException e) {
//            e.printStackTrace();
        }
        final float FACTOR = 4f;
        BufferedImage img2 = ImageIO.read(new File(imgruta));
        int scaleX = (int) (img2.getWidth() * FACTOR);
        int scaleY = (int) (img2.getHeight() * FACTOR);
        Image image = img.getScaledInstance(ancho, alto, Image.SCALE_SMOOTH);
        BufferedImage buffered = new BufferedImage(ancho, alto, Image.SCALE_SMOOTH);
        buffered.getGraphics().drawImage(image, 0, 0, null);
        ImageIcon imageIcon = new ImageIcon(buffered);
        return imageIcon;
    }
}
