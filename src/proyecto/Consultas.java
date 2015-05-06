package proyecto;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQDataSource;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQExpression;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultItem;
import javax.xml.xquery.XQResultSequence;
import javax.xml.xquery.XQStaticContext;
import net.xqj.exist.ExistXQDataSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author idelcano
 */
class Consultas {

    static XQDataSource xqjd;
    static XQConnection xqc;

    private static final String RUTACONSOLAS = "/db/RetroExists/consolas.xml";
    private static final String RUTAROOT = "/db/RetroExists/";

    /**
     * Get the value of RUTACONSOLAS
     *
     * @return the value of RUTACONSOLAS
     */
    private static XQConnection crearconexion(String host, String puerto, String admin, String pass) throws XQException {
        xqjd = new ExistXQDataSource();
        xqjd.setProperty("serverName", host);
        xqjd.setProperty("port", puerto);
        xqjd.setProperty("user", admin);
        xqjd.setProperty("password", pass);
        xqc = xqjd.getConnection();
        return xqc;
    }

    public static void conectar() {
        try {
            xqc = crearconexion("localhost", "8080", "admin", "abc123.");
        } catch (XQException ex) {
            System.out.println("No hay conexion con la base de datos");
        }
    }

    public static void desconectar() {
        try {
            xqc.close();
        } catch (XQException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String consultarMaquina(String nombreMaquina) {
        String xmlmaquina = "";
        ArrayList<String> resultado = null;
        try {
            String[][] todasconsolas = {{"ruta", RUTACONSOLAS}, {"consola", nombreMaquina}};
            String consulta = "declare variable $ruta external;"
                    + "declare variable $consola external;"
                    + "for $c in doc($ruta )//consola[nombre=$consola] \n"
                    + "return   $c ";
            resultado = ejecutarConsulta(consulta, todasconsolas);

            for (String resulta : resultado) {
                xmlmaquina = resulta;
            }
        } catch (XQException ex) {
            System.out.println("Error consultando las consolas disponibles");
        } finally {
            desconectar();
        }
        return xmlmaquina;
    }

    private static String consultarJuego(String ruta, String nombreJuego) {
        String xmljuego = "";
        ArrayList<String> resultado = null;
        try {
//            System.out.println("Buscando:"+nombreJuego+" en "+ RUTAROOT+ruta);
            String[][] parametros = {{"ruta", RUTAROOT + ruta}, {"filtro", nombreJuego}};
            String consulta = "declare variable $ruta external;"
                    + "declare variable $filtro external;"
                    + "for $c in doc($ruta )//menu/game[@name=\"" + nombreJuego + "\"]\n"
                    + "return   $c ";
//            System.out.println(consulta);
            resultado = ejecutarConsulta(consulta, parametros);
            for (String resulta : resultado) {
                xmljuego = resulta;
            }
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error consultando el juego");
        } finally {
            desconectar();
        }
        return xmljuego;
    }

    
    /**
     * Ejecuta consultas
     *
     * @return devuelve el arraylist con el valor de atomicvalue() lo uso para devolver "nodos" xml
     */
    public static ArrayList<String> ejecutarConsulta3(String cadenaAConsultar, String[][] claveValor) throws XQException {
        conectar();
        XQPreparedExpression consulta;
        ArrayList<String> lista = new ArrayList<String>();
        consulta = xqc.prepareExpression(cadenaAConsultar);

        System.out.println("Inicio consulta \n");
        System.out.println(cadenaAConsultar);
        System.out.println("fin consulta \n");
        for (int i = 0; i < claveValor.length; i++) {
            consulta.bindString(new QName(claveValor[i][0]), claveValor[i][1], null);
        }
        XQResultSequence resultado = consulta.executeQuery();
//        System.out.println(resultado.toString());
//            XQResultItem item;
//            while (resultado.next()) {
//                item = (XQResultItem) resultado.getItem();
        String cadena = resultado.getAtomicValue();
//                String cadena= resultado.get;
//                System.out.println(cadena);
//                if (!cadena.equals("") && cadena != null) {
        lista.add(cadena);
//                }
//            } 
//            System.out.println(resultado.getSequenceAsString(null));
//            System.out.println(lista.size());
        return lista;
    }

    public static Juego traerJuego(String ruta, String nombrejuego) {

        String xml = consultarJuego(ruta, nombrejuego);
//        System.out.println(xml);
        Juego juego = null;
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));

            NodeList errNodes = doc.getElementsByTagName("error");
            if (errNodes.getLength() > 0) {
                Element err = (Element) errNodes.item(0);
                System.out.println(err.getElementsByTagName("errorMessage")
                        .item(0).getTextContent());
            } else {
                // success
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(Juego.class);

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            juego = (Juego) jaxbUnmarshaller.unmarshal(doc);

        } catch (SAXException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            desconectar();
        }
        return juego;
    }

    
    /**
     * Carga el jlist de juegos
     *
     * 
     */
    private static ArrayList<String> consultarJuegosjlist(String ruta, String filtro) {
        String xmljuegos = "";
        ArrayList<String> resultado = null;
        try {
            filtro = filtro.toLowerCase();
            String orden = "";
            String vuelta = "{data($c/../@name)}";
            if (filtro.contains("desc")) {
                filtro = filtro.replace("desc", "");
                orden = "descending";
            }
            filtro = filtro.trim();
            switch (filtro) {
                case "año":
                    filtro = "year";
                    break;
                case "genero":
                    filtro = "genre";
                    break;
                case "nombre":
                    filtro = "@name";
                    break;
                case "puntuacion":
                    filtro = "personalizado/estrellas";
                    vuelta = "{data($c/../../@name)}";
                    break;
                case "desarrollador":
                    filtro = "manufacturer";
                    break;
                case "jugado":
                    filtro = "personalizado \n where $c/jugado=\"Si\"";
                    vuelta = " {data($c/../@name)} ";
                    break;
                case "no jugado":
                    filtro = "personalizado \n where $c/jugado=\"No\"";
                    vuelta = " {data($c/../@name)} ";
                    break;
                case "lo tengo":
                    filtro = "personalizado \n where $c/lotengo=\"Si\"";
                    vuelta = " {data($c/../@name)} ";
                    break;
                case "no lo tengo":
                    filtro = "personalizado \n where $c/lotengo=\"No\"";
                    vuelta = " {data($c/../@name)} ";
                    break;
            }
//            System.out.println("Filtro:"+filtro);
            String[][] todasconsolas = {{"ruta", RUTAROOT + ruta}, {"orden", orden}};
            //No se porque las variables order by no la coje supongo que por ser elementos y no valores.
            String consulta = "declare variable $ruta external;\n"
                    + "declare variable $orden external;\n"
                    + "for $c in doc($ruta)//menu/game/" + filtro + " \n"
                    + "order by $c " + orden + " "
                    + "return <name>" + vuelta + "</name>";
//            System.out.println(consulta);
            resultado = ejecutarConsulta2(consulta, todasconsolas);

            for (String resulta : resultado) {
//                System.out.println(resulta);
                xmljuegos = resulta;
            }
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error consultando los juegos disponibles");
        } finally {
            desconectar();
        }
        return resultado;
    }

