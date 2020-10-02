package dame.client;

import cacao.composantgraphique.menu.MenuListe;
import cacao.composantgraphique.*;
import static dame.client.Constante.*;
import jeu.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 *
 * @author Castor
 */
public class JeuDeDame {

    private final int X, Y;
    private final int LARGEUR_FEN, HAUTEUR_FEN;
    private final String pseudo;
    private final LinkedList<String> listePartieLancer = new LinkedList<>();
    private final LinkedList<String> listeSpectacle = new LinkedList<>();
    private boolean attenteAdversaire, joue;
    private final Bouton boutonNouvellePartie, boutonArreterAttenteAdversaire;
    private final PanneauAffichage panneauMenuPartieLancer;
    private final PanneauAffichage panneauSpectateur;
    private final MenuListe menuPartieLancer;
    private final PanneauAffichage panneauAttente;
    private final PanneauAffichage panneauNomJoueur;
    private final PanneauAffichage[] panneauNombrePionsCapture = new PanneauAffichage[2];
    private final Bouton boutonAbandonner, boutonMatchNul;
    private NouvellePartie defi;
    private String pseudoAdverse = null;
    private int indiceEquipe = 0;
    private boolean defiTerminer;
    private String messageDeFin;
    private final Bouton boutonContinuer;
    private boolean isSpectateur;
    private String joueurDuelSuivi = null;

    public JeuDeDame(int x, int y, int largeur, int hauteur, int largeurPlateau, int hauteurPlateau, String pseudo) {
        this.X = x;
        this.Y = y;
        this.LARGEUR_FEN = largeur;
        this.HAUTEUR_FEN = hauteur;
        this.pseudo = pseudo;
        boutonNouvellePartie = new Bouton("Nouvelle partie", X + LARGEUR_FEN / 4, 100, LARGEUR_FEN / 2, 30);
        boutonArreterAttenteAdversaire = new Bouton("Arrêter d' attendre", boutonNouvellePartie.getX(), boutonNouvellePartie.getY(), boutonNouvellePartie.getLargeur(), boutonNouvellePartie.getHauteur());
        boutonNouvellePartie.setCouleurFond(new Color(149, 238, 227));
        boutonArreterAttenteAdversaire.setCouleurFond(new Color(149, 238, 227, 128));
        panneauMenuPartieLancer = new PanneauAffichage(X + (LARGEUR_FEN - 140) / 2, boutonNouvellePartie.getY() + boutonNouvellePartie.getHauteur() + 20, 140, 25, 30);
        menuPartieLancer = new MenuListe(X + (LARGEUR_FEN - 300) / 2, panneauMenuPartieLancer.getY() + panneauMenuPartieLancer.getHauteur() + 3, 300, 150, 10);
        panneauAttente = new PanneauAffichage(menuPartieLancer.getX() + (menuPartieLancer.getLargeur() - 200) / 2, menuPartieLancer.getY() + (menuPartieLancer.getHauteur() - 30) / 2, 200, 30, 50);
        panneauNomJoueur = new PanneauAffichage(X + (imagePlateau.getWidth() - 100) / 2, Y + imagePlateau.getHeight() + 25, 100, 30, 30);
        panneauNomJoueur.setCouleurFond(new Color(149, 238, 227, 128));
        panneauNomJoueur.setCouleurTexte(Color.BLACK);
        panneauNomJoueur.addLigne(pseudo, PanneauAffichage.ALIGNEMENT_CENTRE);
        this.panneauNombrePionsCapture[0] = new PanneauAffichage(X, panneauNomJoueur.getY(), 30, 30, 2);
        this.panneauNombrePionsCapture[0].setCouleurFond(new Color(5, 19, 139, 128));
        this.panneauNombrePionsCapture[1] = new PanneauAffichage(X + imagePlateau.getWidth() - 30, panneauNomJoueur.getY(), 30, 30, 2);
        this.panneauNombrePionsCapture[1].setCouleurFond(new Color(255, 255, 255));
        this.panneauNombrePionsCapture[0].addLigne("0", PanneauAffichage.ALIGNEMENT_CENTRE, Color.WHITE);
        this.panneauNombrePionsCapture[1].addLigne("0", PanneauAffichage.ALIGNEMENT_CENTRE, Color.BLACK);
        boutonAbandonner = new Bouton("Abandonner", X + imagePlateau.getWidth() + 5, Y + 20, LARGEUR_FEN - imagePlateau.getWidth() - 5, 30);
        boutonMatchNul = new Bouton("Match nul", X + imagePlateau.getWidth() + 5, Y + 60, boutonAbandonner.getLargeur(), 30);
        boutonAbandonner.setCouleurFond(new Color(149, 238, 227, 128));
        boutonMatchNul.setCouleurFond(new Color(149, 238, 227, 128));
        panneauSpectateur = new PanneauAffichage(boutonAbandonner.getX(), boutonMatchNul.getY() + boutonMatchNul.getHauteur() + 20, boutonAbandonner.getLargeur(), Constante.imagePlateau.getHeight() - 20 - boutonAbandonner.getHauteur() * 2, 50);
        panneauSpectateur.addLigne("Spectateur", PanneauAffichage.ALIGNEMENT_CENTRE, Color.YELLOW);
        boutonContinuer = new Bouton("Continuer", 3 * LARGEUR_FEN / 8, HAUTEUR_FEN - 160, LARGEUR_FEN / 4, 30);
        boutonContinuer.setCouleurFond(new Color(149, 238, 227, 128));
        chargerInterface();
    }

