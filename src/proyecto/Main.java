/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package proyecto;
 
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 *
 * @author idelcano
 */
public class Main {
    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) { 
        try {
            // TODO code application logic here
//            Consultas.primerInicio();
//            Consultas.generarClaves();
            new NewJFrame1().setVisible(true);
        } catch (IOException ex) {
            System.out.println("HA habido un error fatal");
        }
    }

}
