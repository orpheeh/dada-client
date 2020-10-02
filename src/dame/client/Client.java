package dame.client;

import cacao.util.*;
import cacao.composantgraphique.*;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.util.*;
import dame.Pion;
import jeu.*;
import static dame.client.Constante.*;

/**
 *
 * @author Castor
 */
public class Client implements Etat {

    private final PanneauAffichage panneauNotif;
    private final PanneauAffichage panneauChat;
    private final PanneauAffichage panneauInbox;
    private final PanneauAffichage panneauPersonnesConnectees;
    private PanneauAffichage panneauDeconnection;
    private final ChampSaisiTexte champChat;
    private final ChampSaisiTexte champInbox;
    private final ChampSaisiTexteAvecTitre champDst;
    private final Bouton boutonChat;
    private final Bouton boutonInbox;
    private Bouton boutonQuitter;
    private final JeuDeDame jeuDeDame;
    private final cacao.reseau.Serveur serveur;
    private final String pseudo;
    private boolean connectionInterrompue;
    private final int[] grilleDameSpectacle = new int[100];
    private String etatSpectacle = "AUCUN";
    
    public Client(String adresseDuServeur, int portServeur, String pseudo, cacao.reseau.Serveur serveur, String bienvenue, String[] info, String[] listeSpectacle) throws IOException {
        this.serveur = serveur;
        this.pseudo = pseudo;
        configurerServeur();
        Constante.adresseDuServeur = new InetSocketAddress(adresseDuServeur, portServeur);
        maintenirConnection();
        panneauNotif = new PanneauAffichage(5, 5, 430, 30, 100);
        panneauPersonnesConnectees = new PanneauAffichage(5, panneauNotif.getY() + panneauNotif.getHauteur() + 380, 250, 150, 100);
        panneauPersonnesConnectees.addLigne("** Personnes connectées **", PanneauAffichage.ALIGNEMENT_CENTRE, Color.YELLOW);
        panneauChat = new PanneauAffichage(5 + panneauNotif.getLargeur() + 20, 5, LARGEUR_FENETRE - panneauNotif.getLargeur() - 50, HAUTEUR_FENETRE / 2 - 25, 50);
        panneauChat.addLigne("** CHAT **", PanneauAffichage.ALIGNEMENT_CENTRE, Color.YELLOW);
        champChat = new ChampSaisiTexte(panneauChat.getX(), panneauChat.getY() + panneauChat.getHauteur() + 5, 4 * panneauChat.getLargeur() / 5, 20, 30);
        panneauInbox = new PanneauAffichage(panneauChat.getX(), panneauChat.getHauteur() + 35, panneauChat.getLargeur(), HAUTEUR_FENETRE - panneauChat.getHauteur() - 105, 50);
        panneauInbox.addLigne("** INBOX **", PanneauAffichage.ALIGNEMENT_CENTRE, Color.YELLOW);
        champInbox = new ChampSaisiTexte(panneauChat.getX(), panneauInbox.getY() + panneauInbox.getHauteur() + 5, 4 * panneauInbox.getLargeur() / 5, 20, 30);
        champDst = new ChampSaisiTexteAvecTitre("destinataire : ", panneauChat.getX() + champInbox.getLargeur() - 2 * panneauInbox.getLargeur() / 5, champInbox.getY() + champInbox.getHauteur() + 5, 2 * panneauInbox.getLargeur() / 5, 20, 50);
        boutonChat = new Bouton("Diffuser", panneauChat.getX() + champChat.getLargeur() + 5, panneauChat.getY() + panneauChat.getHauteur() + 5, panneauChat.getLargeur() - champChat.getLargeur() - 5, champChat.getHauteur());
        boutonInbox = new Bouton("Envoyer", panneauInbox.getX() + champInbox.getLargeur() + 5, panneauInbox.getY() + panneauInbox.getHauteur() + 5, panneauInbox.getLargeur() - champInbox.getLargeur() - 5, champInbox.getHauteur());
        panneauPersonnesConnectees.utiliserScrollBar(true);
        panneauChat.utiliserScrollBar(true);
        panneauInbox.utiliserScrollBar(true);
        panneauPersonnesConnectees.setFixeScroll(true);
        panneauChat.setFixeScroll(true);
        panneauInbox.setFixeScroll(true);
        jeuDeDame = new JeuDeDame(panneauNotif.getX(), panneauNotif.getY() + panneauNotif.getHauteur() + 5, 400, 400, 300, 300, pseudo);
        connection(bienvenue, info);
        ajouterActionBouton();
        decorerInterface();
        for(int i = 0; i < listeSpectacle.length; i++){
            String j1 = listeSpectacle[i].split(":")[0];
            String j2 = listeSpectacle[i].split(":")[1];
            int id = Integer.parseInt(listeSpectacle[i].split(":")[2]);
            jeuDeDame.addSpectacle(j1, j2, id);
        }
    }

