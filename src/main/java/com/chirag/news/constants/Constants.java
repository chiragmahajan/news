package com.chirag.news.constants;

public class Constants {
    public final static String LOGIN_CACHE = "loginCache";
    public final static String CAFFEINE_CACHE_MANAGER = "caffeineCacheManager";
    public final static String USER_NEWS_CACHE = "userNewsCacheManager";

    public final static String MASTER_PERSISTENCE_UNIT_NAME = "masterPersistenceUnitName";
    public final static String SLAVE_PERSISTENCE_UNIT_NAME = "slavePersistenceUnitName";
    public final static String MASTER_TRANSACTION_MANAGER = "masterTransactionManager";
    public final static String SLAVE_TRANSACTION_MANAGER = "slaveTransactionManager";

    public final static String DEFAULT_TIMESTAMP = "DATETIME DEFAULT CURRENT_TIMESTAMP";
    public final static String ON_UPDATE_DEFAULT_TIMESTAMP = "DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP";

    public final static String CACHE_ALL_LABELS_KEY = "cacheAllLabelKey";
    public final static String BOOKMARK_KEY = "bookmarkKey";
    public final static String LIKED_NEWS_BY_USER_KEY = "userLikedKey";
    public final static String TOTAL_LIKES_KEY = "totalLikesKey";
    public final static String ALL_NEWS_KEY = "allNewsKeys";
    public final static String USER_NEWS_KEY = "userNewsKey";

}
