package com.warchaser.glsurfaceviewdev.util;

public class Constants {

    public static final String IMAGE_PATH = "IMAGE_PATH";

    public static final String IMAGE_PATH_LIST = "IMAGE_PATH_LIST";

    public static final String IMAGE_INDEX = "IMAGE_INDEX";

    public static final String EXTRA_STARTING_ALBUM_POSITION = "EXTRA_STARTING_ALBUM_POSITION";

    public static final String EXTRA_CURRENT_ALBUM_POSITION = "EXTRA_CURRENT_ALBUM_POSITION";

    public static String getSimpleClassName(Object object){
        Class clazz = object.getClass();
        String str1 = clazz.getName().replace("$", ".");
        String str2 = str1.replace(clazz.getPackage().getName(), "") + ".";

        return str2.substring(1);
    }

}
