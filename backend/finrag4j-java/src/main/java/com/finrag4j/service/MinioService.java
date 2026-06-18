package com.finrag4j.service;

import com.finrag4j.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO文件存储服务
 * 
 * 功能说明：
 * - 文件上传、下载、删除
 * - 生成临时访问URL
 * - 自动创建bucket
 * 
 * @author FinRag4j Team
 * @version 1.0.0
 */
@Slf4j
@Service
@Tag(name = "MinIO文件存储服务")
public class MinioService {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MinioConfig minioConfig;

    /**
     * 初始化：创建默认bucket
     */
    @PostConstruct
    public void init() {
        try {
            String bucketName = minioConfig.getBucketName();
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("MinIO bucket创建成功: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO bucket初始化失败", e);
        }
    }

    /**
     * 上传文件
     * 
     * @param file 文件
     * @param objectName 对象名称（包含路径）
     * @return 文件访问路径
     */
    @Operation(summary = "上传文件")
    public String uploadFile(MultipartFile file, String objectName) {
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            log.info("文件上传成功: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("文件上传失败: {}", objectName, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 下载文件
     * 
     * @param objectName 对象名称
     * @return 文件流
     */
    @Operation(summary = "下载文件")
    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败: {}", objectName, e);
            throw new RuntimeException("文件下载失败", e);
        }
    }

    /**
     * 删除文件
     * 
     * @param objectName 对象名称
     */
    @Operation(summary = "删除文件")
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            log.info("文件删除成功: {}", objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", objectName, e);
            throw new RuntimeException("文件删除失败", e);
        }
    }

    /**
     * 生成临时访问URL
     * 
     * @param objectName 对象名称
     * @param expires 过期时间（秒）
     * @return 访问URL
     */
    @Operation(summary = "生成临时访问URL")
    public String getPresignedUrl(String objectName, int expires) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(expires, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("生成临时URL失败: {}", objectName, e);
            throw new RuntimeException("生成临时URL失败", e);
        }
    }

    /**
     * 检查文件是否存在
     * 
     * @param objectName 对象名称
     * @return 是否存在
     */
    @Operation(summary = "检查文件是否存在")
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}