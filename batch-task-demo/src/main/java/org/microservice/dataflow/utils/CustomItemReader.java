package org.microservice.dataflow.utils;


import javax.batch.api.chunk.ItemReader;
import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @author: Sam Ma
 * Custom File ItemReader reads relative file from directory
 */
public class CustomItemReader implements ItemReader {

    private List<File> files;

    public CustomItemReader(File directory) {
        if (null == directory) {
            throw new IllegalArgumentException("The directory can't be null");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("the specified file must be a directory");
        }
        files = Arrays.asList(directory.listFiles());
    }

    @Override
    public void open(Serializable checkpoint) throws Exception {

    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public Object readItem() throws Exception {
        if (!files.isEmpty()) {
            return files.remove(0);
        }
        return null;
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return null;
    }
}
