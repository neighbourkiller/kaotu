package com.kaotu;

import com.kaotu.base.model.po.User;
import com.kaotu.user.mapper.UserMapper;
import com.kaotu.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SpringBootTest
class KaotuUserApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Test
    void testSelectById() {

        User user = userMapper.selectById("10000000");
        System.out.println(user);
    }

    @Test
    void testLogin() {
        User user = new User();
        user.setUserId("10000000");
        user.setPassword("123456");
        String token = userService.login(user);
        System.out.println(token);
    }

    private static final Logger logger= LoggerFactory.getLogger("browse");

    @Test
    void testLog(){
        // 示例：在您的代码中这样记录日志
        logger.info("用户ID: 1000001, 停留时间: 80s");
    }

/*    private static final Pattern LOG_PATTERN = Pattern.compile("用户ID: (\\d+), 停留时间: (\\d+)ms");
    public List<LogData> parseLogFile(String logFilePath) {
        List<LogData> results = new ArrayList<>();
        Path path = Paths.get(logFilePath);

        if (!Files.exists(path)) {
            System.err.println("日志文件未找到: " + logFilePath);
            return results;
        }

        // 使用 try-with-resources 确保流被正确关闭
        try (Stream<String> lines = Files.lines(path)) {
            lines.forEach(line -> {
                // 在日志行的消息部分查找匹配项
                Matcher matcher = LOG_PATTERN.matcher(line);
                if (matcher.find()) {
                    // group(1) 对应第一个括号 (\\d+) 的内容，即 userId
                    String userId = matcher.group(1);
                    // group(2) 对应第二个括号 (\\d+) 的内容，即 duration
                    long duration = Long.parseLong(matcher.group(2));
                    results.add(new LogData(userId, duration));
                }
            });
        } catch (IOException e) {
            System.err.println("读取日志文件时出错: " + e.getMessage());
        }

        return results;
    }

    @Test
    public void testRead  () {
        // 假设日志文件在项目根目录的 logs 文件夹下
        // 请将 "browse.20231027.log" 替换为您的实际文件名
        String logFile = "logs/browse.20231027.log";

        List<String> parsedData = analyzer.parseLogFile(logFile);

        System.out.println("成功解析 " + parsedData.size() + " 条日志记录。");
        for (LogData data : parsedData) {
            System.out.println("用户ID: " + data.userId() + ", 停留时间: " + data.duration() + "ms");
        }*/
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void testRedis(){
//        redisTemplate.opsForValue().set("key","value");
        redisTemplate.delete("key");
        String value = (String) redisTemplate.opsForValue().get("key");
        System.out.println(value);

    }

}
