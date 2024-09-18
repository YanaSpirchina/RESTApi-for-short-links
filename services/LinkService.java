package test.spring.restapi.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.spring.restapi.models.LinkResponse;
import test.spring.restapi.repositories.LinkRepository;
import test.spring.restapi.util.LinkIsExpiredException;
import test.spring.restapi.util.LinkNotFoundException;

import java.util.Date;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class LinkService {

    final private LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Transactional
    public void save(LinkResponse linkResponse) {
        linkRepository.save(linkResponse);
    }

    public LinkResponse findByFullName(String fullName) {
        return linkRepository.findByFullName(fullName).orElse(null);
    }

    public LinkResponse findLongLinkByHash(String hash) {
        Optional<LinkResponse> linkResponse = linkRepository.findByHash(hash);
        if (linkResponse.isEmpty()) {
            throw new LinkNotFoundException();
        }

        long duration = Math.abs(linkResponse.get().getTimeOfCreating().getTime() - new Date().getTime());
        // 600000  милисекунд = 10 минут
        if (duration > 600000) {
            throw new LinkIsExpiredException();
        }
        return linkResponse.get();
    }

    public LinkResponse findByShortName(String shortName){
        return linkRepository.findByShortName(shortName).orElse(null);
    }

    public LinkResponse generateLinkResponse(LinkResponse linkResponse, String fullName, String hash){

        linkResponse.setFullName(fullName);
        linkResponse.setHash(hash);
        linkResponse.setShortName("http://localhost:8080/api/" + hash);
        linkResponse.setTimeOfCreating(new Date());

        return linkResponse;
    }

}
