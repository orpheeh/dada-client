/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jeu;

import cacao.composantgraphique.PanneauAffichage;
import dame.*;
import dame.client.Constante;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.io.*;
import java.net.*;

/**
 *
 * @author Castor
 */
public class NouvellePartie {

    private final int PLATEAU_X, PLATEAU_Y, TAILLE_PLATEAU, TAILLE_CASE;
    private final Damier damier;
    private final Equipe[] equipes = new Equipe[2];
    private final int indiceMonEquipe;
    private int indiceEquipeJouant = 0;
    private int id;
    private final Color[] couleurPionEquipe = new Color[2];
    private PanneauAffichage panneauNonJoueur;
    private PanneauAffichage[] panneauPionCapturer;
    private String[] pseudoJoueur = new String[2];
    private final LinkedList<String> listeSpectateur = new LinkedList<>();
    
    public NouvellePartie(int indice, int positionPlateauX, int positionPlateauY, int taillePlateau, int idDuel) {
        damier = Damier.create10X10();
        indiceMonEquipe = indice;
        this.id = idDuel;
        this.PLATEAU_X = positionPlateauX + 20;
        this.PLATEAU_Y = positionPlateauY + 20;
        this.TAILLE_PLATEAU = taillePlateau - 40;
        this.TAILLE_CASE = TAILLE_PLATEAU / 10;
        equipes[1 - indiceMonEquipe] = new EquipeDistante(indiceMonEquipe == 1 ? Joueur.BAS : Joueur.HAUT, damier.getNombreDePionUtile(), indiceMonEquipe == 1 ? Pion.NOIR : Pion.BLANC, damier);
        equipes[indiceMonEquipe] = new EquipeLocal(indiceMonEquipe == 0 ? Joueur.BAS : Joueur.HAUT, damier.getNombreDePionUtile(), indiceMonEquipe == 0 ? Pion.NOIR : Pion.BLANC, damier, new Donnees(PLATEAU_X, PLATEAU_Y, TAILLE_PLATEAU), equipes[1 - indiceMonEquipe].getJoueur(), indiceMonEquipe, id);
        ((EquipeDistante) equipes[1 - indiceMonEquipe]).getJoueurAdverse(equipes[indiceMonEquipe].getJoueur());
        
        couleurPionEquipe[0] = new Color(5, 19, 139, 128);
        couleurPionEquipe[1] = new Color(255, 255, 255);
    }

    public void update() {
        if (this.panneauNonJoueur != null) {
            panneauNonJoueur.setCouleurFond(couleurPionEquipe[indiceEquipeJouant]);
            panneauNonJoueur.effacerTouteLesLignes();
            panneauNonJoueur.addLigne(pseudoJoueur[indiceEquipeJouant], PanneauAffichage.ALIGNEMENT_CENTRE, Color.BLACK);
        }
        if (!equipes[this.indiceEquipeJouant].jouer()) {
            indiceEquipeJouant = 1 - indiceEquipeJouant;
            panneauPionCapturer[0].effacerTouteLesLignes();
            panneauPionCapturer[1].effacerTouteLesLignes();
            panneauPionCapturer[indiceMonEquipe].addLigne(equipes[indiceMonEquipe].getNombrePionCapture() + "", PanneauAffichage.ALIGNEMENT_CENTRE, Color.BLACK);
            panneauPionCapturer[1 - indiceMonEquipe].addLigne(equipes[1 - indiceMonEquipe].getNombrePionCapture() + "", PanneauAffichage.ALIGNEMENT_CENTRE, Color.BLACK);
            
            //Envoyer la position des pions a tous les spectateurs;
            for(String s : listeSpectateur){
                envoyerDamier(s);
            }
        }
    }
    
    private synchronized void envoyerDamier(String pseudoSpectateur){
        try {
            try (Socket s = new Socket()) {
                s.connect(Constante.adresseDuServeur);
                try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()))) {
                    pw.println(Constante.REQ_PION_SPECTATEUR);
                    pw.flush();
                    pw.println(pseudoSpectateur);
                    pw.flush();
                    Case[] grille = damier.getGrille();
                    for(int i = 0; i < damier.getGrille().length; i++){
                        pw.println(grille[i].getCouleurPion());
                        pw.flush();
                    }
                }
            }
        } catch(IOException e){ e.printStackTrace(); }
    }
    
    public void dessiner(Graphics2D g2d) {
        equipes[indiceEquipeJouant].dessinerMarques(g2d);
        dessinerPion(g2d);
    }

    private void dessinerPion(Graphics2D g2d) {
        Case[] grille = damier.getGrille();
        int n = damier.getNombreDeCaseParLigne();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int indice = i * n + j;
                if (!grille[indice].estVide()) {
                    Color couleurPion = new Color(5, 19, 139, 128);
                    if (grille[indice].getCouleurPion() == Pion.BLANC) {
                        couleurPion = Color.WHITE;
                    }
                    g2d.setColor(Color.GRAY);
                    g2d.fillOval(PLATEAU_X + j * TAILLE_CASE + TAILLE_CASE / 8, PLATEAU_Y + i * TAILLE_CASE + TAILLE_CASE / 8 + 1, 3 * TAILLE_CASE / 4, 3 * TAILLE_CASE / 4);
                    g2d.setColor(couleurPion);
                    g2d.fillOval(PLATEAU_X + j * TAILLE_CASE + TAILLE_CASE / 8, PLATEAU_Y + i * TAILLE_CASE + TAILLE_CASE / 8 - 1, 3 * TAILLE_CASE / 4, 3 * TAILLE_CASE / 4);
                    if (grille[indice].pionIsDame()) {
                        g2d.setColor(new Color(236, 183, 0));
                        g2d.fillOval(PLATEAU_X + j * TAILLE_CASE + 5 * TAILLE_CASE / 16, PLATEAU_Y + i * TAILLE_CASE + 5 * TAILLE_CASE / 16 - 1, 3 * TAILLE_CASE / 8, 3*TAILLE_CASE / 8);
                    }
                }
            }
        }
    }

    public void getActionIA(LinkedList<ActionEffectuer> pile) {
        ((EquipeDistante) equipes[1 - indiceMonEquipe]).getAction(pile);
        System.out.println("tous es ok" + pile);
    }

    public void setPanneauPionCapture(PanneauAffichage[] panneau) {
        this.panneauPionCapturer = panneau;
    }

    public void setPanneauJoueurCourant(PanneauAffichage panneau) {
        this.panneauNonJoueur = panneau;
    }

    public void setPseudoJoueur(String[] pseudo) {
        this.pseudoJoueur = pseudo;
    }

    public int getId() {
        return id;
    }

    public void addSpectateur(String pseudoSpectateur) {
        listeSpectateur.add(pseudoSpectateur);
    }
    
    public void removeSpectateur(String pseudoSpectateur){
        for(int i = 0; i < listeSpectateur.size(); i++){
            if(listeSpectateur.get(i).equals(pseudoSpectateur)){
                listeSpectateur.remove(i);
                break;
            }
        }
    }
    
    public LinkedList<String> getSpectateur(){
        return listeSpectateur;
    }
    
    public void finPartie(){
        listeSpectateur.clear();
    }
}
