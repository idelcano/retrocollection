/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package proyecto;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author idelcano
 */
 
@XmlRootElement (name="consola")
public class Maquina {

    public Maquina() {
    }
    String clave;
    String file;
    String nombre;
    String compania;
    String europa; 
    String eeuu; 
    String japon;
    String vidacomercial;
    String ventastotales;
    String juegomaspopular;
    String soporte;
    String procesador;
    String ram;
    String otrosdatosdeinteres;
    String rutaimagen;

    public Maquina(String file, String nombre, String compania, String europa, String eeuu, String japon, String vidacomercial, String ventastotales, String juegomaspopular, String soporte, String procesador, String ram, String otrosdatosdeinteres, String rutaimagen) {
        this.file = file;
        this.nombre = nombre;
        this.compania = compania;
        this.europa = europa;
        this.eeuu = eeuu;
        this.japon = japon;
        this.vidacomercial = vidacomercial;
        this.ventastotales = ventastotales;
        this.juegomaspopular = juegomaspopular;
        this.soporte = soporte;
        this.procesador = procesador;
        this.ram = ram;
        this.otrosdatosdeinteres = otrosdatosdeinteres;
        this.rutaimagen = rutaimagen;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
    
    public String getFile() {
        return file;
    }


    public void setFile(String file) {
        this.file = file;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCompania() {
        return compania;
    }

    public void setCompania(String compania) {
        this.compania = compania;
    }

    public String getEuropa() {
        return europa;
    }

    public void setEuropa(String europa) {
        this.europa = europa;
    }

    public String getEeuu() {
        return eeuu;
    }

    public void setEeuu(String eeuu) {
        this.eeuu = eeuu;
    }

    public String getJapon() {
        return japon;
    }

    public void setJapon(String japon) {
        this.japon = japon;
    }

    public String getVidacomercial() {
        return vidacomercial;
    }

    public void setVidacomercial(String vidacomercial) {
        this.vidacomercial = vidacomercial;
    }

    public String getVentastotales() {
        return ventastotales;
    }

    public void setVentastotales(String ventastotales) {
        this.ventastotales = ventastotales;
    }

    public String getJuegomaspopular() {
        return juegomaspopular;
    }

    public void setJuegomaspopular(String juegomaspopular) {
        this.juegomaspopular = juegomaspopular;
    }

    public String getSoporte() {
        return soporte;
    }

    public void setSoporte(String soporte) {
        this.soporte = soporte;
    }

    public String getProcesador() {
        return procesador;
    }

    public void setProcesador(String procesador) {
        this.procesador = procesador;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getOtrosdatosdeinteres() {
        return otrosdatosdeinteres;
    }

    public void setOtrosdatosdeinteres(String otrosdatosdeinteres) {
        this.otrosdatosdeinteres = otrosdatosdeinteres;
    }

    public String getRutaimagen() {
        return rutaimagen;
    }

    public void setRutaimagen(String rutaimagen) {
        this.rutaimagen = rutaimagen;
    }
    public String[][] toConsultar(){
    String[][] parametros={{"ruta",file},
        {"nombre",nombre},{"compania",compania},{"europa",europa},{"eeuu",eeuu},{"japon",japon},{"vidacomercial",vidacomercial},{"ventastotales",ventastotales},{"juegomaspopular",juegomaspopular},{"soporte",soporte},{"procesador",procesador},{"ram",ram},{"otrosdatosdeinteres",otrosdatosdeinteres},{"rutaimagen",rutaimagen}};
    
    return parametros;
    }

    @Override
    public String toString() {
        return "Maquina{" + "file=" + file + ", nombre=" + nombre + ", compania=" + compania + ", europa=" + europa + ", eeuu=" + eeuu + ", japon=" + japon + ", vidacomercial=" + vidacomercial + ", ventastotales=" + ventastotales + ", juegomaspopular=" + juegomaspopular + ", soporte=" + soporte + ", procesador=" + procesador + ", ram=" + ram + ", otrosdatosdeinteres=" + otrosdatosdeinteres + ", rutaimagen=" + rutaimagen + '}';
    }
    
}
