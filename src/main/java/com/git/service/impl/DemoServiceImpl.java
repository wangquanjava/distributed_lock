package com.git.service.impl;

import com.git.mapper.DemoRedisDao;
import com.git.service.DemoService;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DemoServiceImpl implements DemoService {

    public static final int EXPRI_TIME = 60000;

    /**
     * 分布式redis锁
     * 可以解决redis崩溃时，超时时间失效的问题
     * 仍然没有解决，过了超时时间，仍然没有执行完成，其他线程也获取到锁的问题
     */
    @Override
    public Boolean acquire(String key) throws Exception {
        // 尝试获得锁
        Boolean aBoolean = DemoRedisDao.setNx(key, DateUtils.addMilliseconds(new Date(), EXPRI_TIME), 0L);
        if (aBoolean) {
            // 获取成功
            return aBoolean;
        } else {
            while (true) {
                // 取出里面的值
                Date redisDate = (Date) DemoRedisDao.getObject(key);
                if (!redisDate.after(new Date())) {
                    // 如果超时了
                    Date getSetDate = (Date) DemoRedisDao.getSetObject(key, DateUtils.addMilliseconds(new Date(), 60000), 0L);
                    if (!getSetDate.after(new Date())) {
                        // 如果getSet返回的时间已经过期了，证明获取成功
                        return true;
                    }
                }

                // 10毫秒尝试获取一次
                Thread.sleep(10);
            }

        }
    }


}
