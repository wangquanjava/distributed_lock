package com.git.service;

import com.git.domain.DemoEntity;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.UnsupportedEncodingException;

public interface DemoService {
	Boolean acquire(String key) throws Exception;
}
