package com.wireguard.android.configStore;

import android.content.Context;
import android.util.Log;

import com.wireguard.android.Application.ApplicationContext;
import com.wireguard.config.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import java9.util.stream.Collectors;
import java9.util.stream.Stream;

/**
 * Configuration store that uses a {@code wg-quick}-style file for each configured tunnel.
 */

public final class FileConfigStore implements ConfigStore {
    private static final String TAG = FileConfigStore.class.getSimpleName();

    private final Context context;

    public FileConfigStore(@ApplicationContext final Context context) {
        this.context = context;
    }

    @Override
    public Config create(final String name, final Config config) throws IOException {
        final File file = fileFor(name);
        if (!file.createNewFile()) {
            final String message = "Configuration file " + file.getName() + " already exists";
            throw new IllegalStateException(message);
        }
        try (FileOutputStream stream = new FileOutputStream(file, false)) {
            stream.write(config.toString().getBytes(StandardCharsets.UTF_8));
            return config;
        }
    }

    @Override
    public void delete(final String name) throws IOException {
        final File file = fileFor(name);
        if (!file.delete())
            throw new IOException("Cannot delete configuration file " + file.getName());
    }

    @Override
    public Set<String> enumerate() {
        return Stream.of(context.fileList())
                .filter(name -> name.endsWith(".conf"))
                .map(name -> name.substring(0, name.length() - ".conf".length()))
                .collect(Collectors.toUnmodifiableSet());
    }

    private File fileFor(final String name) {
        return new File(context.getFilesDir(), name + ".conf");
    }

    @Override
    public Config load(final String name) throws IOException {
        try (FileInputStream stream = new FileInputStream(fileFor(name))) {
            return Config.from(stream);
        }
    }

    @Override
    public Config save(final String name, final Config config) throws IOException {
        Log.d(TAG, "Requested save config for tunnel " + name);
        final File file = fileFor(name);
        if (!file.isFile()) {
            final String message = "Configuration file " + file.getName() + " not found";
            throw new IllegalStateException(message);
        }
        try (FileOutputStream stream = new FileOutputStream(file, false)) {
            stream.write(config.toString().getBytes(StandardCharsets.UTF_8));
            return config;
        }
    }
}
