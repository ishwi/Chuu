package dao;

import dao.entities.Metadata;
import dao.exceptions.ChuuServiceException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class MusidDaoImpl implements MusicDao {
    @Override
    public void storeMetadata(Connection connection, String identifier, Metadata metadata) {
        String queryString = "INSERT INTO  metadata"
                + " (url,artist,song,album )  VALUES ( ?,?,?,?) on duplicate key update artist  = values(artist), song = values(song), album = values(album)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            int i = 1;
            preparedStatement.setString(i++, identifier);
            preparedStatement.setString(i++, metadata.artist());
            preparedStatement.setString(i++, metadata.song());
            preparedStatement.setString(i, metadata.album());


            preparedStatement.executeUpdate();


        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }

    @Override
    public Optional<Metadata> getMetadata(Connection connection, String identifier) {
        String queryString = "Select artist,song,album,image  from metadata where url = ? ";

        try (PreparedStatement preparedStatement = connection.prepareStatement(queryString)) {

            preparedStatement.setString(1, identifier);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(new Metadata(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3), resultSet.getString(4)));
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new ChuuServiceException(e);
        }
    }
}
