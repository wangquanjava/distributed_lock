package com.git.mapper;

import com.git.utils.SpringContextUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

@SuppressWarnings("all")
public class DemoRedisDao {
    private static final String charSet = "UTF-8";

    private static RedisTemplate redisTemplate = (RedisTemplate) SpringContextUtils.getBeanByClass(RedisTemplate.class);

    /**
     * @param id
     * @return 某个字段自增
     */
    public static Object inc(final String id) {
        return redisTemplate.execute(new RedisCallback() {
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.incr(id.getBytes());
            }
        });
    }

    /**
     * 最基础的添加类,原子性的
     *
     * @param key
     * @param value
     * @param liveTime 如果没有存活时间，就设置为0
     */
    public static void put(final byte[] key, final byte[] value, final long liveTime) {
        // 因为RedisCallback必须有一个泛型，所以种类随意指定了一个Long
        redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                // 开启事务
                connection.multi();
                connection.set(key, value);
                if (liveTime > 0) {
                    connection.expire(key, liveTime);
                }

                // 提交事务
                connection.exec();
                return null;
            }
        });
    }

    public static Boolean setNx(final byte[] key, final byte[] value, final long liveTime) {
        // 因为RedisCallback必须有一个泛型，所以种类随意指定了一个Long
        return (Boolean) redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                Boolean aBoolean = connection.setNX(key, value);

                if (aBoolean == true && liveTime > 0) {
                    connection.expire(key, liveTime);
                }
                return aBoolean;
            }
        });
    }

    public static byte[] getSet(final byte[] key, final byte[] value, final long liveTime) {
        // 因为RedisCallback必须有一个泛型，所以种类随意指定了一个Long
        return (byte[]) redisTemplate.execute(new RedisCallback<byte[]>() {
            @Override
            public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
                byte[] set = connection.getSet(key, value);
                if (ArrayUtils.isEmpty(set)) {
                    return new byte[0];
                }

                if (liveTime > 0) {
                    connection.expire(key, liveTime);
                }
                return set;
            }
        });
    }

    /**
     * 以字符串保存值
     *
     * @param key
     * @param value
     * @param liveTime
     * @throws UnsupportedEncodingException
     * @throws Exception
     */
    public static void putString(String key, String value, long liveTime) throws UnsupportedEncodingException {
        put(key.getBytes(charSet), value.getBytes(charSet), liveTime);

    }

    /**
     * 把对象序列化保存
     *
     * @param key
     * @param value
     * @param liveTime
     * @throws Exception
     * @throws SerializationException
     */
    public static void putObject(String key, Serializable value, long liveTime)
            throws SerializationException, Exception {
        put(key.getBytes(charSet), redisTemplate.getDefaultSerializer().serialize(value), liveTime);
    }

    public static Boolean setNx(String key, Serializable value, long liveTime)
            throws SerializationException, Exception {
        return setNx(key.getBytes(charSet), redisTemplate.getDefaultSerializer().serialize(value), liveTime);
    }

    public static byte[] getSet(String key, Serializable value, long liveTime)
            throws SerializationException, Exception {
        return getSet(key.getBytes(charSet), redisTemplate.getDefaultSerializer().serialize(value), liveTime);
    }

    /**
     * 通过key获取值的最基本方法
     *
     * @param key
     * @return
     */
    public static byte[] get(final byte[] key) {
        return (byte[]) redisTemplate.execute(new RedisCallback<byte[]>() {

            @Override
            public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.get(key);
            }

        });
    }

    public static String getString(String key) {
        try {
            return new String(get(key.getBytes(charSet)), charSet);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static Object getObject(String key) {
        try {
            return redisTemplate.getDefaultSerializer().deserialize(get(key.getBytes(charSet)));
        } catch (SerializationException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static Object getSetObject(String key, Serializable value, long liveTime) {
        try {
            return redisTemplate.getDefaultSerializer().deserialize(getSet(key.getBytes(charSet),redisTemplate.getDefaultSerializer().serialize(value), 0 ));
        } catch (SerializationException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 最基础的增加list操作
     *
     * @param key
     * @param value
     * @param liveTime 如果没有存活时间，就设置为0
     */
    public static void putList(final byte[] key, final byte[] value, final long liveTime) {
        redisTemplate.execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                connection.rPush(key, value);

                if (liveTime > 0) {
                    connection.expire(key, liveTime);
                }
                return null;
            }
        });
    }

    /**
     * 把对象序列化保存
     *
     * @param key
     * @param value
     * @param liveTime
     * @throws Exception
     * @throws SerializationException
     */
    public static void putListObject(String key, Serializable value, long liveTime)
            throws SerializationException, Exception {
        putList(key.getBytes(charSet), redisTemplate.getDefaultSerializer().serialize(value), liveTime);
    }

    /**
     * 通过key获取值的最基本方法
     *
     * @param key
     * @return
     */
    public static Long getListCount(final byte[] key) {
        return (Long) redisTemplate.execute(new RedisCallback<Long>() {

            @Override
            public Long doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.lLen(key);
            }

        });
    }

    /**
     * 通过key获取值的最基本方法
     *
     * @param key
     * @return
     */
    public static byte[] getListByIndex(final byte[] key, final long index) {
        return (byte[]) redisTemplate.execute(new RedisCallback<byte[]>() {

            @Override
            public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.lIndex(key, index);
            }

        });
    }

    public static Object getListObjectByIndex(String key, long index) {
        try {
            return redisTemplate.getDefaultSerializer().deserialize(getListByIndex(key.getBytes(charSet), index));
        } catch (SerializationException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过key获取值的最基本方法
     *
     * @param key
     * @return
     */
    public static byte[] removeListFirst(final byte[] key) {
        return (byte[]) redisTemplate.execute(new RedisCallback<byte[]>() {

            @Override
            public byte[] doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.lPop(key);
            }

        });
    }

}
