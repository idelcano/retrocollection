/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package proyecto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 *
 * @author idelcano
 */

@XmlRootElement (name="personalizado")
@XmlAccessorType(XmlAccessType.FIELD)
public class Personalizado {
    private String estrellas;
    private String lotengo;
    private String jugado;
    private String rutaimagen;

    public Personalizado() {
    }

    @Override
    public String toString() {
        return "Personalizado{" + "estrellas=" + estrellas + ", lotengo=" + lotengo + ", jugado=" + jugado + ", rutaimagen=" + rutaimagen + '}';
    } 
    public String getEstrellas() {
        return estrellas;
    }

    public void setEstrellas(String estrellas) {
        this.estrellas = estrellas;
    } 
//    @XmlElement(name="lotengo")
    
    public String getLotengo() {
        return lotengo;
    }

    public void setLotengo(String lotengo) {
        this.lotengo = lotengo;
    }
    public String getJugado() {
        return jugado;
    }

    public void setJugado(String jugado) {
        this.jugado = jugado;
    }

    public String getRutaimagen() {
        return rutaimagen;
    }

    public void setRutaimagen(String rutaimagen) {
        this.rutaimagen = rutaimagen;
    }
}
