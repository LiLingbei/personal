package org.lubei.bases.web.util;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取登录信息工具类
 *
 * @author LiLingbei
 */
public final class Subjects {

    private static final Logger LOGGER = LoggerFactory.getLogger(Subjects.class);

    private static final String CURRENT_USER_ID = "CURRENT_USER_ID";


    /**
     * 获取当前用户ID
     *
     * @param <T> 用户ID泛型
     * @return 用户ID
     * @throws ClassCastException 类型装换异常
     */
    public static <T> T getCurrentUserId() throws ClassCastException {
        Session session = getSubject().getSession();
        try {
            //noinspection unchecked
            return (T) session.getAttribute(CURRENT_USER_ID);
        } catch (InvalidSessionException e) {
            LOGGER.error("会话异常，未跟踪到异常原因，所以先打印error级别。", e);
            // subject.logout(); // 退出时也会调session.getAttribute报错
            throw new IllegalStateException("获取当前用户ID失败！");
        }
    }

    /**
     * 设置当前用户ID
     *
     * @param userId 用户ID
     */
    public static void setCurrentUserId(Object userId) {
        Session session = getSubject().getSession();
        try {
            session.setAttribute(CURRENT_USER_ID, userId);
        } catch (InvalidSessionException e) {
            LOGGER.error("会话异常，未跟踪到异常原因，所以先打印error级别。", e);
            // subject.logout(); // 退出时也会调session.getAttribute报错
            throw new IllegalStateException("设置当前用户ID失败！");
        }
    }

    /**
     * 获取登录唯一标识
     *
     * @param <T> 唯一标识泛型
     * @return 唯一标识
     * @throws ClassCastException 类型装换异常
     */
    public static <T> T getPrincipal() throws ClassCastException {
        Object principal = getSubject().getPrincipal();
        //noinspection unchecked
        return (T) checkNotNull(principal, "获取登录唯一标识失败！");
    }

    /**
     * 清空认证缓存
     */
    public static void clearAuthenticationCache() {
        RealmSecurityManager manager = (RealmSecurityManager) SecurityUtils.getSecurityManager();
        for (Realm realm : manager.getRealms()) {
            ((AuthorizingRealm) realm).getAuthenticationCache().clear();
        }
    }

    /**
     * 获取Subject对象
     *
     * @return Subject对象
     */
    public static Subject getSubject() {
        Subject sub = SecurityUtils.getSubject();
        checkState(sub.isAuthenticated() || sub.isRemembered(), "登录异常！");
        return sub;
    }

    private Subjects() {
        throw new IllegalAccessError();
    }
}
