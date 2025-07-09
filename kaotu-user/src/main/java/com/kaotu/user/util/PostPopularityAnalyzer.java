package com.kaotu.user.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaotu.base.model.po.HotPost;
import com.kaotu.user.mapper.HotPostMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
public class PostPopularityAnalyzer {

    @Data
    private static class PostStat {
        private Long postId;
        private int viewCount = 0;       // 浏览次数
        private int likeCount = 0;       // 点赞数
        private int collectCount = 0;    // 收藏数
        private long viewDurationSeconds = 0; // 总浏览时长(秒)

        // 计算热门度评分
        public double getPopularityScore() {
            // 权重：浏览量40%，点赞30%，收藏20%，浏览时长10%
            return viewCount * 0.4 + likeCount * 0.3 + collectCount * 0.2 + (viewDurationSeconds / 60.0) * 0.1;
        }
    }

    private static final Pattern POST_ID_PATTERN = Pattern.compile("帖子id: (\\d+)");
    private static final Pattern VIEW_DURATION_PATTERN = Pattern.compile("浏览时长: (\\d+)秒");

    // 缓存最近计算的热门帖子结果
    private List<Long> cachedTopPosts = new ArrayList<>();

    @Value("${kaotu.log.directory}")
    private String logDirectory;

    private final int topPostsCount = 5; // 默认返回前5个热门帖子
    private final int recentDays = 3;    // 只分析最近3天的日志

    @PostConstruct
    public void init() {
        calculateDailyPopularPosts();
    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void calculateDailyPopularPosts() {
        log.info("开始计算每日热门帖子...");
        try {
            List<Long> topPosts = getTopPopularPosts(logDirectory, topPostsCount);
            this.cachedTopPosts = topPosts;
            saveToRedis(topPosts);
            saveToDb(topPosts);
            log.info("计算每日热门帖子完成，热门帖子IDs: {}", topPosts);
        } catch (Exception e) {
            log.error("计算热门帖子时发生错误", e);
        }
    }
    // Redis键名常量
    private static final String REDIS_KEY_HOT_POSTS = "kaotu:hot:posts";

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    private void saveToRedis(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            log.warn("没有热门帖子数据可保存到Redis");
            redisTemplate.delete(REDIS_KEY_HOT_POSTS);
            return;
        }
        try {
            // 直接将List对象存入Redis，并设置24小时过期时间
            redisTemplate.opsForValue().set(REDIS_KEY_HOT_POSTS, postIds, 24, TimeUnit.HOURS);
            log.info("热门帖子数据已直接以List形式保存到Redis, key={}, value={}", REDIS_KEY_HOT_POSTS, postIds);
        } catch (Exception e) {
            log.error("保存热门帖子数据到Redis失败", e);
        }
    }

    @Autowired
    private HotPostMapper hotPostMapper;

    private void saveToDb(List<Long> postIds) {
        log.info("保存热门帖子到数据库: {}", postIds);
        if (postIds == null || postIds.isEmpty()) {
            log.warn("没有热门帖子数据可保存到数据库");
            return;
        }
/*        if(postIds.size()<topPostsCount){

        }*/
        String hot = postIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        HotPost hotPost = new HotPost();
        hotPost.setPostIds(hot);
        hotPost.setCalculationDate(LocalDate.now());
        hotPostMapper.insert(hotPost);
    }

    public List<Long> getHotPostsFromRedis() {
        try {
            Object rawResult = redisTemplate.opsForValue().get(REDIS_KEY_HOT_POSTS);
            if (rawResult instanceof List) {
                // 确认取出的对象是List类型后进行转换
                return (List<Long>) rawResult;
            }
            if (rawResult != null) {
                log.warn("Redis中key '{}' 的数据类型不是List, 实际类型: {}", REDIS_KEY_HOT_POSTS, rawResult.getClass().getName());
            }
        } catch (Exception e) {
            log.error("从Redis获取热门帖子数据失败", e);
        }
        // Redis中无数据或获取失败时，返回内存缓存
        return getCachedTopPopularPosts();
    }

    /**
     * 获取热门帖子列表(优先从Redis获取)
     * @return 热门帖子ID列表
     */
    public List<Long> getHotPosts() {
        List<Long> postsFromRedis = getHotPostsFromRedis();
        // 如果Redis返回的不是空列表，则使用Redis的结果
        if (postsFromRedis != null && !postsFromRedis.isEmpty()) {
            return postsFromRedis;
        }
        // 否则使用数据库中的最新热门帖子数据
        return getPostIdsFromDb();
    }

    /**
     * 获取缓存的热门帖子列表
     * @return 热门帖子ID列表
     */
    public List<Long> getCachedTopPopularPosts() {
        return new ArrayList<>(cachedTopPosts);
    }

