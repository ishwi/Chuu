package dao;

import dao.entities.Metadata;

import java.sql.Connection;
import java.util.Optional;

public interface MusicDao {


    void storeMetadata(Connection connection, String identifier, Metadata metadata);

    Optional<Metadata> getMetadata(Connection connection, String identifier);
}
