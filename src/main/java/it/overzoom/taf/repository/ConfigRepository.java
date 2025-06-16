package it.overzoom.taf.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.overzoom.taf.mapper.Config;

@Repository
public interface ConfigRepository extends MongoRepository<Config, String> {

    List<Config> findByRolesIn(String[] roles);
}
