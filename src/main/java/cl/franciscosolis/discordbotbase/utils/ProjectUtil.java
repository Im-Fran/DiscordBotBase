package cl.franciscosolis.discordbotbase.utils;

import java.io.IOException;
import java.security.CodeSource;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ProjectUtil {

    private static LinkedList<String> getAllClasses() throws IOException {
        // Create linkedlist string object of files
        LinkedList<String> files = new LinkedList<>();
        // Store in the variable 'src' the code source using the class protection domain method
        CodeSource src = ProjectUtil.class.getProtectionDomain().getCodeSource();
        // Retrieve the zip input stream from the source code
        ZipInputStream zipInputStream = new ZipInputStream(src.getLocation().openStream());
        // while true loop get the next file from the zip input stream and store in files the name
        ZipEntry zipEntry = null;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String file = zipEntry.getName();
            if (file == null) continue;
            files.add(file);
        }

        return files;
    }

    // Get all the classes and filter them using a prefix as argument and map the filtered result into a Class object
    public static LinkedList<Class<?>> getClasses(String prefix) throws IOException{
        LinkedList<Class<?>> classes = new LinkedList<>();
        for (String clazz : getAllClasses()) {
            // Replace '/' by '.' in clazz
            String clazzName = clazz.replace('/', '.');
            // Check if the clazzName starts with the prefix and ends with .class
            if (clazzName.startsWith(prefix) && clazzName.endsWith(".class")) {
                // Remove the .class from the clazzName and try to convert to a class and catch the class not found exception and ignore it
                try {
                    classes.add(Class.forName(clazzName.substring(0, clazzName.length() - 6)));
                }catch (ClassNotFoundException ignored) {}
            }
        }
        return classes;
    }




    
}