    /**
     * Ejecuta consultas
     *
     * @return devuelve el arraylist con los valores de getItemasString
     */
    public static ArrayList<String> ejecutarConsulta2(String cadenaAConsultar, String[][] claveValor) throws XQException {
        conectar();
        XQPreparedExpression consulta;
        ArrayList<String> lista = new ArrayList<String>();
        consulta = xqc.prepareExpression(cadenaAConsultar);

        System.out.println("Inicio consulta \n");
        System.out.println(cadenaAConsultar);
        System.out.println("fin consulta \n");
        for (int i = 0; i < claveValor.length; i++) {
            consulta.bindString(new QName(claveValor[i][0]), claveValor[i][1], null);
        }
        XQResultSequence resultado = consulta.executeQuery();
//        System.out.println(resultado.toString());
        XQResultItem item;
        while (resultado.next()) {
            item = (XQResultItem) resultado.getItem();
            String cadena = item.getItemAsString(null);
//                String cadena= resultado.get;
//                System.out.println(cadena);
//                if (!cadena.equals("") && cadena != null) {
            lista.add(cadena);
//                }
        }
//            System.out.println(resultado.getSequenceAsString(null));
        return lista;
    }

    /**
     * Recoge los nombres de juegos
     *
     * @return devuelve el arraylist con el valor de los nombres de juegos
     */
    private static ArrayList<String> buscarNombresDeJuegos(String ruta, String filtro) {
        ArrayList<String> juegos = new ArrayList<String>();
        ArrayList<String> juegosaux;

        juegosaux = consultarJuegosjlist(ruta, filtro);
        for (int i = 0; i < juegosaux.size(); i++) {
            String juego = juegosaux.get(i);
            juego = juego.replace("<name>", "");
            juego = juego.replace("</name>", "");
            juegos.add(juego);
        }
        return juegos;
    }

