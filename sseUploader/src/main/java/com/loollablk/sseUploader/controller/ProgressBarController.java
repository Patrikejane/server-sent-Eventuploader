package com.loollablk.sseUploader.controller;


import com.loollablk.sseUploader.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

@CrossOrigin
@RestController
public class ProgressBarController {
    @Autowired
    FileUploadService fileUploadService;

    private Map<String, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private final  ExecutorService executor = Executors.newSingleThreadExecutor();

    @GetMapping("/home")
    public ResponseEntity<String> gethome(){
        String message = "";
        try {
            message = "Home Found successfull:!";
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (Exception e) {
            message = "Server Error Occured!";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }

    }

    @GetMapping("/progress")
    public SseEmitter eventEmitter() throws IOException {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);

        executor.submit(() -> {
            try {
                UUID guid = UUID.randomUUID();
                sseEmitters.put(guid.toString(), sseEmitter);
                sseEmitter.send(SseEmitter.event().name("GUI_ID").data(guid));
                sseEmitter.onCompletion(() -> sseEmitters.remove(guid.toString()));
                sseEmitter.onTimeout(() -> sseEmitters.remove(guid.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return sseEmitter;
    }


    @PostMapping("/upload/local")
    public ResponseEntity<String> uploadFileLocal(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("guid") String guid) throws IOException {
        String message = "";
        try {
            fileUploadService.save(file, sseEmitters.get(guid), guid);
            sseEmitters.remove(guid);
            message = "Uploaded the file successfull:" + file.getOriginalFilename() + "!";
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (Exception e) {
            message = "Could not upload the file:" + file.getOriginalFilename() + "!";
            sseEmitters.remove(guid);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }

    }
}
