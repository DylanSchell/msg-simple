package com.github.fge.msgsimple.bundle;

import com.github.fge.msgsimple.provider.LoadingMessageSourceProvider;
import com.github.fge.msgsimple.provider.MessageSourceLoader;
import com.github.fge.msgsimple.provider.MessageSourceProvider;
import com.github.fge.msgsimple.source.MessageSource;
import com.github.fge.msgsimple.source.PropertiesMessageSource;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Utility class to build a localized, UTF-8 {@link ResourceBundle}
 *
 * <p>This class only contains one method to return a {@link MessageBundle}
 * which operates nearly the same as a {@link ResourceBundle}, with the
 * difference that property files are read in UTF-8.</p>
 *
 * <p>This means that like a {@link ResourceBundle}, it will try and look up a
 * key in locale-enabled property files (for instance, {@code
 * foo_fr_FR.properties} for bundle {@code foo} and locale {@code fr_FR}).</p>
 *
 * <p>Internally, this method creates a dedicated {@link MessageSourceLoader} to
 * load property files, wraps it into a {@link LoadingMessageSourceProvider},
 * and finally creates a {@link MessageBundle} with this only source.</p>
 *
 * <p>As it is a {@link MessageBundle}, it means you can extend it further by
 * {@link MessageBundle#thaw()}ing it and appending/prepending other message
 * source providers etc.</p>
 *
 * @see PropertiesMessageSource
 */
public final class PropertiesBundle
{
    private static final Pattern SUFFIX = Pattern.compile("\\.properties$");

    private PropertiesBundle()
    {
    }

    /**
     * Create a {@link ResourceBundle}-like {@link MessageBundle}
     *
     * <p>The path given as an argument can be any of the following:</p>
     *
     * <ul>
     *     <li>{@code org/foobar/message.properties};</li>
     *     <li>{@code org/foobar/message};</li>
     *     <li>{@code /org/foobar/message.properties};</li>
     *     <li>{@code /org/foobar/message}.</li>
     * </ul>
     *
     * @param resourcePath the resource path
     * @throws NullPointerException resource path is null
     * @return a {@link MessageBundle}
     */
    public static MessageBundle forPath(final String resourcePath)
    {
        if (resourcePath == null)
            throw new NullPointerException("resource path is null");

        final String s = resourcePath.startsWith("/") ? resourcePath
            : '/' + resourcePath;

        final String realPath = SUFFIX.matcher(s).replaceFirst("");

        final MessageSourceLoader loader = new MessageSourceLoader()
        {
            @Override
            public MessageSource load(final Locale locale)
                throws IOException
            {
                final StringBuilder sb = new StringBuilder(realPath);
                if (!locale.equals(Locale.ROOT))
                    sb.append('_').append(locale.toString());
                sb.append(".properties");

                return PropertiesMessageSource.fromResource(sb.toString());
            }
        };

        final MessageSourceProvider provider
            = LoadingMessageSourceProvider.newBuilder().setLoader(loader)
                .build();

        return MessageBundle.newBuilder().appendProvider(provider).freeze();
    }
}