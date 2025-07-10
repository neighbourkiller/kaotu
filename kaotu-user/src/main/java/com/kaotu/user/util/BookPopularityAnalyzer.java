package com.kaotu.user.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaotu.base.model.po.HotBook;
import com.kaotu.user.mapper.HotBookMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BookPopularityAnalyzer {

    @Data
    private static class BookStat {
        private Integer bookId;
        private int browseCount = 0;    // 浏览次数
        private int commentCount = 0;   // 评论次数
        private int collectCount = 0;   // 收藏次数

        // 定义不同行为的权重
        private static final double BROWSE_WEIGHT = 1.0;
        private static final double COMMENT_WEIGHT = 3.0;
        private static final double COLLECT_WEIGHT = 5.0;

        // 计算热门度评分
        public double getPopularityScore() {
            return browseCount * BROWSE_WEIGHT +
                    commentCount * COMMENT_WEIGHT +
                    collectCount * COLLECT_WEIGHT;
        }
    }

    private static final Pattern BOOK_ID_PATTERN = Pattern.compile("书籍ID: (\\d+)");
    private static final String REDIS_KEY_HOT_BOOKS = "kaotu:hot:books";

    @Value("${kaotu.log.directory}")
    private String logDirectory;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private HotBookMapper hotBookMapper; // 注入HotBookMapper

    private final int topBooksCount = 12; // 返回前12个热门书籍
    private final int recentDays = 5;     // 只分析最近5天的日志

    @PostConstruct
    public void init() {
        calculateDailyPopularBooks();
    }

    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    public void calculateDailyPopularBooks() {
        log.info("开始计算每日热门书籍...");
        try {
            List<Integer> topBooks = getTopPopularBooks(logDirectory, topBooksCount);
            saveToRedis(topBooks);
            saveToDb(topBooks);
            log.info("计算每日热门书籍完成，热门书籍IDs: {}", topBooks);
        } catch (Exception e) {
            log.error("计算热门书籍时发生错误", e);
        }
    }

    public List<Integer> getHotBooks(){
        // 从Redis获取热门书籍
        log.info("尝试从Redis获取热门书籍数据...");
        Object cachedBooks = redisTemplate.opsForValue().get(REDIS_KEY_HOT_BOOKS);
        if (cachedBooks instanceof List) {
            return (List<Integer>) cachedBooks;
        }

        log.info("Redis中没有找到热门书籍数据，尝试从数据库获取...");
        // 如果Redis中没有数据，则从数据库获取
        LambdaQueryWrapper<HotBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotBook::getCalculationDate, LocalDate.now())
                .orderByDesc(HotBook::getCalculationDate);
        HotBook hotBook = hotBookMapper.selectOne(queryWrapper);
        if (hotBook != null && hotBook.getBookIds() != null) {
            return Arrays.stream(hotBook.getBookIds().split(","))
                    .map(Integer::valueOf)
                    .collect(Collectors.toList());
        }

        log.warn("没有找到今日的热门书籍数据");
        return Collections.emptyList();
    }

    private void saveToRedis(List<Integer> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            log.warn("没有热门书籍数据可保存到Redis");
            redisTemplate.delete(REDIS_KEY_HOT_BOOKS);
            return;
        }
        try {
            redisTemplate.opsForValue().set(REDIS_KEY_HOT_BOOKS, bookIds, 24, TimeUnit.HOURS);
            log.info("热门书籍数据已保存到Redis, key={}, value={}", REDIS_KEY_HOT_BOOKS, bookIds);
        } catch (Exception e) {
            log.error("保存热门书籍数据到Redis失败", e);
        }
    }

    private void saveToDb(List<Integer> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            log.warn("没有热门书籍数据可保存到数据库");
            return;
        }
        String hot = bookIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        HotBook hotBook = new HotBook(); // 使用HotBook实体
        hotBook.setBookIds(hot);
        hotBook.setCalculationDate(LocalDate.now());
        hotBookMapper.insert(hotBook);
        log.info("热门书籍数据已保存到数据库");
    }

    public List<Integer> getTopPopularBooks(String logDir, int topN) {
        Map<Integer, BookStat> bookStatsMap = new HashMap<>();

        File dir = new File(logDir);
        if (dir.exists() && dir.isDirectory()) {
            List<File> recentLogDirs = getRecentLogDirs(dir, recentDays);
            log.info("分析最近{}天的书籍日志目录: {}", recentDays,
                    recentLogDirs.stream().map(File::getName).collect(Collectors.joining(", ")));

            for (File dateDir : recentLogDirs) {
                File browseLog = new File(dateDir, "browse.log"); // 指定分析browse.log
                if (browseLog.exists()) {
                    processLogFile(browseLog, bookStatsMap);
                }
            }
        }

        return bookStatsMap.values().stream()
                .sorted(Comparator.comparing(BookStat::getPopularityScore).reversed())
                .limit(topN)
                .map(BookStat::getBookId)
                .collect(Collectors.toList());
    }

    private List<File> getRecentLogDirs(File logDir, int days) {
        File[] allDirs = logDir.listFiles(File::isDirectory);
        if (allDirs == null) return Collections.emptyList();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return Arrays.stream(allDirs)
                .filter(dir -> {
                    try {
                        dateFormat.parse(dir.getName());
                        return true;
                    } catch (ParseException e) {
                        return false;
                    }
                })
                .sorted(Comparator.comparing(File::getName).reversed()) // 按名称降序（即日期降序）
                .limit(days)
                .collect(Collectors.toList());
    }

    private void processLogFile(File logFile, Map<Integer, BookStat> bookStatsMap) {
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher bookIdMatcher = BOOK_ID_PATTERN.matcher(line);
                if (!bookIdMatcher.find()) {
                    continue;
                }

                Integer bookId = Integer.valueOf(bookIdMatcher.group(1));
                BookStat stat = bookStatsMap.computeIfAbsent(bookId, id -> {
                    BookStat s = new BookStat();
                    s.setBookId(id);
                    return s;
                });

                if (line.contains("收藏书籍")) {
                    stat.setCollectCount(stat.getCollectCount() + 1);
                } else if (line.contains("评论书籍")) {
                    stat.setCommentCount(stat.getCommentCount() + 1);
                } else if (line.contains("浏览书籍")) {
                    stat.setBrowseCount(stat.getBrowseCount() + 1);
                }
            }
        } catch (IOException e) {
            log.error("处理书籍日志文件出错: {}", logFile.getAbsolutePath(), e);
        }
    }
}