package com.loollablk.sseUploader.service;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public interface FileUploadService {


    public void save(MultipartFile file, SseEmitter sseEmitter, String guid);



}