    public void addPartieLancer(String pseudoLanceur) {
        if (!this.pseudo.equals(pseudoLanceur)) {
            listePartieLancer.add(pseudoLanceur);
            menuPartieLancer.addBouton("VS " + pseudoLanceur);
            menuPartieLancer.setAction(() -> {
                try {
                    try (Socket s = new Socket(adresseDuServeur.getAddress(), adresseDuServeur.getPort()); PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()))) {
                        pw.println(REQ_START_DUEL);
                        pw.flush();
                        pw.println(pseudoLanceur);
                        pw.flush();
                        pw.println(this.pseudo);
                        pw.flush();
                        pseudoAdverse = pseudoLanceur;
                        this.panneauNomJoueur.effacerTouteLesLignes();
                        this.panneauNomJoueur.addLigne(pseudoAdverse, PanneauAffichage.ALIGNEMENT_CENTRE, Color.BLACK);
                        indiceEquipe = 1;
                    }
                } catch (IOException e) {
                }
            });
        }
    }

    public void addSpectateur(String pseudoSpectateur) {
        if (defi != null) {
            defi.addSpectateur(pseudoSpectateur);
            panneauSpectateur.addLigne(pseudoSpectateur, PanneauAffichage.ALIGNEMENT_CENTRE, Color.WHITE);
        }
    }

    public void removeSpectateur(String pseudoSpectateur) {
        if (defi != null) {
            defi.removeSpectateur(pseudoSpectateur);
            panneauSpectateur.effacerTouteLesLignes();
            for (String s : defi.getSpectateur()) {
                panneauSpectateur.addLigne(s, PanneauAffichage.ALIGNEMENT_CENTRE, Color.BLACK);
            }
        }
    }

    public void addSpectacle(String j1, String j2, int id) {
        menuPartieLancer.addBouton("(Spectateur): " + j1 + " VS " + j2);
        listeSpectacle.add(id + ":" + j1 + ":" + j2);
        menuPartieLancer.setAction(() -> {
            try {
                try (Socket s = new Socket()) {
                    s.connect(adresseDuServeur);
                    try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()))) {
                        pw.println(REQ_DEVENIR_SPECTATEUR);
                        pw.flush();
                        pw.println(pseudo);
                        pw.flush();
                        pw.println(id);
                        pw.flush();
                        menuPartieLancer.removeBouton("(Spectateur): " + j1 + " VS " + j2);
                        joueurDuelSuivi = j1 + ":" + j2;
                        isSpectateur = true;

                    }
                }
            } catch (IOException e) {
            }
        });
    }

    public String getJoueurDuelSuivi() {
        return joueurDuelSuivi;
    }

    public boolean getIsSpectateur() {
        return isSpectateur;
    }

    public void setIsSpectateur(boolean b) {
        isSpectateur = b;
    }

    public void finDuelSuivi() {
        isSpectateur = false;
        joueurDuelSuivi = null;
    }

    public void removeSpectacle(int idSpectacle) {
        for (String s : listeSpectacle) {
            if (s.split(":")[0].equals(idSpectacle + "")) {
                String j1 = s.split(":")[1];
                String j2 = s.split(":")[2];
                menuPartieLancer.removeBouton("(Spectateur): " + j1 + " VS " + j2);
                break;
            }
        }
    }

    public void lancerDuel(int idDuel, String pseudoAdversaire) {
        defi = new NouvellePartie(indiceEquipe, X, Y + 20, imagePlateau.getWidth(), idDuel);
        defi.setPanneauJoueurCourant(this.panneauNomJoueur);
        defi.setPanneauPionCapture(panneauNombrePionsCapture);
        if (pseudoAdverse == null) {
            defi.setPseudoJoueur(new String[]{this.pseudo, pseudoAdversaire});
        } else {
            defi.setPseudoJoueur(new String[]{this.pseudoAdverse, this.pseudo});
        }
        joue = true;
        attenteAdversaire = false;
    }

    public void removePartieLancer(String pseudo) {
        for (int i = 0; i < listePartieLancer.size(); i++) {
            if (listePartieLancer.get(i).equals(pseudo)) {
                listePartieLancer.remove(i);
                break;
            }
        }
        menuPartieLancer.removeBouton("VS " + pseudo);
    }

    public void update() {
        if (!joue) {
            if (attenteAdversaire) {
                boutonArreterAttenteAdversaire.update();
            } else {
                menuPartieLancer.update();
                boutonNouvellePartie.update();
            }
        } else {
            if (defiTerminer) {
                boutonContinuer.update();
            } else {
                boutonAbandonner.update();
                boutonMatchNul.update();
                defi.update();
            }

        }
    }

    public void dessiner(Graphics2D g2d) {
        if (!defiTerminer) {
            if (!joue) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .3f));
            }
            g2d.drawImage(imagePlateau, X, Y + 20, null);
            panneauNomJoueur.dessiner(g2d);
            this.panneauNombrePionsCapture[0].dessiner(g2d);
            this.panneauNombrePionsCapture[1].dessiner(g2d);
        }
        if (!joue) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP));
            menuPartieLancer.dessiner(g2d);
            panneauMenuPartieLancer.dessiner(g2d);
            if (attenteAdversaire) {
                boutonArreterAttenteAdversaire.dessiner(g2d);
                panneauAttente.dessiner(g2d);
            } else {
                boutonNouvellePartie.dessiner(g2d);
            }
        } else {
            if (defiTerminer) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(fontLinlibertineRB);
                FontMetrics fm = g2d.getFontMetrics();
                int hauteur = fm.getAscent() + fm.getDescent() + fm.getLeading();
                g2d.drawString(this.messageDeFin, X + (LARGEUR_FEN - fm.stringWidth(messageDeFin)) / 2, Y + boutonContinuer.getY() - hauteur - 20 - fm.getAscent());
                boutonContinuer.dessiner(g2d);
            } else {
                this.boutonAbandonner.dessiner(g2d);
                this.boutonMatchNul.dessiner(g2d);
                this.panneauSpectateur.dessiner(g2d);
                defi.dessiner(g2d);
            }
        }
    }

    private void chargerInterface() {
        boutonNouvellePartie.setAction(() -> {
            attenteAdversaire = true;
            try {
                try (Socket s = new Socket()) {
                    s.connect(adresseDuServeur);
                    PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                    pw.println(REQ_NOT_NEW_DUEL);
                    pw.flush();
                    pw.println(pseudo);
                    pw.flush();
                    pw.close();
                }
            } catch (IOException e) {
            }
        });

        boutonArreterAttenteAdversaire.setAction(() -> {
            attenteAdversaire = false;
            try {
                Socket s = new Socket();
                s.connect(adresseDuServeur);
                PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
                pw.println(REQ_STOP_NEW_DUEL);
                pw.flush();
                pw.println(pseudo);
                pw.flush();
                pw.close();
                s.close();
            } catch (IOException e) {
            }
        });
        this.boutonAbandonner.setAction(() -> {
            abandon();
        });
        this.boutonMatchNul.setAction(() -> {
            matchNul();
        });
        this.boutonContinuer.setAction(() -> {
            this.stopDefi();
        });
        panneauMenuPartieLancer.addLigne("rejoindre une partie ?", PanneauAffichage.ALIGNEMENT_CENTRE, new Color(255, 255, 255));
        this.panneauMenuPartieLancer.setCouleurFond(new Color(121, 0, 18));
        this.panneauMenuPartieLancer.setArrondir(false);
        panneauAttente.addLigne("Attente d'adversaire", PanneauAffichage.ALIGNEMENT_CENTRE, new Color(255, 255, 255));
        panneauAttente.setCouleurBordure(new Color(0, 0, 0, 0));
        panneauAttente.setCouleurFond(new Color(0, 0, 0, 0));
        menuPartieLancer.setCouleurFondsBouton(new Color(0, 0, 0, 128));
        menuPartieLancer.setCouleurTexteBouton(Color.BLACK);
        decoration();
    }

    public void decoration() {
        decorerBoutonCalibri(boutonAbandonner);
        decorerBoutonCalibri(boutonMatchNul);
        decorerBoutonCalibri(boutonNouvellePartie);
        decorerBoutonCalibri(boutonArreterAttenteAdversaire);
        decorerBoutonCalibri(boutonContinuer);
        Client.decorerPanneau(panneauSpectateur);
        this.panneauAttente.setCouleurFond(new Color(0, 0, 0, 0));
        this.panneauAttente.setCouleurBordure(new Color(0, 0, 0, 0));
        this.panneauAttente.setFont(fontpoetsenOne20);
        this.menuPartieLancer.setCouleurFond(new Color(0, 0, 0, 0));
        this.menuPartieLancer.setCouleurFondsBouton(new Color(0, 0, 0, 0));
        this.menuPartieLancer.setCouleurTexteBouton(new Color(255, 255, 255));
        ScrollBar bar = this.menuPartieLancer.getScrollBar();
        Client.decorerScrollBar(bar);
        this.menuPartieLancer.setCouleurBordure(new Color(255, 255, 255));
        this.menuPartieLancer.setCouleurFond(new Color(0, 0, 0));
        this.menuPartieLancer.setCouleurSurvolBouton(new Color(121, 0, 18));
    }
    
    public void getActionIA(LinkedList<ActionEffectuer> pile) {
        System.out.println("Envoie action a defi : " + pile);
        defi.getActionIA(pile);
    }

    public void stopDefi() {
        if (defi != null) {
            defi.finPartie();
        }
        defi = null;
        joue = false;
        defiTerminer = false;
        pseudoAdverse = null;
        indiceEquipe = 0;
    }

    private void abandon() {
        try {
            try (Socket s = new Socket()) {
                s.connect(adresseDuServeur);
                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()))) {
                    pw.println(REQ_ABANDONNER);
                    pw.flush();
                    pw.println(defi.getId() + ":" + pseudo);
                    pw.flush();
                }
            }
        } catch (IOException e) {
        }
    }

    private void matchNul() {
        try {
            try (Socket s = new Socket()) {
                s.connect(adresseDuServeur);
                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()))) {
                    pw.println(REQ_MATCH_NUL);
                    pw.flush();
                    pw.println(defi.getId());
                    pw.flush();
                }
            }
        } catch (IOException e) {
        }
    }

    public void finDuel(String msg) {
        messageDeFin = msg;
        defiTerminer = true;
        pseudoAdverse = null;
        indiceEquipe = 0;
        if (defi != null) {
            defi.finPartie();
        }
    }
}
