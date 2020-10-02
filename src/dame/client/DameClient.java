package dame.client;

import cacao.reseau.Serveur;
import cacao.util.Etat;
import cacao.rendu.*;
import cacao.composantgraphique.*;
import static dame.client.Constante.*;
import java.awt.*;
import java.io.*;
import java.net.*;

/**
 *
 * @author Castor
 */
public class DameClient implements Etat {

    private final Application application;
    private final ChampSaisiTexteAvecTitre champServeurPort;
    private final ChampSaisiTexteAvecTitre champServeurAdresse;
    private final ChampSaisiTexteAvecTitre champPseudo;
    private final PanneauAffichage panneauError;
    private final Bouton boutonSeConnecter;
    private final Bouton boutonQuitter;
    private Etat etatSuivant;
    private boolean montrerPanneauError;
    private Serveur serveur;

    public DameClient() {
        chargerAssets();
        application = new Application(LARGEUR_FENETRE, HAUTEUR_FENETRE, "dame-client", this, 15, true);
        champServeurPort = new ChampSaisiTexteAvecTitre("port serveur : ", LARGEUR_FENETRE / 3 - 100, 180, LARGEUR_FENETRE / 3, 30, 10);
        champServeurAdresse = new ChampSaisiTexteAvecTitre("adresse serveur : ", LARGEUR_FENETRE / 3 - 100, champServeurPort.getY() + champServeurPort.getHauteur() + 50, LARGEUR_FENETRE / 3, 30, 20);
        champPseudo = new ChampSaisiTexteAvecTitre("votre pseudo (10) : ", LARGEUR_FENETRE / 3 - 100, champServeurAdresse.getY() + champServeurAdresse.getHauteur() + 50, LARGEUR_FENETRE / 3, 30, 10);
        panneauError = new PanneauAffichage(champPseudo.getX() + champPseudo.getLargeur(), champServeurAdresse.getY() - 20, LARGEUR_FENETRE / 2, 100, 100);
        boutonSeConnecter = new Bouton("Se connecter", 3 * LARGEUR_FENETRE / 4 - 20, HAUTEUR_FENETRE - 30 - 20, LARGEUR_FENETRE / 4, 30);
        boutonQuitter = new Bouton("Quitter", 20, HAUTEUR_FENETRE - 30 - 20, LARGEUR_FENETRE / 4, 30);
        designerInterface();
        ajouterActionBouton();
    }

    @Override
    public void update() {
        this.champPseudo.update();
        this.champServeurAdresse.update();
        this.champServeurPort.update();
        this.boutonSeConnecter.update();
        this.boutonQuitter.update();
        this.panneauError.update();
    }

