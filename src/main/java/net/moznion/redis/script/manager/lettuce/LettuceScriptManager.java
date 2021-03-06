package net.moznion.redis.script.manager.lettuce;

import java.util.Collection;
import java.util.List;

import net.moznion.redis.script.manager.core.ScriptManager;

import com.lambdaworks.redis.RedisCommandExecutionException;
import com.lambdaworks.redis.ScriptOutputType;
import com.lambdaworks.redis.api.sync.RedisScriptingCommands;

public class LettuceScriptManager<K, V> extends ScriptManager<K, V> {
    private static final Object[] EMPTY_VALUE = {};

    private final RedisScriptingCommands<K, V> commands;
    private final V script;
    private final boolean useEvalSHA;
    private final ScriptOutputType outputType;

    boolean isNoScript;

    public LettuceScriptManager(final RedisScriptingCommands<K, V> commands,
                                final V script,
                                final ScriptOutputType outputType) {
        this(commands, script, null, outputType, true);
    }

    public LettuceScriptManager(final RedisScriptingCommands<K, V> commands,
                                final V script,
                                final String sha1,
                                final ScriptOutputType outputType) {
        this(commands, script, sha1, outputType, true);
    }

    public LettuceScriptManager(final RedisScriptingCommands<K, V> commands,
                                final V script,
                                final ScriptOutputType outputType,
                                final boolean useEvalSHA) {
        this(commands, script, null, outputType, useEvalSHA);
    }

    public LettuceScriptManager(final RedisScriptingCommands<K, V> commands,
                                final V script,
                                final String sha1,
                                final ScriptOutputType outputType,
                                final boolean useEvalSHA) {
        super(sha1);
        this.commands = commands;
        this.script = script;
        this.outputType = outputType;
        this.useEvalSHA = useEvalSHA;
        isNoScript = true;
    }

    @SuppressWarnings({ "unchecked", "SuspiciousArrayCast" })
    @Override
    public Object eval(final K[] keys) {
        return eval(keys, (V[]) EMPTY_VALUE);
    }

    @Override
    public Object eval(final K[] keys, final V[] values) {
        isNoScript = false;

        if (useEvalSHA) {
            final String sha1 = getSHA1(script);
            try {
                final Object evaled = commands.evalsha(sha1, outputType, keys, values);
                if (evaled instanceof List<?>) {
                    // For ScriptOutputType.MULTI
                    if (!((Collection<?>) evaled).isEmpty()) {
                        final Object head = ((List<?>) evaled).get(0);
                        if (head instanceof RuntimeException) {
                            throw (RuntimeException) head;
                        }
                    }
                }
                return evaled;
            } catch (RedisCommandExecutionException e) {
                if (!e.getMessage().startsWith("NOSCRIPT")) {
                    // Not "NOSCRIPT" error; unexpected
                    throw e;
                }
            }
        }

        // When "NOSCRIPT" error was raised, eval script and register it
        isNoScript = true;
        return commands.eval(script.toString(), outputType, keys, values);
    }
}
