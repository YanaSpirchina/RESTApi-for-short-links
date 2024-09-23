package test.spring.restapi.services;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import test.spring.restapi.models.LinkResponse;
import test.spring.restapi.repositories.LinkRepository;
import test.spring.restapi.util.LinkIsExpiredException;
import test.spring.restapi.util.LinkNotFoundException;
import test.spring.restapi.util.LinksTooManyRequestsException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
@Transactional(readOnly = true)
public class LinkService {

    final private LinkRepository linkRepository;

    final private RedisTemplate<String, String> redisTemplate;

    private static final String SEMAPHORE_KEY = "semaphore_key";
    private static final int MAX_PERMITS = 2;
    private static final long TTL_MINUTES = 1;

    public LinkService(LinkRepository linkRepository, RedisTemplate<String, String> redisTemplate) {
        this.linkRepository = linkRepository;
        this.redisTemplate = redisTemplate;
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

    public LinkResponse findByShortName(String shortName) {
        Optional<LinkResponse> linkResponse = linkRepository.findByShortName(shortName);
        if (linkResponse.isEmpty()) {
            throw new LinkNotFoundException();
        }
        return linkResponse.get();
    }

    public void generateLinkResponse(LinkResponse linkResponse, String fullName, String hash) {
        linkResponse.setFullName(fullName);
        linkResponse.setHash(hash);
        linkResponse.setShortName("http://localhost:8080/api/" + hash);
        linkResponse.setTimeOfCreating(new Date());
    }

    public String generateHashForShortLink(String link) {
        if (!acquirePermit()) {
            throw new LinksTooManyRequestsException();
        }

        MessageDigest digest;

        try {
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            byte[] hash = digest.digest(link.getBytes());

            return Base64.getEncoder().encodeToString(hash).substring(0, 6);
        } finally {
            releasePermit();
        }
    }

    private boolean acquirePermit() {

        SetOperations<String, String> setOperations = redisTemplate.opsForSet();

        Long currentPermits = setOperations.size(SEMAPHORE_KEY);

        if (currentPermits >= MAX_PERMITS) {
            return false; // Лимит достигнут
        }

        String uniqueKey = UUID.randomUUID().toString();

        setOperations.add(SEMAPHORE_KEY, uniqueKey);
        redisTemplate.expire(SEMAPHORE_KEY, TTL_MINUTES, TimeUnit.MINUTES);

        return true;
    }

    private void releasePermit() {
        SetOperations<String, String> setOperations = redisTemplate.opsForSet();
        setOperations.remove(SEMAPHORE_KEY, "1");
    }
}
