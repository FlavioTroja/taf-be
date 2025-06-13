package it.overzoom.taf.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import it.overzoom.taf.model.Municipal;

@Repository
public interface MunicipalRepository extends MongoRepository<Municipal, String> {

    Optional<Municipal> findByCityAndProvinceAndRegion(String city, String province, String region);

    Optional<Municipal> findByCityAndProvince(String city, String province);
}
