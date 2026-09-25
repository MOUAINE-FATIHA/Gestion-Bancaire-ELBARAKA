package service;
import dao.ClientDAO;
import dao.CompteDAO;
import entity.Client;
import entity.Compte;
import exception.DaoException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientService {

    private final ClientDAO clientDAO;
    private final CompteDAO compteDAO;

    public ClientService(ClientDAO clientDAO, CompteDAO compteDAO) {
        this.clientDAO = clientDAO;
        this.compteDAO = compteDAO;
    }

    public Client ajouter(String nom, String email) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom du client est obligatoire.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Email invalide : " + email);
        }
        return clientDAO.create(new Client(null, nom, email));
    }

    public void modifier(Long id, String nouveauNom, String nouvelEmail) {
        Client existant = clientDAO.findById(id)
                .orElseThrow(() -> new DaoException("Client introuvable avec l'id " + id));
        Client miseAJour = new Client(existant.id(), nouveauNom, nouvelEmail);
        clientDAO.update(miseAJour);
    }

    public void supprimer(Long id) {
        clientDAO.delete(id);
    }

    public Optional<Client> rechercherParId(Long id) {
        return clientDAO.findById(id);
    }

    public List<Client> rechercherParNom(String nom) {
        return clientDAO.findByNom(nom);
    }

    public List<Client> listerTous() {
        return clientDAO.findAll();
    }
    public double soldeTotal(Long idClient) {
        return compteDAO.findByClient(idClient).stream()
                .mapToDouble(Compte::getSolde)
                .sum();
    }

    public int nombreDeComptes(Long idClient) {
        return compteDAO.findByClient(idClient).size();
    }
    public String infosPourRapport(Long idClient) {
        Client client = clientDAO.findById(idClient)
                .orElseThrow(() -> new DaoException("Client introuvable avec l'id " + idClient));
        List<Compte> comptes = compteDAO.findByClient(idClient);
        double total = comptes.stream().mapToDouble(Compte::getSolde).sum();

        return "%s (%s) - %d compte(s) - solde total : %.2f".formatted(
                client.nom(), client.email(), comptes.size(), total);
    }
    public List<String> listerNoms() {
        return clientDAO.findAll().stream()
                .map(Client::nom)
                .collect(Collectors.toList());
    }
}