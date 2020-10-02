package jeu;

import static dame.client.Constante.*;
import static cacao.util.Utile.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import dame.*;

/**
 *
 * @author Castor
 */
public class EquipeLocal extends Equipe {

    private final int TAILLE_CASE;
    private final Donnees data;
    private int caseChoisi = -1;
    private final Damier damier;
    private final LinkedList<Integer> listeChoix = new LinkedList<>();
    private final int DELIMITEUR = 0;
    private final Joueur joueurAdverse;
    private boolean rejouer, passerLaMain;
    private final LinkedList<ActionEffectuer> pileAction = new LinkedList<>();
    private final int indiceMonEquipe;
    private int idDuel;
    
    public EquipeLocal(int sensDeplacement, int nombrePions, char couleur, Damier damier, Donnees data, Joueur joueurAdverse, int indiceMonEquipe, int idDuel) {
        super(sensDeplacement, nombrePions, couleur, damier);
        this.data = data;
        TAILLE_CASE = data.TAILLE_PLATEAU / 10;
        this.damier = damier;
        this.joueurAdverse = joueurAdverse;
        this.indiceMonEquipe = indiceMonEquipe;
        this.idDuel = idDuel;
    }

    @Override
    public boolean jouer() {
        boolean isDame;
        if (SOURIS.boutonAppuyerUneFois(MouseEvent.BUTTON1)) {
            Point p = SOURIS.getPosition();
            if (pointDansRectangle(p.x, p.y, data.PLATEAU_X, data.PLATEAU_Y, data.TAILLE_PLATEAU, data.TAILLE_PLATEAU)) {
                int ligne = (p.y - data.PLATEAU_Y) / TAILLE_CASE;
                int colonne = (p.x - data.PLATEAU_X) / TAILLE_CASE;
                if (this.validerPionChoisi()) {
                    isDame = damier.getCase(caseChoisi).pionIsDame();
                    this.deplacerPionChoisi(ligne, colonne);
                    if (!rejouer) {
                        reinitialiser();
                    } else {
                        if(damier.getCase(caseChoisi).pionIsDame() && !isDame){
                            rejouer = false;
                            passerLaMain = true;
                            reinitialiser();
                        }
                    }
                } else if (!rejouer) {
                    reinitialiser();
                    this.choisirPion(ligne, colonne);
                }
            }
        }
        if (passerLaMain) {
            envoyerAction();
            pileAction.clear();
            passerLaMain = false;
            return false;
        }
        return true;
    }

    @Override
    public void dessinerMarques(Graphics2D g2d) {
        int n = damier.getNombreDeCaseParLigne();
        if (caseChoisi > 0) {
            int i = caseChoisi / n;
            int j = caseChoisi % n;
            g2d.setColor(new Color(0, 255, 0, 128));
            g2d.fillRect(data.PLATEAU_X + j * TAILLE_CASE, data.PLATEAU_Y + i * TAILLE_CASE, TAILLE_CASE, TAILLE_CASE);
        }
        for (int val : listeChoix) {
            if (val != DELIMITEUR) {
                int v = val;
                g2d.setColor(new Color(0, 255, 0, 128));
                if (v < 0) {
                    g2d.setColor(new Color(255, 0, 0, 128));
                    v = -v;
                }
                int i = v / n;
                int j = v % n;
                g2d.fillRect(data.PLATEAU_X + j * TAILLE_CASE, data.PLATEAU_Y + i * TAILLE_CASE, TAILLE_CASE, TAILLE_CASE);
            }
        }
    }

    private void choisirPion(int ligne, int colonne) {
        if (!damier.getCase(ligne, colonne).estVide() && (damier.getCase(ligne, colonne).getCouleurPion() == joueur.getCouleur())) {
            caseChoisi = ligne * damier.getNombreDeCaseParLigne() + colonne;
            getActionPossible();
        }
    }

    private boolean validerPionChoisi() {
        if (caseChoisi < 0) {
            return false;
        }
        if (neContientQueDesZero()) {
            return false;
        }
        return !(peutCapturer() && neContientQuePositif());
    }

    private boolean neContientQueDesZero() {
        for (int v : listeChoix) {
            if (v != DELIMITEUR) {
                return false;
            }
        }
        return true;
    }

    private boolean neContientQuePositif() {
        for (int v : listeChoix) {
            if (v < 0) {
                return false;
            }
        }
        return true;
    }