    private void envoieMessage(String req, String msg) {
        try {
            try (Socket s = new Socket(adresseDuServeur.getAddress(), adresseDuServeur.getPort()); PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()))) {
                pw.println(req);
                pw.flush();
                pw.println(msg);
                pw.flush();
            }
        } catch (IOException e) {
        }
    }

    private void connection(String bienvenue, String[] info) {
        panneauNotif.addLigne(bienvenue, PanneauAffichage.ALIGNEMENT_CENTRE, Color.RED);
        for (String info1 : info) {
            panneauPersonnesConnectees.addLigne(info1, PanneauAffichage.ALIGNEMENT_GAUCHE);
        }
    }

    private boolean pseudoExiste(String pseudo) {
        for (String s : Constante.LISTE_PERSONNES_CONNECTEES) {
            if (s.equals(pseudo)) {
                return true;
            }
        }
        return false;
    }

    private void traitementRequeteSurServeur(String req, String msg) {
        String joueur1, joueur2;
        switch (req) {
            case REQ_LOGIN:
                panneauPersonnesConnectees.addLigne(msg, PanneauAffichage.ALIGNEMENT_GAUCHE);
                Constante.LISTE_PERSONNES_CONNECTEES.add(msg);
                break;
            case REQ_CHAT:
                boolean monMess = msg.contains("[" + pseudo + "]");
                panneauChat.addLigne(msg, monMess ? PanneauAffichage.ALIGNEMENT_DROITE : PanneauAffichage.ALIGNEMENT_GAUCHE, (monMess ? Color.WHITE : Color.RED));
                break;
            case REQ_IN:
                panneauInbox.addLigne(msg, PanneauAffichage.ALIGNEMENT_GAUCHE, Color.RED);
                break;
            case REQ_DECONNECTER:
                for (int i = 0; i < LISTE_PERSONNES_CONNECTEES.size(); i++) {
                    if (LISTE_PERSONNES_CONNECTEES.get(i).equals(msg)) {
                        LISTE_PERSONNES_CONNECTEES.remove(i);
                        jeuDeDame.removeSpectateur(msg);
                        break;
                    }
                }
                panneauPersonnesConnectees.effacerTouteLesLignes();
                for (String s : LISTE_PERSONNES_CONNECTEES) {
                    panneauPersonnesConnectees.addLigne(s, PanneauAffichage.ALIGNEMENT_GAUCHE);
                }
                break;
            case REQ_NOT_NEW_DUEL:
                jeuDeDame.addPartieLancer(msg);
                break;
            case REQ_STOP_NEW_DUEL:
                jeuDeDame.removePartieLancer(msg);
                break;
            case REQ_START_DUEL:
                String pseudoAdversaire = msg.split(":")[0];
                int id = Integer.parseInt(msg.split(":")[1]);
                jeuDeDame.lancerDuel(id, pseudoAdversaire);
                break;
            case REQ_STOP_ERROR_DUEL:
                jeuDeDame.stopDefi();
                break;
            case REQ_FIN_DUEL:
                jeuDeDame.finDuel(msg);
                break;
            case REQ_NOUVEAU_SPECTACLE:
                joueur1 = msg.split(":")[0];
                joueur2 = msg.split(":")[1];
                if(!joueur1.equals(pseudo) && !joueur2.equals(pseudo)){
                    int idDuel = Integer.parseInt(msg.split(":")[2]);
                    jeuDeDame.addSpectacle(joueur1, joueur2, idDuel);
                }
                break;
            case REQ_STOP_NOUVEAU_SPECTACLE:
                if(jeuDeDame.getIsSpectateur()){
                    jeuDeDame.finDuelSuivi();
                }
                jeuDeDame.removeSpectacle(Integer.parseInt(msg));
                break;
            case REQ_AJOUTER_SPECTATEUR:
                jeuDeDame.addSpectateur(msg);
                break;
            case REQ_RETIRER_SPECTATEUR:
                jeuDeDame.removeSpectateur(msg);
                break;
            case REQ_FIN_SPECTACLE:
                jeuDeDame.removeSpectacle(Integer.parseInt(msg));
                reinitGrille();
                break;
        }
    }

    @Override
    public void update() {
        if (connectionInterrompue == false) {
            panneauNotif.update();
            panneauPersonnesConnectees.update();
            panneauChat.update();
            panneauInbox.update();
            champChat.update();
            champInbox.update();
            champDst.update();
            boutonChat.update();
            boutonInbox.update();
            jeuDeDame.update();
        } else {
            panneauDeconnection.update();
            boutonQuitter.update();
        }
    }

    @Override
    public void rendu(Graphics2D g2d) {
        g2d.drawImage(imagefond, 0, 0, null);
        panneauNotif.dessiner(g2d);
        panneauPersonnesConnectees.dessiner(g2d);
        panneauChat.dessiner(g2d);
        panneauInbox.dessiner(g2d);
        champChat.dessiner(g2d);
        champInbox.dessiner(g2d);
        champDst.dessiner(g2d);
        boutonChat.dessiner(g2d);
        boutonInbox.dessiner(g2d);
        jeuDeDame.dessiner(g2d);
        dessinerDamier(g2d, this.panneauPersonnesConnectees.getX() + this.panneauPersonnesConnectees.getLargeur() + 30,
                this.panneauPersonnesConnectees.getY());
        if (connectionInterrompue) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, LARGEUR_FENETRE, HAUTEUR_FENETRE);
            panneauDeconnection.dessiner(g2d);
            boutonQuitter.dessiner(g2d);
        }
    }

    private void dessinerDamier(Graphics2D g2d, int x, int y) {
        int tailleCarre = 15;
        g2d.setFont(new Font("Comic sans MS", Font.BOLD, 13));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(Color.BLACK);
        g2d.drawString("SPECTACLE: ", x, y - 3);
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                int k = i * 10 + j;
                if ((i + j) % 2 == 0) {
                    g2d.setColor(new Color(244, 237, 202));
                } else {
                    g2d.setColor(Color.BLACK);
                }
                g2d.fillRect(x + j * tailleCarre, y + i * tailleCarre, tailleCarre, tailleCarre);
                switch((char)grilleDameSpectacle[k]){
                    case Pion.NOIR:
                        g2d.setColor(new Color(91, 91, 255));
                        break;
                    case Pion.BLANC:
                        g2d.setColor(Color.WHITE);
                        break;
                }
                g2d.fillOval(x + j * tailleCarre + tailleCarre / 8 + 1, y + i * tailleCarre + tailleCarre / 8 + 1, 3 * tailleCarre / 4, 3 * tailleCarre / 4);
            }
        }

        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, tailleCarre * 10, tailleCarre * 10);
        String joueurs = jeuDeDame.getJoueurDuelSuivi();
        if(joueurs != null){
            g2d.drawString(joueurs.split(":")[0]+"(Bleu)" + " VS " + joueurs.split(":")[1]+ "(Blanc)", x, y + tailleCarre * 10 + fm.getAscent());
        } else {
            reinitGrille();
        }
    }

    @Override
    public Etat basculer() {
        return null;
    }

    private void maintenirConnection() {
        Thread thread = new Thread(() -> {
            try {
                Socket s = new Socket();
                s.connect(adresseDuServeur);
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                br.readLine();
            } catch (IOException e) {
                interrompreConnexion();
            }

        });
        thread.start();
    }

    private void interrompreConnexion() {
        panneauDeconnection = new PanneauAffichage(LARGEUR_FENETRE / 4, 3 * HAUTEUR_FENETRE / 8, LARGEUR_FENETRE / 2, HAUTEUR_FENETRE / 4, 100);
        panneauDeconnection.setFont(new Font("Trebuchet MS", Font.BOLD, 16));
        panneauDeconnection.addLigne("", PanneauAffichage.ALIGNEMENT_CENTRE, Color.RED);
        panneauDeconnection.addLigne("*** PROBLEME ***", PanneauAffichage.ALIGNEMENT_CENTRE, Color.RED);
        panneauDeconnection.addLigne("Liaison rompu avec le serveur !", PanneauAffichage.ALIGNEMENT_CENTRE);
        boutonQuitter = new Bouton("Quitter", (LARGEUR_FENETRE - 100) / 2, panneauDeconnection.getY() + panneauDeconnection.getHauteur() + 20, 100, 30);
        boutonQuitter.setAction(() -> {
            System.exit(0);
        });
        connectionInterrompue = true;
    }

    private void action(BufferedReader br) {
        try {
            LinkedList<ActionEffectuer> pile = new LinkedList<>();
            int n = Integer.parseInt(br.readLine());
            for (int i = 0; i < n; i++) {
                int numCase = Integer.parseInt(br.readLine());
                int numCapture = Integer.parseInt(br.readLine());
                int numDst = Integer.parseInt(br.readLine());
                pile.add(new ActionEffectuer(numCase, numCapture, numDst));
            }
            jeuDeDame.getActionIA(pile);
            br.close();
        } catch (IOException e) {
        }
    }

    private void decorerInterface() {
        decorerBoutonCalibri(this.boutonChat);
        decorerBoutonCalibri(this.boutonInbox);
        decorerPanneau(this.panneauChat);
        decorerPanneau(this.panneauInbox);
        decorerPanneau(this.panneauPersonnesConnectees);
        decorerChamp(this.champChat);
        decorerChamp(this.champInbox);
        decorerChamp(this.champDst);
        //this.panneauPersonnesConnectees.setFont(fontpoetsenOne);
        //this.panneauPersonnesConnectees.setCouleurFond(new Color(36, 82, 91));
        this.panneauNotif.setFont(fontpoetsenOne);
        this.panneauNotif.setCouleurFond(Color.BLACK);
        this.panneauNotif.setCouleurBordure(new Color(0, 0, 0, 0));
        champDst.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        champDst.setFontTitre(fontpoetsenOne);
    }

    private void decorerBouton(Bouton bouton) {
        bouton.setCouleurFond(new Color(55, 71, 136));
        bouton.setCouleurClick(new Color(112, 129, 197));
        bouton.setCouleurTexte(Color.WHITE);
        bouton.setCouleurSurvol(new Color(36, 47, 89));
        bouton.setFont(fontpoetsenOneSmall);
    }

    public static void decorerPanneau(PanneauAffichage panneau) {
        panneau.setFixeScroll(false);
        ScrollBar bar = panneau.getScrollBar();
        panneau.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
        panneau.setCouleurFond(Color.BLACK);
        decorerScrollBar(bar);
    }

    public static void decorerScrollBar(ScrollBar bar) {
        bar.setCouleurFond(new Color(0, 0, 0, 0));
        bar.setCouleurBordure(new Color(0, 0, 0, 0));
        bar.setFont(fontpoetsenOne);
        bar.setCouleurBordure(new Color(0, 0, 0, 0));
        bar.setCouleurBordureBouton(new Color(0, 0, 0, 0));
        bar.setCouleurFondBouton(new Color(0, 0, 0, 0));
        bar.setCouleurSurvolBouton(new Color(36, 47, 89));
        bar.setCouleurClickBouton(new Color(112, 129, 197));
        bar.setCouleurTexte(Color.WHITE);
    }

    public void decorerChamp(ChampSaisiTexte champ) {
        champ.setFont(new Font("Comic Sans MS", Font.BOLD, 14));
    }

    private void configurerServeur() {
        serveur.setRequete((Socket s) -> {
            try {
                String req;
                String message;
                try (BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                    req = br.readLine();
                    if (req.equals(REQ_ACTION)) {
                        action(br);
                    } else if (req.equals(REQ_PION_SPECTATEUR)) {
                        chargerDamierSpectacle(br);
                    } else {
                        message = br.readLine();
                        traitementRequeteSurServeur(req, message);
                    }
                }
                s.close();
            } catch (IOException ex) {
            }
        });

        new Thread(() -> {
            try {
                serveur.start();
            } catch (IOException e) {
            }
        }).start();
    }

    private void ajouterActionBouton() {
        boutonChat.setAction(() -> {
            if (!champChat.texte().equals("")) {
                this.envoieMessage(REQ_CHAT, "[" + pseudo + "]>" + champChat.texte());
            }
        });

        boutonInbox.setAction(() -> {
            if (champInbox.texte().equals("") == false && !champDst.texte().equals("")) {
                if (pseudoExiste(champDst.texte())) {
                    this.envoieMessage(REQ_IN, champDst.texte() + ":[" + pseudo + "]>" + champInbox.texte());
                    panneauInbox.addLigne("[" + champDst.texte() + "]<<" + champInbox.texte(), PanneauAffichage.ALIGNEMENT_DROITE, Color.WHITE);
                }
            }
        });
    }

    private void chargerDamierSpectacle(BufferedReader br) throws IOException {
        for (int i = 0; i < 100; i++) {
            grilleDameSpectacle[i] = Integer.parseInt(br.readLine());
            System.out.print(grilleDameSpectacle[i] + " ");
            if((i+1) % 10 == 0) System.out.println();
        }
    }
    
    private void reinitGrille(){
        for(int i = 0; i < this.grilleDameSpectacle.length; i++){
            grilleDameSpectacle[i] = 0;
        }
    }
}
