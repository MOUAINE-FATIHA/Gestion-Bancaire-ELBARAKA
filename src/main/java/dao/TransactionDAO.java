package dao;

import entity.Transaction;
import enums.TypeTransaction;
import exception.DaoException;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public Transaction create(Transaction transaction) {
        String sql = """
                INSERT INTO transaction (date_op, montant, type, lieu, id_compte)
                VALUES (?, ?, ?, ?, ?) RETURNING id
                """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(transaction.date().atStartOfDay()));
            stmt.setDouble(2, transaction.montant());
            stmt.setString(3, transaction.type().name());
            stmt.setString(4, transaction.lieu());
            stmt.setLong(5, transaction.idCompte());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Long generatedId = rs.getLong("id");
                    return new Transaction(generatedId, transaction.date(), transaction.montant(),
                            transaction.type(), transaction.lieu(), transaction.idCompte());
                }
                throw new DaoException("Aucun id généré lors de la création de la transaction.");
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la création de la transaction : " + e.getMessage(), e);
        }
    }

    public List<Transaction> findByCompte(Long idCompte) {
        String sql = "SELECT * FROM transaction WHERE id_compte = ? ORDER BY date_op";
        List<Transaction> resultats = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idCompte);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultats.add(mapRow(rs));
                }
            }
            return resultats;
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la recherche des transactions du compte : " + e.getMessage(), e);
        }
    }

    public List<Transaction> findAll() {
        String sql = "SELECT * FROM transaction ORDER BY date_op";
        List<Transaction> resultats = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                resultats.add(mapRow(rs));
            }
            return resultats;
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la récupération des transactions : " + e.getMessage(), e);
        }
    }

    public void delete(Long id) {
        String sql = "DELETE FROM transaction WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int lignes = stmt.executeUpdate();
            if (lignes == 0) {
                throw new DaoException("Aucune transaction trouvée avec l'id " + id + " pour la suppression.");
            }
        } catch (SQLException e) {
            throw new DaoException("Erreur lors de la suppression de la transaction : " + e.getMessage(), e);
        }
    }

    private Transaction mapRow(ResultSet rs) throws SQLException {
        LocalDate date = rs.getTimestamp("date_op").toLocalDateTime().toLocalDate();
        return new Transaction(
                rs.getLong("id"),
                date,
                rs.getDouble("montant"),
                TypeTransaction.valueOf(rs.getString("type")),
                rs.getString("lieu"),
                rs.getLong("id_compte")
        );
    }
}