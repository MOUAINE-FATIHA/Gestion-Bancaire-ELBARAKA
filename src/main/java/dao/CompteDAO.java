package dao;

import entity.Compte;
import entity.CompteCourant;
import entity.CompteEpargne;
import enums.TypeCompte;
import exception.DaoException;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CompteDAO {

    public Compte create(Compte compte) {
        String sql = """
                INSERT INTO compte (numero, solde, id_client, type_compte, decouvert_autorise, taux_interet)
                VALUES (?, ?, ?, ?, ?, ?) RETURNING id
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, compte.getNumero());
            stmt.setDouble(2, compte.getSolde());
            stmt.setLong(3, compte.getIdClient());

            if (compte instanceof CompteCourant courant) {
                stmt.setString(4, TypeCompte.COURANT.name());
                stmt.setDouble(5, courant.getDecouvertAutorise());
                stmt.setNull(6, java.sql.Types.NUMERIC);
            } else if (compte instanceof CompteEpargne epargne) {
                stmt.setString(4, TypeCompte.EPARGNE.name());
                stmt.setNull(5, java.sql.Types.NUMERIC);
                stmt.setDouble(6, epargne.getTauxInteret());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long generatedId = rs.getLong("id");
                    return reconstruire(generatedId, compte);
                }
                throw new DaoException("Aucun id généré lors de la création du compte.");
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création du compte : " + e.getMessage(), e);
        }
    }

    public Optional<Compte> findByNumero(String numero) {
        String sql = "SELECT * FROM compte WHERE numero = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numero);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche du compte par numéro : " + e.getMessage(), e);
        }
    }

    public List<Compte> findByClient(Long idClient) {
        String sql = "SELECT * FROM compte WHERE id_client = ? ORDER BY id";
        List<Compte> resultats = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idClient);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapRow(rs));
                }
            }
            return resultats;
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche des comptes du client : " + e.getMessage(), e);
        }
    }

    public List<Compte> findAll() {
        String sql = "SELECT * FROM compte ORDER BY id";
        List<Compte> resultats = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultats.add(mapRow(rs));
            }
            return resultats;
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des comptes : " + e.getMessage(), e);
        }
    }

    public void updateSolde(Long id, double nouveauSolde) {
        String sql = "UPDATE compte SET solde = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nouveauSolde);
            stmt.setLong(2, id);

            int lignes = stmt.executeUpdate();
            if (lignes == 0) {
                throw new DaoException("Aucun compte trouvé avec l'id " + id + " pour la mise à jour du solde.");
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la mise à jour du solde : " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM compte WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int lignes = stmt.executeUpdate();
            if (lignes == 0) {
                throw new DaoException("Aucun compte trouvé avec l'id " + id + " pour la suppression.");
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression du compte : " + e.getMessage(), e);
        }
    }
    private Compte mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String numero = rs.getString("numero");
        double solde = rs.getDouble("solde");
        Long idClient = rs.getLong("id_client");
        String typeCompte = rs.getString("type_compte");

        return switch (TypeCompte.valueOf(typeCompte)) {
            case COURANT -> new CompteCourant(id, numero, solde, idClient, rs.getDouble("decouvert_autorise"));
            case EPARGNE -> new CompteEpargne(id, numero, solde, idClient, rs.getDouble("taux_interet"));
        };
    }

    private Compte reconstruire(Long id, Compte source) {
        if (source instanceof CompteCourant courant) {
            return new CompteCourant(id, courant.getNumero(), courant.getSolde(), courant.getIdClient(), courant.getDecouvertAutorise());
        } else if (source instanceof CompteEpargne epargne) {
            return new CompteEpargne(id, epargne.getNumero(), epargne.getSolde(), epargne.getIdClient(), epargne.getTauxInteret());
        }
        throw new DaoException("Type de compte inconnu.");
    }
}