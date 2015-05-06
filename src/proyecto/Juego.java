/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package proyecto;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 *
 * @author idelcano
 */

@XmlRootElement (name="game")
//@XmlAccessorType(XmlAccessType.FIELD)
class Juego {
    private String clave;
    private String name;
    private String description;
    private String manufacturer;
    
  private List<Personalizado> personalizado= new ArrayList<Personalizado>(); 
    private String rating;
    private String year;
    private String genre;

    public Juego() {
    } 

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
    
//   @XmlElementWrapper(name="personalizado") 
    public List<Personalizado> getPersonalizado() {
        return personalizado;
    }

    public void setPersonalizado(List<Personalizado> personalizado) {
        this.personalizado = personalizado;
    }

    @Override
    public String toString() {
        return "Juego{" + "name=" + name + ", description=" + description + ", manufacturer=" + manufacturer + ", personalizado=" + personalizado.toString() + ", rating=" + rating + ", year=" + year + ", genre=" + genre + '}';
    }
 

    @XmlAttribute(name="name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
 
    
}
