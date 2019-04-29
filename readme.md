
[TOC]

# shiro 整合Spring + Spring mvc

## 引入相关jar包

- shiro-core
- shiro-spring

## shiro 过滤器

shiro 的拦截器链是代理serlvet自带的拦截器链的，它必须是先于servlet的Filter执行

因此在web.xml中配置的过滤器类是过滤器代理类：

- DelegatingFilterProxy

但其实shiro过滤器是以下两个类：

- AccessControlFilter
- ShiroFilter

其继承关系如下图所示：

![image](https://raw.githubusercontent.com/Autom-liu/shiro-learn/ss/image/AccessControlFilter.png)

![image](https://raw.githubusercontent.com/Autom-liu/shiro-learn/ss/image/ShiroFilter.png)

### OncePerRequestFilter

主要用于防止多次进入拦截器链，也就是说一次请求只会进入一次拦截器链
        另外它还提供了enable属性，表示是否开启该拦截器链，如果不想让该拦截器链工作，设置为`False`即可
```java
/**
     * Determines generally if this filter should execute or let requests fall through to the next chain element.
     *
     * @see #isEnabled()
     */
    private boolean enabled = true; //most filters wish to execute when configured, so default to true
```

### ShiroFilter

是整个shiro的入口点，拦截需要安全控制的请求处理

它用于纯selvet环境的，如果需要整合spring，需要用到另外一个过滤器工厂

ShiroFilterFactoryBean

在这个类源码注释中有详细的描述如何使用

它有一个内部类，专门生产基于spring的shiro过滤器对象

同时还有一个重要的属性，就是接下来要描述的安全控制中心。

```java
public class ShiroFilterFactoryBean implements FactoryBean, BeanPostProcessor {
    // ...
    private SecurityManager securityManager;

    private Map<String, Filter> filters;

    private Map<String, String> filterChainDefinitionMap; //urlPathExpression_to_comma-delimited-filter-chain-definition

    private String loginUrl;
    private String successUrl;
    private String unauthorizedUrl;

    private AbstractShiroFilter instance;
    // ... 此处省略万行代码
}
```

需要spring即可注入：

```xml
<bean class="org.apache.shiro.spring.web.ShiroFilterFactoryBean" id="shiroFilter">
    <property name="securityManager" ref="securityManager" />
    <property name="loginUrl" value="/user/login" />
    <property name="unauthorizedUrl" value="/403.html" />
    <property name="filterChainDefinitions">
        <value>
            /user/login = anon
            /** = authc
        </value>
    </property>
</bean>
```

### AdviceFilter

提供了AOP支持，类似于spring mvc 的拦截器interceptor

相关源码如下：

```java
    /**
     * 这个方法类似于拦截器的前置增强
     * Returns {@code true} if the filter chain should be allowed to continue, {@code false} otherwise.
     * It is called before the chain is actually consulted/executed.
     * <p/>
     * The default implementation returns {@code true} always and exists as a template method for subclasses.
     *
     * @param request  the incoming ServletRequest
     * @param response the outgoing ServletResponse
     * @return {@code true} if the filter chain should be allowed to continue, {@code false} otherwise.
     * @throws Exception if there is any error.
     */
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        return true;
    }
```

```java
    /**
     * 这个方法类似于后置返回增强，在过滤器返回后执行
     * Allows 'post' advice logic to be called, but only if no exception occurs during filter chain execution.  That
     * is, if {@link #executeChain executeChain} throws an exception, this method will never be called.  Be aware of
     * this when implementing logic.  Most resource 'cleanup' behavior is often done in the
     * {@link #afterCompletion(javax.servlet.ServletRequest, javax.servlet.ServletResponse, Exception) afterCompletion(request,response,exception)}
     * implementation, which is guaranteed to be called for every request, even when the chain processing throws
     * an Exception.
     * <p/>
     * The default implementation does nothing (no-op) and exists as a template method for subclasses.
     *
     * @param request  the incoming ServletRequest
     * @param response the outgoing ServletResponse
     * @throws Exception if an error occurs.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {
    }
```

> 除此之外，还有一个afterCompletion 最终增强，不管有无异常都会进行

这个类只是给出了默认空实现，下面的子类才是真正的实现。

### PathMatchingFilter

提供了请求路径匹配的功能

主要看它的`preHandle`方法：

```java
    /**
     * Implementation that handles path-matching behavior before a request is evaluated.  If the path matches and
     * the filter
     * {@link #isEnabled(javax.servlet.ServletRequest, javax.servlet.ServletResponse, String, Object) isEnabled} for
     * that path/config, the request will be allowed through via the result from
     * {@link #onPreHandle(javax.servlet.ServletRequest, javax.servlet.ServletResponse, Object) onPreHandle}.  If the
     * path does not match or the filter is not enabled for that path, this filter will allow passthrough immediately
     * to allow the {@code FilterChain} to continue executing.
     * <p/>
     * In order to retain path-matching functionality, subclasses should not override this method if at all
     * possible, and instead override
     * {@link #onPreHandle(javax.servlet.ServletRequest, javax.servlet.ServletResponse, Object) onPreHandle} instead.
     *
     * @param request  the incoming ServletRequest
     * @param response the outgoing ServletResponse
     * @return {@code true} if the filter chain is allowed to continue to execute, {@code false} if a subclass has
     *         handled the request explicitly.
     * @throws Exception if an error occurs
     */
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {

        if (this.appliedPaths == null || this.appliedPaths.isEmpty()) {
            if (log.isTraceEnabled()) {
                log.trace("appliedPaths property is null or empty.  This Filter will passthrough immediately.");
            }
            return true;
        }

        for (String path : this.appliedPaths.keySet()) {
            // If the path does match, then pass on to the subclass implementation for specific checks
            //(first match 'wins'):
            if (pathsMatch(path, request)) {
                log.trace("Current requestURI matches pattern '{}'.  Determining filter chain execution...", path);
                Object config = this.appliedPaths.get(path);
                return isFilterChainContinued(request, response, path, config);
            }
        }

        //no path matched, allow the request to go through:
        return true;
    }
```

该方法就用于配置的path和请求路径进行匹配的方法，如果匹配就返回true仅此而已，它并没有做权限校验
但接着`isFilterChainContinued`往下看，该方法最终是否拦截，是看`onPreHandle`方法，因此子类只需重写
该方法完成权限校验...
```java
    private boolean isFilterChainContinued(ServletRequest request, ServletResponse response,
                                           String path, Object pathConfig) throws Exception {
            // 此处省略万行代码....
            return onPreHandle(request, response, pathConfig);
            // 此处省略万行代码....
```

### AccessControlFilter

提供了权限控制的基础功能，如是否允许访问，访问拒绝时如何处理等等情况...

其中有两个抽象方法很重要：

isAccessAllowed：这个方法用来决定是否允许访问，参数mappedValue就是配置中拦截器的参数部分
    
```java
protected abstract boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception;
```

onAccessDenied： 这个方法用来描述被拒绝访问后如何处理
如果返回true表示需要继续处理，如果为false表示拦截器已经处理完成，直接返回即可。

```java
protected abstract boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception;
```

## SecurityManager 安全控制中心

可以先看一下SecurityManager的类图

![image](https://raw.githubusercontent.com/Autom-liu/shiro-learn/ss/image/DefaultWebSecurityManager.png)

可以说它的内容非常丰富，包含了接下来将会描述的所有内容
它作为控制中心，就像一个大公司一样，管理手下的好几个部门彼此协调，正常运转。
因此这里就顺便看一下有哪些需要了解的吧：

- AuthenticatingSecurityManager： 授权认证中心，主要属性为Authenticator，一个需要被实现的接口
- AuthorizingSecurityManager：权限校验管理中心，主要属性为Authorizer，一个需要被实现的接口
- RealmSecurityManager：Realm管理中心，主要属性为Realm集合，可以有多个Realm，也是一个需要被实现的接口
        
        当存在多个Realm的时候，需要由AuthenticationStrategy 策略决定Realm的决定关系（某个Realm不满足如何处理等）
        
- SessionsSecurityManager：会话管理中心，主要属性为sessionManager，同样又是需要被实现的接口。
- CachingSecurityManager：缓存管理中心，主要属性为cacheManager，同样又是需要被实现的接口

所有的**Subject 主体** 的请求都必须经过 安全控制中心，由它去委派其他管理中心执行任务，
同时安全控制中心还能使用一些工具，那就是加密服务

## AuthenticatingSecurityManager 授权认证中心

授权认证中心，主要属性为Authenticator，它是一个接口，在shiro中主要实现类为

ModularRealmAuthenticator

这个类包含两个重要属性：

```java
public class ModularRealmAuthenticator extends AbstractAuthenticator {
    private Collection<Realm> realms;
    private AuthenticationStrategy authenticationStrategy;
    // ...此处省略上万行代码
```

估计知道什么用的吧？就之前所描述，它就是将配置好的realms集合
根据authenticationStrategy策略来决定认证是否通过，authenticationStrategy是一个接口，shiro给我们提供了一些实现，默认实现是AtLeastOneSuccessfulStrategy（至少一个通过），如果有业务需求，可以仿照这些实现类进行自定义实现
当然如果只有一个realm，那么完全没有必要自定义实现

对于每一个Realm首先通过support方法判断程序是否需要使用shiro提供的UsernamePasswordToken这种token，如果返回false，则就不会调用接下来的getAuthenticationInfo进行认证了：

Realm接口的详细描述
```java
public interface Realm {
    String getName();
    boolean supports(AuthenticationToken token);
    AuthenticationInfo getAuthenticationInfo(AuthenticationToken token) throws AuthenticationException;
}
```

而对于用于认证的Realm，使用的是`AuthenticatingRealm`
这还是一个抽象类，实现了前两个方法，最后一个具体的认证逻辑getAuthenticationInfo仍然需要手动实现
```java
    protected abstract AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException;

```
虽然shiro提供了一些内置的Realm，但是在实际开发项目中很少用到它们
多数情况下都需要自定义实现Realm，但是自定义的realm并不是直接继承该类，在下一章节将会描述如何自定义

在此之前，先介绍一下密码匹配器

## CredentialsMatcher

密码匹配器，它作为认证中心的一个属性存在：

```java
public abstract class AuthenticatingRealm extends CachingRealm implements Initializable {
    //... 此处省略万行代码
    private CredentialsMatcher credentialsMatcher;
    //... 此处省略万行代码
}
```

在它里面用到该匹配器的地方，就是用来校验主体信息和用户信息是否匹配的：

```java

    protected void assertCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) throws AuthenticationException {
        CredentialsMatcher cm = getCredentialsMatcher();
        if (cm != null) {
            if (!cm.doCredentialsMatch(token, info)) {
                //not successful - throw an exception to indicate this:
                String msg = "Submitted credentials for token [" + token + "] did not match the expected credentials.";
                throw new IncorrectCredentialsException(msg);
            }
        } else {
            throw new AuthenticationException("A CredentialsMatcher must be configured in order to verify " +
                    "credentials during authentication.  If you do not wish for credentials to be examined, you " +
                    "can configure an " + AllowAllCredentialsMatcher.class.getName() + " instance.");
        }
    }
```

它是一个接口，里面只有一个用于校验凭证是否匹配的方法：

```java
public interface CredentialsMatcher {
    boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info);
}
```

shiro提供常用的有两个实现类：一种是`SimpleCredentialsMatcher`提供简单的密码校验，
另外一种是`HashedCredentialsMatcher` 提供基于常用hash加密算法校验。

当然如果对凭证校验这一块有特殊的业务需求，那也需要自己手动实现一下了

## AuthorizingSecurityManager

权限校验管理中心，主要属性为Authorizer，一个需要被实现的接口

先来看看这个接口的定义

```java
public interface Authorizer {
    boolean isPermitted(PrincipalCollection principals, String permission);
    boolean isPermitted(PrincipalCollection subjectPrincipal, Permission permission);
    boolean[] isPermitted(PrincipalCollection subjectPrincipal, String... permissions);
    boolean[] isPermitted(PrincipalCollection subjectPrincipal, List<Permission> permissions);
    boolean isPermittedAll(PrincipalCollection subjectPrincipal, String... permissions);
    boolean isPermittedAll(PrincipalCollection subjectPrincipal, Collection<Permission> permissions);
    
    void checkPermission(PrincipalCollection subjectPrincipal, String permission) throws AuthorizationException;
    void checkPermission(PrincipalCollection subjectPrincipal, Permission permission) throws AuthorizationException;
    void checkPermissions(PrincipalCollection subjectPrincipal, String... permissions) throws AuthorizationException;
    void checkPermissions(PrincipalCollection subjectPrincipal, Collection<Permission> permissions) throws AuthorizationException;
  
    boolean hasRole(PrincipalCollection subjectPrincipal, String roleIdentifier);
    boolean[] hasRoles(PrincipalCollection subjectPrincipal, List<String> roleIdentifiers);
    boolean hasAllRoles(PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers);

    void checkRole(PrincipalCollection subjectPrincipal, String roleIdentifier) throws AuthorizationException;
    void checkRoles(PrincipalCollection subjectPrincipal, Collection<String> roleIdentifiers) throws AuthorizationException;
    void checkRoles(PrincipalCollection subjectPrincipal, String... roleIdentifiers) throws AuthorizationException;  
}
```

虽然方法众多，但根据重载形式却可以看成只有2个方法，要么校验主体是否有该权限，要么校验主体是否有该角色
而每种方法有两种形式，is开头的，有返回值，校验失败的时候能返回`false`。check开头的，没有返回值，校验失败就给抛出`AuthorizationException`异常
然后每种形式又有各种各样的重载，这里就不多说了...

对于这个类的默认实现，shiro提供的是AuthorizingRealm，这个类有两个Resolver，它可用于将参数字符串分解为`Permission`接口的实现类
Permission 接口就一个方法，就是用来判断两个权限是否能匹配的。

```java
public interface Permission {
    boolean implies(Permission p);
}
```

继续来看AuthorizingRealm，两个很完美的地方，首先它是一个抽象类，抽象了啥？
这个方法：
```java
protected abstract AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals);
```

由它往上推，发现主要处理逻辑均在`getAuthorizationInfo`上，而这个方法调用了该抽象方法`doGetAuthorizationInfo`，
与此同时，所有重写Authorizer最后均进入了`getAuthorizationInfo`方法上，
意味着，权限信息管理的主要逻辑，可以推迟到子类去实现，Authorizer最终校验权限的逻辑，全都是根据子类设置的权限来定义的。

而这个类有点奇怪的地方在于，它的命名是Realm，但它不是个Realm，因为没有实现Realm接口，
但是它却继承了上面所说的用于认证的AuthorizingRealm，严格来说认证和授权是两码事，
但是估计shiro的设计目的在于集中realm的功能。但是AuthorizingRealm这个类已经作为抽象类。
它自己本身呢，必须是个抽象类，不能是接口，java又不给多继承，怎么办呢？索性，让授权类去继承认证类吧。
即使没多少关系，但也是个设计方案吧。

## 自定义Realm

从上面关系可知，让授权Realm是认证Realm的一个子类，那么要想实现自己的授权和认证逻辑，就应该继承的是授权Realm而不是认证Realm了

```java
public class CustomRealm extends AuthorizingRealm {
     @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
         
        }
        
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
         
        }
}
```

接下来详细介绍这俩个方法：

doGetAuthenticationInfo：获取认证后的用户信息，通常用于登录，那么狠明显，参数AuthenticationToken就是主体提交表单上来的用户名和密码等信息了
但是发现AuthenticationInfo接口和AuthenticationToken接口其实都是一样的，都仅仅只能获取用户名和密码...
这可麻烦了

```java
public interface AuthenticationInfo extends Serializable {
    PrincipalCollection getPrincipals();
    Object getCredentials();
}
```

对于AuthenticationToken，如果业务要求不仅要用户名密码认证，还要图片验证码认证，要手机号加手机验证码认证，还要微信登录的access_token认证等等等等...
而对于登录后返回的用户信息AuthenticationInfo，业务上肯定不止是用户名和密码，还需要头像啊，昵称啊，乱七八糟的，怎么破？


这时候，就发挥java强大的面向对象语言优势了，
最基本的思路那就是继承接口并实现，把所有新增的业务字段都补齐。
要么可以直接继承它们对应的两个基本实现类：`SimpleAuthenticationInfo`和`UsernamePasswordToken`

总之这方面的扩展性问题，就看你喜欢怎么整咯。

最后，自定义完了Realm后，不要忘记配置相关Spring Bean，首先是将自定义的Realm配进来，其次要将自定义的Realm放进安全控制中心去。

```xml
    <bean class="org.apache.shiro.web.mgt.DefaultWebSecurityManager" id="securityManager">
        <property name="realm" ref="customRealm" />
    </bean>
    <!-- 自定义Realm -->
    <bean class="com.edu.scnu.web.shiro.CustomRealm" id="customRealm">
        <property name="credentialsMatcher" ref="matcher" />
    </bean>
```

## SessionsSecurityManager

会话管理中心，主要属性为sessionManager，它是一个接口，shiro默认提供了基于java SE环境的会话
以及java web环境session的会话，当然，也可以不依赖与底层tomcat容器。

提供了会话管理，会话事件监听，会话持久化存储，容器集群，过期支持，对web透明，SSO单点登录支持。

直接shiro会话可以代替web容器的会话。

shiro提供的默认实现是`DefaultSessionManager`、`DefaultWebSessionManager` 和 `ServletContainerSessionManager`

- DefaultSessionManager 适用于java SE环境
- ServletContainerSessionManager：适用于web环境，使用的是tomcat提供的session会话管理
- DefaultWebSessionManager：是shiro自带的适用于web环境的会话管理，比servlet的session更强大，因此也更常用。

他们之间的类关系以及依赖关系如下图：

![image](https://raw.githubusercontent.com/Autom-liu/shiro-learn/ss/image/SessionManager.png)

![image](https://raw.githubusercontent.com/Autom-liu/shiro-learn/ss/image/SessionSecurityManager.png)

### 自定义session容器

在这些类中有一个很重要的属性`sessionDAO`它是一个接口，提供了开发者灵活的对session进行增删改查的控制。
因此它不依赖于指定容器，可以使用tomcat的session，也可以使用redis、memcache等缓存工具。

但是只需要实现基本的增删改查操作，剩下的校验管理相关的问题交给自带的session管理器搞定。

```java
public interface SessionDAO {
    Serializable create(Session session);   // 创建session操作，指定你使用那个session容器
    Session readSession(Serializable sessionId) throws UnknownSessionException; // 读操作
    void update(Session session) throws UnknownSessionException;    // 更新操作
    void delete(Session session);   // 删除操作
    Collection<Session> getActiveSessions();    // 读操作（获取所有键）
}
```

具体如何实现，可以参考shiro提供的一个默认实现类：`MemorySessionDAO` 
这是基于Map管理的session容器，改用其它容器原理也类似。
它是继承自`AbstractSessionDAO` 的，这个抽象类实现了上面接口的通用功能，同时也增加了以do开头的抽象方法，供子类实现。
比如readSession方法，就增加了对空判断，主要逻辑还是由子类`doReadSession`定义，返回空则能抛出shiro的通用异常。

```java
public Session readSession(Serializable sessionId) throws UnknownSessionException {
    Session s = doReadSession(sessionId);  // 抽象方法，由子类实现
    if (s == null) {
        throw new UnknownSessionException("There is no session with id [" + sessionId + "]");
    }
    return s;
}
```

使用了自定义的容器后，在web项目中，还需要整个spring注入：

```xml
   <!--  shiro 会话管理器  -->
    <bean class="com.edu.scnu.web.shiro.CustomSessionManager" id="sessionManager">
        <property name="sessionDAO" ref="redisSession" />
    </bean>
    <!--  自定义的会话管理操作  -->
    <bean class="com.edu.scnu.web.shiro.RedisSession" id="redisSession" />
```

就那么简单！

### 自定义session 管理器

有时候根据性能优化的需要，可能不能采用默认的session管理器，而需要手动重写DefaultWebSessionManager

而推荐重写的是DefaultWebSessionManager 或 DefaultSessionManager 而不是他们的抽象类 AbstractValidatingSessionManager
因为只需重写一个最重要的读操作即可：

```java
/**
     * Looks up a session from the underlying data store based on the specified session key.
     */
    protected abstract Session retrieveSession(SessionKey key) throws UnknownSessionException;
```

这个方法就是用来管理读操作的，可以参考 shiro的核心默认实现

```java
    protected Session retrieveSession(SessionKey sessionKey) throws UnknownSessionException {
        // 推荐使用父类的getSessionId方法
        Serializable sessionId = getSessionId(sessionKey);
        // 下面语句相当于 sessionDAO.readSession(sessionId);  即读操作
        Session s = retrieveSessionFromDataSource(sessionId);
        return s;
    }
```

在web环境中，使用DefaultWebSessionManager的getSessionId方法，默认会从两个地方获取sessionKey
一个是在cookie中，另一个是Url参数中，DefaultWebSessionManager会为我们重写Url从而能在cookie失效的环境下获取sessionId

如果不需要cookie或重写URL那么可以设置`setSessionIdCookieEnabled`和`setSessionIdUrlRewritingEnabled`为false
但是这样并不会生效的，因此请你一定要调用`getSessionId`方法。

## 缓存管理

先来看一下缓存管理相关类图：

![image](https://raw.githubusercontent.com/Autom-liu/shiro-learn/ss/image/CachingManager.png)

核心有3个接口：

### cache<K, V>

```java
public interface Cache<K, V> {
    V get(K var1) throws CacheException;

    V put(K var1, V var2) throws CacheException;

    V remove(K var1) throws CacheException;

    void clear() throws CacheException;

    int size();

    Set<K> keys();

    Collection<V> values();
}
```

对于缓存，由于使用的容器不同，redis, memcache, monogodb等，造成了操作方法都不一样。
为了统一所有不同容器的缓存操作，因此就有了shiro提供的缓存接口。
shiro提供的基本实现是MapCache，基于内存的缓存。

### CacheManager

```java
public interface CacheManager {
    <K, V> Cache<K, V> getCache(String var1) throws CacheException;
}
```

只有一个方法，那就是根据名称获得一个Cache

### CacheManagerAware

```java
public interface CacheManagerAware {
    void setCacheManager(CacheManager var1);
}
```

只有一个方法，用于注入CacheManager，
DefaultSecurityManager会检查对象是否实现了CacheManagerAware接口
只有实现的对象才会被注入。

### CachingRealm

先来看看Realm的缓存类图：

![image](https://raw.githubusercontent.com/Autom-liu/shiro-learn/ss/image/CacheRealm.png)

CachingRealm 就是用来缓存上面说的那两个Realm数据的

要实现Realm缓存，首先对Cache接口进行实现，即完成缓存容器的增删改查操作

接下来实现的是缓存管理器CacheManager，重写getCache方法，返回之前定义好的Cache实现对象。

最后定义bean注入即可：

```xml
    <bean class="org.apache.shiro.web.mgt.DefaultWebSecurityManager" id="securityManager">
        <property name="realm" ref="customRealm" />
        <property name="sessionManager" ref="sessionManager"/>
        <property name="cacheManager" ref="cacheManager" />
    </bean>
    <bean class="com.edu.scnu.web.shiro.RedisCacheManager" id="cacheManager" />
```

### AbstractCacheManage

最后来看一下session缓存，sessionSecurityManager会对实现了CacheManagerAware的SessionManager进行缓存

同时也会对实现了CacheManagerAware的sessionDAO进行缓存。

当然，如果我们的sessionDAO本身就基于缓存的，那么session缓存不用也罢。

因此在实际开发中，缓存主要用来Realm缓存，角色和权限数据.


完~~~
