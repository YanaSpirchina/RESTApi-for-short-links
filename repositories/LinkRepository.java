package test.spring.restapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import test.spring.restapi.models.LinkResponse;

import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<LinkResponse, Integer> {
    Optional<LinkResponse> findByShortName(String shortName);
    Optional<LinkResponse> findByFullName(String fullName);

    Optional<LinkResponse> findByHash(String hash);

}
