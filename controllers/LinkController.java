package test.spring.restapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import test.spring.restapi.dto.LinkResponseDTO;
import test.spring.restapi.models.LinkResponse;
import test.spring.restapi.services.LinkService;
import test.spring.restapi.util.LinkErrorResponse;
import test.spring.restapi.util.LinkIsExpiredException;
import test.spring.restapi.util.LinkNotFoundException;
import test.spring.restapi.util.LinksTooManyRequestsException;

@RestController
@RequestMapping("/api")
public class LinkController {

    final private LinkService linkService;

    @Autowired
    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }


    @PostMapping("/createShort")
    public ResponseEntity<LinkResponse> createShortLink(@RequestBody LinkResponseDTO linkResponseDTO) {
        LinkResponse linkResponse = linkService.findByFullName(linkResponseDTO.getName());

        if (linkResponse != null) {
            return new ResponseEntity<>(linkResponse, HttpStatus.FOUND);
        }

        String hash = linkService.generateHashForShortLink(linkResponseDTO.getName());

        linkResponse = new LinkResponse();
        linkService.generateLinkResponse(linkResponse, linkResponseDTO.getName(), hash);

        linkService.save(linkResponse);

        return new ResponseEntity<>(linkResponse, HttpStatus.CREATED);
    }

    @GetMapping("/getLongLink")
    public ResponseEntity<LinkResponse> getLongLink(@RequestBody LinkResponseDTO linkResponseDTO) {
        LinkResponse linkResponse = linkService.findByShortName(linkResponseDTO.getName());

        return new ResponseEntity<>(linkResponse, HttpStatus.OK);
    }

    @GetMapping("/{hash}")
    public ResponseEntity<?> redirectToLongLink(@PathVariable("hash") String hash) {

        LinkResponse linkResponse = linkService.findLongLinkByHash(hash);
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", linkResponse.getFullName())
                .build();

    }

    @ExceptionHandler
    private ResponseEntity<LinkErrorResponse> handleException(LinkNotFoundException e) {
        LinkErrorResponse linkErrorResponse =
                new LinkErrorResponse("link was not found!", System.currentTimeMillis());

        return new ResponseEntity<>(linkErrorResponse, HttpStatus.NOT_FOUND);

    }

    @ExceptionHandler
    private ResponseEntity<LinkErrorResponse> handleException(LinkIsExpiredException e) {
        LinkErrorResponse linkErrorResponse =
                new LinkErrorResponse("link is expired!", System.currentTimeMillis());

        return new ResponseEntity<>(linkErrorResponse, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
    }

    @ExceptionHandler
    private ResponseEntity<LinkErrorResponse> handleException(LinksTooManyRequestsException e) {
        LinkErrorResponse linkErrorResponse =
                new LinkErrorResponse("Too many concurrent requests, please try again later!",
                        System.currentTimeMillis());

        return new ResponseEntity<>(linkErrorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }
}
