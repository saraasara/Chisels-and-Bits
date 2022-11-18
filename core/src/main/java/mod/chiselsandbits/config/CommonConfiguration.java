package mod.chiselsandbits.config;

import com.communi.suggestu.scena.core.config.ConfigurationType;
import com.communi.suggestu.scena.core.config.IConfigurationBuilder;
import com.communi.suggestu.scena.core.config.IConfigurationManager;
import mod.chiselsandbits.api.config.ICommonConfiguration;
import mod.chiselsandbits.api.util.constants.Constants;

import java.util.function.Supplier;

public class CommonConfiguration implements ICommonConfiguration
{

    private final Supplier<Boolean> enableHelp;
    private final Supplier<Long> collisionBoxCacheSize;
    private final Supplier<Integer> blockSaveThreadCount;

    public CommonConfiguration() {
        final IConfigurationBuilder builder = IConfigurationManager.getInstance().createBuilder(
          ConfigurationType.NOT_SYNCED, Constants.MOD_ID + "-common"
        );

        enableHelp = builder.defineBoolean("help.enabled-in-tooltips", true);
        collisionBoxCacheSize = builder.defineLong("performance.caches.sizes.collision-boxes", 10000, 0, Long.MAX_VALUE);
        blockSaveThreadCount = builder.defineInteger("performance.saving.thread-count", Math.max(1, Runtime.getRuntime().availableProcessors()) / 2, 1, Runtime.getRuntime()
          .availableProcessors());

        builder.setup();
    }

    @Override
    public Supplier<Boolean> getEnableHelp()
    {
        return enableHelp;
    }

    @Override
    public Supplier<Long> getCollisionBoxCacheSize()
    {
        return collisionBoxCacheSize;
    }

    @Override
    public Supplier<Integer> getBlockSaveThreadCount()
    {
        return blockSaveThreadCount;
    }
}
