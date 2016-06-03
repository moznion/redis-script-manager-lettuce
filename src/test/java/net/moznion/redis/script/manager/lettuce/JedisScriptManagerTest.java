package net.moznion.redis.script.manager.lettuce;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import net.moznion.redis.script.manager.core.ScriptManager;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.ScriptOutputType;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.sync.RedisCommands;

public class JedisScriptManagerTest {
    @Test
    public void lettuceTestBasic() {
        final StatefulRedisConnection<String, String> connect =
                RedisClient.create().connect(new RedisURI("127.0.0.1", 6379, 10, TimeUnit.SECONDS));
        final RedisCommands<String, String> commands = connect.sync();
        commands.scriptFlush();

        final LettuceScriptManager<String, String> scriptManager =
                new LettuceScriptManager<>(commands,
                                           "redis.call('SET', KEYS[1], ARGV[1])",
                                           ScriptOutputType.VALUE);
        final String key = "script_manager_test";

        assertThat(scriptManager.isNoScript).isTrue();
        scriptManager.eval(new String[] { key }, new String[] { "42" });
        assertThat(commands.get(key)).isEqualTo("42");
        scriptManager.eval(new String[] { key }, new String[] { "43" });
        assertThat(commands.get(key)).isEqualTo("43");
        assertThat(scriptManager.isNoScript).isFalse();
    }

    @Test
    public void lettuceTestWithoutValue() {
        final StatefulRedisConnection<String, String> connect =
                RedisClient.create().connect(new RedisURI("127.0.0.1", 6379, 10, TimeUnit.SECONDS));
        final RedisCommands<String, String> commands = connect.sync();
        commands.scriptFlush();

        final LettuceScriptManager<String, String> scriptManager =
                new LettuceScriptManager<>(commands,
                                           "redis.call('SET', KEYS[1], 666)",
                                           ScriptOutputType.VALUE);
        final String key = "script_manager_test";

        scriptManager.eval(new String[] { key });
        assertThat(commands.get(key)).isEqualTo("666");
    }

    @Test
    public void lettuceTestWithSHA1() {
        final StatefulRedisConnection<String, String> connect =
                RedisClient.create().connect(new RedisURI("127.0.0.1", 6379, 10, TimeUnit.SECONDS));
        final RedisCommands<String, String> commands = connect.sync();
        commands.scriptFlush();

        final String script = "redis.call('SET', KEYS[1], ARGV[1])";

        final LettuceScriptManager<String, String> scriptManager =
                new LettuceScriptManager<>(commands,
                                           script,
                                           ScriptManager.digestSHA1(script),
                                           ScriptOutputType.VALUE);
        final String key = "script_manager_test";

        assertThat(scriptManager.isNoScript).isTrue();
        scriptManager.eval(new String[] { key }, new String[] { "42" });
        assertThat(commands.get(key)).isEqualTo("42");
        scriptManager.eval(new String[] { key }, new String[] { "43" });
        assertThat(commands.get(key)).isEqualTo("43");
        assertThat(scriptManager.isNoScript).isFalse();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void forScriptOutputTypeMULTI() {
        final StatefulRedisConnection<String, String> connect =
                RedisClient.create().connect(new RedisURI("127.0.0.1", 6379, 10, TimeUnit.SECONDS));
        final RedisCommands<String, String> commands = connect.sync();
        commands.scriptFlush();

        final String script = "redis.call('SET', KEYS[1], ARGV[1]); " +
                              "redis.call('SET', KEYS[2], ARGV[2]); " +
                              "local foo = redis.call('GET', KEYS[1]); " +
                              "local bar = redis.call('GET', KEYS[2]); " +
                              "return {foo,bar};";

        final LettuceScriptManager<String, String> scriptManager =
                new LettuceScriptManager<>(commands,
                                           script,
                                           ScriptManager.digestSHA1(script),
                                           ScriptOutputType.MULTI);

        List<String> evaled;

        assertThat(scriptManager.isNoScript).isTrue();
        evaled = (List<String>) scriptManager.eval(new String[] { "foo", "bar" }, new String[] { "1", "2" });
        assertThat(evaled.get(0)).isEqualTo("1");
        assertThat(evaled.get(1)).isEqualTo("2");
        evaled = (List<String>) scriptManager.eval(new String[] { "foo", "bar" }, new String[] { "3", "4" });
        assertThat(evaled.get(0)).isEqualTo("3");
        assertThat(evaled.get(1)).isEqualTo("4");
        assertThat(scriptManager.isNoScript).isFalse();
    }
}
