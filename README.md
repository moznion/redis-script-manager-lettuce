Redis Script Manager for Java with [lettuce](https://github.com/mp911de/lettuce) [![Build Status](https://travis-ci.org/moznion/redis-script-manager-lettuce.svg?branch=master)](https://travis-ci.org/moznion/redis-script-manager-lettuce) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/net.moznion/redis-script-manager-lettuce/badge.svg)](https://maven-badges.herokuapp.com/maven-central/net.moznion/redis-script-manager-lettuce)
==

Simple manager for scripting of Redis with [lettuce](https://github.com/mp911de/lettuce).  
This library is Java port of [p5-Redis-Script](https://github.com/shogo82148/p5-Redis-Script).

Usage
--

```java
final StatefulRedisConnection<String, String> connect =
        RedisClient.create().connect(new RedisURI("127.0.0.1", 6379, 10, TimeUnit.SECONDS));
final RedisCommands<String, String> commands = connect.sync();
final LettuceScriptManager<String, String> scriptManager =
        new LettuceScriptManager<>(commands,
                                   "redis.call('SET', KEYS[1], ARGV[1])",
                                   ScriptOutputType.VALUE);
scriptManager.eval(new String[] { "sample_key" }, new String[] { "42" });
```

See Also
--

- [https://github.com/moznion/redis-script-manager-core](https://github.com/moznion/redis-script-manager-core)
- [https://github.com/moznion/redis-script-manager-jedis](https://github.com/moznion/redis-script-manager-jedis)
- [https://github.com/shogo82148/p5-Redis-Script](https://github.com/shogo82148/p5-Redis-Script)

Author
--

moznion (<moznion@moznion.net>)

License
--

MIT