    static void borrarJuego(String ruta, Juego juego) {
        String consulta = "update delete doc(\"" + RUTAROOT + ruta + "\")//game[clave=\"" + juego.getClave() + "\"]";
        conectar();
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + juego.getName());
        } finally {
            desconectar();
        }
    }

    static void añadirJuego(String ruta, Juego juego) {

        String consulta = "let $c :=  doc(\"" + RUTAROOT + ruta + "\")//menu/game  \n"
                + "return \n"
                + "update insert  \n"
                + "<game name=\"" + juego.getName() + "\" index=\"\" image=\"\">\n"
                + "        <description>" + juego.getDescription() + "</description>\n"
                + "        <cloneof/>\n"
                + "        <manufacturer>" + juego.getManufacturer() + "</manufacturer>\n"
                + "        <personalizado>\n"
                + "            <estrellas>" + juego.getPersonalizado().get(0).getEstrellas() + "</estrellas>\n"
                + "            <lotengo>" + juego.getPersonalizado().get(0).getLotengo() + "</lotengo>\n"
                + "            <jugado>" + juego.getPersonalizado().get(0).getJugado() + "</jugado>\n"
                + "            <rutaimagen>" + juego.getPersonalizado().get(0).getRutaimagen() + "</rutaimagen>\n"
                + "        </personalizado>\n"
                + "        <rating>" + juego.getRating() + "</rating>\n"
                + "        <year>" + juego.getYear() + "</year>\n"
                + "        <genre>" + juego.getGenre() + "</genre>\n"
                + "<clave>{max($c/clave/text())+1 }</clave>\n"
                + "    </game>\n"
                + "into doc(\"" + RUTAROOT + ruta + "\")//menu";
        conectar();
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + juego.getName());
        } finally {
            desconectar();
        }
    }

    static void editarJuego(String ruta, Juego juego) {
        conectar();
        String valor = juego.getDescription();
        String filtro = "";
        String id = juego.getClave();

        valor = juego.getName();
        filtro = "@name";
//            String[][] parametros = {{"ruta", RUTAROOT+ruta},{"nombre",juego.getName()},{"valor",juego.getDescription()},{"filtro",filtro}};
        String consulta = "update replace doc(\"" + RUTAROOT + ruta + "\")//menu/game[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with \"" + valor + "\"";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = juego.getDescription();
        filtro = "description";
//            String[][] parametros = {{"ruta", RUTAROOT+ruta},{"nombre",juego.getName()},{"valor",juego.getDescription()},{"filtro",filtro}};
        consulta = "update replace doc(\"" + RUTAROOT + ruta + "\")//menu/game[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <" + filtro + ">" + valor + "</" + filtro + ">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        valor = juego.getManufacturer();
        filtro = "manufacturer";
        consulta = "update replace doc(\"" + RUTAROOT + ruta + "\")//menu/game[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <" + filtro + ">" + valor + "</" + filtro + ">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        valor = juego.getPersonalizado().get(0).getEstrellas();

        filtro = "personalizado/estrellas";
        consulta = "update replace doc(\"" + RUTAROOT + ruta + "\")//menu/game[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <estrellas>" + valor + "</estrellas>";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        valor = juego.getPersonalizado().get(0).getJugado();

        filtro = "personalizado/jugado";
        consulta = "update replace doc(\"" + RUTAROOT + ruta + "\")//menu/game[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <jugado>" + valor + "</jugado>";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        valor = juego.getPersonalizado().get(0).getLotengo();

        filtro = "personalizado/lotengo";
        consulta = "update replace doc(\"" + RUTAROOT + ruta + "\")//menu/game[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <lotengo>" + valor + "</lotengo>";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        valor = juego.getPersonalizado().get(0).getRutaimagen();

        filtro = "personalizado/rutaimagen";
        consulta = "update replace doc(\"" + RUTAROOT + ruta + "\")//menu/game[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <rutaimagen>" + valor + "</rutaimagen>";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = juego.getRating();

        filtro = "rating";
        consulta = "update replace doc(\"" + RUTAROOT + ruta + "\")//menu/game[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <" + filtro + ">" + valor + "</" + filtro + ">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = juego.getYear();

        filtro = "year";
        consulta = "update replace doc(\"" + RUTAROOT + ruta + "\")//menu/game[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <" + filtro + ">" + valor + "</" + filtro + ">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = juego.getGenre();

        filtro = "genre";
        consulta = "update replace doc(\"" + RUTAROOT + ruta + "\")//menu/game[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <" + filtro + ">" + valor + "</" + filtro + ">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        desconectar();
    }

    static void editarMaquina(Maquina maquina) {
        conectar();

        String id = maquina.getClave();
        String valor = "";
        String filtro = "";
        valor = maquina.getCompania();
        filtro = "compania";
        String consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        valor = maquina.getNombre();
        filtro = "nombre";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        valor = maquina.getEuropa();
        filtro = "europa";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = maquina.getEeuu();
        filtro = "eeuu";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = maquina.getJapon();
        filtro = "japon";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = maquina.getJuegomaspopular();
        filtro = "juegomaspopular";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = maquina.getVidacomercial();
        filtro = "vidacomercial";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = maquina.getOtrosdatosdeinteres();
        filtro = "otrosdatosdeinteres";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = maquina.getProcesador();
        filtro = "procesador";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = maquina.getRam();
        filtro = "ram";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }

        valor = maquina.getSoporte();
        filtro = "soporte";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        valor = maquina.getRutaimagen();
        filtro = "rutaimagen";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        valor = maquina.getVentastotales();
        filtro = "ventastotales";
        consulta = "update replace doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + id + "\"]/" + filtro + " \n"
                + " with <"+filtro+">" + valor + "</"+filtro+">";
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + filtro);
        }
        desconectar();
    }

    static void añadirMaquina(Maquina maquina) {

        String consulta = "let $c :=  doc(\"" + RUTACONSOLAS + "\")//consolas/consola  \n"
                + "return \n"
                + "update insert  \n"
                + "  <consola>\n"
                + "        <nombre>" + maquina.getNombre() + "</nombre>\n"
                + "        <compania>" + maquina.getCompania() + "</compania>\n"
                + "        <europa>" + maquina.getEuropa() + "</europa>\n"
                + "        <eeuu>" + maquina.getEeuu() + "</eeuu>\n"
                + "        <japon>" + maquina.getJapon() + "</japon>\n"
                + "        <vidacomercial>" + maquina.getVidacomercial() + "</vidacomercial>\n"
                + "        <ventastotales>" + maquina.getVentastotales() + "</ventastotales>\n"
                + "        <juegomaspopular>" + maquina.getJuegomaspopular() + "</juegomaspopular>\n"
                + "        <soporte>" + maquina.getSoporte() + "</soporte>\n"
                + "        <procesador>" + maquina.getProcesador() + "</procesador>\n"
                + "        <ram>" + maquina.getRam() + "</ram>\n"
                + "        <otrosdatosdeinteres>" + maquina.getOtrosdatosdeinteres() + "</otrosdatosdeinteres>\n"
                + "        <rutaimagen>" + maquina.getRutaimagen() + "\n"
                + "</rutaimagen>\n"
                + "        <file>" + maquina.getFile() + "</file>\n"
                + "<clave>{max($c/clave/text())+1 }</clave>\n"
                + "    </consola>\n"
                + "into doc(\"" + RUTACONSOLAS + "\")//consolas";
        conectar();
        try {
            ejecutarConsultaU(consulta);
          consulta = 
                 "declare namespace exist = \"http://exist.sourceforge.net/NS/exist\"; \n"
                + "declare namespace request=\"http://exist-db.org/xquery/request\"; \n"
                + "    declare namespace xmldb=\"http://exist-db.org/xquery/xmldb\";  \n"
                + "    declare variable $file as xs:string { \""
                + "<menu>\n"
                + "    <maquina>\n"
                + "        <listname>"+maquina.getNombre()+"</listname>\n"
                + "    </maquina>\n"
                + "    <game name=\'0\'>\n"
                + "        <description></description>\n"
                + "        <cloneof/>\n"
                + "        <manufacturer></manufacturer>\n"
                + "        <personalizado>\n"
                + "            <estrellas>0</estrellas>\n"
                + "            <lotengo>No</lotengo>\n"
                + "            <jugado>No</jugado>\n"
                + "            <rutaimagen/>\n"
                + "        </personalizado>\n"
                + "        <rating></rating>\n"
                + "        <year></year>\n"
                + "        <genre></genre>\n"
                + "        <clave>1</clave>\n"
                + "    </game>\n"
                + "</menu>\""
                + " };  \n"
                + "    declare variable $name as xs:string { \""+maquina.getFile()+"\" };  \n"
                + "    declare variable $collection as xs:string { \""+RUTAROOT+"\" };  \n"
                + "      \n"
                + "    <results>  \n"
                + "    {  \n"
                + "    let $load-status := xmldb:store($collection, $name, $file)  \n"
                + "    return <load-status> { $load-status } </load-status>  \n"
                + "    }  \n"
                + "    </results>  ";
            
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en la actualizacion de " + maquina.getNombre());
        } finally {
            desconectar();
        }

    }

    static void borrarMaquina(String text) {
    String consulta = "update delete doc(\"" + RUTACONSOLAS + "\")//consola[clave=\"" + text + "\"]";
        conectar();
        try {
            ejecutarConsultaU(consulta);
        } catch (XQException ex) {
            ex.printStackTrace();
            System.out.println("Error en el borrado de " + text);
        } finally {
            desconectar();
        }
    }

    public Consultas() {
    }

    public static Maquina traerMaquina(String nombreMaquina) {
        String xml = consultarMaquina(nombreMaquina);
        Maquina maquina = null;
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));

            NodeList errNodes = doc.getElementsByTagName("error");
            if (errNodes.getLength() > 0) {
                Element err = (Element) errNodes.item(0);
                System.out.println(err.getElementsByTagName("errorMessage")
                        .item(0).getTextContent());

            } else {
                // success
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(Maquina.class
            );

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            maquina = (Maquina) jaxbUnmarshaller.unmarshal(doc);

        } catch (SAXException ex) {
            Logger.getLogger(Consultas.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Consultas.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(Consultas.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Consultas.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return maquina;
    }

    public static ComboBoxModel poblarComboboxMaquinas(JComboBox jComboBox2) {

        ArrayList<String> lista;
        lista = consultarNombresConsolas();
        jComboBox2.setModel(new DefaultComboBoxModel());
        for (Object item : lista) {
            jComboBox2.addItem(item);
        }
        try {
            Operaciones.redimensionarImagenes(consultarImagenConsola(jComboBox2.getSelectedItem().toString()).trim(), 200, 200);
        } catch (IOException ex) {
            System.out.println("error convirtiendo imagen:" + consultarImagenConsola(jComboBox2.getSelectedItem().toString()) + "finImagen");
        }

        return jComboBox2.getModel();

    }

    public static void poblarJlistJuegos(String ruta, JComboBox filtro, JList jList1) {
        DefaultListModel modeloLista = new DefaultListModel();
        ArrayList<String> lista = buscarNombresDeJuegos(ruta, filtro.getSelectedItem().toString());

        for (Object item : lista) {
            modeloLista.addElement(item);
        }
        jList1.setModel(modeloLista);
//        try {
//            Operaciones.redimensionarImagenes(consultarImagenConsola(jComboBox2.getSelectedItem().toString()).trim());
//        } catch (IOException ex) {
//            System.out.println("error convirtiendo imagen:" + consultarImagenConsola(jComboBox2.getSelectedItem().toString())+"finImagen");
//        }

    }

    /**
     * Método que crea un nuevo xml cuando creamos una nueva maquina
     */
    /**
     * Método Que ejecuta la consulta y le añade sus variables pasandoselas en
     * un string[clave][valor], sirve para ejecutar cualquier consulta
     *
     * @return devuelve el resultado en formato XQResultSequence por si queremos
     * tratarlo.
     */
    public static ArrayList<String> ejecutarConsulta(String cadenaAConsultar, String[][] claveValor) throws XQException {
        conectar();
        XQPreparedExpression consulta;
        ArrayList<String> lista;
        consulta = xqc.prepareExpression(cadenaAConsultar);

        System.out.println("Inicio consulta \n");
        System.out.println(cadenaAConsultar);
        System.out.println("fin consulta \n");
        for (int i = 0; i < claveValor.length; i++) {
            consulta.bindString(new QName(claveValor[i][0]), claveValor[i][1], null);
        }
        XQResultSequence resultado = consulta.executeQuery();
//        System.out.println(resultado.toString());
        lista = consultaToArraylist(resultado);
        return lista;
    }

    /**
     * Ejecuta consultas de actualizacion, inserccion, o borrado.
     *
     * @return 
     */
    public static void ejecutarConsultaU(String cadenaAConsultar) throws XQException {
//        conectar();
//        desconectar(); Como van a ser llamadas en bloques los pongo en el metodo que llama a ejecutarconsultaU para no hacer tantas reconexiones
        XQExpression consulta;
        consulta = xqc.createExpression();
        System.out.println("Inicio consulta \n");
        System.out.println(cadenaAConsultar);
        System.out.println("fin consulta \n");
        consulta.executeCommand(cadenaAConsultar);
    }

    public static ArrayList<String> consultaToArraylist(XQResultSequence resultado) {
        ArrayList<String> lista = new ArrayList<String>();
        try {
//        System.out.println("Mostrar resultado de mostrar articulos:");
            XQResultItem item;
            while (resultado.next()) {
                item = (XQResultItem) resultado.getItem();
                String cadena = item.getItemAsString(null);
                cadena = cadena.replace("xmlns=\"\"", "");
                if (!cadena.equals("") && cadena != null) {
                    lista.add(cadena);
                }
            }
//        System.out.println("---------");
        } catch (XQException ex) {

            System.out.println("Error recorriendo resultado");
        } finally {
            desconectar();
        }
        return lista;
    }

    public static ArrayList<String> consultarNombresConsolas() {
        ArrayList<String> resultado = null;
        try {
            String[][] todasconsolas = {{"ruta", RUTACONSOLAS}};
            String consulta = "declare variable $ruta external;"
                    + "for $c in doc($ruta)\n"
                    + "return data($c//nombre)";
            resultado = ejecutarConsulta(consulta, todasconsolas);
        } catch (XQException ex) {
            System.out.println("Error consultando las consolas disponibles");
        } finally {
            desconectar();
        }

        return resultado;
    }

    public static String consultarImagenConsola(String consola) {
        String rutaImagen = "";
        ArrayList<String> resultado = null;
        try {
            String[][] todasconsolas = {{"ruta", RUTACONSOLAS}, {"consola", consola}};
            String consulta = "declare variable $ruta external;"
                    + "declare variable $consola external;"
                    + " \n"
                    + "for $c in doc($ruta)\n"
                    + "return data($c//rutaimagen[../nombre=$consola])";
            resultado = ejecutarConsulta(consulta, todasconsolas);

            for (String resulta : resultado) {
                rutaImagen = resulta;
            }
        } catch (XQException ex) {
            System.out.println("Error consultando las consolas disponibles");
        } finally {
            desconectar();
        }
        return rutaImagen;
    }

    public static ArrayList<String> modificarMaquina(Maquina maquina) {
        ArrayList<String> resultado = null;
        String consulta = "declare variable $ruta external;"
                + "declare variable $nombre external;"
                + "declare variable $compania external;"
                + "declare variable $europa external;"
                + "declare variable $eeuu external;"
                + "declare variable $japon external;"
                + "declare variable $vidacomercial external;"
                + "declare variable $ventastotales external;"
                + "declare variable $juegomaspopular external;"
                + "declare variable $soporte external;"
                + "declare variable $procesador external;"
                + "declare variable $ram external;"
                + "declare variable $otrosdatosdeinteres external;"
                + "declare variable $rutaimagen external;"
                + "                + \"    <consola>\\n\"\n"
                + "                + \"        <nombre>$nombre</nombre>\\n\"\n"
                + "                + \"        <compania>$compania</compania>\\n\"\n"
                + "                + \"        <europa>$europa</europa>\\n\"\n"
                + "                + \"        <eeuu>$eeuu</eeuu>\\n\"\n"
                + "                + \"        <japon>$japon</japon>\\n\"\n"
                + "                + \"        <vidacomercial>$vidacomercial</vidacomercial>\\n\"\n"
                + "                + \"        <ventastotales>$ventastotales</ventastotales>\\n\"\n"
                + "                + \"        <juegomaspopular>$juegomaspopular</juegomaspopular>\\n\"\n"
                + "                + \"        <soporte>$soporte</soporte>\\n\"\n"
                + "                + \"        <procesador>$procesador</procesador>\\n\"\n"
                + "                + \"        <ram>$ram</ram>\\n\"\n"
                + "                + \"        <otrosdatosdeinteres>$otrosdatosdeinteres</otrosdatosdeinteres>\\n\"\n"
                + "                + \"        <rutaimagen>$rutaimagen</rutaimagen>\\n\"\n"
                + "                + \"    </consola>\\n\""
                + "";
        try {
            resultado = ejecutarConsulta(consulta, maquina.toConsultar());
        } catch (XQException ex) {
            System.out.println("Error creando fichero de maquina");
        } finally {
            desconectar();
        }
        return resultado;
    }

    /**
     * Método que crea la consulta para añadir una nueva maquina( creando su
     * archivo de juegos correspondiente).
     * Ejecutado en el ide de exits.
     */
    public static ArrayList<String> nuevaMaquinafile(Maquina maquina) {
        ArrayList<String> resultado = null;
        String consulta = "declare variable $ruta external;"
                + "declare variable $nombre external;"
                + "declare namespace exist = \"http://exist.sourceforge.net/NS/exist\"; \n"
                + "declare namespace request=\"http://exist-db.org/xquery/request\"; \n"
                + "    declare namespace xmldb=\"http://exist-db.org/xquery/xmldb\";  \n"
                + "    declare variable $file as xs:string { "
                + "<menu>\n"
                + "    <maquina>\n"
                + "        <listname>$nombre</listname>\n"
                + "    </maquina>\n"
                + "    <game name=\"\" index=\"true\" image=\"b\">\n"
                + "        <description></description>\n"
                + "        <cloneof/>\n"
                + "        <manufacturer></manufacturer>\n"
                + "        <personalizado>\n"
                + "            <estrellas>0</estrellas>\n"
                + "            <lotengo>No</lotengo>\n"
                + "            <jugado>No</jugado>\n"
                + "            <rutaimagen/>\n"
                + "        </personalizado>\n"
                + "        <rating></rating>\n"
                + "        <year></year>\n"
                + "        <genre></genre>\n"
                + "    </game>\n"
                + "</menu>"
                + " };  \n"
                + "    declare variable $name as xs:string { $ruta };  \n"
                + "    declare variable $collection as xs:string { \""+RUTAROOT+"\" };  \n"
                + "      \n"
                + "    <results>  \n"
                + "    {  \n"
                + "(:    let $collection-status :=   :)\n"
                + "(:      if(not(xmldb:collection-exists($collection))) then   :)\n"
                + "(:        xmldb:create-collection(\"\", $collection)  :)\n"
                + "(:      else (\"Collection already exists.\")  :)\n"
                + "(:    return <collection-status> { $collection-status } </collection-status>  :)\n"
                + "(:    ,  :)\n"
                + "    let $load-status := xmldb:store($collection, $name, $file)  \n"
                + "    return <load-status> { $load-status } </load-status>  \n"
                + "    }  \n"
                + "    </results>  ";
        try {
            resultado = ejecutarConsulta(consulta, maquina.toConsultar());
        } catch (XQException ex) {
            System.out.println("Error creando fichero de maquina");
        } finally {
            desconectar();
        }
        return resultado;
    }

    /**
     * Método que transforma los xml que listan juegos encontrados en la red
     * para adaptarlos a nuestra aplicacion
     */
    public static void primerInicio() {
        conectar();

        String consulta = ""
                + "update rename doc(\"/db/RetroExists/sonyplaystation2.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/sonyplaystation2.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/sonyplaystation2.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/sonyplaystation2.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/sonyplaystation2.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/sonyplaystation2.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/sonyplaystation2.xml\")//genre,\n"
                + "update rename doc(\"/db/RetroExists/snkneogeopocket.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocket.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocket.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocket.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocket.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocket.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/snkneogeopocket.xml\")//genre,\n"
                + "update rename doc(\"/db/RetroExists/segacd.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/segacd.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/segacd.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/segacd.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/segacd.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/segacd.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/segacd.xml\")//genre,\n"
                + "update rename doc(\"/db/RetroExists/msdos.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/msdos.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/msdos.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/msdos.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/msdos.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/msdos.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/msdos.xml\")//genre,\n"
                + "\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/snkneogeopocketcolor.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocketcolor.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocketcolor.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocketcolor.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocketcolor.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocketcolor.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/snkneogeopocketcolor.xml\")//genre,\n"
                + "\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/snkneogeopocket.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocket.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocket.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocket.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocket.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeopocket.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/snkneogeopocket.xml\")//genre,\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/snkneogeocd.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/snkneogeocd.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeocd.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeocd.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeocd.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/snkneogeocd.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/snkneogeocd.xml\")//genre,\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/segasaturn.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/segasaturn.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/segasaturn.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/segasaturn.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/segasaturn.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/segasaturn.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/segasaturn.xml\")//genre,\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/segamegacd.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/segamegacd.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/segamegacd.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/segamegacd.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/segamegacd.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/segamegacd.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/segamegacd.xml\")//genre,\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/segamastersystem.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/segamastersystem.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/segamastersystem.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/segamastersystem.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/segamastersystem.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/segamastersystem.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/segamastersystem.xml\")//genre,\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/segagenesis.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/segagenesis.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/segagenesis.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/segagenesis.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/segagenesis.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/segagenesis.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/segagenesis.xml\")//genre,\n"
                + "\n"
                + "\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/segadreamcast.xml\")//header as \"maquina\",\n"
                + "update rename doc(\"/db/RetroExists/segadreamcast.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/segadreamcast.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/segadreamcast.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/segadreamcast.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/segadreamcast.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/segadreamcast.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/segadreamcast.xml\")//genre,\n"
                + "update rename doc(\"/db/RetroExists/segadreamcast.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/nintendogamecube.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/nintendogamecube.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/nintendogamecube.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/nintendogamecube.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/nintendogamecube.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/nintendogamecube.xml\")//genre,\n"
                + "update rename doc(\"/db/RetroExists/nintendods.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/nintendods.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/nintendods.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/nintendods.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/nintendods.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/nintendods.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/nintendods.xml\")//genre,\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/snes.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/snes.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/snes.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/snes.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/snes.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/snes.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/snes.xml\")//genre,\n"
                + "update rename doc(\"/db/RetroExists/nes.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/nes.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/nes.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/nes.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/nes.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/nes.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/nes.xml\")//genre,\n"
                + "\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/msdos.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/msdos.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/msdos.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/msdos.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/msdos.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/msdos.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/msdos.xml\")//genre,\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/gameboycolor.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/gameboycolor.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/gameboycolor.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/gameboycolor.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/gameboycolor.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/gameboycolor.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/gameboycolor.xml\")//genre,\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/gameboyadvance.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/gameboyadvance.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/gameboyadvance.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/gameboyadvance.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/gameboyadvance.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/gameboyadvance.xml\")//enabled,\n"
                + "update insert \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/gameboyadvance.xml\")//genre,\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "\n"
                + "update rename doc(\"/db/RetroExists/gameboy.xml\")//header as \"maquina\",\n"
                + "update delete doc(\"/db/RetroExists/gameboy.xml\")//lastlistupdate,\n"
                + "update delete doc(\"/db/RetroExists/gameboy.xml\")//listversion,\n"
                + "update delete doc(\"/db/RetroExists/gameboy.xml\")//exporterversion,\n"
                + "update delete doc(\"/db/RetroExists/gameboy.xml\")//crc,\n"
                + "update delete doc(\"/db/RetroExists/gameboy.xml\")//enabled,\n"
                + "update insert         \n"
                + "<personalizado>\n"
                + "<estrellas>0</estrellas>\n"
                + "<lotengo>No</lotengo>\n"
                + "<jugado>No</jugado>\n"
                + "<rutaimagen/>\n"
                + "</personalizado>\n"
                + "preceding doc(\"/db/RetroExists/gameboy.xml\")//genre";

        XQPreparedExpression consultar;
        ArrayList<String> lista = new ArrayList<String>();
        try {
            consultar = xqc.prepareExpression(consulta);
            XQResultSequence resultado = consultar.executeQuery();
            String cadena = resultado.getAtomicValue();

        } catch (XQException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void generarClaves() {
        try {
            String consulta = "let $sortResult := for $item in  doc(\"/db/RetroExists/consolas.xml\")//consola\n"
                    + "                    order by $item/id\n"
                    + "                    return $item\n"
                    + "                    for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem, "
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/gameboy.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/gameboyadvance.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/gameboycolor.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/msdos.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/nes.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/nintendods.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/nintendogamecube.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/segacd.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/segadreamcast.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/segagenesis.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/segamegadrive.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/segasaturn.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/segamastersystem.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/snes.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/snkneogeocd.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/snkneogeopocket.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/snkneogeopocketcolor.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ,"
                    + "let $sortResult := for $item in  doc(\"/db/RetroExists/sonyplaystation2.xml\")//game\n"
                    + "order by $item/id\n"
                    + "return $item\n"
                    + "\n"
                    + "for $sortItem at $position in $sortResult\n"
                    + "return update insert  element clave{ $position } into $sortItem ";
            XQExpression consultar;
            conectar();
            System.out.println(consulta);
            consultar = xqc.createExpression();
            consultar.executeCommand(consulta);
        } catch (XQException ex) {
            Logger.getLogger(Consultas.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            desconectar();
        }

    }
}