    private boolean peutCapturer() {
        int n = damier.getNombreDeCaseParLigne();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Case laCase = damier.getCase(i, j);
                if (!laCase.estVide() && laCase.getCouleurPion() == joueur.getCouleur()) {
                    if (pionPeutCapturer(i, j)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean pionPeutCapturer(int ligne, int colonne) {
        boolean isDame = damier.getCase(ligne, colonne).pionIsDame();
        if (pionPeutCapturer(ligne, colonne, Joueur.HAUT, Joueur.GAUCHE, isDame)) {
            return true;
        }
        if (pionPeutCapturer(ligne, colonne, Joueur.HAUT, Joueur.DROITE, isDame)) {
            return true;
        }
        if (pionPeutCapturer(ligne, colonne, Joueur.BAS, Joueur.GAUCHE, isDame)) {
            return true;
        }
        return pionPeutCapturer(ligne, colonne, Joueur.BAS, Joueur.DROITE, isDame);
    }

    private boolean pionPeutCapturer(int ligne, int colonne, int dirLigne, int dirColonne, boolean isDame) {
        int n = damier.getNombreDeCaseParLigne();
        for (int l = ligne + dirLigne, c = colonne + dirColonne; nombreDansIntervalle(l, 0, n - 1) && nombreDansIntervalle(c, 0, n - 1); l += dirLigne, c += dirColonne) {
            if (!damier.getCase(l, c).estVide() && damier.getCase(l, c).getCouleurPion() != joueur.getCouleur()
                    && nombreDansIntervalle(l + dirLigne, 0, n - 1) && nombreDansIntervalle(c + dirColonne, 0, n - 1)) {
                return damier.getCase(l + dirLigne, c + dirColonne).estVide();
            } else if (!damier.getCase(l, c).estVide()) {
                return false;
            }
            if (!isDame) {
                return false;
            }
        }
        return false;
    }

    private void getActionPossible() {
        int directionVerticale = joueur.getSensDeDeplacement();
        int directionHorizontale = Joueur.GAUCHE;
        int n = damier.getNombreDeCaseParLigne();
        boolean pionIsDame = damier.getCase(caseChoisi).pionIsDame();

        getActionPossibleDiagonal(directionVerticale, directionHorizontale, pionIsDame);
        getActionPossibleDiagonal(directionVerticale, -directionHorizontale, pionIsDame);
        if (pionIsDame || this.pionPeutCapturer(caseChoisi / n, caseChoisi % n, -directionVerticale, directionHorizontale, pionIsDame)) {
            getActionPossibleDiagonal(-directionVerticale, directionHorizontale, pionIsDame);
        }
        if (pionIsDame || this.pionPeutCapturer(caseChoisi / n, caseChoisi % n, -directionVerticale, -directionHorizontale, pionIsDame)) {
            getActionPossibleDiagonal(-directionVerticale, -directionHorizontale, pionIsDame);
        }
    }

    public void getActionPossibleDiagonal(int directionVerticale, int directionHorizontale, boolean pionIsDame) {
        int n = damier.getNombreDeCaseParLigne();
        int ligne = caseChoisi / n;
        int colonne = caseChoisi % n;
        boolean capturer = false; // Lorsque on tombe sur un pion adverse capturable (true)

        for (int l = ligne + directionVerticale, c = colonne + directionHorizontale; nombreDansIntervalle(l, 0, n - 1) && nombreDansIntervalle(c, 0, n - 1); l += directionVerticale, c += directionHorizontale) {
            if (!pionIsDame) {
                if (damier.getCase(l, c).estVide()) {
                    listeChoix.add(l * n + c);
                } else if (damier.getCase(l, c).getCouleurPion() != joueur.getCouleur() && nombreDansIntervalle(l + directionVerticale, 0, n - 1) && nombreDansIntervalle(c + directionHorizontale, 0, n - 1)) {
                    if (damier.getCase(l + directionVerticale, c +  directionHorizontale).estVide()) {
                        listeChoix.add(-(l * n + c));
                        listeChoix.add((l + directionVerticale) * n + c + directionHorizontale);
                    }
                }
                listeChoix.add(DELIMITEUR);
                break;
            } else {
                if (capturer) {
                    if (damier.getCase(l, c).estVide()) {
                        listeChoix.add(l * n + c);
                    } else {
                        listeChoix.add(DELIMITEUR);
                        break;
                    }
                    if(!(nombreDansIntervalle(l + directionVerticale, 0, n - 1) && nombreDansIntervalle(c + directionHorizontale, 0, n - 1))){
                        listeChoix.add(DELIMITEUR);
                    }
                } else if (damier.getCase(l, c).estVide() && !(nombreDansIntervalle(l + directionVerticale, 0, n - 1) && nombreDansIntervalle(c + directionHorizontale, 0, n - 1))) {
                    ajouter(l, c, ligne, colonne, -directionVerticale, -directionHorizontale);
                    listeChoix.add(DELIMITEUR);
                    break;
                } else if (!damier.getCase(l, c).estVide()) {
                    Case laCase = damier.getCase(l, c);
                    if (laCase.getCouleurPion() == joueur.getCouleur() || (laCase.getCouleurPion() != joueur.getCouleur()
                            && nombreDansIntervalle(l + directionVerticale, 0, n - 1) && nombreDansIntervalle(c + directionHorizontale, 0, n - 1)
                            && !damier.getCase(l + directionVerticale, c + directionHorizontale).estVide())
                            || !nombreDansIntervalle(l + directionVerticale, 0, n - 1) || !nombreDansIntervalle(c + directionHorizontale, 0, n - 1)) {
                        ajouter(l - directionVerticale, c - directionHorizontale, ligne, colonne, -directionVerticale, -directionHorizontale);
                        listeChoix.add(DELIMITEUR);
                        break;
                    } else {
                        listeChoix.add(-(l * n + c));
                        capturer = true;
                    }
                }
            }
        }
    }

    private void ajouter(int premiereLigne, int premiereColonne, int derniereLigne, int derniereColonne, int pasLigne, int pasColonne) {
        int n = damier.getNombreDeCaseParLigne();
        for (int l = premiereLigne, c = premiereColonne; l != derniereLigne && c != derniereColonne; l += pasLigne, c += pasColonne) {
            listeChoix.add(l * n + c);
        }
    }

    private void deplacerPionChoisi(int ligne, int colonne) {
        if (peutCapturer()) {
            if(damier.getCase(caseChoisi).pionIsDame()){
                System.out.println("les choix de la dame : " + listeChoix);
            }
            int pionACapturer = capture(listeChoix.indexOf(ligne * damier.getNombreDeCaseParLigne() + colonne));
            if (pionACapturer < 0) {
                pileAction.add(new ActionEffectuer(caseChoisi, damier.getCase(-pionACapturer).getNumeroPion(), ligne * damier.getNombreDeCaseParLigne() + colonne));
                joueur.deplacer(damier.getCase(caseChoisi).getNumeroPion(), damier.getCase(ligne, colonne));
                joueurAdverse.retirerPion(damier.getCase(-pionACapturer).getNumeroPion());
                caseChoisi = ligne * damier.getNombreDeCaseParLigne() + colonne;
                rejouer = doitRejouer();
                passerLaMain = !rejouer;
                nombrePionCapture++;
            }
        } else if (listeChoix.contains(ligne * damier.getNombreDeCaseParLigne() + colonne)) {
            joueur.deplacer(damier.getCase(caseChoisi).getNumeroPion(), damier.getCase(ligne, colonne));
            pileAction.add(new ActionEffectuer(caseChoisi, -1, ligne * damier.getNombreDeCaseParLigne() + colonne));
            passerLaMain = true;
        }
    }

    private boolean doitRejouer() {
        listeChoix.clear();
        this.getActionPossible();
        return !this.neContientQuePositif();
    }

    private int capture(int indice) {
        for (int i = indice; i >= 0; i--) {
            if (listeChoix.get(i) == DELIMITEUR) {
                break;
            }
            if (listeChoix.get(i) < 0) {
                return listeChoix.get(i);
            }
        }
        return DELIMITEUR;
    }

    private void envoyerAction() {
        try {
            try (Socket s = new Socket(adresseDuServeur.getAddress(), adresseDuServeur.getPort()); PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()))) {
                pw.println(REQ_ACTION);
                pw.flush();
                pw.println(idDuel);
                pw.flush();
                if(joueurAdverse.perdu()){
                    pw.println(indiceMonEquipe);
                } else {
                    pw.println("-1");
                }
                pw.flush();
                pw.println((1 - indiceMonEquipe));
                pw.flush();
                pw.println(pileAction.size());
                pw.flush();
                for (ActionEffectuer a : pileAction) {
                    pw.println(a.numeroCaseChoisi);
                    pw.flush();
                    pw.println(a.numeroCasePionCapturer);
                    pw.flush();
                    pw.println(a.numeroNouvelleCase);
                    pw.flush();
                }
                
                System.out.println("envoie de  " + pileAction + " recepteur : " + (1-indiceMonEquipe));
            }
        } catch (IOException e) { }
    }
    
    private void reinitialiser() {
        caseChoisi = -1;
        listeChoix.clear();
    }
}
