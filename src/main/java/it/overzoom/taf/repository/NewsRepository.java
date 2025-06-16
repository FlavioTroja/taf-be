package it.overzoom.taf.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.overzoom.taf.model.News;

@Repository
public interface NewsRepository extends MongoRepository<News, String> {

}
