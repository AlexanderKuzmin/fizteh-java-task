package ru.fizteh.fivt.students.almazNasibullin.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;
import ru.fizteh.fivt.students.almazNasibullin.IOUtils;

/**
 * 15.10.12
 * @author almaz
 */

public class Main {

    public static void main(String[] args) {
        handlerArguments(args);
    }

    public static void handlerArguments(String[] args) { // обработчик аргументов
        int len = args.length;
        if (len == 0) { // Интерактивный режим
            System.out.println(System.getProperty("user.dir"));
            System.out.print("$ ");
            while (true) {
                BufferedReader buf = new BufferedReader(new InputStreamReader(System.in));
                String str = "";
                try {
                    str = buf.readLine();
                } catch (IOException e) {
                    IOUtils.printErrorAndExit(e.getMessage());
                }
                StringTokenizer st = new StringTokenizer(str, ";"); // разделяем аргументы
                while (st.hasMoreTokens()) {
                    String cur = st.nextToken();
                    handlerCommand(cur);
                }
                System.out.print("$ ");
            }
        } else { // пакетный  режим
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; ++i) { // сливаем все аргументы в одну строку
                sb.append(args[i]);
                sb.append(" ");
            }
            StringTokenizer st = new StringTokenizer(sb.toString(), ";");
            while (st.hasMoreTokens()) {
                String str = st.nextToken();
                handlerCommand(str);
            }
        }
    }

    public static void handlerCommand(String str) { // обработчик каждой команды
        StringTokenizer st = new StringTokenizer(str, " \t\n");
        boolean commandExecute = false;
        /* выполнилась или нет команда в текущем наборе аргументов */
        while (st.hasMoreTokens()) {
            String cur = st.nextToken();
            if (cur.equals("cd") && !commandExecute) {
                if (st.hasMoreTokens()) { // проверка на наличие аргуметов для данной команды
                    char c = File.separatorChar;
                    String path = st.nextToken();
                    if (path.charAt(0) == c) {
                        System.setProperty("user.dir", File.separator);
                    }
                    StringTokenizer stSecond = new StringTokenizer(path, File.separator);
                    while (stSecond.hasMoreTokens()) {
                        cdExecute(stSecond.nextToken());
                    }
                } else {
                    IOUtils.printErrorAndExit("Usage: cd <absolute path|relative path>");
                }
                commandExecute = true;
            } else if (cur.equals("mkdir") && !commandExecute) {
                // проверка на наличие аргуметов для данной команды
                if (st.hasMoreTokens()) {
                    String fileName = st.nextToken();
                    mkdirExecute(fileName);
                } else {
                    IOUtils.printErrorAndExit("Usage: mkdir <dirname>");
                }
                commandExecute = true;
            } else if (cur.equals("pwd") && !commandExecute) {
                System.out.println(System.getProperty("user.dir"));
                commandExecute = true;
            } else if (cur.equals("rm") && !commandExecute) {
                // проверка на наличие аргуметов для данной команды
                if (st.hasMoreTokens()) {
                    String fileName = st.nextToken();
                    rmExecute(fileName);
                } else {
                    IOUtils.printErrorAndExit("Usage: rm <file|dir>");
                }
                commandExecute = true;
            } else if (cur.equals("cp") && !commandExecute) {
                // проверка на наличие аргуметов для данной команды
                if (st.hasMoreTokens()) {
                    String source = st.nextToken();
                    if (st.hasMoreTokens()) {
                            String dist = st.nextToken();
                            cpExecute(source, dist, true);
                    } else {
                        IOUtils.printErrorAndExit("Usage: cp <source> <destination>");
                    }
                } else {
                    IOUtils.printErrorAndExit("Usage: cp <source> <destination>");
                }
                commandExecute = true;
            } else if (cur.equals("mv") && !commandExecute) {
                // проверка на наличие аргуметов для данной команды
                if (st.hasMoreTokens()) {
                    String source = st.nextToken();
                    if (st.hasMoreTokens()) {
                            String dist = st.nextToken();
                            mvExecute(source, dist);
                    } else {
                        IOUtils.printErrorAndExit("Usage: cp <source> <destination>");
                    }
                } else {
                    IOUtils.printErrorAndExit("Usage: cp <source> <destination>");
                }
                commandExecute = true;
            } else if (cur.equals("dir") && !commandExecute) {
                if (st.hasMoreTokens()) {
                    IOUtils.printErrorAndExit("'dir' does not support the arguments");
                } else {
                    File f = new File(System.getProperty("user.dir"));
                    String[] files = f.list();
                    for (int i = 0; i < files.length; ++i) {
                        System.out.println(files[i]);
                    }
                }
                commandExecute = true;
            } else if (cur.equals("exit") && !commandExecute) {
                commandExecute = true;
                System.exit(0);
            } else {
                if (!commandExecute) { // условие некорректного ввода
                    IOUtils.printErrorAndExit((cur + ": command not found"));
                }
            }
        }
    }

    public static void cdExecute(String path) { // выполнение команды 'cd'
        String curDir = System.getProperty("user.dir");
        if (path.equals("..")) {
            File f = new File(curDir);
            String parent = f.getParent();
            if (parent != null) {
                System.setProperty("user.dir", parent);
            }
        } else if (path.equals(".")) {
        } else {
            File f = new File(path).getAbsoluteFile();
            if (f.exists()) {
                if (!f.isDirectory()) {
                    IOUtils.printErrorAndExit("cd: " + path +" Not a directory");
                }
                System.setProperty("user.dir", f.getAbsolutePath());
            } else {
                IOUtils.printErrorAndExit("cd: " + path + ": No such file or directory");
            }
        }
    }

    public static File mkdirExecute(String fileName) { // выполнение команды 'mkdir'
        File f = new File(fileName).getAbsoluteFile();
        if (f.exists()) {
            IOUtils.printErrorAndExit("mkdir: cannot create directory `" +
                    fileName + "': File exists");
        } else {
            f.mkdir();
        }
        return f;
    }

    public static void rmExecute(String fileName) { // выполнение команды 'rm'
        if (fileName.equals("..")) {
            IOUtils.printErrorAndExit("rm: cannot remove directory: `..'");
        }
        if (fileName.equals(".")) {
            IOUtils.printErrorAndExit("rm: cannot remove directory: `.'");
        }
        File f = new File(fileName).getAbsoluteFile();
        if (f.exists()) {
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                if (!f.delete()) {
                    IOUtils.printErrorAndExit("Can not delete " + fileName);
                }
            }
        } else {
            IOUtils.printErrorAndExit("rm: failed to remove `" +
                    fileName + "': No such file or directory");
        }
    }

    public static void deleteDirectory(File f) { // рекурсивное удаление директории
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; ++i) {
            if (files[i].isDirectory()) {
                deleteDirectory(files[i]);
            } else {
                if (!files[i].delete()) {
                    System.out.println("Can not delete " + files[i].getName());
                    System.exit(1);
                }
            }
        }
        if (!f.delete()) {
            IOUtils.printErrorAndExit("Can not delete " + f.getName());
        }
    }

    public static void cpExecute(String source, String dist, boolean key) { 
        /* выполнение команды 'cp'
         * key отвечает за начало копирования, то есть если source - папка,
         * то либо сама папка тоже копируется, либо ее содержимое
         */
        File from = new File(source).getAbsoluteFile();
        if (!from.exists()) {
            IOUtils.printErrorAndExit("cp: `" + source + "': No such file or directory");
        }
        File to = new File(dist).getAbsoluteFile();
        if (!to.exists()) {
            to = mkdirExecute(to.getAbsolutePath());
        }
        if (key) {
            if (from.isDirectory()) {
                File[] files = from.listFiles();
                for (int i = 0; i < files.length; ++i ) {
                    copyFile(files[i], to);
                }
            } else {
                copyFile(from, to);
            }
        } else {
            copyFile(from, to);
        }
    }

    public static void copyFile(File from, File to) { // рекурсивное копирование
        if (from.isDirectory()) {
            File f = new File(to.getAbsolutePath() + File.separator + from.getName());
            f.mkdir();
            File[] file = from.listFiles();
            for (int i = 0; i < file.length; i++) {
                copyFile(file[i], f);
            }
        } else {
            FileChannel srcChannel = null;
            FileChannel dstChannel = null;
            try {
                File f = new File(to.getAbsolutePath() + File.separator + from.getName());
                try {
                    f.createNewFile();
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
                srcChannel = new FileInputStream(
                        from.getAbsolutePath()).getChannel();
                dstChannel = new FileOutputStream(
                        f.getAbsolutePath()).getChannel();
                dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            } catch (Exception e) {
                IOUtils.printErrorAndExit(e.getMessage());
            } finally {
                try {
                    if (srcChannel != null) {
                        srcChannel.close();
                    }
                } catch (Exception e) {
                    IOUtils.printErrorAndExit(e.getMessage());
                }
                try {
                    if (dstChannel != null) {
                        dstChannel.close();
                    }
                } catch (Exception e) {
                    IOUtils.printErrorAndExit(e.getMessage());
                }
            }
        }
    }

    public static void mvExecute(String source, String dist) { // выполнение команды 'mv'
        File from = new File(source).getAbsoluteFile();
        File to = new File(dist).getAbsoluteFile();
        if (!from.exists()) {
            IOUtils.printErrorAndExit("mv: `" + source + "': No such file or directory");
        }
        if (!to.exists()) {
            to = mkdirExecute(to.getAbsolutePath());
        }
        if(from.getParent().equals(to.getAbsolutePath())) {
            /* если файл/папка перемещаются в ту же директорию, что и были до этого,
             * то переименоваем файл/папка, добавляя в название единицы
             */
            String cur = from.getAbsolutePath();
            cur += "1";
            File renamed = new File(cur);
            while (renamed.exists()) {
                cur += "1";
                renamed = new File(cur);
            }
            from.renameTo(renamed);
        } else {
            cpExecute(source, dist, false);
            rmExecute(from.getAbsolutePath());
        }   
    }
}
