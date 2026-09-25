package service;
import dao.CompteDAO;
import entity.Compte;
import entity.CompteCourant;
import entity.CompteEpargne;
import exception.DaoException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CompteService {

    private final CompteDAO compteDAO;

    public CompteService(CompteDAO compteDAO) {
        this.compteDAO = compteDAO;
    }

    public Compte creerCompteCourant(String numero, double soldeInitial, Long idClient, double decouvertAutorise) {
        validerNumero(numero);
        if (decouvertAutorise < 0) {
            throw new IllegalArgumentException("Le découvert autorisé ne peut pas être négatif.");
        }
        CompteCourant compte = new CompteCourant(null, numero, soldeInitial, idClient, decouvertAutorise);
        return compteDAO.create(compte);
    }

    public Compte creerCompteEpargne(String numero, double soldeInitial, Long idClient, double tauxInteret) {
        validerNumero(numero);
        if (tauxInteret < 0) {
            throw new IllegalArgumentException("Le taux d'intérêt ne peut pas être négatif.");
        }
        CompteEpargne compte = new CompteEpargne(null, numero, soldeInitial, idClient, tauxInteret);
        return compteDAO.create(compte);
    }

    private void validerNumero(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("Le numéro de compte est obligatoire.");
        }
    }

    public void mettreAJourSolde(Long idCompte, double nouveauSolde) {
        compteDAO.updateSolde(idCompte, nouveauSolde);
    }

    public Optional<Compte> rechercherParNumero(String numero) {
        return compteDAO.findByNumero(numero);
    }

    public List<Compte> rechercherParClient(Long idClient) {
        return compteDAO.findByClient(idClient);
    }

    public List<Compte> listerTous() {
        return compteDAO.findAll();
    }

    public Optional<Compte> compteSoldeMax() {
        return compteDAO.findAll().stream()
                .max(Comparator.comparingDouble(Compte::getSolde));
    }
    public Optional<Compte> compteSoldeMin() {
        return compteDAO.findAll().stream()
                .min(Comparator.comparingDouble(Compte::getSolde));
    }
    public Compte majDecouvertAutorise(Long idCompte, double nouveauDecouvert) {
        Compte compte = compteDAO.findAll().stream()
                .filter(c -> c.getId().equals(idCompte))
                .findFirst()
                .orElseThrow(() -> new DaoException("Compte introuvable avec l'id " + idCompte));

        if (!(compte instanceof CompteCourant)) {
            throw new IllegalArgumentException("Le compte " + idCompte + " n'est pas un compte courant.");
        }
        return new CompteCourant(compte.getId(), compte.getNumero(), compte.getSolde(), compte.getIdClient(), nouveauDecouvert);
    }
}