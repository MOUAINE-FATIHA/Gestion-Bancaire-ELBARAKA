package dao;
import exception.DaoException;
import util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDAO {

    public Client create(Client client) {
        String sql = "INSERT INTO client (nom, email) VALUES (?, ?) RETURNING id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.nom());
            stmt.setString(2, client.email());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long generatedId = rs.getLong("id");
                    return new Client(generatedId, client.nom(), client.email());
                }
                throw new DaoException("Aucun id généré lors de la création du client.");
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création du client : " + e.getMessage(), e);
        }
    }

    public Optional<Client> findById(Long id) {
        String sql = "SELECT id, nom, email FROM client WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche du client par id : " + e.getMessage(), e);
        }
    }

    public List<Client> findByNom(String nom) {
        String sql = "SELECT id, nom, email FROM client WHERE nom ILIKE ?";
        List<Client> resultats = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nom + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapRow(rs));
                }
            }
            return resultats;
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche du client par nom : " + e.getMessage(), e);
        }
    }

    public List<Client> findAll() {
        String sql = "SELECT id, nom, email FROM client ORDER BY id";
        List<Client> resultats = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultats.add(mapRow(rs));
            }
            return resultats;
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des clients : " + e.getMessage(), e);
        }
    }

    public void update(Client client) {
        String sql = "UPDATE client SET nom = ?, email = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, client.nom());
            stmt.setString(2, client.email());
            stmt.setLong(3, client.id());

            int lignesModifiees = stmt.executeUpdate();
            if (lignesModifiees == 0) {
                throw new DaoException("Aucun client trouvé avec l'id " + client.id() + " pour la mise à jour.");
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la mise à jour du client : " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM client WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int lignesSupprimees = stmt.executeUpdate();
            if (lignesSupprimees == 0) {
                throw new DaoException("Aucun client trouvé avec l'id " + id + " pour la suppression.");
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression du client : " + e.getMessage(), e);
        }
    }

    private Client mapRow(ResultSet rs) throws SQLException {
        return new Client(
                rs.getLong("id"),
                rs.getString("nom"),
                rs.getString("email")
        );
    }
}