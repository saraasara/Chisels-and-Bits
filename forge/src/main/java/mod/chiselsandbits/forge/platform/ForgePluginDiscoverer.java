package mod.chiselsandbits.forge.platform;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import mod.chiselsandbits.api.plugin.IPluginDiscoverer;
import mod.chiselsandbits.api.util.ClassUtils;
import mod.chiselsandbits.api.util.GroupingUtils;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ForgePluginDiscoverer implements IPluginDiscoverer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ForgePluginDiscoverer INSTANCE = new ForgePluginDiscoverer();

    public static ForgePluginDiscoverer getInstance() {
        return INSTANCE;
    }

    private ForgePluginDiscoverer() {
    }

    @SuppressWarnings("unchecked")
    @Override
    public <A, I extends Annotation, T> Collection<T> loadPlugins(Class<A> annotationType, Class<I> instanceAnnotationType, Class<T> pluginSpecificationType, Function<T, String> idExtractor) {
        Type pluginType = Type.getType(annotationType);

        ModList modList = ModList.get();
        List<T> plugins = new ArrayList<>();
        for (ModFileScanData scanData : modList.getAllScanData()) {
            for (ModFileScanData.AnnotationData data : scanData.getAnnotations()) {
                if (pluginType.equals(data.annotationType())) {
                    final ArrayList<String> requiredMods = (ArrayList<String>) data.annotationData().get("requiredMods");
                    if (requiredMods != null && requiredMods.size() > 0) {
                        if (requiredMods.stream().anyMatch(modId -> !ModList.get().isLoaded(modId))) {
                            continue;
                        }
                    }

                    T plugin = createPluginFrom(
                            data.memberName(),
                            pluginSpecificationType,
                            instanceAnnotationType,
                            idExtractor
                    );

                    if (plugin != null) {
                        plugins.add(plugin);
                        LOGGER.info("Found and loaded ChiselsAndBits plugin: {}", idExtractor.apply(plugin));
                    }
                }
            }
        }

        final Collection<Collection<T>> groupedByIds = GroupingUtils.groupByUsingSet(plugins, idExtractor);
        final Collection<String> idsWithDuplicates = groupedByIds.stream()
                .filter(p -> p.size() > 1)
                .map(p -> p.iterator().next())
                .map(idExtractor)
                .collect(Collectors.toSet());

        if (idsWithDuplicates.size() > 0) {
            throw new RuntimeException(String.format("Can not load C&B there are multiple instances of the plugins: [%s]", String.join(", ", idsWithDuplicates)));
        }

        return ImmutableSet.copyOf(plugins);
    }

    @Nullable
    private static <T, I extends Annotation> T createPluginFrom(
            String className,
            final Class<T> pluginSpecificationType,
            final Class<I> instanceAnnotationType,
            final Function<T, String> idExtractor
    ) {
        return ClassUtils.createOrGetInstance(
                className,
                pluginSpecificationType,
                instanceAnnotationType,
                idExtractor
        );
    }
}
