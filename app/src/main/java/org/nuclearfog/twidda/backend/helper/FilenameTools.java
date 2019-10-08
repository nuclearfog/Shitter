package org.nuclearfog.twidda.backend.helper;

import androidx.annotation.NonNull;

public abstract class FilenameTools {

    public enum FileType {
        IMAGE,
        VIDEO,
        STREAM,
        ANGIF,
        NONE
    }

    private static String getExtension(@NonNull String path) {
        String filename = getFilename(path);
        String ext = "";
        int start = lastIndexOf(filename, '.') + 1;
        if (start > 0 && start < filename.length()) {
            int end = lastIndexOf(filename, '?');
            if (end > 0)
                ext = filename.substring(start, end);
            else
                ext = filename.substring(start);
            ext = asciiLowerCase(ext);
        }
        return ext;
    }

    private static String getFilename(@NonNull String path) {
        String filename = "";
        int end = lastIndexOf(path, '/') + 1;
        if (end > 0 && end < path.length())
            filename = path.substring(end);
        return filename;
    }

    public static FileType getFileType(String path) {
        String ext = getExtension(path);
        switch (ext) {
            case "png":
            case "jpg":
            case "jpeg":
                return FileType.IMAGE;

            case "mp4":
            case "3gp":
                return FileType.VIDEO;

            case "m3u8":
                return FileType.STREAM;

            case "gif":
                return FileType.ANGIF;

            default:
                return FileType.NONE;
        }
    }

    private static String asciiLowerCase(String ext) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < ext.length(); index++) {
            char current = ext.charAt(index);
            if (current >= 'A' && current <= 'Z')
                current += 0x20;
            result.append(current);
        }
        return result.toString();
    }

    private static int lastIndexOf(String text, char symbol) {
        int position = 0;
        for (int index = 0; index < text.length(); index++) {
            if (text.charAt(index) == symbol)
                position = index;
        }
        return position;
    }
}