    @Override
    public void rendu(Graphics2D g2d) {
        g2d.drawImage(imagefond, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .1f));
        g2d.drawImage(imagePlateau, LARGEUR_FENETRE - imagePlateau.getWidth() - 20, this.champServeurPort.getY() - 50, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP));
        this.champPseudo.dessiner(g2d);
        this.champServeurAdresse.dessiner(g2d);
        this.champServeurPort.dessiner(g2d);
        this.boutonSeConnecter.dessiner(g2d);
        this.boutonQuitter.dessiner(g2d);
        if (this.montrerPanneauError) {
            this.panneauError.dessiner(g2d);
        }
    }

    @Override
    public Etat basculer() {
        return etatSuivant;
    }
    
    private void start() {
        application.lancer();
    }
    
    private boolean pseudoCorrecte() {
        String pseudo = champPseudo.texte();
        if (pseudo.equals("")) {
            return false;
        }
        return !pseudo.contains(":");
    }

    private void designerInterface() {
        decorerChamp(this.champPseudo);
        decorerChamp(this.champServeurAdresse);
        decorerChamp(this.champServeurPort);
        decorerBouton(this.boutonQuitter);
        decorerBouton(this.boutonSeConnecter);
        panneauError.setCouleurFond(new Color(0, 0, 0, 0));
        panneauError.setCouleurBordure(new Color(0, 0, 0, 0));
        panneauError.setFont(fontpoetsenOne20);
    }

    public static void decorerBouton(Bouton bouton) {
        bouton.setCouleurFond(new Color(55, 71, 136));
        bouton.setCouleurClick(new Color(112, 129, 197));
        bouton.setCouleurTexte(Color.WHITE);
        bouton.setCouleurSurvol(new Color(36, 47, 89));
        bouton.setFont(fontpoetsenOne);
    }

    private void decorerChamp(ChampSaisiTexteAvecTitre champ) {
        champ.setCouleurTitre(new Color(51, 11, 45));
        champ.setFontTitre(fontpoetsenOne);
        champ.setFont(new Font("Calibri", Font.BOLD, 14));
        champ.setCouleurBordure(new Color(36, 47, 89));
        champ.setCouleurFond(Color.WHITE);
        champ.setCouleurTexte(new Color(0, 0, 0));
    }
    
    private void ajouterActionBouton(){
        boutonSeConnecter.setAction(() -> {
            try {
                if (pseudoCorrecte()) {
                    try (Socket s = new Socket(champServeurAdresse.texte(), Integer.parseInt(champServeurPort.texte()))) {
                        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(s.getOutputStream()))) {
                            pw.println(REQ_LOGIN);
                            pw.flush();
                            pw.println(champPseudo.texte());
                            pw.flush();
                            try (BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                                String messageDeBienvenue = br.readLine();
                                if (messageDeBienvenue.contains("[BI]")) {
                                    //Reception liste de personne connectées
                                    int nbConnecte = Integer.parseInt(br.readLine());
                                    String[] listePersonneConnectees = new String[nbConnecte];
                                    for (int i = 0; i < nbConnecte; i++) {
                                        listePersonneConnectees[i] = br.readLine();
                                        LISTE_PERSONNES_CONNECTEES.add(listePersonneConnectees[i]);
                                    }
                                    
                                    //Reception listeSpectacle
                                    int nbSpectacle = Integer.parseInt(br.readLine());
                                    String[] listeSpectacle = new String[nbSpectacle];
                                    for(int i = 0; i < nbSpectacle; i++){
                                        listeSpectacle[i] = br.readLine();
                                    }
                                    //FIN
                                    s.close();
                                    serveur = new cacao.reseau.Serveur(s.getLocalPort());
                                    application.setActionClose(() -> {
                                        serveur.stop();
                                    });
                                    etatSuivant = new Client(champServeurAdresse.texte(), Integer.parseInt(champServeurPort.texte()), champPseudo.texte(), serveur, messageDeBienvenue.substring(4, messageDeBienvenue.length()), listePersonneConnectees, listeSpectacle);
                                } else {
                                    montrerPanneauError = true;
                                    panneauError.effacerTouteLesLignes();
                                    panneauError.addLigne(messageDeBienvenue, PanneauAffichage.ALIGNEMENT_CENTRE, new Color(181,15,72));
                                }
                            }
                        }
                    }
                } else {
                    montrerPanneauError = true;
                    panneauError.effacerTouteLesLignes();
                    panneauError.addLigne("Pseudo incorrecte !", PanneauAffichage.ALIGNEMENT_CENTRE, new Color(181,15,72));
                }
            } catch (IOException | NumberFormatException e) {
                panneauError.effacerTouteLesLignes();
                panneauError.addLigne("connection impossible à", PanneauAffichage.ALIGNEMENT_CENTRE, new Color(181,15,72));
                panneauError.addLigne("[" + (champServeurAdresse.texte().equals("") ? "localhost" : champServeurAdresse.texte()) + ":" + champServeurPort.texte() + "]", PanneauAffichage.ALIGNEMENT_CENTRE, new Color(181,15,72));
                this.montrerPanneauError = true;
            }
        });

        boutonQuitter.setAction(() -> {
            System.exit(0);
        });
    }
    
    public static void main(String[] args) {
        DameClient dc = new DameClient();
        dc.start();
    }

}