    public List<Long> getPostIdsFromDb(){
        HotPost hotPost = hotPostMapper.selectOne(new LambdaQueryWrapper<HotPost>()
                .orderByDesc(HotPost::getCalculationDate)
                .last("limit 1"));
        if (hotPost != null && hotPost.getPostIds() != null) {
            String[] postIdArray = hotPost.getPostIds().split(",");
            return Arrays.stream(postIdArray)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
        } else {
            return getCachedTopPopularPosts();
        }
    }

    /**
     * 分析日志目录获取热门帖子
     *
     * @param logDir 日志目录
     * @param topN   返回前N个热门帖子
     * @return 热门帖子ID列表
     */
    public List<Long> getTopPopularPosts(String logDir, int topN) {
        Map<Long, PostStat> postStatsMap = new HashMap<>();

        // 遍历日志目录
        File dir = new File(logDir);
        if (dir.exists() && dir.isDirectory()) {
            // 获取最近3天的日志目录
            List<File> recentLogDirs = getRecentLogDirs(dir, recentDays);
            log.info("分析最近{}天的日志目录: {}", recentDays,
                    recentLogDirs.stream().map(File::getName).collect(Collectors.joining(", ")));

            for (File dateDir : recentLogDirs) {
                File otherLog = new File(dateDir, "other.log");
                if (otherLog.exists()) {
                    processLogFile(otherLog, postStatsMap);
                }
            }
        }

        // 按热度得分排序并返回前N个帖子ID
        return postStatsMap.values().stream()
                .sorted(Comparator.comparing(PostStat::getPopularityScore).reversed())
                .limit(topN)
                .map(PostStat::getPostId)
                .collect(Collectors.toList());
    }

    /**
     * 获取最近N天的日志目录
     *
     * @param logDir 日志根目录
     * @param days 天数
     * @return 最近N天的日志目录列表
     */
    private List<File> getRecentLogDirs(File logDir, int days) {
        File[] allDirs = logDir.listFiles(File::isDirectory);
        if (allDirs == null || allDirs.length == 0) {
            return Collections.emptyList();
        }

        // 假设日志目录命名格式为 yyyy-MM-dd
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // 将目录按日期排序
        List<File> sortedDirs = Arrays.stream(allDirs)
                .filter(dir -> {
                    try {
                        dateFormat.parse(dir.getName());
                        return true;
                    } catch (ParseException e) {
                        return false; // 忽略不符合日期格式的目录
                    }
                })
                .sorted((dir1, dir2) -> {
                    try {
                        Date date1 = dateFormat.parse(dir1.getName());
                        Date date2 = dateFormat.parse(dir2.getName());
                        return date2.compareTo(date1); // 降序排序，最新的日期在前面
                    } catch (ParseException e) {
                        return 0;
                    }
                })
                .collect(Collectors.toList());

        // 返回最近days天的目录
        return sortedDirs.stream()
                .limit(days)
                .collect(Collectors.toList());
    }

    /**
     * 处理单个日志文件，提取帖子统计信息
     *
     * @param logFile      日志文件
     * @param postStatsMap 存储帖子统计信息的Map
     */
    private void processLogFile(File logFile, Map<Long, PostStat> postStatsMap) {
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 提取帖子ID
                Matcher postIdMatcher = POST_ID_PATTERN.matcher(line);
                if (!postIdMatcher.find()) {
                    continue;
                }

                Long postId = Long.valueOf(postIdMatcher.group(1));
                PostStat stat = postStatsMap.computeIfAbsent(postId, id -> {
                    PostStat s = new PostStat();
                    s.setPostId(id);
                    return s;
                });

                // 处理不同类型的记录
                if (line.contains("浏览-帖子id:")) {
                    stat.setViewCount(stat.getViewCount() + 1);

                    // 检查是否有浏览时长
                    Matcher durationMatcher = VIEW_DURATION_PATTERN.matcher(line);
                    if (durationMatcher.find()) {
                        long duration = Long.parseLong(durationMatcher.group(1));
                        stat.setViewDurationSeconds(stat.getViewDurationSeconds() + duration);
                    }
                } else if (line.contains("点赞-帖子id:")) {
                    stat.setLikeCount(stat.getLikeCount() + 1);
                } else if (line.contains("取消点赞-帖子id:")) {
                    stat.setLikeCount(stat.getLikeCount() - 1);
                } else if (line.contains("收藏-帖子id:")) {
                    stat.setCollectCount(stat.getCollectCount() + 1);
                } else if (line.contains("取消收藏-帖子id:")) {
                    stat.setCollectCount(stat.getCollectCount() - 1);
                }
            }
        } catch (IOException e) {
            log.error("处理日志文件出错", e);
        }
    }
}