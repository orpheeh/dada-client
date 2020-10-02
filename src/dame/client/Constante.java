package dame.client;

import cacao.composantgraphique.Bouton;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.net.*;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import java.io.*;
/**
 *
 * @author Castor
 */
public class Constante { 
    public static final int LARGEUR_FENETRE = 930, HAUTEUR_FENETRE = 590;
    
    public static final String REQ_NOT_NEW_DUEL = "[ND]";
    public static final String REQ_STOP_NEW_DUEL = "[SN]";
    public static final String REQ_START_DUEL = "[SD]";
    public static final String REQ_ACTION = "[AC]";
    public static final String REQ_STOP_ERROR_DUEL = "[SE]";
    public static final String REQ_ABANDONNER = "[AA]";
    public static final String REQ_MATCH_NUL = "[BB]";
    public static final String REQ_FIN_DUEL = "[FD]";
    public static final String REQ_LOGIN = "[LO]";
    public static final String REQ_CHAT = "[CH]";
    public static final String REQ_IN = "[IN]";
    public static final String REQ_NOTIFICATION = "[NO]";
    public static final String REQ_ACK = "[AC]";
    public static final String REQ_DECONNECTER = "[DE]";
    public static final String REQ_PION_SPECTATEUR = "[CC]";
    public static final String REQ_DEVENIR_SPECTATEUR = "[DD]";
    public static final String REQ_NOUVEAU_SPECTACLE = "[EE]";
    public static final String REQ_STOP_NOUVEAU_SPECTACLE = "[FF]";
    public static final String REQ_AJOUTER_SPECTATEUR = "[GG]";
    public static final String REQ_RETIRER_SPECTATEUR = "[HH]";
    public static final String REQ_FIN_SPECTACLE = "[II]";
    
    public static Font fontLinlibertineRB;
    public static Font fontMarketDeco;
    public static Font fontBubblebody;
    public static Font fontpoetsenOne;
    public static Font fontconsolas;
    public static Font fontpoetsenOne20;
    public static Font fontpoetsenOneSmall;
    public static BufferedImage imagePlateau;
    public static BufferedImage imagefond;
    
    public static final LinkedList<String> LISTE_PERSONNES_CONNECTEES = new LinkedList<>();
    public static InetSocketAddress adresseDuServeur;
    
    public static void chargerAssets() {
        try {
            imagefond = ImageIO.read(ClassLoader.getSystemResourceAsStream("images/snow.png"));
            imagePlateau = ImageIO.read(ClassLoader.getSystemResourceAsStream("images/plateau.png"));
            fontLinlibertineRB = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemResourceAsStream("fonts/LinLibertine_RB.ttf")).deriveFont(Font.BOLD, 36.0f);
            fontMarketDeco = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemResourceAsStream("fonts/Market_Deco.ttf")).deriveFont(Font.BOLD, 14.0f);
            fontpoetsenOne = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemResourceAsStream("fonts/PoetsenOne.ttf")).deriveFont(Font.BOLD, 14.0f);
            fontconsolas = new Font("consolas", Font.BOLD, 14);
            fontpoetsenOne20 = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemResourceAsStream("fonts/PoetsenOne.ttf")).deriveFont(Font.BOLD, 20.0f);
            fontpoetsenOneSmall = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemResourceAsStream("fonts/PoetsenOne.ttf")).deriveFont(Font.PLAIN, 11.0f);
            fontBubblebody = Font.createFont(Font.TRUETYPE_FONT, ClassLoader.getSystemResourceAsStream("fonts/bubblebody.ttf")).deriveFont(Font.BOLD, 14.0f);
        } catch (IOException | FontFormatException e) { }
    }
    
    public static void decorerBoutonCalibri(Bouton bouton){
        bouton.setCouleurFond(new Color(0, 64, 128));
        bouton.setCouleurBordure(new Color(0, 64, 128));
        bouton.setCouleurClick(new Color(128, 128, 128));
        bouton.setCouleurSurvol(new Color(0, 128, 128));
        bouton.setCouleurTexte(Color.WHITE);
        bouton.setFont(new Font("Calibri", Font.PLAIN, 16));
    }
}
