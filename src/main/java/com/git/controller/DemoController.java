package com.git.controller;

import com.git.domain.AjaxJson;
import com.git.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DemoController {
    @Autowired
    private DemoService demoService;

    /**
     * 获取Redis分布式锁
     */
    @RequestMapping("acquire")
    @ResponseBody
    public AjaxJson acquire(String key) throws SerializationException, Exception {
        demoService.acquire(key);
        return new AjaxJson(true, "", null);
    }


}
